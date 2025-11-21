import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * DownloadCleaner
 * ---------------
 * Konsolenprogramm, das einen gewählten Ordner (typischerweise den Download-Ordner)
 * analysiert und Dateien nach Typ in Unterordner sortiert.
 *
 * Geeignet als Schulungsprojekt zum Üben von:
 * - Arbeiten mit java.nio.file (Path, Files, DirectoryStream)
 * - Konsolenein- und -ausgabe (Scanner, System.out)
 * - Kontrollstrukturen (if, switch, Schleifen)
 * - Einfacher Objektorientierung (innere Hilfsklassen)
 * - Konfiguration über Textdatei (config.txt)
 * - Logging und einfachem Undo-Mechanismus
 *
 * Erweiterungen:
 * - Trockenlauf-Modus (Dry-Run): zeigt nur an, was verschoben würde
 * - Konfigurierbare Kategorien über config.txt im gewählten Ordner
 * - Log-Datei log.txt mit Zeitstempeln aller Verschiebe-Aktionen
 * - Undo-light: letzte Sortierung anhand undo_last_sort.txt teilweise rückgängig machen
 */
public class DownloadCleaner {

    /**
     * Ein globaler Scanner zum Einlesen von Benutzereingaben über die Konsole.
     */
    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Format für Zeitstempel im Log.
     * Beispiel: 2025-11-21 02:30:45
     */
    private static final DateTimeFormatter LOG_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Kategorie-Konfiguration (Standardwerte + optionale config.txt).
     * Wird nach Auswahl des Arbeitsordners geladen.
     */
    private static KategorieKonfiguration KATEGORIEN;

    /**
     * Einstiegspunkt des Programms.
     * Ablauf:
     * 1. Begrüßung ausgeben.
     * 2. Standard-Download-Ordner vorschlagen und Benutzerfrage.
     * 3. Kategorie-Konfiguration laden (inkl. config.txt, falls vorhanden).
     * 4. Menüschleife anzeigen, bis Benutzer das Programm beendet.
     *
     * @param args Kommandozeilenargumente (werden nicht verwendet).
     */
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    DownloadCleaner – Schulungsprojekt");
        System.out.println("===========================================\n");

        // Standard-Download-Ordner ermitteln (z. B. C:\Users\<Name>\Downloads)
        Path downloadDir = vorschlagDownloadOrdner();
        // Benutzer entscheiden lassen, ob dieser Ordner genutzt werden soll
        downloadDir = frageNachDownloadOrdner(downloadDir);

        if (downloadDir == null) {
            System.out.println("Programm wird beendet.");
            return;
        }

        // Kategorien aus config.txt (falls vorhanden) oder Standard laden
        KATEGORIEN = KategorieKonfiguration.laden(downloadDir);

        // Hauptmenü-Schleife
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Hauptmenü");
            System.out.println("---------");
            System.out.println("1) Ordner analysieren");
            System.out.println("2) Dateien nach Typ sortieren (normal)");
            System.out.println("3) Trockenlauf: nur anzeigen, keine Dateien verschieben");
            System.out.println("4) Letzte Sortierung (Undo-light) versuchen rückgängig zu machen");
            System.out.println("0) Beenden");
            System.out.print("Ihre Wahl: ");

            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    analysiereOrdner(downloadDir);
                    break;
                case "2":
                    sortiereOrdner(downloadDir, false);
                    break;
                case "3":
                    sortiereOrdner(downloadDir, true);
                    break;
                case "4":
                    rueckgaengigMachen(downloadDir);
                    break;
                case "0":
                    running = false;
                    System.out.println("Auf Wiedersehen!");
                    break;
                default:
                    System.out.println("Ungültige Eingabe. Bitte 0, 1, 2, 3 oder 4 wählen.");
            }
        }
    }

    /**
     * Liefert einen Vorschlag für den Download-Ordner des aktuellen Benutzers.
     * Verwendet System-Property "user.home" und hängt "Downloads" an.
     *
     * @return Pfad zum vorgeschlagenen Download-Ordner.
     */
    private static Path vorschlagDownloadOrdner() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "Downloads");
    }

    /**
     * Fragt den Benutzer, welcher Ordner verwendet werden soll.
     *
     * Regeln:
     * - Leere Eingabe: vorgeschlagenen Ordner verwenden.
     * - Eingabe "q" (oder "Q"): Programmabbruch, Methode liefert null.
     * - Andere Eingabe: als Pfad interpretieren.
     *
     * Existenz und Verzeichnis-Eigenschaft werden geprüft. Bei ungültigem Pfad
     * wird eine Fehlermeldung ausgegeben und null zurückgegeben.
     *
     * @param vorgeschlagen vorgeschlagener Ordner (z. B. Download-Ordner).
     * @return vom Benutzer gewählter Ordner oder null bei Abbruch/Fehler.
     */
    private static Path frageNachDownloadOrdner(Path vorgeschlagen) {
        System.out.println("Vorgeschlagener Download-Ordner: " + vorgeschlagen.toAbsolutePath());
        System.out.println("[Enter] = Vorschlag nutzen");
        System.out.println("Pfad eingeben = anderen Ordner wählen");
        System.out.println("'q' = Programm abbrechen");
        System.out.print("Ihre Eingabe: ");

        String input = SCANNER.nextLine().trim();

        Path chosen;
        if (input.isEmpty()) {
            chosen = vorgeschlagen;
        } else if (input.equalsIgnoreCase("q")) {
            return null;
        } else {
            chosen = Paths.get(input);
        }

        if (!Files.exists(chosen) || !Files.isDirectory(chosen)) {
            System.out.println("Fehler: Der Ordner existiert nicht oder ist kein Verzeichnis.");
            return null;
        }

        System.out.println("Verwende Ordner: " + chosen.toAbsolutePath());
        return chosen;
    }

    /**
     * Analysiert den angegebenen Ordner:
     * - zählt, wie viele Dateien in jede Kategorie fallen,
     * - gibt das Ergebnis in der Konsole aus.
     *
     * Es werden nur Dateien auf der obersten Ebene betrachtet, keine Unterordner.
     *
     * @param downloadDir zu analysierender Ordner.
     */
    private static void analysiereOrdner(Path downloadDir) {
        System.out.println("\nAnalysiere Ordner: " + downloadDir.toAbsolutePath());

        KategorieZaehler zaehler = new KategorieZaehler();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    String kategorie = bestimmeKategorie(entry.getFileName().toString());
                    zaehler.erhoehe(kategorie);
                }
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen des Ordners: " + e.getMessage());
            return;
        }

        zaehler.druckeErgebnis();
    }

    /**
     * Sortiert alle Dateien im angegebenen Ordner nach Kategorie in Unterordner.
     *
     * Verhalten:
     * - Wenn dryRun = true:
     *   - Es wird nur angezeigt, wohin jede Datei verschoben würde.
     *   - Es werden keine Dateien verschoben.
     *   - Es werden keine Log- oder Undo-Dateien geschrieben.
     * - Wenn dryRun = false:
     *   - Dateien werden physisch in Kategorie-Unterordner verschoben.
     *   - Jede Bewegung wird in log.txt festgehalten (mit Zeitstempel).
     *   - Die letzte Sortierung wird in undo_last_sort.txt protokolliert.
     *
     * @param downloadDir zu sortierender Ordner.
     * @param dryRun      true = nur Simulation, false = echte Sortierung.
     */
    private static void sortiereOrdner(Path downloadDir, boolean dryRun) {
        System.out.println();
        if (dryRun) {
            System.out.println("Trockenlauf: Es werden KEINE Dateien verschoben.");
        }
        System.out.println("Sortiere Dateien in: " + downloadDir.toAbsolutePath());

        Path logFile = downloadDir.resolve("log.txt");
        Path undoFile = downloadDir.resolve("undo_last_sort.txt");

        // Trockenlauf: nur Anzeige, keine Änderungen am Dateisystem
        if (dryRun) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDir)) {
                for (Path entry : stream) {
                    if (!Files.isRegularFile(entry)) {
                        continue;
                    }

                    String fileName = entry.getFileName().toString();
                    String kategorie = bestimmeKategorie(fileName);

                    Path zielOrdner = downloadDir.resolve(kategorie);
                    Path zielDatei = zielOrdner.resolve(fileName);

                    System.out.println("[DRY] Würde verschieben: "
                            + entry.getFileName() + " -> " + zielDatei.toAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("Fehler beim Trockenlauf: " + e.getMessage());
            }

            System.out.println("Trockenlauf abgeschlossen. Keine Änderungen vorgenommen.");
            return;
        }

        // Normaler Modus: Sortierung mit Logging und Undo-Protokoll
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDir);
             BufferedWriter logWriter = Files.newBufferedWriter(logFile,
                     StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.APPEND);
             BufferedWriter undoWriter = Files.newBufferedWriter(undoFile,
                     StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            LocalDateTime start = LocalDateTime.now();
            logWriter.write("=== Sortierung gestartet: " + start.format(LOG_TS_FORMAT) + " ===");
            logWriter.newLine();

            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) {
                    continue;
                }

                String fileName = entry.getFileName().toString();
                String kategorie = bestimmeKategorie(fileName);

                // Zielordner anlegen (falls noch nicht vorhanden)
                Path zielOrdner = downloadDir.resolve(kategorie);
                Files.createDirectories(zielOrdner);

                Path zielDatei = zielOrdner.resolve(fileName);

                // Relative Pfade zur Dokumentation (robuster, falls Root verschoben wird)
                Path relativeQuelle = downloadDir.relativize(entry);
                Path relativeZiel = downloadDir.relativize(zielDatei);

                try {
                    Files.move(entry, zielDatei, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Verschoben: " + fileName + " -> " + zielOrdner.getFileName());

                    String ts = LocalDateTime.now().format(LOG_TS_FORMAT);
                    logWriter.write(ts + " MOVE " + relativeQuelle + " -> " + relativeZiel);
                    logWriter.newLine();

                    // Für Undo: Ziel | Quelle (jeweils relativ)
                    undoWriter.write(relativeZiel + "|" + relativeQuelle);
                    undoWriter.newLine();

                } catch (IOException e) {
                    System.out.println("Konnte Datei nicht verschieben: "
                            + fileName + " (" + e.getMessage() + ")");
                }
            }

            logWriter.write("=== Sortierung beendet ===");
            logWriter.newLine();

        } catch (IOException e) {
            System.out.println("Fehler beim Sortieren: " + e.getMessage());
        }

        System.out.println("Sortierung abgeschlossen.");
        System.out.println("Log-Datei:   " + logFile.toAbsolutePath());
        System.out.println("Undo-Datei:  " + undoFile.toAbsolutePath());
    }

    /**
     * Macht die letzte Sortierung nach Möglichkeit rückgängig.
     *
     * Grundlage ist die Datei undo_last_sort.txt im Arbeitsordner.
     * Format pro Zeile:
     *   relativerZielPfad|relativerQuellPfad
     *
     * Dabei ist:
     * - relativerZielPfad: wohin die Datei beim Sortieren verschoben wurde.
     * - relativerQuellPfad: wo sie ursprünglich lag.
     *
     * Die Methode versucht für jede Zeile, die Datei vom Zielpfad zurück zum
     * ursprünglichen Pfad zu bewegen. Fehlende Dateien werden übersprungen.
     *
     * @param downloadDir Arbeitsordner, in dem sich undo_last_sort.txt befindet.
     */
    private static void rueckgaengigMachen(Path downloadDir) {
        Path undoFile = downloadDir.resolve("undo_last_sort.txt");
        if (!Files.exists(undoFile)) {
            System.out.println("Keine Undo-Datei gefunden: " + undoFile.toAbsolutePath());
            System.out.println("Es wurde vermutlich noch keine Sortierung durchgeführt.");
            return;
        }

        System.out.println("Versuche, die letzte Sortierung rückgängig zu machen.");
        System.out.println("Quelle der Informationen: " + undoFile.toAbsolutePath());

        int rueckgaengigZaehler = 0;

        try (BufferedReader reader = Files.newBufferedReader(undoFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("|")) {
                    continue;
                }

                String[] parts = line.split("\\|", 2);
                if (parts.length != 2) {
                    continue;
                }

                String relZiel = parts[0].trim();   // Wohin wir beim Sortieren verschoben haben
                String relQuelle = parts[1].trim(); // Ursprünglicher Speicherort

                Path zielPfad = downloadDir.resolve(relZiel);
                Path quellPfad = downloadDir.resolve(relQuelle);

                try {
                    Files.createDirectories(quellPfad.getParent());

                    if (Files.exists(zielPfad)) {
                        Files.move(zielPfad, quellPfad, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Zurück verschoben: " + zielPfad + " -> " + quellPfad);
                        rueckgaengigZaehler++;
                    } else {
                        System.out.println("Übersprungen (Datei nicht gefunden): " + zielPfad);
                    }
                } catch (IOException e) {
                    System.out.println("Fehler beim Rückgängig-Machen für " + zielPfad + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen der Undo-Datei: " + e.getMessage());
            return;
        }

        System.out.println("Undo-light abgeschlossen. Dateien zurückverschoben: " + rueckgaengigZaehler);
        System.out.println("Hinweis: Dieser einfache Undo-Mechanismus deckt nur die letzte Sortierung ab.");
    }

    /**
     * Bestimmt anhand der Dateiendung die passende Kategorie.
     *
     * Delegiert die eigentliche Logik an KategorieKonfiguration, damit sowohl
     * Standardkategorien als auch Einträge aus config.txt berücksichtigt werden.
     *
     * @param fileName Dateiname inkl. Endung.
     * @return Kategoriename, z. B. "Bilder", "Dokumente", "Archive", "Java", "Sonstiges".
     */
    private static String bestimmeKategorie(String fileName) {
        if (KATEGORIEN == null) {
            // Fallback, sollte im Normalfall nicht vorkommen
            KATEGORIEN = KategorieKonfiguration.ladeStandardNur();
        }
        return KATEGORIEN.bestimmeKategorie(fileName);
    }

    /**
     * Hilfsklasse, die zählt, wie viele Dateien in jede Kategorie fallen.
     *
     * Anstelle fester Felder (bilder, dokumente, …) wird eine Map verwendet.
     * So können auch dynamische Kategorien aus config.txt problemlos mitgezählt werden.
     */
    private static class KategorieZaehler {
        /**
         * Map von Kategorienamen auf Zählerstand.
         * LinkedHashMap behält die Einfügereihenfolge bei.
         */
        private final Map<String, Integer> werte = new LinkedHashMap<>();

        /**
         * Erhöht den Zähler für die angegebene Kategorie um 1.
         * Fehlt die Kategorie, wird sie mit dem Wert 1 neu angelegt.
         *
         * @param kategorie Kategoriename.
         */
        void erhoehe(String kategorie) {
            werte.put(kategorie, werte.getOrDefault(kategorie, 0) + 1);
        }

        /**
         * Gibt alle gezählten Kategorienamen und Werte in der Konsole aus.
         * Wenn keine Dateien gezählt wurden, wird ein entsprechender Hinweis ausgegeben.
         */
        void druckeErgebnis() {
            System.out.println("Ergebnis:");
            if (werte.isEmpty()) {
                System.out.println("  (keine Dateien gefunden)");
                return;
            }
            for (Map.Entry<String, Integer> entry : werte.entrySet()) {
                System.out.printf("  %s: %d%n", entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Verwaltet die Zuordnung von Dateiendungen zu Kategorien.
     *
     * Quellen:
     * - Standardkategorien im Code (Bilder, Dokumente, Archive, Installer, Java, Sonstiges).
     * - Optional: Konfigurationsdatei config.txt im Arbeitsordner.
     *
     * Format der config.txt:
     *   KategorieName=.ext1,.ext2,.ext3
     *
     * Beispiel:
     *   Bilder=.png,.jpg,.jpeg
     *   Dokumente=.pdf,.doc,.docx,.txt
     */
    private static class KategorieKonfiguration {
        /**
         * Map von Kategorienamen auf Arrays von Dateiendungen.
         * Endungen werden in Kleinschreibung und inklusive Punkt gespeichert (z. B. ".png").
         */
        private final Map<String, String[]> kategorien = new LinkedHashMap<>();

        /**
         * Konstruktor lädt automatisch die Standardkonfiguration.
         */
        private KategorieKonfiguration() {
            ladeStandard();
        }

        /**
         * Liefert eine Konfiguration, die nur die im Code hinterlegten Standardkategorien enthält.
         *
         * @return neue KategorieKonfiguration mit Standardwerten.
         */
        public static KategorieKonfiguration ladeStandardNur() {
            return new KategorieKonfiguration();
        }

        /**
         * Lädt eine KategorieKonfiguration auf Basis von Standardwerten und optionaler config.txt.
         *
         * Verhalten:
         * - Zuerst werden Standardkategorien gesetzt.
         * - Wenn config.txt existiert, überschreibt sie die Standardkonfiguration vollständig.
         * - Die Kategorie "Sonstiges" wird ggf. ergänzt, falls sie fehlt.
         *
         * @param basisOrdner Ordner, in dem nach config.txt gesucht wird.
         * @return vollständig initialisierte KategorieKonfiguration.
         */
        public static KategorieKonfiguration laden(Path basisOrdner) {
            KategorieKonfiguration konfig = new KategorieKonfiguration();
            Path configFile = basisOrdner.resolve("config.txt");

            if (Files.exists(configFile)) {
                System.out.println("Lade Kategorien aus Konfigurationsdatei: " + configFile.toAbsolutePath());
                konfig.ladeAusDatei(configFile);
            } else {
                System.out.println("Keine config.txt im Ordner gefunden. Verwende Standard-Kategorien.");
            }

            if (!konfig.kategorien.containsKey("Sonstiges")) {
                konfig.kategorien.put("Sonstiges", new String[0]);
            }

            return konfig;
        }

        /**
         * Initialisiert Standardkategorien und dazugehörige Dateiendungen.
         * Wird verwendet, wenn keine Konfigurationsdatei vorhanden ist oder gelesen werden konnte.
         */
        private void ladeStandard() {
            kategorien.clear();
            kategorien.put("Bilder", new String[]{
                    ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"
            });
            kategorien.put("Dokumente", new String[]{
                    ".pdf", ".doc", ".docx", ".txt",
                    ".ppt", ".pptx",
                    ".xls", ".xlsx",
                    ".odt", ".md"
            });
            kategorien.put("Archive", new String[]{
                    ".zip", ".rar", ".7z", ".tar", ".gz",
                    ".iso", ".img", ".dmg", ".vhd", ".vhdx"
            });
            kategorien.put("Installer", new String[]{
                    ".exe", ".msi"
            });
            kategorien.put("Java", new String[]{
                    ".jar"
            });
            kategorien.put("Sonstiges", new String[0]);
        }

        /**
         * Liest eine Konfigurationsdatei im Format:
         *   Kategorie=ext1,ext2,ext3
         *
         * Hinweise:
         * - Zeilen, die mit '#' beginnen, werden als Kommentar ignoriert.
         * - Leerzeilen werden ignoriert.
         * - Dateiendungen werden getrimmt, in Kleinschreibung konvertiert und erhalten bei Bedarf einen führenden Punkt.
         * - Bei IO-Problemen wird eine Fehlermeldung ausgegeben und wieder auf Standardkonfiguration gewechselt.
         *
         * @param configFile Pfad zur Konfigurationsdatei.
         */
        private void ladeAusDatei(Path configFile) {
            try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                kategorien.clear(); // Konfig überschreibt Standard vollständig

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    int eqIndex = line.indexOf('=');
                    if (eqIndex <= 0) {
                        // keine gültige "Kategorie=..."-Zeile
                        continue;
                    }

                    String katName = line.substring(0, eqIndex).trim();
                    String extPart = line.substring(eqIndex + 1).trim();
                    if (katName.isEmpty() || extPart.isEmpty()) {
                        continue;
                    }

                    String[] extsRaw = extPart.split(",");
                    for (int i = 0; i < extsRaw.length; i++) {
                        String ext = extsRaw[i].trim().toLowerCase();
                        if (!ext.isEmpty() && !ext.startsWith(".")) {
                            ext = "." + ext;
                        }
                        extsRaw[i] = ext;
                    }

                    kategorien.put(katName, extsRaw);
                }

                System.out.println("Konfiguration geladen. Kategorien: " + kategorien.keySet());
            } catch (IOException e) {
                System.out.println("Konnte config.txt nicht lesen. Verwende Standard-Kategorien. Ursache: " + e.getMessage());
                ladeStandard();
            }
        }

        /**
         * Bestimmt anhand des Dateinamens die passende Kategorie.
         *
         * Vorgehen:
         * - Dateiname in Kleinbuchstaben umwandeln.
         * - Alle Kategorien außer "Sonstiges" durchlaufen.
         * - Prüfen, ob der Name mit einer der hinterlegten Endungen endet.
         * - Bei Treffer: Kategoriename zurückgeben.
         * - Andernfalls: "Sonstiges" als Fallback.
         *
         * @param fileName Dateiname inkl. Endung.
         * @return Kategoriename oder "Sonstiges".
         */
        public String bestimmeKategorie(String fileName) {
            String lower = fileName.toLowerCase();

            // Alle Kategorien außer "Sonstiges" prüfen
            for (Map.Entry<String, String[]> entry : kategorien.entrySet()) {
                String katName = entry.getKey();
                if ("Sonstiges".equals(katName)) {
                    continue;
                }

                String[] exts = entry.getValue();
                if (exts == null) {
                    continue;
                }

                for (String ext : exts) {
                    if (ext != null && !ext.isEmpty() && lower.endsWith(ext)) {
                        return katName;
                    }
                }
            }

            // Fallback, wenn keine Kategorie passt
            return "Sonstiges";
        }
    }
}

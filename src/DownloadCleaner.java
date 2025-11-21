import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * DownloadCleaner
 * ----------------
 * Ein einfaches Konsolenprogramm, das den Download-Ordner
 * nach Dateitypen sortiert. Geeignet als Schulungsprojekt.
 *
 * Erweiterungen:
 *  - Trockenlauf-Modus (Dry-Run): zeigt nur an, was verschoben würde
 *  - Konfigurierbare Kategorien über config.txt im gewählten Ordner
 *  - Log-Datei log.txt mit Zeitstempeln aller Verschiebe-Aktionen
 *  - Undo-light: letzte Sortierung anhand undo_last_sort.txt teilweise rückgängig machen
 */
public class DownloadCleaner {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter LOG_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Kategorie-Konfiguration wird nach Auswahl des Ordners geladen
    private static KategorieKonfiguration KATEGORIEN;

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    DownloadCleaner – Schulungsprojekt");
        System.out.println("===========================================\n");

        Path downloadDir = vorschlagDownloadOrdner();
        downloadDir = frageNachDownloadOrdner(downloadDir);

        if (downloadDir == null) {
            System.out.println("Programm wird beendet.");
            return;
        }

        // Kategorien aus config.txt (falls vorhanden) oder Standard laden
        KATEGORIEN = KategorieKonfiguration.laden(downloadDir);

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

    private static Path vorschlagDownloadOrdner() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "Downloads");
    }

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
     * Sortiert alle Dateien im Ordner nach Kategorie in Unterordner.
     * Wenn dryRun = true, werden nur Meldungen ausgegeben, aber keine Dateien verschoben.
     */
    private static void sortiereOrdner(Path downloadDir, boolean dryRun) {
        System.out.println();
        if (dryRun) {
            System.out.println("Trockenlauf: Es werden KEINE Dateien verschoben.");
        }
        System.out.println("Sortiere Dateien in: " + downloadDir.toAbsolutePath());

        Path logFile = downloadDir.resolve("log.txt");
        Path undoFile = downloadDir.resolve("undo_last_sort.txt");

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

        // Normale Sortierung mit Logging und Undo-Protokoll
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

                Path zielOrdner = downloadDir.resolve(kategorie);
                Files.createDirectories(zielOrdner);

                Path zielDatei = zielOrdner.resolve(fileName);

                Path relativeQuelle = downloadDir.relativize(entry);
                Path relativeZiel = downloadDir.relativize(zielDatei);

                try {
                    Files.move(entry, zielDatei, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Verschoben: " + fileName + " -> " + zielOrdner.getFileName());

                    String ts = LocalDateTime.now().format(LOG_TS_FORMAT);
                    logWriter.write(ts + " MOVE " + relativeQuelle + " -> " + relativeZiel);
                    logWriter.newLine();

                    // Für Undo: Ziel | Quelle (relativ zum Download-Ordner)
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
     * Undo-light:
     * Liest die Datei undo_last_sort.txt und versucht,
     * die dort protokollierten Bewegungen rückgängig zu machen.
     *
     * Format pro Zeile:
     *   relativerZielPfad|relativerQuellPfad
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

                String relZiel = parts[0].trim();   // wohin wir beim Sortieren verschoben haben
                String relQuelle = parts[1].trim(); // wo die Datei ursprünglich lag

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
     * Bestimmt auf Basis der Dateiendung eine Kategorie.
     * Delegiert an die KategorieKonfiguration (Standard + config.txt).
     */
    private static String bestimmeKategorie(String fileName) {
        if (KATEGORIEN == null) {
            // Fallback, sollte im Normalfall nicht vorkommen
            KATEGORIEN = KategorieKonfiguration.ladeStandardNur();
        }
        return KATEGORIEN.bestimmeKategorie(fileName);
    }

    /**
     * Hilfsmethode: zählt pro Kategorie dynamisch (alle Kategorien, die in Konfiguration vorkommen).
     */
    private static class KategorieZaehler {
        private final Map<String, Integer> werte = new LinkedHashMap<>();

        void erhoehe(String kategorie) {
            werte.put(kategorie, werte.getOrDefault(kategorie, 0) + 1);
        }

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
     * Kapselt die Logik für Kategorien:
     *  - Standard-Kategorien im Code
     *  - optionale Überschreibung / Ergänzung durch config.txt
     */
    private static class KategorieKonfiguration {
        private final Map<String, String[]> kategorien = new LinkedHashMap<>();

        private KategorieKonfiguration() {
            ladeStandard();
        }

        /**
         * Lädt Kategorien mit Standardwerten, ohne Config-Datei.
         */
        public static KategorieKonfiguration ladeStandardNur() {
            return new KategorieKonfiguration();
        }

        /**
         * Lädt Kategorien mit Standardwerten und versucht,
         * eine config.txt im angegebenen Ordner zu lesen.
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

            // sicherstellen, dass "Sonstiges" existiert
            if (!konfig.kategorien.containsKey("Sonstiges")) {
                konfig.kategorien.put("Sonstiges", new String[0]);
            }

            return konfig;
        }

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

        private void ladeAusDatei(Path configFile) {
            try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                kategorien.clear(); // Config überschreibt Standard vollständig

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    int eqIndex = line.indexOf('=');
                    if (eqIndex <= 0) {
                        continue; // ungültige Zeile
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

        public String bestimmeKategorie(String fileName) {
            String lower = fileName.toLowerCase();

            // Zuerst alle Kategorien außer "Sonstiges" prüfen
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

            // Fallback
            return "Sonstiges";
        }
    }
}

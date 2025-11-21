import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

/**
 * DownloadCleaner
 * ----------------
 * Ein einfaches Konsolenprogramm, das den Download-Ordner
 * nach Dateitypen sortiert. Geeignet als Schulungsprojekt.
 */
public class DownloadCleaner {

    private static final Scanner SCANNER = new Scanner(System.in);

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

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Hauptmenü");
            System.out.println("---------");
            System.out.println("1) Ordner analysieren");
            System.out.println("2) Dateien nach Typ sortieren");
            System.out.println("0) Beenden");
            System.out.print("Ihre Wahl: ");

            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    analysiereOrdner(downloadDir);
                    break;
                case "2":
                    sortiereOrdner(downloadDir);
                    break;
                case "0":
                    running = false;
                    System.out.println("Auf Wiedersehen!");
                    break;
                default:
                    System.out.println("Ungültige Eingabe. Bitte 0, 1 oder 2 wählen.");
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

        System.out.println("Ergebnis:");
        System.out.println("  Bilder:    " + zaehler.bilder);
        System.out.println("  Dokumente: " + zaehler.dokumente);
        System.out.println("  Archive:   " + zaehler.archive);
        System.out.println("  Installer: " + zaehler.installer);
        System.out.println("  Java:      " + zaehler.javaDateien);
        System.out.println("  Sonstiges: " + zaehler.sonstiges);
    }

    private static void sortiereOrdner(Path downloadDir) {
        System.out.println("\nSortiere Dateien in: " + downloadDir.toAbsolutePath());

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDir)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) {
                    continue;
                }

                String fileName = entry.getFileName().toString();
                String kategorie = bestimmeKategorie(fileName);

                Path zielOrdner = downloadDir.resolve(kategorie);
                Files.createDirectories(zielOrdner);

                Path zielDatei = zielOrdner.resolve(fileName);

                try {
                    Files.move(entry, zielDatei, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Verschoben: " + fileName + " -> " + zielOrdner.getFileName());
                } catch (IOException e) {
                    System.out.println("Konnte Datei nicht verschieben: " + fileName + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Sortieren: " + e.getMessage());
        }

        System.out.println("Sortierung abgeschlossen.");
    }

    /**
     * Bestimmt auf Basis der Dateiendung eine Kategorie.
     * Mögliche Rückgaben:
     *   "Bilder", "Dokumente", "Archive", "Installer", "Java", "Sonstiges"
     */
    private static String bestimmeKategorie(String fileName) {
        String lower = fileName.toLowerCase();

        // Bilder
        if (endetMit(lower, ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp")) {
            return "Bilder";
        }

        // Dokumente inkl. .md
        if (endetMit(lower,
                ".pdf", ".doc", ".docx", ".txt",
                ".ppt", ".pptx",
                ".xls", ".xlsx",
                ".odt", ".md"    // NEU: Markdown als Dokument
        )) {
            return "Dokumente";
        }

        // Archive inkl. Image-/Container-Dateien
        if (endetMit(lower,
                ".zip", ".rar", ".7z", ".tar", ".gz",
                ".iso", ".img", ".dmg", ".vhd", ".vhdx"  // NEU: ISO/IMG usw.
        )) {
            return "Archive";
        }

        // Installer
        if (endetMit(lower, ".exe", ".msi")) {
            return "Installer";
        }

        // Java
        if (endetMit(lower, ".jar")) {  // NEU: jar als eigene Kategorie
            return "Java";
        }

        // Hier kann man leicht weitere Zuordnungen ergänzen (Audio, Video, Code etc.)
        return "Sonstiges";
    }

    private static boolean endetMit(String name, String... endungen) {
        for (String endung : endungen) {
            if (name.endsWith(endung)) {
                return true;
            }
        }
        return false;
    }

    private static class KategorieZaehler {
        int bilder;
        int dokumente;
        int archive;
        int installer;
        int javaDateien;
        int sonstiges;

        void erhoehe(String kategorie) {
            switch (kategorie) {
                case "Bilder":
                    bilder++;
                    break;
                case "Dokumente":
                    dokumente++;
                    break;
                case "Archive":
                    archive++;
                    break;
                case "Installer":
                    installer++;
                    break;
                case "Java":
                    javaDateien++;
                    break;
                default:
                    sonstiges++;
            }
        }
    }
}

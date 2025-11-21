# DownloadCleaner – Java Schulungsprojekt

Dieses Repository enthält eine kleine Java-Konsolenanwendung, mit der Lernende Schritt für Schritt verstehen können, wie man eine einfache, aber im Alltag nützliche Anwendung entwickelt.

Das Programm **DownloadCleaner** analysiert einen Download-Ordner (typischerweise den Windows-Download-Ordner) und sortiert Dateien automatisch in Unterordner nach Typ (z. B. Bilder, Dokumente, Archive, Installer, Java, Sonstiges).  
Zusätzlich stehen ein **Trockenlauf-Modus**, **konfigurierbare Kategorien**, eine **Log-Datei** sowie ein einfaches **Undo-light** zur Verfügung.

---

## 1. Ziel des Projekts

Das Projekt ist als **Schulungsanwendung** gedacht, z. B. für:

- Umschulung / Ausbildung Fachinformatiker Anwendungsentwicklung  
- Informatik-Unterricht (Berufsschule, Weiterbildung)  
- Selbststudium von Java-Grundlagen

Der Fokus liegt auf:

- gut lesbarem, einfachem Quellcode
- klarer Struktur (Methoden, Hilfsklassen)
- direktem Praxisbezug (Ordnung im Download-Ordner)
- Erweiterbarkeit (Konfiguration, Logging, Undo)

---

## 2. Funktionsumfang

Die Anwendung bietet aktuell:

1. **Analyse des Download-Ordners**
   - Zählt, wie viele Dateien pro Kategorie vorhanden sind, z. B.:
     - Bilder
     - Dokumente (inkl. `.md`)
     - Archive (inkl. `.iso`, `.img`, `.dmg`, `.vhd`, `.vhdx`)
     - Installer (`.exe`, `.msi`)
     - Java (`.jar`)
     - Sonstiges
   - Kategorien können über eine `config.txt` angepasst werden.

2. **Sortierung nach Typ (Normalmodus)**
   - Verschiebt alle Dateien im gewählten Ordner in Unterordner nach Kategorie:
     - `Bilder/`
     - `Dokumente/`
     - `Archive/`
     - `Installer/`
     - `Java/`
     - `Sonstiges/` (oder weitere Kategorien aus der Konfiguration)
   - Unterordner werden automatisch erstellt, falls noch nicht vorhanden.
   - Aktionen werden in einer **Log-Datei `log.txt`** mit Datum/Zeit protokolliert.
   - Für die letzte Sortierung wird eine **Undo-Datei `undo_last_sort.txt`** erzeugt.

3. **Trockenlauf-Modus**
   - Zeigt nur an, **welche Dateien wohin verschoben würden**, ohne tatsächlich Änderungen am Dateisystem vorzunehmen.
   - Ideal für Schulungszwecke und zum Testen der Konfiguration.

4. **Undo-light (Rückgängig machen)**
   - Versucht, die **letzte Sortierung** anhand der Datei `undo_last_sort.txt` rückgängig zu machen.
   - Alle in dieser Datei verzeichneten Moves werden, soweit möglich, zurückverschoben.
   - Hinweis: Dieser Mechanismus ist bewusst einfach gehalten und deckt nur die letzte Sortierung ab.

5. **Konfigurierbare Kategorien**
   - Über eine optionale Datei `config.txt` im gewählten Ordner können Kategorien und zugehörige Dateiendungen angepasst werden.
   - Fehlt die `config.txt`, verwendet das Programm Standard-Kategorien aus dem Code.

6. **Einfaches Konsolenmenü**
   - Ordner analysieren
   - Dateien sortieren (Normalmodus)
   - Trockenlauf (nur anzeigen)
   - Letzte Sortierung rückgängig machen (Undo-light)
   - Programm beenden

---

## 3. Lernziele

Anhand dieses Projekts können Lernende u. a. folgende Themen üben:

- Aufbau einer Java-Klasse mit `main`-Methode
- Konsolen-Ein- und -Ausgabe (`System.out`, `Scanner`)
- Kontrollstrukturen:
  - `if` / `else`
  - `switch`
  - Schleifen (`while`, `for`, foreach über `DirectoryStream`)
- Arbeiten mit Methoden
  - sinnvolle Aufteilung von Logik in kleine, lesbare Methoden
- Einfache Objektorientierung
  - innere Hilfsklassen für Zähler und Konfiguration
- Arbeiten mit dem Dateisystem (NIO.2-API)
  - `Path`, `Paths`
  - `Files.newDirectoryStream`
  - `Files.isRegularFile`
  - `Files.createDirectories`
  - `Files.move`
  - `Files.newBufferedReader` / `Files.newBufferedWriter`
- Umgang mit Konfigurationsdateien (Key-Value-Format)
- Einfache Logging-Mechanismen (Zeilen mit Zeitstempel in `log.txt`)
- Grundlagen eines Undo-Mechanismus (Protokollierung der letzten Aktion)

---

## 4. Voraussetzungen

- Installiertes **Java Development Kit (JDK)** (z. B. Temurin, Oracle JDK, OpenJDK)
- Grundlegende Kenntnisse:
  - Arbeiten mit der Konsole / dem Terminal
  - Navigieren in Ordnern
- Betriebssystem:
  - Getestet unter **Windows** (Standard-Download-Ordner: `C:\Users\<Benutzername>\Downloads`)
  - Sollte grundsätzlich auch unter anderen Betriebssystemen laufen, wenn ein passender Ordner gewählt wird.

---

## 5. Projektstruktur

Beispielhafte, minimale Struktur:

```text
src/
  DownloadCleaner.java
README.md
LICENSE
````

* `src/DownloadCleaner.java`
  Enthält die komplette Logik der Anwendung in einer Datei (ideal für Schulungszwecke).
* `README.md`
  Diese Datei, mit Erläuterungen für Lehrende und Lernende.
* `LICENSE`
  Lizenztext (MIT License).

Zur Laufzeit können im gewählten Download-Ordner zusätzlich entstehen:

* `config.txt` (optional, vom Benutzer erstellt)
* `log.txt` (wird beim Sortieren geschrieben/erweitert)
* `undo_last_sort.txt` (wird bei jeder Sortierung neu geschrieben)

---

## 6. Kompilieren und Ausführen

### Variante 1: Direkt im `src`-Ordner

1. Konsole / Terminal öffnen und in den `src`-Ordner wechseln:

   ```bash
   cd /Pfad/zum/Projekt/src
   ```

2. Java-Datei kompilieren:

   ```bash
   javac DownloadCleaner.java
   ```

   Dadurch wird eine `DownloadCleaner.class` erzeugt.

3. Programm starten:

   ```bash
   java DownloadCleaner
   ```

### Variante 2: Projektordner mit `src`-Unterordner

1. In den Projektordner wechseln:

   ```bash
   cd /Pfad/zum/Projekt
   ```

2. Kompilieren mit Ausgabeverzeichnis `out`:

   ```bash
   javac -d out src/DownloadCleaner.java
   ```

3. Starten:

   ```bash
   cd out
   java DownloadCleaner
   ```

---

## 6.1 Windows-Pfade und Umgebungsvariablen

Der DownloadCleaner schlägt beim Start automatisch einen **Standard-Download-Ordner** vor. Intern verwendet das Programm:

```java
String userHome = System.getProperty("user.home");
Path downloads = Paths.get(userHome, "Downloads");
```

### Was bedeutet das unter Windows?

Unter Windows entspricht `System.getProperty("user.home")` im Normalfall dem Verzeichnis:

```text
C:\Users\<DeinBenutzername>
```

Der vorgeschlagene Download-Ordner ist damit:

```text
C:\Users\<DeinBenutzername>\Downloads
```

### Alternative Sicht: Windows-Umgebungsvariablen

In der Windows-Welt gibt es mehrere Umgebungsvariablen, die auf dein Benutzerverzeichnis zeigen:

* `%USERPROFILE%`
  Typischerweise: `C:\Users\<DeinBenutzername>`
* `%HOMEDRIVE%` + `%HOMEPATH%`
  Typischerweise: `C:` + `\Users\<DeinBenutzername>`

Dadurch ergeben sich unter der Eingabeaufforderung (CMD) z. B. folgende äquivalente Pfade:

```bat
%USERPROFILE%\Downloads
%HOMEDRIVE%%HOMEPATH%\Downloads
C:\Users\<DeinBenutzername>\Downloads
```

In PowerShell sähe das z. B. so aus:

```powershell
$env:USERPROFILE\Downloads
```

### Bezüge zwischen Java und Windows-Variablen

* Java-Seite: `System.getProperty("user.home")`
* Windows-Seite: `%USERPROFILE%` (bzw. `%HOMEDRIVE%%HOMEPATH%`)

Für Lernzwecke ist es sinnvoll, den Zusammenhang klarzumachen:

* **Java** kennt keine `%VAR%`-Syntax, sondern arbeitet mit System-Properties.
* **Windows** kennt Umgebungsvariablen, die oft auf dieselben Ordner verweisen.

Beim **Eingeben eines Pfades** im Programm muss der Benutzer immer einen „fertigen“ Pfad eingeben, z. B.:

```text
C:\Users\Marcus\Downloads
D:\Testdaten\Downloads
```

Das Programm wertet keine `%USERPROFILE%`-Strings aus – diese Logik ist bewusst einfach gehalten, damit Lernende sich auf Java konzentrieren können.

---

## 7. Bedienung aus Sicht der Nutzer

Nach dem Start zeigt das Programm zuerst einen **vorgeschlagenen Download-Ordner** an, z. B.:

```text
Vorgeschlagener Download-Ordner: C:\Users\DeinName\Downloads
[Enter] = Vorschlag nutzen
Pfad eingeben = anderen Ordner wählen
'q' = Programm abbrechen
Ihre Eingabe:
```

* Einfach **Enter** drücken, um den vorgeschlagenen Ordner zu verwenden.
* Einen eigenen Pfad eingeben, um einen anderen Ordner zu wählen.
* Mit `q` abbrechen.

Danach erscheint das **Hauptmenü**:

```text
Hauptmenü
---------
1) Ordner analysieren
2) Dateien nach Typ sortieren (normal)
3) Trockenlauf: nur anzeigen, keine Dateien verschieben
4) Letzte Sortierung (Undo-light) versuchen rückgängig zu machen
0) Beenden
Ihre Wahl:
```

* `1` → Nur Analyse durchführen, keine Dateien werden verschoben.
* `2` → Dateien nach Typ in Unterordner verschieben (normaler Sortierlauf mit Log- und Undo-Protokoll).
* `3` → Trockenlauf: zeigt nur an, was passieren würde, ohne Dateien zu verändern.
* `4` → Versucht, die letzte Sortierung anhand von `undo_last_sort.txt` rückgängig zu machen.
* `0` → Programm beenden.

---

## 8. Ablauf im Detail – wie das Programm intern arbeitet

Dieser Abschnitt ist für Lernende gedacht, die verstehen wollen, **was im Code wirklich passiert**.

### 8.1 Programmstart (`main`-Methode)

1. Begrüßungstext anzeigen.

2. Standard-Download-Ordner ermitteln:

   ```java
   Path downloadDir = vorschlagDownloadOrdner();
   ```

3. Benutzer nach dem zu verwendenden Ordner fragen:

   ```java
   downloadDir = frageNachDownloadOrdner(downloadDir);
   ```

4. Kategorien laden (Standard + optionale `config.txt` im gewählten Ordner).

5. In eine **Menüschleife** wechseln:

   ```java
   while (running) {
       // Menü anzeigen
       // Auswahl mit switch-case auswerten
   }
   ```

### 8.2 Ordner-Vorschlag (`vorschlagDownloadOrdner`)

```java
String userHome = System.getProperty("user.home");
Path downloads = Paths.get(userHome, "Downloads");
return downloads;
```

### 8.3 Ordner-Abfrage beim Benutzer (`frageNachDownloadOrdner`)

* Vorschlag anzeigen.
* Benutzereingabe auslesen:

  * Leer → Vorschlag übernehmen.
  * `q` → `null` zurückgeben (Abbruch).
  * Anderer Text → als Pfad interpretieren.
* Prüfen, ob der Pfad existiert und ein Verzeichnis ist.

### 8.4 Analyse des Ordners (`analysiereOrdner`)

* Mit `Files.newDirectoryStream(downloadDir)` alle Dateien im Ordner durchlaufen (nicht rekursiv).
* Kategorie mit `bestimmeKategorie(...)` bestimmen.
* Zähler pro Kategorie in einer Hilfsklasse `KategorieZaehler`.
* Ergebnis am Ende ausgeben.

### 8.5 Sortierung des Ordners (`sortiereOrdner`)

Die Methode arbeitet in zwei Modi:

1. **Trockenlauf (`dryRun = true`)**

   * Es werden nur Meldungen der Form:

     ```text
     [DRY] Würde verschieben: Datei.ext -> Zielpfad
     ```

     ausgegeben.
   * Keine Dateien werden verschoben.
   * Es werden auch keine Log-/Undo-Dateien geschrieben.

2. **Normalmodus (`dryRun = false`)**

   * Für jede reguläre Datei im Ordner:

     * Kategorie bestimmen (`bestimmeKategorie`).
     * Zielordner anlegen (`Files.createDirectories`).
     * Datei verschieben (`Files.move(..., REPLACE_EXISTING)`).
   * Parallel:

     * In `log.txt` wird jede Move-Operation mit Zeitstempel protokolliert.
     * In `undo_last_sort.txt` wird für jede verschobene Datei die Zuordnung
       `relativerZielPfad|relativerQuellPfad` gespeichert (relativ zum gewählten Ordner).

Damit entsteht automatisch ein Protokoll, mit dem man die letzte Sortierung zumindest teilweise rückgängig machen kann.

### 8.6 Undo-light (`rueckgaengigMachen`)

* Liest `undo_last_sort.txt`.

* Für jede Zeile:

  ```text
  zielPfadRelativ|quellPfadRelativ
  ```

* Versucht, die Datei vom Ziel zurück zur ursprünglichen Position zu verschieben.

* Fehlende Dateien werden protokolliert und übersprungen.

* Am Ende wird die Anzahl der erfolgreich zurückverschobenen Dateien ausgegeben.

Hinweis:
Dieser Mechanismus ist **kein vollwertiges Versionskontrollsystem**, sondern ein einfacher Ansatz, um einen Sortierlauf für Schulungszwecke nachvollziehbar zu machen.

### 8.7 Kategorisierung & Konfiguration (`KategorieKonfiguration`)

* Standard-Kategorien sind im Code hinterlegt.
* Wenn im gewählten Ordner eine `config.txt` existiert, wird sie gelesen und die Standard-Kategorien vollständig **überschrieben**.

**Format der `config.txt`:**

```text
# Kategorie=Endungen, durch Komma getrennt
Bilder=.png,.jpg,.jpeg,.gif,.bmp,.webp
Dokumente=.pdf,.doc,.docx,.txt,.ppt,.pptx,.xls,.xlsx,.odt,.md
Archive=.zip,.rar,.7z,.tar,.gz,.iso,.img,.dmg,.vhd,.vhdx
Installer=.exe,.msi
Java=.jar
Sonstiges=
```

* Zeilen, die mit `#` beginnen oder leer sind, werden ignoriert.
* Endungen ohne führenden Punkt werden automatisch mit `.` ergänzt.
* Die Kategorie **„Sonstiges“** sollte entweder in der Config definiert sein oder wird vom Programm automatisch ergänzt.

`bestimmeKategorie(fileName)` delegiert an diese Konfiguration und gibt den Kategorienamen zurück.

---

## 9. Kategorielogik (Standard-Dateitypen)

Die Standardzuordnung erfolgt aktuell wie folgt (alles in Kleinbuchstaben verglichen):

### Bilder

* `.png`
* `.jpg`
* `.jpeg`
* `.gif`
* `.bmp`
* `.webp`

### Dokumente

* `.pdf`
* `.doc`
* `.docx`
* `.txt`
* `.ppt`
* `.pptx`
* `.xls`
* `.xlsx`
* `.odt`
* `.md` (Markdown)

### Archive / Container

* `.zip`
* `.rar`
* `.7z`
* `.tar`
* `.gz`
* `.iso`
* `.img`
* `.dmg`
* `.vhd`
* `.vhdx`

### Installer

* `.exe`
* `.msi`

### Java

* `.jar`

### Sonstiges

* Alle Dateitypen, die in keine der obigen Kategorien fallen.

Über `config.txt` können diese Zuordnungen überschrieben oder erweitert werden.

---

## 10. Hinweise & Sicherheit

* Das Programm **verschiebt Dateien** innerhalb des gewählten Ordners in Unterordner.
* Standardmäßig wird mit `StandardCopyOption.REPLACE_EXISTING` gearbeitet:

  * Existiert eine Datei mit gleichem Namen im Zielordner, wird sie **überschrieben**.
* Für Schulungszwecke empfiehlt es sich, zuerst:

  * Mit Option `1` (Analyse) zu starten.
  * Einen Testordner mit Beispiel-Dateien anzulegen, statt den echten Download-Ordner zu verwenden.
* Der Undo-light-Mechanismus kann nur die **letzte Sortierung** rückgängig machen und ist nicht als Ersatz für Backups oder Versionskontrolle gedacht.

### Empfehlung für den Unterricht

* Einen separaten Ordner, z. B. `C:\Temp\DownloadTest`, anlegen.
* Testdateien mit verschiedenen Endungen hineinkopieren.
* Optional eine eigene `config.txt` erstellen und mit den Kategorien experimentieren.
* Zuerst Trockenlauf (`3`), dann Sortierung (`2`), danach Undo (`4`) demonstrieren.

---

## 11. Weitere Erweiterungsideen

Einige sinnvolle Übungsaufgaben für Fortgeschrittene:

* **Rekursive Sortierung**
  Unterordner mit einbeziehen (z. B. mittels `Files.walkFileTree`).
* **Auswahl einzelner Kategorien**
  Nur bestimmte Kategorien sortieren (z. B. nur Bilder oder nur Archive).
* **Interaktive Konfiguration im Programm**
  Kategorien und Endungen im Programm anpassen und in `config.txt` zurückschreiben.
* **Einfache GUI**
  Auf Basis dieses Konsolentools eine kleine Swing-Oberfläche bauen (z. B. Auswahl des Ordners per Datei-Dialog, Buttons für Analyse/Sortierung/Undo).
* **Mehrstufiges Undo**
  Statt nur `undo_last_sort.txt` eine Historie (z. B. `undo_2025-11-21_120000.txt`) pflegen und mehrere Durchläufe rückgängig machen.

---

## 12. Lizenz

Dieses Projekt steht unter der **MIT-Lizenz**.

---

# MIT License

Copyright (c) 2025 Marcus Dziersan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

```
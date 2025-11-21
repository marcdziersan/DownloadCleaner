# DownloadCleaner – Java Schulungsprojekt

Dieses Repository enthält eine kleine Java-Konsolenanwendung, mit der Lernende Schritt für Schritt verstehen können, wie man eine einfache, aber im Alltag nützliche Anwendung entwickelt.

Das Programm **DownloadCleaner** analysiert einen Download-Ordner (typischerweise den Windows-Download-Ordner) und sortiert Dateien automatisch in Unterordner nach Typ (Bilder, Dokumente, Archive, Installer, Java, Sonstiges).

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

---

## 2. Funktionsumfang

Die Anwendung bietet aktuell:

1. **Analyse des Download-Ordners**
   - Zählt, wie viele Dateien pro Kategorie vorhanden sind:
     - Bilder
     - Dokumente (inkl. `.md`)
     - Archive (inkl. `.iso`, `.img`, `.dmg`, `.vhd`, `.vhdx`)
     - Installer (`.exe`, `.msi`)
     - Java (`.jar`)
     - Sonstiges

2. **Sortierung nach Typ**
   - Verschiebt alle Dateien im gewählten Ordner in Unterordner:
     - `Bilder`
     - `Dokumente`
     - `Archive`
     - `Installer`
     - `Java`
     - `Sonstiges`
   - Unterordner werden automatisch erstellt, falls noch nicht vorhanden.

3. **Einfaches Konsolenmenü**
   - Ordner analysieren
   - Dateien sortieren
   - Programm beenden

---

## 3. Lernziele

Anhand dieses Projekts können Lernende u. a. folgende Themen üben:

- Aufbau einer Java-Klasse mit `main`-Methode
- Konsolen-Ein- und -Ausgabe (`System.out`, `Scanner`)
- Kontrollstrukturen:
  - `if` / `else`
  - `switch`
  - Schleifen (`while`, `for`)
- Arbeiten mit Methoden
  - sinnvolle Aufteilung von Logik in kleine, lesbare Methoden
- Einfache Objektorientierung
  - innere Hilfsklasse `KategorieZaehler`
- Arbeiten mit dem Dateisystem (NIO.2-API)
  - `Path`, `Paths`
  - `Files.newDirectoryStream`
  - `Files.isRegularFile`
  - `Files.createDirectories`
  - `Files.move`

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

Minimaler Stand:

```text
DownloadCleaner.java
README.md
```

- `DownloadCleaner.java`  
  Enthält die komplette Logik der Anwendung in einer Datei (ideal für Schulungszwecke).
- `README.md`  
  Diese Datei, mit Erläuterungen für Lehrende und Lernende.

---

## 6. Kompilieren und Ausführen

1. Konsole / Terminal öffnen und in den Projektordner wechseln:

   ```bash
   cd /Pfad/zum/Projekt
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

- `%USERPROFILE%`  
  Typischerweise: `C:\Users\<DeinBenutzername>`
- `%HOMEDRIVE%` + `%HOMEPATH%`  
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

- Java-Seite: `System.getProperty("user.home")`
- Windows-Seite: `%USERPROFILE%` (bzw. `%HOMEDRIVE%%HOMEPATH%`)

Für Lernzwecke ist es sinnvoll, den Zusammenhang klarzumachen:

- **Java** kennt keine `%VAR%`-Syntax, sondern arbeitet mit System-Properties.
- **Windows** kennt Umgebungsvariablen, die oft auf dieselben Ordner verweisen.

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

- Einfach **Enter** drücken, um den vorgeschlagenen Ordner zu verwenden.
- Einen eigenen Pfad eingeben, um einen anderen Ordner zu wählen.
- Mit `q` abbrechen.

Danach erscheint das **Hauptmenü**:

```text
Hauptmenü
---------
1) Ordner analysieren
2) Dateien nach Typ sortieren
0) Beenden
Ihre Wahl:
```

- `1` → Nur Analyse durchführen, keine Dateien werden verschoben.  
- `2` → Dateien nach Typ in Unterordner verschieben.  
- `0` → Programm beenden.

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

4. Falls der Benutzer abbricht (`q` oder ungültiger Ordner), Programm beenden.
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

- `user.home` liefert das Benutzerverzeichnis (unter Windows meist `C:\Users\Name`).
- `Paths.get(userHome, "Downloads")` hängt `Downloads` hinten an.
- Das Ergebnis ist ein `Path`, der auf den vermuteten Download-Ordner zeigt.

### 8.3 Ordner-Abfrage beim Benutzer (`frageNachDownloadOrdner`)

- Vorschlag anzeigen.
- Benutzereingabe auslesen:
  - Leer → Vorschlag übernehmen.
  - `q` → `null` zurückgeben (Abbruch).
  - Anderer Text → als Pfad interpretieren.
- Prüfen, ob der Pfad existiert und ein Verzeichnis ist:

  ```java
  if (!Files.exists(chosen) || !Files.isDirectory(chosen)) {
      // Fehlermeldung, Rückgabe null
  }
  ```

- Gültigen `Path` zurückgeben.

### 8.4 Analyse des Ordners (`analysiereOrdner`)

1. Eine neue Instanz von `KategorieZaehler` erzeugen.
2. Mit `Files.newDirectoryStream(downloadDir)` alle Einträge im Ordner durchlaufen.
3. Für jeden Eintrag prüfen:

   ```java
   if (Files.isRegularFile(entry)) {
       String kategorie = bestimmeKategorie(entry.getFileName().toString());
       zaehler.erhoehe(kategorie);
   }
   ```

4. Keine Dateien werden verändert – nur gezählt.
5. Ergebnis am Ende übersichtlich ausgeben.

**Wichtig:**  
`Files.newDirectoryStream(downloadDir)` geht **nicht rekursiv** in Unterordner. Es werden nur Dateien auf der obersten Ebene betrachtet. Das ist für Schulungszwecke bewusst einfach gehalten.

### 8.5 Sortierung des Ordners (`sortiereOrdner`)

1. Wieder alle Einträge im Ordner durchlaufen.
2. Für jeden Eintrag:

   - Nur reguläre Dateien betrachten (`Files.isRegularFile`).
   - Kategorie bestimmen:

     ```java
     String kategorie = bestimmeKategorie(fileName);
     ```

   - Zielordner konstruieren:

     ```java
     Path zielOrdner = downloadDir.resolve(kategorie);
     Files.createDirectories(zielOrdner);
     ```

     Falls der Zielordner noch nicht existiert, wird er automatisch angelegt.

   - Zieldatei bestimmen und verschieben:

     ```java
     Path zielDatei = zielOrdner.resolve(fileName);
     Files.move(entry, zielDatei, StandardCopyOption.REPLACE_EXISTING);
     ```

3. Am Ende eine Abschlussmeldung ausgeben.

**Wichtig für Lernende:**

- `Files.move` mit `REPLACE_EXISTING` ersetzt gleichnamige Dateien im Zielordner.
- Es findet nur ein **einfacher Move** statt – kein Kopieren, kein Rückgängig-Mechanismus.

### 8.6 Kategorisierung (`bestimmeKategorie` & `endetMit`)

- `bestimmeKategorie(String fileName)` wandelt den Dateinamen in Kleinbuchstaben um.
- Dann wird in fester Reihenfolge geprüft, ob der Name mit bestimmten Endungen endet.
- Die Hilfsmethode `endetMit(String name, String... endungen)` kapselt die Logik:

  ```java
  for (String endung : endungen) {
      if (name.endsWith(endung)) {
          return true;
      }
  }
  return false;
  ```

- So lassen sich neue Kategorien sehr leicht ergänzen, indem man:
  - neue `if (endetMit(...))`-Blöcke hinzufügt, und
  - die Klasse `KategorieZaehler` sowie die Ausgabe entsprechend erweitert.

### 8.7 Zähler-Klasse (`KategorieZaehler`)

- Eine kleine innere Klasse mit Feldern für jede Kategorie:

  ```java
  int bilder;
  int dokumente;
  int archive;
  int installer;
  int javaDateien;
  int sonstiges;
  ```

- Die Methode `erhoehe(String kategorie)` verwendet `switch`, um den passenden Zähler zu erhöhen.
- Die Klasse zeigt:
  - Wie man zustandsbehaftete Objekte verwendet.
  - Wie man solche Objekte an Methoden übergeben kann (by reference).

---

## 9. Kategorielogik (Dateitypen)

Die Zuordnung erfolgt aktuell wie folgt (alles in Kleinbuchstaben verglichen):

### Bilder

- `.png`
- `.jpg`
- `.jpeg`
- `.gif`
- `.bmp`
- `.webp`

### Dokumente

- `.pdf`
- `.doc`
- `.docx`
- `.txt`
- `.ppt`
- `.pptx`
- `.xls`
- `.xlsx`
- `.odt`
- `.md` (Markdown)

### Archive / Container

- `.zip`
- `.rar`
- `.7z`
- `.tar`
- `.gz`
- `.iso`
- `.img`
- `.dmg`
- `.vhd`
- `.vhdx`

### Installer

- `.exe`
- `.msi`

### Java

- `.jar`

### Sonstiges

- Alle Dateitypen, die in keine der obigen Kategorien fallen.

Die Kategorisierung erfolgt in der Methode `bestimmeKategorie(String fileName)` und kann leicht erweitert werden (z. B. um Audio, Video, Code-Dateien etc.).

---

## 10. Hinweise & Sicherheit

- Das Programm **verschiebt Dateien** innerhalb des gewählten Ordners in Unterordner.
- Standardmäßig wird mit `StandardCopyOption.REPLACE_EXISTING` gearbeitet:
  - Existiert eine Datei mit gleichem Namen im Zielordner, wird sie **überschrieben**.
- Für Schulungszwecke empfiehlt es sich, zuerst:
  - Mit Option `1` (Analyse) zu starten.
  - Einen Testordner mit Beispiel-Dateien anzulegen, statt den echten Download-Ordner zu verwenden.

### Empfehlung für den Unterricht

- Einen separaten Ordner z. B. `C:\Temp\DownloadTest` anlegen.
- Testdateien mit verschiedenen Endungen hineinkopieren.
- Lernende den Pfad explizit eingeben lassen.
- Wirkung der Sortierung sichtbar machen.

---

## 11. Erweiterungsideen

Einige sinnvolle Übungsaufgaben für Fortgeschrittene:

- **Trockenlauf-Modus**:
  - Option hinzufügen, die nur anzeigt, wohin Dateien verschoben würden, ohne tatsächlich zu verschieben.
- **Konfigurierbare Kategorien**:
  - Dateiendungen aus einer Konfigurationsdatei (z. B. `config.txt`) einlesen.
- **Log-Datei**:
  - Alle Verschiebe-Aktionen mit Datum/Zeit in eine `log.txt` schreiben.
- **Rückgängig machen (Undo-light)**:
  - Letzte Sortierung in einer Datei protokollieren, um sie teilweise zurücknehmen zu können.
- **Einfache GUI**:
  - Auf Basis dieses Konsolentools eine kleine Swing-Oberfläche bauen (z. B. Auswahl des Ordners per Datei-Dialog).

---

## 12. Lizenz

Dieses Projekt steht unter der MIT-Lizenz.

---

MIT License
===========

Copyright (c) {{YEAR}} {{YOUR NAME}}

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

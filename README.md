# Business Process Sonification
A multimodal tool combining sonification and visualization for ex post process mining and process analysis using XES event logs.

There is also a functionally similar version of the tool that integrates itself as package/plugin in the process mining tool ProM (http://www.promtools.org/).

For more details and instructions how to use the package/plugin for ProM see also the project homepage (http://cs.univie.ac.at/wst/forschung/projekte/projekt/infproj/1063/).

## Authors
* Faculty of Computer Science University of Vienna, Austria
  * Felix Amerbauer
  * Tobias Hildebrandt
  * Stefanie Rinderle-Ma

## Build Preparation
* Copy the following JARs to the `lib` folder
  * Open XES 2.15 https://svn.win.tue.nl/trac/prom/export/latest/Releases/OpenXES/OpenXES-20160212.jar
  * JFugue 5.0.7 http://www.jfugue.org/jfugue-5.0.7.jar
  * slickerbox http://code.deckfour.org/slickerbox/slickerbox-1.0rc1.tar.gz (extract TAR)
  * Spex (spex.jar Version 1.1 take from http://www.promtools.org/prom6/downloads/prom-6.6-all-platforms.tar.gz)
  * Widgets (take from http://www.promtools.org/prom6/packages66/Widgets/Widgets-6.6.153-all.zip)

* Other steps
  * Install JDK 1.8+ (Tested with Oracle JDK)
  * Install the build tool SBT http://www.scala-sbt.org/download.html
  * Optionally download soundfount
    - go to http://www.schristiancollins.com/generaluser.php and download the latest version of the soundfont in the Current section of the page (https://dl.dropboxusercontent.com/u/8126161/GeneralUser_GS_1.47.zip)
    - extract zip and copy GeneralUser GS v1.47.sf2 to the folder containing the fat JAR

## Build
```bash
# Build executable fat JAR
sbt assembly
```
## Usage
```bash
cd target/scala-2.11
java -jar sonification_1.0.5.jar
```

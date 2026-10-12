#!/bin/bash
# Builds TastyBite.app (macOS) from the project sources.
# Run it from the project folder:   ./build_mac_app.sh
# Needs: a full JDK 17 or newer (it must include "jpackage") and the MySQL Connector/J jar in the lib folder.

set -e
cd "$(dirname "$0")"

command -v javac >/dev/null 2>&1 || { echo "ERROR: javac not found. Install a JDK 17 or newer (see HOW_TO_MAKE_MAC_APP.md)."; exit 1; }
command -v jpackage >/dev/null 2>&1 || { echo "ERROR: jpackage not found. You need a full JDK 17 or newer, not just Java."; exit 1; }
ls lib/*.jar >/dev/null 2>&1 || { echo "ERROR: put the MySQL Connector/J jar inside the lib folder first."; exit 1; }

echo "Using: $(javac -version 2>&1)"
rm -rf build release dist
mkdir -p build/classes dist

echo "1/4  Compiling..."
javac -d build/classes -cp "lib/*" $(find src -name "*.java")

echo "2/4  Adding the background image and the MySQL driver..."
cp -R resources/. build/classes/
for j in lib/*.jar; do
  (cd build/classes && unzip -qo "../../$j" -x 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*.DSA' 'META-INF/MANIFEST.MF' 'module-info.class' 'META-INF/versions/*/module-info.class' >/dev/null 2>&1 || true)
done

echo "3/4  Creating TastyBite.jar..."
jar cfe dist/TastyBite.jar com.tastybite.Main -C build/classes .

echo "4/4  Creating the app..."
ICON=""
[ -f TastyBite.icns ] && ICON="--icon TastyBite.icns"
jpackage --type app-image --name TastyBite --input dist --main-jar TastyBite.jar \
  --main-class com.tastybite.Main --dest release $ICON

echo
echo "Done. Your app is in the release folder:"
ls release

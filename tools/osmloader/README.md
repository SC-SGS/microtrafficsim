# OpenStreetMap XML downloader

The simulator uses map files in the OpenStreetMap (OSM) XML format as, for example, provided by the [OpenStreetMap web interface](https://www.openstreetmap.org).
The OSM web interface helps finding the right coordinates.

The python tool `osmloader` can be used to download such map files (e.g. if your file is too large for OSM).
Though, the used API is the `overpass`-API because it allows the download of larger files than OpenStreetMap does.

Really big maps (like countries or member states) can be downloaded from [Geofabrik](http://download.geofabrik.de).

The script offers downloading self-defined maps (name and coordinates given as cmdline args) or predefined ones (see `-h`).


## Requirements

* `python3`

using additional python packages
* `urllib3`


## Usage

You can call the script directly using python or using a Gradle task:
```shell
# using python
py tools/osmloader/src/main.py <args>

# Gradle
./gradlew :tools:osmloader:run -Dexec.args="<args>"
```
For further information, set `<args>` to `-h`.

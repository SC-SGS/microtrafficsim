# Microscopic Traffic Simulation
with OpenStreetMap data.

## Demonstration and Examples
Demonstrations and examples can be executed using `gradle`. All
OpenStreetMap (OSM) files need to be in the OSM XML format, as, for
example, provided by the
[OpenStreetMap web interface](https://www.openstreetmap.org).

#### Main UI
The main UI provides an overview of the simulation. The scenario is
currently fixed, however various simulation parameters, as well as the
used map, can be adjusted. At the moment, the main ui uses a segment-based
visualization and thus may be slow when using large maps. It can be
executed using

```shell
./gradlew :microtrafficsim-ui:run
```

The temporary branch `tmp/presentation` currently contains an
experimental implementation using a tile-based visualization.

#### Fixed Simulation Example
The fixed simulation example features a fixed scenario, fixed parameters,
and a segment-based visualization. It can be executed using
```shell
./gradlew :examples:simulation:run -Dexec.args="<path to OSM-XML file>"
```

#### Map-Viewer Example
Map-Viewer examples can be executed using 
```shell
./gradlew :examples:mapviewer_tilebased:run -Dexec.args="<path to OSM-XML file>"
```
for the tile-based version and
```shell
./gradlew :examples:mapviewer_segmentbased:run -Dexec.args="<path to OSM-XML file>"
```
for the segment based version. In the `master`-branch, the tile-based version
is not fully implemented yet. An experimental (functioning) version can be found in
the branch `wip/visualizaton`.


## Please Note
This software is still in an experimental state. Due to a few deadlines the
documentation is sparse and will be provided later, expect it after the 18.06.

# Microscopic Traffic Simulation
with OpenStreetMap data.


## News
On 18 June 2016, we will have a stand at the
[Tag der Wissenschaft Uni Stuttgart](http://www.uni-stuttgart.de/tag/2016/). You are welcome to visit us.


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
for the segment based version.


## Supported traffic attributes
All attributes can be en-/disabled.
* different vehicle types (inclusive different max velocities, acceleration functions etc.)
* static routing: fastest vs. shortest route
* streets' max velocity
* single laned streets
* crossing logic: street priorities (roundabouts are handled with right-before-left)
* crossing logic: right-before-left
* crossing logic: left-before-right
* crossing logic: random
* "friendly-standing-in-jam": If a vehicle has to wait at a crossroad, it relinquishes its right of way for an other vehicle that has not to wait.


## Please Note
This software is still in an experimental state. Due to a few deadlines, the
documentation is sparse and will be provided later, expect it after the 18.06.

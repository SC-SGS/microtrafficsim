# Microscopic Traffic Simulation
with OpenStreetMap data.


## News
We are cleaning up the existing project. This includes detailed Javadocs,
removing quick fixes or creating branches for them.


## Demonstration and Examples
Demonstrations and examples can be executed using `gradle`. All OpenStreetMap
(OSM) files need to be in the OSM XML format, as, for example, provided by the
[OpenStreetMap web interface](https://www.openstreetmap.org). A (somewhat)
stable demonstration can be executed from the `tmp/tdw`-branch (see Main UI).

#### Main UI
The main UI provides an overview of the simulation. The scenario is currently
fixed (random routes), however various simulation parameters, as well as the
number of vehicles and the used map, can be adjusted

```shell
./gradlew :microtrafficsim-ui:run
```

#### Fixed Simulation Example
The fixed simulation example features a fixed scenario and fixed parameters, It
represents a former state of development, therefore the simulation parameters
can't be changed without changing code. It can be executed using
```shell
./gradlew :examples:simulation:run -Dexec.args="<path to OSM-XML file>"
```

#### Map-Viewer Example
The map-viewer example can be executed using
```shell
./gradlew :examples:mapviewer:run -Dexec.args="<path to OSM-XML file>"
```


## Supported traffic attributes
* different vehicle types (inclusive different max velocities, acceleration
  functions etc.)
* static routing: fastest vs. shortest route
* streets' max velocity
* single laned streets

All following attributes can be en-/disabled.

* crossing logic: street priorities
* crossing logic: right-before-left XOR left-before-right XOR random
* "friendly-standing-in-jam": If a vehicle has to wait at a crossroad, it
  relinquishes its right of way for an other vehicle that has not to wait.


## Please Note
This software is still in an experimental state. Due to a few deadlines, the
documentation is sparse. We are currently working on this.

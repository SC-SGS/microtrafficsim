# Microscopic Traffic Simulation
with OpenStreetMap data.

![Teaser: New York](teaser.png "Teaser: New York")

## News
Testing the simulation execution for determinism has finished. According to our test cases the simulation execution is deterministic.

Our next task is implementing a scenario handling the input from the polygon overlay, that allows an user to choose polygonial areas on a map with an ui dynamically. Finishing this, we will start developing multilane logic. This will be used in a bachelor thesis for traffic jam cause research.

In parallel, we implement a serialization for our graphs, route calculations and scenarios. This feature allows us simulating scenarios with more than a few 1000 vehicles without the need to wait every time for finishing the route calculations. This is helpful for the bachelor thesis.


## Demonstration and Examples
Demonstrations and examples can be executed using `gradle`. All OpenStreetMap (OSM) files need to be in the OSM XML format, as, for example, provided by the [OpenStreetMap web interface](https://www.openstreetmap.org). If your file is too large for OSM, you can use our python-script to download it (`osm_get_xml.py`). A (somewhat) stable demonstration can be executed from the `master`-branch (see Main UI).

#### Main UI
The main UI provides an overview of the simulation. The scenario is currently fixed (random routes), however various simulation parameters, as well as the number of vehicles and the used map, can be adjusted

```shell
./gradlew :microtrafficsim-ui:run
```

#### Fixed Simulation Example
The fixed simulation example features a fixed scenario and fixed parameters, It represents a former state of development, therefore the simulation parameters can't be changed without changing code. It can be executed using
```shell
./gradlew :examples:simulation:run -Dexec.args="<path to OSM-XML file>"
```

#### Map-Viewer Example
The map-viewer example can be executed using
```shell
./gradlew :examples:mapviewer:run -Dexec.args="<path to OSM-XML file>"
```


## Supported features
#### Traffic attributes
* different vehicle types (inclusive different max velocities, acceleration
  functions etc.)
* static routing: fastest vs. shortest route
* streets' max velocity
* single laned streets
* driver behaviour (e.g. in acceleration) limited by the vehicles "physical" behaviour

#### Crossing logic
All following attributes can be en-/disabled.
* street priorities
* right-before-left XOR left-before-right XOR random
* more than one vehicle can cross a crossroad if the are not intersecting eachother's ways
* "friendly-standing-in-jam": If a vehicle has to wait at a crossroad, it
  relinquishes its right of way for an other vehicle that has not to wait.

#### Main-ui features
* parse any map file in OSM MAP format
* interrupt parsing or route calculations without exiting the main-ui
* different style sheets can be used (the preferred style sheet has to be chosen in code)
* different vehicle colors depending on the vehicle's attributes (e.g. anger of its driver)


## Please Note
This software is still in an experimental state.


## Current contributors

**Fabian Franzelin**  
has been our supervisor for the first 6 months and he is still helping us in monthly/weekly meetings with advices and his experience.

**Maximilian Luz**  
is part of our team since beginning. He has written the OSM parser and is our visualization guy.

**Dominic Parga Cacheiro**  
is part of our team since beginning. As well as managing organizational work, he developes and improves the vehicle/traffic logic and shortest path algorithms. He also implements the user interface.


## Former contributors
Some people have worked on this project, but there is no right place to name them. Thus we name them here.

**Jan-Oliver Schmidt**  
has been part of our team in the very first 6 months. He worked at the single-laned traffic logic, the shortest path algorithms (especially A-star) and our first paper.


## Papers

#### [Microscopic Traffic Simulation](https://www.informatik.uni-stuttgart.de/studium/interessierte/bsc-studiengaenge/informatik/projekt-inf/2016-06-03/Gruppe_5.pdf)
Mobility plays a huge role in modern society and is the reason for the necessity of a well developed and carefully planned infrastructure. Unfortunately, the volume of traffic is irregular and its flow is influenced on certain, hardly controllable parameters (such as weather) which leads to the occurance of traffic jams. Observing reasons for the emergence of such phenomneas in reality is difficult, trying to predict bottlenecks on planned streets without a simulation almost impossible. This paper deals with an approach to a real-time microscopic traffic simulation, based on OpenStreetMap data and single-laned streets. We base this approach on the well known Nagel-Schreckenberg model which we later extend, and will focus on explaining the logic at crossroads.

*Please consider: The only appearance of the word "evaluation" is misspelled as "evacuation", which could be a word you are looking for. Furthermore, due to an implementation bug, unfortunately our examples in the following paper are wrong. Theory is correct.*

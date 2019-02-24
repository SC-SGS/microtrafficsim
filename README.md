# Microscopic Traffic Simulation

with OpenStreetMap data.

![Teaser: Stuttgart](resources/teaser_2018-06-12_Stuttgart.png "Teaser: Stuttgart")


## News

The bachelor thesis about Uncertainty Quantification and multilane traffic simulation is finally linked [in the wiki](https://github.com/sgs-us/microtrafficsim/wiki/papers#quantifizierung-von-unsicherheiten-in-mikroskopischer-verkehrssimulation).
A new [release](https://github.com/sgs-us/microtrafficsim/releases) containing the multilane logic has been published.

The next few months will be attended to maintenance.
In general, the project should be prepared for workers other than project's initiators.
This includes:
* A lot of Documentation (besides existing comments)
* Writing/extending a Github Wiki including explanations about the code structure itself
* Include [Travis CI](https://travis-ci.org) for doing (JUnit) tests automatically on serverside.
* Improvements for interacting with the simulator, e.g. simple scenario creations for developers outside this project.
  The simulation code has many lines being able to support some interesting features like different driver profiles, but the simulator is not prepared to fully support these features' potential (mixed driver profiles at once).

In addition to that, some maintainability issues occured:
* In this project, [JOGL](http://jogamp.org/jogl/www/) is used as Java binding for the OpenGL API.
Regarding this [link](http://jogamp.org/deployment/archive/rc/) in May 2018, JOGL hasn't been updated since 2015.
Thus the project will use another binding.
[LWJGL](https://www.lwjgl.org) is a good alternative.
* This project uses own math-code to handle calculations.
It is helpful to "outsource" this code.
[JOML - Java OpenGL Math Library](https://github.com/JOML-CI/JOML) seems to meet our needs.
JOML has been updated recently (May 2018), it is usable in concurrent execution environments and, on its main page, it has an own chapter about its usage with LWJGL (among others).

Last but not least, sometimes, when writing Java, you miss kind of handy "syntactical sugar".
Two alternatives for Java are [Kotlin](https://kotlinlang.org) vs. [Scala](https://www.scala-lang.org/).
The syntax of both languages is fresh and modern.
Since respectively Kotlin or Scala files can be added to a Java-project and their compiler are able to handle it, Kotlin and Scala are 100% interoperable with Java.
Unfortunately, both languages aren't fully supported by many IDEs yet (Jan 2019).
Therefore, for the moment, the traffic simulation code will still be written in Java.


## Usage

Demonstrations and examples can be executed using `gradle`.
This project contains a `gradle wrapper` file, so you don't need to install it.
A (somewhat) stable demonstration can be executed from the `master`-branch.
For the execution commands and helpful information, please refer [to our wiki](https://github.com/sgs-us/microtrafficsim/wiki/Usage#setup-demonstration-and-examples).


## Contribution

For contributing the project, please refer [to our contribution section](CONTRIBUTING.md).


## Supported features

#### Traffic attributes

* different vehicle types (inclusive different max velocities,
acceleration functions etc.)
* static routing: fastest vs. shortest route
* streets' max velocity
* multilaned streets
* driver behaviour (e.g. in acceleration) limited by the vehicles
"physical" behaviour


#### Crossing logic

All following attributes can be en-/disabled.
* street priorities
* right-before-left XOR left-before-right XOR random
* more than one vehicle can cross a crossroad if the are not
intersecting eachother's ways
* "friendly-standing-in-jam": If a vehicle has to wait at a crossroad,
it relinquishes its right of way for an other vehicle that has not to
wait.


#### Main UI features

* parse any map file in OSM MAP format
* start and end areas can be chosen using the shortcuts described
[in our wiki](https://github.com/sgs-us/microtrafficsim/wiki/Usage#controls)
* interrupt parsing or route calculations without exiting the main-ui
* routes can be serialized
* different style sheets can be used (the preferred style sheet has to
be chosen in code for now)
* different vehicle colors depending on the vehicle's attributes (e.g.
anger of its driver or the vehicle's velocity)


## Please Note

This software is still in an experimental state.

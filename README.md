# Microscopic Traffic Simulation
with OpenStreetMap data.

## Demonstration and Examples
Demonstrations and examples can be executed using `gradle`:

- main ui:
    ```./gradlew :microtrafficsim-ui:run```
- fixed simulation example:
    ```./gradlew :examples:simulation:run -Dexec.args="<path to OSM-XML file>"```
- tilebased mapviewer:
    ```./gradlew :examples:mapviewer_tilebased:run -Dexec.args="<path to OSM-XML file>"```

## Please Note
This software is still in an experimental state. Due to a few deadlines the
documentation is sparse and will be provided later, expect it after the 18.06.

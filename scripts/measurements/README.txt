#######################
##  PLOT GENERATION  ##
#######################
Im Folgenden ist erklärt, wie man aus Daten der Simulation Plots generieren
kann. Das ganze ist aufgrund von Zeitmangel etwas provisorisch oder schlampig
aufgebaut und hat unter Umständen Verbesserungspotential, aber auf alle Fälle
Verschönerungspotential. Die Erklärung verläuft chronologisch, d.h. die
Schritte können in der Reihenfolge ausgeführt werden, in der sie hier erklärt
werden.

------------------------------
--  ./data/raw_java_output  --
------------------------------
Dient als Zwischenablage für "frische Daten" aus einer Simulation. Diese Daten
sind als csv-Datei gespeichert und können dann weiter verarbeitet werden.

--------------------
--  ./data/input  --
--------------------
Für das Java Programm, welches den Durchschnitt mehrerer csv-Dateien des
gleichen Typs (z.B. age_when_despawning.pdf) berechnet, werden hier csv-Dateien
gespeichert. Die Ordner hierfür sind age, anger, linear_distance, spawned_count
und total_anger. Die generierten csv-Dateine mit den Durchschnittswerten werden
standardmäßig in ./data/csv gespeichert.

------------------
--  ./data/csv  --
------------------
Hier sind die Dateien gespeichert, die von py verarbeitet werden. Die müssen
vor dem Ausführen hier abgelegt werden.

------------------
--  ./data/pdf  --
------------------
Hier werden die Plots gespeichert, die py aus den csv-Dateien produziert.


#############
##  OTHER  ##
#############
Hier sind noch ein paar andere Dateien/Ordner erläutert.

-------------
--  ./src  --
-------------
Hier ist das Projekt gespeichert, welches aus mehreren csv-Dateien einen
Durchschnitt bildet.

-------------
--  ./py  --
-------------
Hier sind alle python-Dateien gespeichert, die aus den csv-Daten plots
generieren. Wie oben beschrieben werden die csv-Dateien aus ./data/csv geladen
und in ./data/pdf werden die Plots abgelegt. Abhängig vom Skript kann das
manuell beim Ausführen eingegeben werden. Prinzipiell kann jedes Skript mit -h
einen Beschreibungstext für die Bedienung auf die Konsole schreiben.

---------------------
--  csv_rules.txt  --
---------------------
In dieser Datei wird die Struktur der csv-Dateien beschrieben.

--------------------
--  planning.txt  --
--------------------
Diese Datei diente als Hilfe zur Planung der csv_rules.txt und kann prinzipiell
ignoriert werden. Möglicherweise hilft sie beim Verständnis.

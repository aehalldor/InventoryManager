# CSCE_315 Group 22 Project 2 Phase 3

To compile:
javac -cp ".;jcommon-1.0.0.jar;jfreechart-1.0.1.jar" ManagerGUI.java GUI.java InventoryUsage.java

To run:
java -cp ".;postgresql-42.2.8.jar;jcommon-1.0.0.jar;jfreechart-1.0.1.jar"  GUI [0 | 1]
Use arg 0 or no additional cmd args to open the server GUI.
Use arg 1 to open just the manager GUI


To test insertions, use week 10, day Sunday. That way we know what day we used to test and can easily remove bad data.
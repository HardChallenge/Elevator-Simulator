# Elevator-Simulator
Inspired by the real life elevator, this multi-threaded, graphic application implements an exact replica of a set of elevators which can work in any building. For the algorithmic part is uses Java with database side (JDBC and PostgreSQL) and for the graphics part it uses JavaFX.

# Implementation
1. 'Main' thread which holds the new calls from the clients which come and push the button from a specific elevator.
2. 'Elevator' threads which are holding the algorithm behind the workflow.
3. 'TripCreator' thread which is reponsible with inserting the logs of elevators into the database which are 'dumped' into a ConcurrentLinkedQueue in the 'Main' thread.
4. 'GUI' thread which is reponsible with all the graphics displayed on the screen.

# Known bugs
Algorithm side: none  
GUI side: 
- when the algorithm will not take any more clients because the elevator is at limit capacity ((maxCapacity - 100) -> maxCapacity) the GUI will 'falsely' take them, eventually returning to the same floor where the clients dissappeared

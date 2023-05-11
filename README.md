# Elevator-Simulator
Inspired by the real life elevator, this multi-threaded application implements an exact replica of a set of elevators which can work in any building as long as the number
of floors is specified. It uses simple Java combined with PostgreSQL through JDBC.

# Implementation
1. 'Main' thread which holds the new calls from the clients which come and push the button from a specific elevator.
2. 'Elevator' threads which are holding the algorithm behind the workflow.
3. 'TripCreator' thread which is reponsible with inserting the logs of elevators which are 'dumped' into a ConcurrentLinkedQueue in the 'Main' thread.

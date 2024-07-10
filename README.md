# Elevator Simulator

![Build Status](https://img.shields.io/badge/status-working-brightgreen) 
![Version](https://img.shields.io/badge/version-v1.0-blue)
[![GitHub contributors](https://img.shields.io/github/contributors/HardChallenge/Elevator-Simulator)](https://github.com/HardChallenge/Elevator-Simulator/graphs/contributors)
![Total Views](https://views.whatilearened.today/views/github/HardChallenge/Elevator-Simulator.svg)
[![GitHub issues](https://img.shields.io/github/issues/HardChallenge/Elevator-Simulator)](https://github.com/HardChallenge/Elevator-Simulator/issues)
[![License](https://img.shields.io/badge/license-MIT-green)](https://github.com/HardChallenge/Elevator-Simulator/blob/main/LICENSE)

Inspired by the real life elevator, this multi-threaded, graphic application implements an exact replica of a set of elevators which can work in any building. For the algorithmic part is uses Java with database side (JDBC and PostgreSQL) and for the graphics part it uses JavaFX.

--- 

## Contents

- [Introduction](#introduction)
- [Functionalities](#functionalities)
- [Implementation](#implementation)
- [Technologies](#technologies)
- [Setup Guide](#setup-guide)
- [Usage](#usage)

---

## Introduction


**Elevators Simulator** is a robust Java application, leveraging JavaFX for an intuitive graphical interface and multi-threading for efficient logging. It accurately emulates a system of elevators operating across any number of floors, providing a realistic simulation environment. With support for complex elevator logic and scalable floor configurations, this tool is ideal for testing and optimizing elevator algorithms and systems.


--- 

## Functionalities

- **Multi-Threaded Logging**: Utilizes multi-threading to log elevator activities asynchronously, ensuring accurate and efficient record-keeping without impacting performance.
- **Scalable Simulation**: Supports any number of floors and elevators, allowing for flexible and extensive simulation scenarios.
- **Intuitive User Interface**: Implements JavaFX to provide a user-friendly graphical interface, making it easy to visualize elevator movements and operations.
- **Realistic Elevator Logic**: Emulates realistic elevator behaviors and algorithms, including handling multiple simultaneous requests and prioritizing floors efficiently.

---

# Implementation
1. 'Main' thread which holds the new calls from the clients which come and push the button from a specific elevator.
2. 'Elevator' threads which are holding the algorithm behind the workflow.
3. 'TripCreator' thread which is reponsible with inserting the logs of elevators into the database which are 'dumped' into a ConcurrentLinkedQueue in the 'Main' thread.
4. 'GUI' thread which is reponsible with all the graphics displayed on the screen.

---

## Technologies

- **Backend**: Java, PostgreSQL
- **Frontend**: JavaFX

---

## Setup Guide

1. Clone the repository:

```bash
git clone https://github.com/HardChallenge/Elevator-Simulator.git
```

2. Change the directory:

```bash
cd Elevator-Simulator
```

---

## Usage

Run the application:

```bash
javac elevator_project/src/main/org/example/Main.java
java elevator_project/src/main/org/example/Main
```

---



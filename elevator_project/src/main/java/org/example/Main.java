package org.example;

import GUI.GUI;
import models.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    private static final String url = "jdbc:postgresql://localhost/Elevators";
    private static final String user = "postgres";
    private static final String password = "postgres";
    public static final Scanner scanner = new Scanner(System.in);
    public static int numberOfElevators = 0, numberOfFloors = 0;
    public static List<Elevator> elevatorList;
    private static volatile List<List<Client>> calls;
    public static volatile ConcurrentLinkedQueue<Trip> trips;
    public static volatile List<Integer> floorsToGo;
    public static final int WAIT_FOR_CLIENTS = 5000; // asteptam clientii sa urce in lift
    public static final int TRAVERSE_FLOOR = 5000; // asteptam traversarea unui etaj

    static{
        calls = new ArrayList<>();
        trips = new ConcurrentLinkedQueue<>();
        floorsToGo = new ArrayList<>();
    }


    public static void main(String[] args) {

        System.out.println("Initializing project...");

        // DATABASE
        Repository repository = null;
        getRequiredData();
        try{
            repository = new Repository(url, user, password);
            repository.initialize();
        } catch(SQLException e){
            System.out.println("Database error: " + e.getMessage());
            System.exit(2);
        }
        // --------
        elevatorList = new ElevatorCreator(numberOfElevators, repository).create();
        for(int i = 0, n = elevatorList.size(); i<n; i++){
            calls.add(new ArrayList<>());
            floorsToGo.add(0);
        }
        TripCreator tripCreator = new TripCreator(repository);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
            for (Elevator thread : elevatorList) {
                thread.stopRunning();
                thread.join();
                }
            tripCreator.stopRunning();
            tripCreator.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        new Thread(() -> {
            GUI.launch(GUI.class, args);
        }).start();

        System.out.println("Project initialized successfully.\nTo add clients -> client <from> <to> <elevatorId> <weight>");

        // starting the threads
        for(Elevator elevator : elevatorList) elevator.start();
        tripCreator.start(); // client 2 5 0 70  client 6 1 0 30 client 2 5 0 30
        //--------
        String command = "";
        scanner.nextLine(); 
        while(!command.equals("exit")){

            command = scanner.nextLine();
            if(command.matches("^client\\s\\d+\\s\\d+\\s\\d+\\s\\d+")){
                String[] parts = command.split(" ");
                int from = Integer.parseInt(parts[1]), to = Integer.parseInt(parts[2]), elevatorId = Integer.parseInt(parts[3]), weight = Integer.parseInt(parts[4]);
                if(validateInput(from, to, elevatorId, weight)){
                    access("write", elevatorId, new Client(from, to, weight, elevatorId));
                } else {
                    System.out.println("Invalid data.");
                }
            } else {
                System.out.println("Wrong command.");
            }
        }

        // stopping all the threads
        for(Elevator elevator : elevatorList){
            elevator.interrupt();
        }

        // DATABASE
        try{
            repository.connection.close();
        } catch(SQLException e){
            System.out.println("Error at closing the database: " + e.getMessage());
            System.exit(2);
        }
        // ------
    }

    private static void getRequiredData(){
        while (numberOfElevators < 1 || numberOfFloors < 2) {
            System.out.println("Specify the number of elevators:");
            numberOfElevators = scanner.nextInt();
            System.out.println("Specify the number of floors of the building:");
            numberOfFloors = scanner.nextInt();
        }
    }

    public static synchronized List<Client> access(String type, Object... params){
        if(type.equals("read")){
            // sunt un thread care vrea sa isi citeasca din calls
            // params: int elevatorId -> params[0]
            int elevatorId = (int) params[0];
            List<Client> called = new ArrayList<>(calls.get(elevatorId));
            calls.set(elevatorId, new ArrayList<>());
            return called;

        } else if (type.equals("write")){
            //sunt main thread care vrea sa scrie o noua cerere
            //params: int elevatorId -> params[0], Client client -> params[1];
            int elevatorId = (int) params[0];
            Client client = (Client) params[1];
            GUI.getDestinationsFromMain.add(client.from);
            GUI.personElevator.add(client.elevatorId);
            calls.get(elevatorId).add(client);
            return null;
        }
        return new ArrayList<>();
    }

    private static boolean validateInput(int from, int to, int elevatorId, int weight){
        if(from < 0 || from > numberOfFloors - 1 || to < 0 || to > numberOfFloors - 1
                || weight < 5 || weight > 100 || elevatorId < 0 || elevatorId > elevatorList.size()-1) return false;
        return true;
    }

    public static synchronized void setFloorsToGo(int elevatorId, int floor){
        floorsToGo.set(elevatorId, floor);
    }
}
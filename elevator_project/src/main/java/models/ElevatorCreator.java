package models;

import com.github.javafaker.Faker;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElevatorCreator {
    private final int numberOfElevators;
    Repository repository;

    public ElevatorCreator(int numberOfElevators, Repository repository) {
        this.numberOfElevators = numberOfElevators;
        this.repository = repository;
    }

    public List<Elevator> create(){
        List<Elevator> elevatorList;
        elevatorList = IntStream.range(0, numberOfElevators).mapToObj(n -> {
            Random rand = new Random();
            int maxCapacity = rand.nextInt(0, 201) + 400; // capacity between 400-600kg
            boolean haveMirror = rand.nextBoolean();
            String backgroundColor = new Faker().color().name();
            Elevator elevator = new Elevator(n, maxCapacity, haveMirror, backgroundColor);

            try {
                repository.create("elevators", maxCapacity, haveMirror, backgroundColor);
            } catch (SQLException e) {
                System.out.println("Error at creating a elevator! Message: " + e.getMessage());
            }

            return elevator;
        }).collect(Collectors.toList());
        return elevatorList;
    }


}

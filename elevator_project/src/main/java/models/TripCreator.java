package models;

import org.example.Main;

import java.sql.SQLException;

public class TripCreator extends Thread{
    private boolean running = true;
    Repository repository;

    public TripCreator(Repository repository){
        this.repository = repository;
    }

    @Override
    public void run() {
        Trip trip;
        while(running){
            while(Main.trips.size() != 0){
                trip = Main.trips.poll();
                try {
                    repository.create("trips", trip.elevatorId, trip.startedAt, trip.stoppedAt, trip.numberOfStops, trip.direction, trip.weightTransported);
                } catch (SQLException e) {
                    System.out.println("Error at creating trip in database. " + e.getMessage());
                }
            }
            try {
                sleep(8000); // waiting for elevators to post data about trips
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Trip creator is stopping.");
    }

    public void stopRunning(){
        running = false;
    }
}

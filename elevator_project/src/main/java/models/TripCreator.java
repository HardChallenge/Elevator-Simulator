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
        Trip trip = null;
        while(running){
            while(!(Main.trips.size() == 0)){
                System.out.println("Trip creator: Found a trip!" );
                trip = Main.trips.poll();
                System.out.println(trip.toString());
                try {
                    repository.create("trips", trip.elevatorId, trip.startedAt, trip.stoppedAt, trip.numberOfStops, trip.direction, trip.weightTransported);
                } catch (SQLException e) {
                    System.out.println("Error at creating trip in database. " + e.getMessage());
                }
            }
            System.out.println("Trip Creator: Didn't found any trips.");
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

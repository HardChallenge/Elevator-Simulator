package models;

import org.example.Main;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Elevator extends Thread{
    private final int elevatorID, maxCapacity;
    private int currentCapacity;
    private final boolean haveMirror;
    private final String backgroundColor;
    private int currentFloor = 0;
    private List<Client> requests;
    private List<Client> called;
    private List<Client> newCalled;
    private String direction = "Going Up";
    private String nextFloor = "none -1";
    private boolean justStarted = false, justForbiddenClients = false;
    private volatile boolean running = true;
    public int weightTransported  = 0, numberOfStops = 0;
    public Timestamp startedAt, stoppedAt;


    public Elevator(int elevatorID, int maxCapacity, boolean haveMirror, String backgroundColor){
        this.elevatorID = elevatorID;
        this.maxCapacity = maxCapacity;
        this.haveMirror = haveMirror;
        this.backgroundColor = backgroundColor;
        this.currentCapacity = 0;
        this.called = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.newCalled = new ArrayList<>();
    }

    public void stopRunning() {
        running = false;
    }

    @Override
    public void run() {
        int floorToGo;
        while(running){
            while(called.size() == 0 && running){ // nu avem nici request-uri
                try {
                    sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.called = Main.access("read", elevatorID);
                justStarted = false;
            }

            weightTransported = numberOfStops = 0;
            startedAt = new Timestamp(System.currentTimeMillis());

            while((called.size() != 0 || requests.size() != 0) && running){
//                System.out.printf("currentCapacity: %d, requests: %d, called: %d, direction: %s, floorToGo: %s ", currentCapacity, requests.size(), called.size() + newCalled.size(), direction, nextFloor);
                if(!justStarted){
                    justStarted = true;
                    nextFloor = nextCalled(); // verificam in directia in care a mers ultima data liftul
                    if(nextFloor.equals("none -1")){ // daca nu gasim, sigur avem in cealalta directie, schimbam directia
                        changeDirection();
                        nextFloor = nextCalled(); // client 2 5 0 100  client 4 1 0 100  client 8 2 0 100 client 2 3 0 100 client
                    }
                    Main.setFloorsToGo(elevatorID, Integer.parseInt(nextFloor.split(" ")[1]));
                }
                if(newCalled.size() != 0)
                    checkNew();

                floorToGo = Integer.parseInt(nextFloor.split(" ")[1]);
                if(floorToGo == currentFloor) {
                    numberOfStops++;
//                    System.out.printf("Elevator (ID: %d) reached a destination: %s. %n", elevatorID, nextFloor);
                    takeClients(currentFloor);
                    dropClients(currentFloor);
                    nextFloor = nextCalled();
                    if(nextFloor.equals("none -1") && (called.size() != 0 || requests.size() != 0)){
                        stoppedAt = new Timestamp(System.currentTimeMillis());
                        Main.trips.add(new Trip(elevatorID, startedAt, stoppedAt, numberOfStops, direction, weightTransported));
                        changeDirection();
                        nextFloor = nextCalled();
                    }

                    Main.setFloorsToGo(elevatorID, Integer.parseInt(nextFloor.split(" ")[1]));

                    try {
                        sleep(Main.WAIT_FOR_CLIENTS);
                    } catch (InterruptedException e) { // asteptam clientii sa se urce in lift
                        throw new RuntimeException(e);
                    }

                }
                if(direction.equals("Going Down") && !nextFloor.equals("none -1")){
                    currentFloor -= 1;
                    if (justForbiddenClients) {
                        resetAllowance(currentFloor + 1);
                        justForbiddenClients = false;
                    }
//                    System.out.printf("Elevator (ID: %d) going down, %d -> %d\n", elevatorID, currentFloor + 1, currentFloor);
                } else if (direction.equals("Going Up") && !nextFloor.equals("none -1")){
                    currentFloor += 1;
                    if(justForbiddenClients) {
                        resetAllowance(currentFloor - 1);
                        justForbiddenClients = false;
                    }
//                    System.out.printf("Elevator (ID: %d) going up, %d -> %d\n",elevatorID, currentFloor - 1, currentFloor);
                } else {
//                    System.out.printf("Elevator (ID: %d) in idle state, floor %d.\n", elevatorID, currentFloor);
                    stoppedAt = new Timestamp(System.currentTimeMillis());
                }

                try {
                    sleep(Main.TRAVERSE_FLOOR); // pentru traversarea unui etaj
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.newCalled = Main.access("read", elevatorID);
            }
        }

        System.out.println("Elevator " + elevatorID + " is stopping.");
    }

    private String nextCalled(){
        int best;
        if(called.size() == 0) return nextRequest("none -1");
        if(direction.contains("Going Up")){
            best = Main.numberOfFloors;
            for(Client client : called){
                if(client.from >= currentFloor && client.from <= best && client.allowed){
                    best = client.from;
                }
            }
            if(best == Main.numberOfFloors) return nextRequest("none -1"); // nu mai avem calluri in sus
                else return nextRequest("Call " + best);
        } else {
            best = -1;
            for(Client client : called){
                if(client.from <= currentFloor && client.from >= best && client.allowed){
                   best = client.from;
                }
            }
            if(best == -1) return nextRequest("none -1"); // nu mai avem calluri in jos
                else return nextRequest("Call " + best);
        }

    }

    private String nextRequest(String resultCalled){
        if(requests.size() == 0) return resultCalled;
        int bestRequest, bestCall = Integer.parseInt(resultCalled.split(" ")[1]);

        if(direction.contains("Going Up")){
            bestRequest = Main.numberOfFloors;
            for(Client client : requests){
                if(client.to >= currentFloor && client.to <= bestRequest && client.allowed) bestRequest = client.to;
            }
            if(bestCall == -1 && bestRequest == Main.numberOfFloors) return "none -1";
            if(bestCall == -1) return "Request " + bestRequest;
            if(bestRequest == Main.numberOfFloors) return resultCalled;
                else
                    return bestRequest < bestCall ? "Request " + bestRequest : resultCalled;
        } else { // Going Down
            bestRequest = -1;
            for(Client client : requests){
                if(client.to <= currentFloor && client.to >= bestRequest && client.allowed) bestRequest = client.to;
            }

            if(bestCall == -1 && bestRequest == -1) return "none -1";
            if(bestCall == -1) return "Request " + bestRequest;
            if(bestRequest == -1) return resultCalled;
                else
                    return bestRequest > bestCall ? "Request " + bestRequest : resultCalled;
        }
    }

    private void checkNew(){
        int floorToGo = Integer.parseInt(nextFloor.split(" ")[1]);
        if(direction.contains("Going Up"))
        {
            for(Client client : newCalled){
                if(client.from > currentFloor && client.from < floorToGo) {
                    floorToGo = client.from;
                    nextFloor = "Call " + floorToGo;
                    Main.setFloorsToGo(elevatorID, Integer.parseInt(nextFloor.split(" ")[1]));
                }
                called.add(client);
            }

        } else { // Going Down
            for(Client client : newCalled){
                if(client.from < currentFloor && client.from > floorToGo) {
                    floorToGo = client.from;
                    nextFloor = "Call " + floorToGo;
                    Main.setFloorsToGo(elevatorID, Integer.parseInt(nextFloor.split(" ")[1]));
                }
                called.add(client);
            }
        }

        this.newCalled = new ArrayList<>();
    }

    private void takeClients(int from) {
        Iterator<Client> iterator = called.iterator();
        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.from == from) {
                if (client.weight + currentCapacity < maxCapacity - 100) {
                    currentCapacity += client.weight;
                    weightTransported += client.weight;
                    requests.add(client);
                    iterator.remove(); // remove the element using the iterator
                } else {
                    System.out.println("Nu se poate lua clientul, capacitate maxima depasita.");
                    client.allowed = false;
                    justForbiddenClients = true;
                }
            }
        }
    }

    private void dropClients(int to){
        Iterator<Client> iterator = requests.iterator();
        while(iterator.hasNext()) {
            Client client = iterator.next();
            if (client.to == to) {
                currentCapacity -= client.weight;
                iterator.remove();
            }
        }
    }

    private void changeDirection(){
        if(direction.equals("Going Up")) direction = "Going Down";
            else direction = "Going Up";
    }

    private void resetAllowance(int from){
        for (Client client : called){
            if(client.from == from) client.allowed = true;
        }
    }
}

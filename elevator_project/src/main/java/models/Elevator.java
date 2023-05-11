package models;

import org.example.Main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Elevator extends Thread{
    private int elevatorID, maxCapacity, currentCapacity = 0;
    private boolean haveMirror;
    private String backgroundColor;
    private int currentFloor = 0;
    private List<Client> requests;
    private List<Client> called;
    private List<Client> newCalled;
    private String direction = "Going Up";
    private String nextFloor = "none -1";
    private boolean justStarted = false;
    private volatile boolean running = true;


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
                System.out.println("Elevator " + elevatorID + ": Waiting for calls...");
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.called = Main.access("read", elevatorID);
                justStarted = false;
            }


            while((called.size() != 0 || requests.size() != 0) && running){
                System.out.println(String.format("Calls: %d, requests: %d", called.size() + newCalled.size(), requests.size()));
                if(!justStarted){
                    justStarted = true;
                    nextFloor = nextCalled(); // verificam in directia in care a mers ultima data liftul
                    if(nextFloor.equals("none -1")){ // daca nu gasim, sigur avem in cealalta directie
                        changeDirection();
                        nextFloor = nextCalled();
                    }
                }
                if(newCalled.size() != 0) {
                    System.out.println("New called nu este gol!!!");
                    checkNew();
                }
                floorToGo = Integer.parseInt(nextFloor.split(" ")[1]);
                System.out.println("Current floor: " + currentFloor + ", floorToGO: " + floorToGo);
                if(floorToGo == currentFloor) {
                    System.out.println(String.format("Elevator %d: Am ajuns la o destinatie, etaj: %s", elevatorID, nextFloor));
                    takeClients(currentFloor);
                    System.out.println("sunt si aiiiiici 7");
                    dropClients(currentFloor);
                    nextFloor = nextCalled();
                    if(nextFloor.equals("none -1") && (called.size() != 0 || requests.size() != 0)){
                        changeDirection();
                        nextFloor = nextCalled();
                    }

                    try {
                        sleep(5000);
                    } catch (InterruptedException e) { // asteptam clientii sa se urce in lift
                        throw new RuntimeException(e);
                    }

                }
                if(direction.equals("Going Down") && !nextFloor.equals("none -1")){
                    currentFloor -= 1;
                    System.out.println(String.format("Coboram (out) de la etajul %d la %d", currentFloor + 1, currentFloor));
                } else if (direction.equals("Going Up") && !nextFloor.equals("none -1")){
                    currentFloor += 1;
                    System.out.println(String.format("Urcam (out) de la etajul %d la %d", currentFloor - 1, currentFloor));
                } else {
                    System.out.println("Nu mai am cereri, voi sta pe loc. Sunt la etajul " + currentFloor);
                }

                try {
                    sleep(8000); // pentru traversarea unui etaj
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
        System.out.println(1);
        if(called.size() == 0) return nextRequest("none -1");
        System.out.println(2);
        if(direction.contains("Going Up")){
            best = Main.numberOfFloors; // client.from = 1, 1 > 0, 1 < 5, 1 < -1
            for(Client client : called){
                if(client.from > currentFloor && client.from < best){
                    best = client.from;
                }
            }
            System.out.println("Best (1) call is: " + best);
            if(best == Main.numberOfFloors) return nextRequest("none -1"); // nu mai avem calluri in sus
                else return nextRequest("Call " + best);
        } else {
            best = -1;
            for(Client client : called){
                if(client.from < currentFloor && client.from > best){
                   best = client.from;
                }
            }
            System.out.println("Best call (2) is: " + best);
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
                if(client.to > currentFloor && client.to < bestRequest) bestRequest = client.to;
            }
            if(bestCall == -1 && bestRequest == Main.numberOfFloors) return "none -1";
            if(bestCall == -1) return "Request " + bestRequest;
            if(bestRequest == Main.numberOfFloors) return resultCalled;
                else
                    return bestRequest < bestCall ? "Request " + bestRequest : resultCalled;
        } else { // Going Down
            bestRequest = -1;
            for(Client client : requests){
                if(client.to < currentFloor && client.to > bestRequest) bestRequest = client.to;
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
                }
                called.add(client);
            }

        } else { // Going Down
            for(Client client : newCalled){
                if(client.from < currentFloor && client.from > floorToGo) {
                    floorToGo = client.from;
                    nextFloor = "Call " + floorToGo;
                }
                called.add(client);
            }
        }

        System.out.println("nextFloor dupa checkNew(): " + nextFloor);

        this.newCalled = new ArrayList<>();
    }

    private void takeClients(int from) {
        Iterator<Client> iterator = called.iterator();
        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.from == from) {
                if (client.weight + currentCapacity < maxCapacity - 100) {
                    currentCapacity += client.weight;
                    requests.add(client);
                    iterator.remove(); // remove the element using the iterator
                } else {
                    System.out.println("Nu se poate lua clientul, capacitate maxima depasita.");
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
}

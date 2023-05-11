package models;

public class Elevator extends Thread{
    public int elevatorID, maxCapacity, currentCapacity;
    public boolean haveMirror;
    public String backgroundColor;

    public Elevator(int elevatorID, int maxCapacity, boolean haveMirror, String backgroundColor){
        this.elevatorID = elevatorID;
        this.maxCapacity = maxCapacity;
        this.haveMirror = haveMirror;
        this.backgroundColor = backgroundColor;
        this.currentCapacity = 0;
    }

    @Override
    public void run() {

    }
}

package models;

public class Client {
    public int from, to, weight, elevatorId;
    public boolean allowed = true;

    public Client(int from, int to, int weight, int elevatorId) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.elevatorId = elevatorId;
    }

    @Override
    public String toString() {
        return "Client{" +
                "from=" + from +
                ", to=" + to +
                ", weight=" + weight +
                '}';
    }
}

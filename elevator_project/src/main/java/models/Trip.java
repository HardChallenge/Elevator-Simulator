package models;

import java.sql.Timestamp;

public class Trip {
    public int elevatorId, numberOfStops, weightTransported;
    public Timestamp startedAt, stoppedAt;
    public String direction;

    public Trip(int elevatorId, Timestamp startedAt, Timestamp stoppedAt, int numberOfStops, String direction, int weightTransported) {
        this.elevatorId = elevatorId;
        this.numberOfStops = numberOfStops;
        this.weightTransported = weightTransported;
        this.startedAt = startedAt;
        this.stoppedAt = stoppedAt;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "elevatorId=" + elevatorId +
                ", numberOfStops=" + numberOfStops +
                ", weightTransported=" + weightTransported +
                ", startedAt=" + startedAt +
                ", stoppedAt=" + stoppedAt +
                ", direction='" + direction + '\'' +
                '}';
    }
}

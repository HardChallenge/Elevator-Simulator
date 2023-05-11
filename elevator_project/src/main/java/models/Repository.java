package models;

import java.sql.*;

public class Repository {
    public Connection connection;

    public Repository(String url, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
    }

    public void initialize() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("call initialize()");
    }

    public void create(String name, Object... params) throws SQLException {
        String tableName = name.toLowerCase(), DDL;
        PreparedStatement statement;
        if(tableName.equals("elevators")){
            DDL = "INSERT INTO elevators(maxCapacity, haveMirror, backgroundColor) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(DDL);
            statement.setInt(1, (int) params[0]);
            statement.setBoolean(2, (boolean) params[1]);
            statement.setString(3, (String) params[2]);
            statement.executeUpdate();

        } else if(tableName.equals("trips")){
            DDL = "INSERT INTO trips(elevatorID, startedAt, stoppedAt, numberOfStops, direction, weightTransported) VALUES (?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(DDL);
            statement.setInt(1, (int) params[0]);
            statement.setTimestamp(2, (Timestamp) params[1]);
            statement.setTimestamp(3, (Timestamp) params[2]);
            statement.setInt(4, (int) params[3]);
            statement.setString(5, (String) params[4]);
            statement.setInt(6, (int) params[5]);
            statement.executeUpdate();

        }
    }
}

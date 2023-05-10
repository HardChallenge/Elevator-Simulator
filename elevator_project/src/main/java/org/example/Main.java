package org.example;

import models.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Main {
    private static final String url = "jdbc:postgresql://localhost/Elevators";
    private static final String user = "postgres";
    private static final String password = "postgres";

    public static void main(String[] args) {
        // DATABASE
        Repository repository = null;
        try{
            repository = new Repository(url, user, password);
            repository.initialize();
            repository.create("elevators", 100, true, "blue");
            repository.create("trips", 5,new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 10, "up", 600);
        } catch(SQLException e){
            System.out.println("Database error: " + e.getMessage());
            System.exit(2);
        }
        // --------



        // DATABASE
        try{
            repository.connection.close();
        } catch(SQLException e){
            System.out.println("Error at closing the database: " + e.getMessage());
            System.exit(2);
        }
        // ------
    }
}
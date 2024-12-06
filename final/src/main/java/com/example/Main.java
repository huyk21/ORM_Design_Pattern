package com.example;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {

        MySQLConnectionFactory factory1 = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "ORMX", "root", "mysql");

        try (Connection connection = factory1.createConnection()) {
            System.out.println("Connected to MySQL database!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
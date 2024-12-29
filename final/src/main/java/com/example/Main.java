package com.example;

import java.sql.SQLException;
import java.util.List;

import com.example.GenericDao.SelectBuilder;
import com.example.client.Subject;
import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

public class Main {

    public static void main(String[] args) throws SQLException {
        // Initialize session and DAO
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault("localhost", "3306", "damframework", "root", "ducanh123");
        DatabaseSession session = new DatabaseSession(factory);
        GenericDao<User> userDao = new GenericDao<>(session, User.class);

        try {
            SelectBuilder builder = userDao.dynamicJoinBuilder();
String query = builder
        .addColumn("users.id")
        .addColumn("users.username")
        .addColumn("users.email")
        .addColumn("users.full_name")
        .addColumn("s.name AS subject_name")
        .addColumn("s.credit AS subject_credit")
        .addJoin(Subject.class, "s", "s.user_id = users.id") // Define alias for subjects
        .where("s.name = 'Mathematics'")
        .buildSelectQuery();

System.out.println("Generated Query: " + query);

List<Object[]> results = session.executeCustomJoinQuery(query);
for (Object[] row : results) {
    System.out.println("User ID: " + row[0] + ", Username: " + row[1] + ", Subject: " + row[4] + ", Credit: " + row[5]);
}

            
            
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

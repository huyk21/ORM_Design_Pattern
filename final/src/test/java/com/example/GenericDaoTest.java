package com.example;

import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.entity.GenericDaoImpl;

public class GenericDaoTest {
    private static DatabaseSession session;
    private static GenericDaoImpl<User> userDao;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // Initialize the DatabaseSession with the test database
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "damframework", "root", "ducanh123"
        );
        session = new DatabaseSession(factory);

        // Initialize DAO for the User entity
        userDao = new GenericDaoImpl<>(session, User.class);

       
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        // Close the database session
        session.closeConnection();
    }

    @Before
    public void setUp() {
        // Set up before each test if necessary
    }

    @After
    public void tearDown() {
        // Clean up after each test if necessary
    }

    @Test
    public void testCreate() throws SQLException, IllegalAccessException, NoSuchFieldException {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        userDao.create(user);

        assertNotEquals(0, user.getId());
    }

    

    

   
}

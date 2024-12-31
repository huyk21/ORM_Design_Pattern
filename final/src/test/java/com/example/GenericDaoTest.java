package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

public class GenericDaoTest {
    private static DatabaseSession session;
    private static ArrayList<User> createdUser;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // Initialize the DatabaseSession with the test database
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "orm_test", "root", "mysql"
        );
        session = new DatabaseSession(factory);

        createdUser = new ArrayList<>();
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

    public void compareUserTest(User u1, User u2) {
        assertAll(
            () -> assertEquals(u1.getId(), u2.getId()),
            () -> assertEquals(u1.getFullName(), u2.getFullName()),
            () -> assertEquals(u1.getEmail(), u2.getEmail()),
            () -> assertEquals(u1.getPassword(), u2.getPassword()),
            () -> assertEquals(u1.isActive(), u2.isActive()),
            () -> assertEquals(u1.getDateOfBirth(), u2.getDateOfBirth())
        );
    }

    @Test
    public void testCreate1() throws SQLException, IllegalAccessException, NoSuchFieldException {
        User user = new User();
        user.setUsername("test_user1");
        user.setEmail("test_user1@example.com");
        user.setPassword("test_password1");
        user.setActive(false);
        user.setFullName("Test student 1");
        user.setDateOfBirth(new Date(33, 11, 29));

        var dao = new GenericDao<User>(session, User.class);
        
        assertDoesNotThrow(() -> {
            dao.create(user);
            createdUser.add(user);
    
            compareUserTest(user, user);
        });
    }

    @Test
    public void testCreate2() {
        User user = new User();
        user.setUsername("test_user2");
        user.setEmail("test_user2@example.com");
        user.setPassword(null);
        user.setActive(false);
        user.setFullName(null);

        var dao = new GenericDao<User>(session, User.class);

        assertDoesNotThrow(() -> {
            dao.create(user);
            createdUser.add(user);
    
            compareUserTest(user, user);
        });
    }

    @Test
    public void testDynamicJoinBuilder() {
        var dao = new GenericDao<User>(session, User.class);
        var builder = dao.dynamicJoinBuilder();
        
        // change to other
    }

    @Test
    public void testFindById() {
        var dao = new GenericDao<User>(session, User.class);
        User target = new User();

        target.setId(3);
        target.setUsername("student1");
        target.setFullName("Jane Student");
        target.setEmail("student1@example.com");
        target.setPassword("password3");
        target.setActive(true);
        target.setDateOfBirth(new Date(1109782800000l));

        assertDoesNotThrow(() -> {
            var user = dao.findById(target.getId()).get();

            compareUserTest(user, target);
        });
    }

    @Test
    public void testGetLazy() {
        var dao = new GenericDao<User>(session, User.class);
        User target = new User();

        target.setId(3);
        target.setUsername("student1");
        target.setFullName("Jane Student");
        target.setEmail("student1@example.com");
        target.setPassword("password3");
        target.setActive(true);
        target.setDateOfBirth(new Date(1109782800000l));

        var user = dao.getLazy(User.class, target.getId());
        
        compareUserTest(user, target);
    }

    @Test
    public void testRead() {
        var dao = new GenericDao<User>(session, User.class);

        assertDoesNotThrow(() -> {
            User target = dao.findById(3).get();
            var user = dao.read("users.password = \"password3\"");
            compareUserTest(user.get(0), target);
        });
    }

    @Test
    public void testSelect() {

    }

    @Test
    public void testUpdate() {
        
    }

    @Test
    public void testDelete() {
        var dao = new GenericDao<User>(session, User.class);

        assertDoesNotThrow(() -> {
            dao.delete("is_active = 0");
            var users = dao.read("");
            assertEquals(users.size(), 5);
        });
    }   
}

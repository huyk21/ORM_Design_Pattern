package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.connection.PostgreSQLConnectionFactory;
import com.example.connection.SqlServerConnectionFactory;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.annotation.validator.NotNull;
import com.example.cache.CachedDao;
import com.example.cache.FIFOEvictionPolicy;
import com.example.cache.LRUEvictionPolicy;
import com.example.client.Subject;
import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.entity.GenericDaoImpl;
import com.example.iterator.ForwardLazyIterator;
import com.example.iterator.IterableDao;
import com.example.iterator.LazyIterator;
import com.example.validator.NotNullValidator;
import com.example.validator.ValidationProcessor;

public class GenericDaoImplTest {
    private static DatabaseSession session;
    private static ArrayList<User> createdUser;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // Initialize the DatabaseSession with the test database
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "damframework", "root", "ducanh123"
        );
//        PostgreSQLConnectionFactory factory = PostgreSQLConnectionFactory.createDefault(
//                "localhost", "5432", "ORMX", "postgres", "postgres");

//        SqlServerConnectionFactory factory = SqlServerConnectionFactory.createDefault(
//                "localhost", "1433", "ORMX", "sa", "SQLServer123@");

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

        var dao = new GenericDaoImpl<User>(session, User.class);

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

        var dao = new GenericDaoImpl<User>(session, User.class);

        assertDoesNotThrow(() -> {
            dao.create(user);
            createdUser.add(user);

            compareUserTest(user, user);
        });
    }

    @Test
    public void testDynamicJoinBuilder() {
        var dao = new GenericDaoImpl<User>(session, User.class);
        var builder = dao.dynamicJoinBuilder();

        // change to other
    }

    @Test
    public void testFindById() {
        var dao = new GenericDaoImpl<User>(session, User.class);
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
    public void testRead() {
        var dao = new GenericDaoImpl<User>(session, User.class);

        assertDoesNotThrow(() -> {
            User target = dao.findById(3).get();
            var user = dao.read("users.password = \"password3\"");
            compareUserTest(user.get(0), target);
        });
    }

    @Test
    public void testSelect() {
        var dao = new GenericDaoImpl<User>(session, User.class);

        assertDoesNotThrow(() -> {
            var users = dao.read("is_active = 1");
            assertEquals(users.size(), 5);
        });
    }


    @Test
    public void testUpdate() throws SQLException, IllegalAccessException {
        var dao = new GenericDaoImpl<User>(session, User.class);
        User user = new User();

        user.setId(3);
        user.setUsername("student1");
        user.setFullName("Jane Student Updated");
        user.setEmail("student1_updated@example.com");
        user.setPassword("password3_updated");
        user.setActive(true);
        user.setDateOfBirth(new Date(1109782800000L));

        assertDoesNotThrow(() -> {
            dao.update(user, "id = 3");
            var updatedUser = dao.findById(3).get();

            compareUserTest(updatedUser, user);
        });
    }

    @Test
    public void testDelete() {
        var dao = new GenericDaoImpl<User>(session, User.class);

        assertDoesNotThrow(() -> {
            dao.delete("is_active = 0");
            var users = dao.read("");
            assertEquals(users.size(), 5);
        });
    }
    @Test
    public void testCache() throws SQLException, ReflectiveOperationException {
        var dao = new GenericDaoImpl<User>(session, User.class);
    
        // Set up a cache with an FIFO eviction policy (cache size = 1)
        var evictionPolicy = new FIFOEvictionPolicy<Integer, User>(2);
        var cachedDao = new CachedDao<>(dao, evictionPolicy);
    
        assertDoesNotThrow(() -> {
            // Fetch user with ID 1 (cache miss, fetch from DB)
            var user1 = cachedDao.findById(1).get();
            System.out.println("Fetched user1: " + user1);
    
            // Fetch user with ID 1 again (cache hit)
            var cachedUser1 = cachedDao.findById(1).get();
            System.out.println("Cached user1: " + cachedUser1);
            assertEquals(user1, cachedUser1); // Cache hit
    
            // Fetch user with ID 2 (cache miss, fetch from DB)
            var user2 = cachedDao.findById(2).get();
            System.out.println("Fetched user2: " + user2);
    
            // Add a third user to exceed cache size
            var user3 = cachedDao.findById(3).get();
            System.out.println("Fetched user3: " + user3);
    
            // Verify eviction of user1
            System.out.println("Expecting user1 to be evicted...");
            var evictedUser1 = cachedDao.findById(1).get(); // Cache miss, re-fetch from DB
            System.out.println("Evicted user1: " + evictedUser1);
            assertNotEquals(user1, evictedUser1); // Ensure a fresh instance is fetched
        });
    }
    

    
    
    
    @Test
    public void testIterator() throws SQLException, ReflectiveOperationException {
        var dao = new IterableDao<Subject>(session, Subject.class);
    
        assertDoesNotThrow(() -> {
            LazyIterator<Subject> iterator = dao.iterate(null, (resultSet, mapper) -> new ForwardLazyIterator<>(resultSet, mapper));
    
            int count = 0;
            while (iterator.hasNext()) {
                Subject subject = iterator.next();
                System.out.println("Subject: " + subject.getName() + ", Credit: " + subject.getCredit());
                count++;
            }
            iterator.close(); // Ensure ResultSet is closed
    
            assertEquals(4, count); // Adjust to the actual number of rows in the subjects table
        });
    }
    
    
    
    @Test
    public void testNotNullValidation() throws SQLException, IllegalAccessException {
        var dao = new GenericDaoImpl<User>(session, User.class);
        var validator = new ValidationProcessor<User>();
    
        // Register the NotNull validator
        validator.registerValidator(NotNull.class, new NotNullValidator<>());
    
        // Valid user (should pass validation)
        User validUser = new User();
        validUser.setUsername("valid_student");
        validUser.setEmail("valid_student@example.com");
        validUser.setPassword("valid_password");
    
        // Invalid user (should fail validation)
        User invalidUser = new User();
        invalidUser.setUsername(null); // Should fail due to @NotNull
        invalidUser.setEmail("invalid_student@example.com");
    
        // Test validation directly
        assertDoesNotThrow(() -> {
            assertTrue(validator.validate(validUser));  // Should pass
            assertFalse(validator.validate(invalidUser)); // Should fail
        });
    
        
    
       
    }
    

}


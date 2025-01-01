package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private static User targetUser;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        // Initialize the DatabaseSession with the test database
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "orm_test", "root", "mysql");
        session = new DatabaseSession(factory);

        createdUser = new ArrayList<>();

        targetUser = new User();
        targetUser.setId(3);
        targetUser.setUsername("student1");
        targetUser.setFullName("Jane Student");
        targetUser.setEmail("student1@example.com");
        targetUser.setPassword("password3");
        targetUser.setActive(true);
        targetUser.setDateOfBirth(new Date(1109782800000l));
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
                () -> assertEquals(u1.getDateOfBirth(), u2.getDateOfBirth()));
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

        var dao = new GenericDaoImpl<>(session, User.class);

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

        var dao = new GenericDaoImpl<>(session, User.class);

        assertDoesNotThrow(() -> {
            dao.create(user);
            createdUser.add(user);

            compareUserTest(user, user);
        });
    }

    @Test
    public void testFindById() {
        var dao = new GenericDaoImpl<>(session, User.class);
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
        var dao = new GenericDaoImpl<>(session, User.class);
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
        var dao = new GenericDaoImpl<>(session, User.class);

        assertDoesNotThrow(() -> {
            User target = dao.findById(3).get();
            var user = dao.read("users.password = \"password3\"");
            compareUserTest(user.get(0), target);
        });
    }

    @Test
    public void testJoinedSelect() {
        // Step 1: Build the query using SelectBuilder with JOIN and GROUP BY
        var userDao = new GenericDaoImpl<>(session, User.class);

        SelectBuilder<User> builder = new SelectBuilder<>(User.class)
                .addScalar("MAX", "users.id", "max_user_id")
                .addScalar("MIN", "users.id", "min_user_id")
                .addColumn("users.username")
                .addJoin("JOIN", "classes", "c", "users.class_id = c.id")
                .groupBy("users.username");

        // Step 2: Generate the SQL query string
        String query = builder.buildSelectQuery();
        System.out.println("Generated Query: " + query);

        assertDoesNotThrow(() -> {
            // Step 3: Execute the query using the DAO's select method
            List<Object[]> results = userDao.select(builder);

            List<Object[]> expected = new ArrayList<>();
            expected.add(new Object[] {"teacher1", 1, 1});
            expected.add(new Object[] {"teacher2", 2, 2});
            expected.add(new Object[] {"student2", 4, 4});
            expected.add(new Object[] {"student3", 5, 5});

            // Step 4: Process and print the results
            int i = 0;
            for (Object[] row : results) {
                final int index = i;

                assertAll(
                    () -> assertEquals(expected.get(index)[0], row[2]),
                    () -> assertEquals(expected.get(index)[1], row[0]),
                    () -> assertEquals(expected.get(index)[2], row[1])
                );
                ;

                i++;
            }
        });
    }

    @Test
    public void testSimpleSelect() {
        // Step 1: Build the query using SelectBuilder with JOIN and GROUP BY
        var userDao = new GenericDaoImpl<>(session, User.class);

        SelectBuilder<User> builder = new SelectBuilder<>(User.class);

        // Step 2: Generate the SQL query string
        String query = builder.buildSelectQuery();
        System.out.println("Generated Query: " + query);

        assertDoesNotThrow(() -> {
            // Step 3: Execute the query using the DAO's select method
            List<Object[]> results = userDao.select(builder);

            // Step 4: Process and print the results
            int i = 1;
            for (Object[] row : results) {
                assertEquals(i, (int) row[0]);
                i++;
            }
        });
    }

    @Test
    public void testUpdate() {
        var dao = new GenericDaoImpl<>(session, User.class);
        User target = new User();

        target.setId(3);
        target.setUsername("updated");
        target.setFullName("Updated Student");
        target.setEmail("student1@example.com");
        target.setPassword("no_password");
        target.setActive(false);
        target.setDateOfBirth(new Date(1109782800000l));

        assertDoesNotThrow(() -> {
            dao.update(target, "id = " + target.getId());
            var user = dao.findById(target.getId()).get();

            compareUserTest(user, target);
        });

        target.setId(3);
        target.setUsername("student1");
        target.setFullName("Jane Student");
        target.setEmail("student1@example.com");
        target.setPassword("password3");
        target.setActive(true);
        target.setDateOfBirth(new Date(1109782800000l));

        assertDoesNotThrow(() -> {
            dao.update(target, "id = " + target.getId());
            var user = dao.findById(target.getId()).get();

            compareUserTest(user, target);
        });
    }

    @Test
    public void testDelete() {
        var dao = new GenericDaoImpl<>(session, User.class);

        User target = new User();

        target.setUsername("deleted");
        target.setFullName("Deleted Student");
        target.setEmail("student1221@example.com");
        target.setPassword("no_password");
        target.setActive(false);

        assertDoesNotThrow(() -> {
            var users = dao.read(null);
            int size = users.size();

            dao.create(target);
            
            users = dao.read("");
            assertEquals(users.size(), size + 1);

            dao.delete("id = " + target.getId());
            users = dao.read("");
            assertEquals(users.size(), size);

            dao.delete("is_active = 0");
            
            users = dao.read("");
            assertEquals(users.size(), 5);
        });
    }
}

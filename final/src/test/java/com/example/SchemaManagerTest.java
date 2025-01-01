package com.example;

import com.example.client.Class;
import com.example.client.Subject;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;
import com.example.connection.PostgreSQLConnectionFactory;
import com.example.connection.SqlServerConnectionFactory;
import com.example.schema.factory.MySQLStrategyFactory;
import com.example.schema.factory.PostgreStrategyFactory;
import com.example.schema.factory.SqlServerStrategyFactory;
import com.example.client.User;
import com.example.schema.SchemaManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaManagerTest {
    private static DatabaseSession mysqlSession;
    private static DatabaseSession postgresSession;
    private static DatabaseSession sqlServerSession;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        mysqlSession = new DatabaseSession(MySQLConnectionFactory.createDefault(
                "localhost", "3306", "ORMX", "root", "mysql"));
        postgresSession = new DatabaseSession(PostgreSQLConnectionFactory.createDefault(
                "localhost", "5432", "ORMX", "postgres", "postgres"));
        sqlServerSession = new DatabaseSession(SqlServerConnectionFactory.createDefault(
                "localhost", "1433", "ORMX", "sa", "SQLServer123@"));
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        mysqlSession.closeConnection();
        postgresSession.closeConnection();
        sqlServerSession.closeConnection();
    }

    @Test
    public void testMySQLDDLGeneration() throws SQLException {
        SchemaManager schemaManager = new SchemaManager(mysqlSession, new MySQLStrategyFactory());

        schemaManager.dropTable(Subject.class);
        assertFalse(tableExists(mysqlSession, "subjects"));

        schemaManager.dropTable(User.class);
        assertFalse(tableExists(mysqlSession, "users"));

        schemaManager.dropTable(Class.class);
        assertFalse(tableExists(mysqlSession, "classes"));

        schemaManager.createTable(Class.class);
        assertTrue(tableExists(mysqlSession, "classes"));

        schemaManager.createTable(User.class);
        assertTrue(tableExists(mysqlSession, "users"));

        schemaManager.createTable(Subject.class);
        assertTrue(tableExists(mysqlSession, "subjects"));
    }

    @Test
    public void testPostgresDDLGeneration() throws SQLException {
        SchemaManager schemaManager = new SchemaManager(postgresSession, new PostgreStrategyFactory());

        schemaManager.dropTable(Subject.class);
        assertFalse(tableExists(postgresSession, "subjects"));

        schemaManager.dropTable(User.class);
        assertFalse(tableExists(postgresSession, "users"));

        schemaManager.dropTable(Class.class);
        assertFalse(tableExists(postgresSession, "classes"));

        schemaManager.createTable(Class.class);
        assertTrue(tableExists(postgresSession, "classes"));

        schemaManager.createTable(User.class);
        assertTrue(tableExists(postgresSession, "users"));

        schemaManager.createTable(Subject.class);
        assertTrue(tableExists(postgresSession, "subjects"));
    }

    @Test
    public void testSqlServerDDLGeneration() throws SQLException {
        SchemaManager schemaManager = new SchemaManager(sqlServerSession, new SqlServerStrategyFactory());

        schemaManager.dropTable(Subject.class);
        assertFalse(tableExists(sqlServerSession, "subjects"));

        schemaManager.dropTable(User.class);
        assertFalse(tableExists(sqlServerSession, "users"));

        schemaManager.dropTable(Class.class);
        assertFalse(tableExists(sqlServerSession, "classes"));

        schemaManager.createTable(Class.class);
        assertTrue(tableExists(sqlServerSession, "classes"));

        schemaManager.createTable(User.class);
        assertTrue(tableExists(sqlServerSession, "users"));

        schemaManager.createTable(Subject.class);
        assertTrue(tableExists(sqlServerSession, "subjects"));
    }

    private boolean tableExists(DatabaseSession session, String tableName) throws SQLException {
        String query;
        if (session.getConnection().getMetaData().getDatabaseProductName().equalsIgnoreCase("MySQL")) {
            query = "SHOW TABLES LIKE '" + tableName + "'";
        } else if (session.getConnection().getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL")) {
            query = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = '"
                    + tableName.toLowerCase() + "')";
        } else if (session.getConnection().getMetaData().getDatabaseProductName()
                .equalsIgnoreCase("Microsoft SQL Server")) {
            query = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'";
        } else {
            throw new UnsupportedOperationException("Unsupported DBMS");
        }

        var resultSet = session.executeQuery(query);
        return resultSet.next()
                && (session.getConnection().getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL")
                        ? resultSet.getBoolean(1)
                        : true);
    }
}
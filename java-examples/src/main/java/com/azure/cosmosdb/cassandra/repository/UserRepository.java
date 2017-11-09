package com.azure.cosmosdb.cassandra.repository;

import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gives examples of create, delete table on Cassandra database
 * Insert & select data from the table
 */
public class UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);
    private Session session;

    public UserRepository(Session session) {
        this.session = session;
    }

    /**
     * Create keyspace uprofile in cassandra DB
     */
    public void createKeyspace() {
        final String query = "CREATE KEYSPACE IF NOT EXISTS uprofile WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3' } ";
        session.execute(query);
		LOGGER.info("Created keyspace 'uprofile'");
    }

    /**
     * Create user table in cassandra DB
     */
    public void createTable() {
        final String query = "CREATE TABLE IF NOT EXISTS uprofile.user (user_id int PRIMARY KEY, user_name text, user_bcity text)";
        session.execute(query);
		LOGGER.info("Created table 'user'");
    }

    /**
     * Select all rows from user table
     */
    public void selectAllUsers() {

        final String query = "SELECT * FROM uprofile.user";
        List<Row> rows = session.execute(query).all();

        for (Row row : rows) {
            LOGGER.info("Obtained row: {} | {} | {} ", row.getInt("user_id"), row.getString("user_name"), row.getString("user_bcity"));
        }
    }

    /**
     * Select a row from user table
     *
     * @param id user_id
     */
    public void selectUser(int id) {
        final String query = "SELECT * FROM uprofile.user where user_id = 3";
        Row row = session.execute(query).one();

        LOGGER.info("Obtained row: {} | {} | {} ", row.getInt("user_id"), row.getString("user_name"), row.getString("user_bcity"));
    }

    /**
     * Delete user table.
     */
    public void deleteTable() {
        final String query = "DROP TABLE IF EXISTS uprofile.user";
        session.execute(query);
    }

    /**
     * Insert a row into user table
     *
     * @param id user_id
     * @param name user_name
     * @param city user_bcity
     */
    public void insertUser(PreparedStatement statement, int id, String name, String city) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(id, name, city));
    }

    /**
     * Create a PrepareStatement to insert a row to user table
     * @return PreparedStatement
     */
    public PreparedStatement prepareInsertStatement() {
        final String insertStatement = "INSERT INTO  uprofile.user (user_id, user_name , user_bcity) VALUES (?,?,?)";
        return session.prepare(insertStatement);
    }
}
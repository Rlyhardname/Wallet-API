package com.dimitrovsolutions.dao;

import com.dimitrovsolutions.model.component.SupportedCurrencies;
import com.dimitrovsolutions.dao.exceptions.InsertionSqlException;
import com.dimitrovsolutions.model.component.ExternalTransactionsUtil;
import com.dimitrovsolutions.model.entity.User;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dimitrovsolutions.dao.utils.JdbcUtils.*;

/**
 * Responsible for user operations
 */
@Repository
public class UserDaoImpl {

    public static final Logger logger = Logger.getLogger(UserDaoImpl.class.getName());
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    private final SupportedCurrencies supportedCurrencies;

    private final ExternalTransactionsUtil externalTransactionsUtil;

    public UserDaoImpl(SupportedCurrencies supportedCurrencies, ExternalTransactionsUtil externalTransactionsUtil) {
        this.supportedCurrencies = supportedCurrencies;
        this.externalTransactionsUtil = externalTransactionsUtil;
    }

    public Optional<User> fetchUserByEmail(String email) {
        logger.log(Level.INFO, "Starting fetch user by email: " + email);
        String sql = "select * from users where email = ?";
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = connect();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("egn"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            UUID.fromString(rs.getString("account_id")));
                    logger.log(Level.INFO, String.format("Successful fetch user with email %s", email));
                    return Optional.of(user);
                }

            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Optional.empty();
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }

        logger.log(Level.INFO, String.format("User with email %s not found", email));
        return Optional.empty();
    }

    /**
     * Attempts to register user, unless email is already in use.
     */
    public void addUser(String email, String password, String egn, String firstName, String lastName) throws SQLException {
        logger.log(Level.INFO, "Starting add user by email: " + email);
        String registerSQL = """
                INSERT INTO users (email, password, egn, first_name, last_name) VALUES (?, ?, ?, ?, ?);
                """;
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            try (PreparedStatement registerStatement = connection.prepareStatement(registerSQL)) {
                registerStatement.setString(1, email);
                registerStatement.setString(2, password);
                registerStatement.setString(3, egn);
                registerStatement.setString(4, firstName);
                registerStatement.setString(5, lastName);
                int userRows = registerStatement.executeUpdate();
                if (userRows < 1) {
                    throw new InsertionSqlException();
                }

                logger.log(Level.INFO, "Successfully add user with email: " + email);
                connection.commit();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            rollback(connection);
            throw new RuntimeException(e);
        } catch (InsertionSqlException e) {
            logger.log(Level.INFO, "Failed to add user with email: " + email);
            rollback(connection);
            throw new InsertionSqlException();
        } finally {
            closeConnection(connection);
        }
    }
}
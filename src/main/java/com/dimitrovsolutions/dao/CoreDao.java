package com.dimitrovsolutions.dao;

import com.dimitrovsolutions.dao.exceptions.InsertionSqlException;
import com.dimitrovsolutions.dao.exceptions.InvalidDepositRequest;
import com.dimitrovsolutions.dao.exceptions.WalletWithCurrencyAlreadyExistsException;
import com.dimitrovsolutions.model.component.SupportedCurrencies;
import com.dimitrovsolutions.model.entity.StatementEntry;
import com.dimitrovsolutions.model.entity.User;
import com.dimitrovsolutions.model.entity.Wallet;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dimitrovsolutions.dao.utils.JdbcUtils.*;

/**
 * Responsible for deposits, withdraws and statements
 */
@Repository
public class CoreDao {

    public static final Logger logger = Logger.getLogger(CoreDao.class.getName());

    private final SupportedCurrencies supportedCurrencies;

    private final String OPERATION_REQUEST_DEPOSIT = "deposit";

    private final String OPERATION_REQUEST_WITHDRAW = "withdraw";

    private final String OPERATION_STATUS_ACCEPTED = "accepted";

    private final String OPERATION_STATUS_DECLINED = "declined";

    public CoreDao(SupportedCurrencies supportedCurrencies) {
        this.supportedCurrencies = supportedCurrencies;
    }

    public void addWallet(User user, String currency) throws SQLException {
        logger.log(Level.INFO, String.format("Starting add wallet for user %s with currency %s", user, currency));
        if (fetchWalletByAccountIdAndCurrency(user.getAccountId(), currency).isPresent()) {
            logger.log(Level.INFO, String.format("Wallet with currency %s already for user %s exists", currency, user));
            throw new WalletWithCurrencyAlreadyExistsException(currency);
        }

        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);

            String chosenCurrency = supportedCurrencies.containsCurrency(currency) ? currency.toUpperCase() : SupportedCurrencies.DEFAULT_CURRENCY;
            String createWalletSQL = """
                    INSERT INTO wallet(account_id, currency) VALUES (?, ?);
                    """;
            try (PreparedStatement addWalletStatement = connection.prepareStatement(createWalletSQL)) {
                addWalletStatement.setObject(1, user.getAccountId());
                addWalletStatement.setString(2, chosenCurrency);
                int walletRows = addWalletStatement.executeUpdate();
                if (walletRows < 1) {
                    logger.log(Level.INFO, String.format("Error inserting wallet for user %s with currency %s", user, currency));
                    throw new InsertionSqlException(String.format("Error inserting wallet for user %s with currency %s", user, currency));
                }

                logger.log(Level.INFO, String.format("Successfully add wallet for user %s with currency %s ", user, currency));
                connection.commit();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            connection.rollback();
        } finally {
            closeConnection(connection);
        }
    }

    public List<Wallet> fetchAllWallets(UUID accountId) {
        logger.log(Level.INFO, String.format("Start Fetch all wallets for account %s", accountId));
        String fetchAllWalletsSql = """
                SELECT * FROM wallet
                WHERE account_id = ?;
                """;
        Connection connection = null;
        ResultSet rs = null;
        List<Wallet> wallets = new ArrayList<>();
        try {
            connection = connect();
            try (PreparedStatement stmt = connection.prepareStatement(fetchAllWalletsSql)) {
                stmt.setObject(1, accountId);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    UUID account_id = (UUID) rs.getObject("account_id");
                    String amount = rs.getString("amount");
                    String currency = rs.getString("currency");
                    wallets.add(new Wallet(id, account_id, amount, currency));
                }

                logger.log(Level.INFO, String.format("Successfully fetch all wallets for account %s ", accountId));
                return wallets;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return List.of();
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }
    }


    public Optional<Wallet> fetchWalletByAccountIdAndCurrency(UUID accountId, String chosenCurrency) {
        logger.log(Level.INFO, String.format("Start Fetch wallet for account %s, with currency %s", accountId, chosenCurrency));
        Connection connection = null;
        ResultSet rs = null;
        String fetchWalletByAccountIdAndCurrency = """
                SELECT * FROM wallet
                WHERE account_id = ? AND currency = ?;
                """;
        try {
            connection = connect();
            try (PreparedStatement stmt = connection.prepareStatement(fetchWalletByAccountIdAndCurrency)) {
                stmt.setObject(1, accountId);
                stmt.setString(2, chosenCurrency.toUpperCase());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    UUID account_id = (UUID) rs.getObject("account_id");
                    String amount = rs.getString("amount");
                    String currency = rs.getString("currency");
                    logger.log(Level.INFO, String.format("Successfully fetch wallet for account %s with currency %s ", accountId, currency));
                    return Optional.of(new Wallet(id, account_id, amount, currency));
                }

                logger.log(Level.INFO, String.format("Wallet with accound id %s and currency %s doesn't exist", accountId, chosenCurrency));
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return Optional.empty();
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }
    }

    public void externalWithdrawDeclined(User user, UUID walletId, String statementId,
                                         String cardNumber, String currency, double amount) {
        String withdrawDeclined = """
                INSERT INTO statement (id, wallet_id, email, card_number, amount,
                currency, operation, status)
                VALUES (?,?,?,?,?,?,?,?);
                RETURNING *;
                """;
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(withdrawDeclined)) {
                stmt.setString(1, statementId);
                stmt.setObject(2, walletId);
                stmt.setString(3, user.getEmail());
                stmt.setString(4, cardNumber);
                stmt.setBigDecimal(5, BigDecimal.valueOf(amount));
                stmt.setString(6, currency.toUpperCase());
                stmt.setString(7, OPERATION_REQUEST_DEPOSIT);
                stmt.setString(8, OPERATION_STATUS_DECLINED);
                int rows = stmt.executeUpdate();

                if (rows != 1) {
                    logger.log(Level.INFO, "Failed to insert statement for user" + user + " " + cardNumber + " " + currency + " " + amount + " failed");
                    connection.rollback();
                }

                logger.log(Level.INFO, String.format("Successfully inserted withdraw decline statement for user %s", user));
                connection.commit();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    public boolean deposit(User user, Wallet wallet, String statementId,
                           String cardNumber, String currency, double chosenAmount) {
        logger.log(Level.INFO, String.format("Start deposit in wallet %s for user %s with currency %s", wallet, user, currency));
        String updateWalletSql = """
                UPDATE wallet
                SET amount = amount + ?
                WHERE id = ?
                RETURNING id,amount;
                """;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = connect();
            try (PreparedStatement stmt = connection.prepareStatement(updateWalletSql)) {
                connection.setAutoCommit(false);
                stmt.setBigDecimal(1, BigDecimal.valueOf(chosenAmount));
                stmt.setObject(2, wallet.getId());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    BigDecimal bigDecimal = rs.getBigDecimal("amount");

                    BigDecimal expected = BigDecimal.valueOf(Double.parseDouble(wallet.getValue()))
                            .add(BigDecimal.valueOf(chosenAmount));

                    if (expected.compareTo(bigDecimal) != 0) {
                        logger.log(Level.SEVERE, String.format(
                                "Deposit amount of %f.2 did not insert correctly for wallet %s ", chosenAmount, wallet));
                        connection.rollback();
                        return false;
                    }
                } else {
                    logger.log(Level.INFO, String.format("Wallet id %s not found for user %s",
                            wallet.getId().toString(),
                            user));
                    connection.rollback();
                    return false;
                }
            }

            Optional<StatementEntry> opt = insertIntoStatement(user, wallet.getId(), statementId, cardNumber,
                    currency, chosenAmount, OPERATION_REQUEST_DEPOSIT, OPERATION_STATUS_ACCEPTED);

            if (opt.isEmpty()) {
                logger.log(Level.INFO, String.format("Deposit failed for wallet %s since no statement was returned for ", wallet));
                rollback(connection);
                return false;
            }

            logger.log(Level.INFO, String.format("Successful deposit in wallet %s for user %s with currency %s", wallet, user, currency));
            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            rollback(connection);
            throw new InvalidDepositRequest();
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }
    }

    public Object[] withdraw(User user, Wallet chosenWallet, String statementId,
                             String cardNumber, String chosenCurrency, double chosenAmount) {
        logger.log(Level.INFO, String.format("Start withdraw for wallet %s with user %s and currency %s",
                chosenWallet, user, chosenCurrency));
        String withdrawFromWalletReturnUpdatedWalletEntry = """
                UPDATE wallet
                SET amount = amount - ?
                WHERE id = ?
                RETURNING *;
                """;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            Wallet updatedWallet = null;
            try (PreparedStatement stmt = connection.prepareStatement(withdrawFromWalletReturnUpdatedWalletEntry)) {
                stmt.setBigDecimal(1, BigDecimal.valueOf(chosenAmount));
                stmt.setObject(2, chosenWallet.getId());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    UUID accountId = (UUID) rs.getObject("account_id");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String currency = rs.getString("currency");

                    updatedWallet = new Wallet(id, accountId, amount.toString(), currency);
                } else {
                    logger.log(Level.INFO, String.format("Wallet id %s not found for user %s",
                            chosenWallet.getId().toString(),
                            user));
                    rollback(connection);
                    throw new IllegalArgumentException("wallet does not exist");
                }

                if (!BigDecimal.valueOf(Double.parseDouble(updatedWallet.getValue())).add(
                        BigDecimal.valueOf((chosenAmount))).equals(
                        BigDecimal.valueOf(Double.parseDouble(chosenWallet.getValue())))) {
                    logger.log(Level.SEVERE, String.format(
                            "Withdraw amount of %f.2 did not update correctly for wallet %s ",
                            chosenAmount, chosenWallet));
                    rollback(connection);
                    return new Object[0];
                }
            }

            Optional<StatementEntry> opt = insertIntoStatement(user, chosenWallet.getId(), statementId,
                    cardNumber, chosenCurrency, chosenAmount, OPERATION_REQUEST_WITHDRAW, OPERATION_STATUS_ACCEPTED);

            if (opt.isEmpty()) {
                logger.log(Level.INFO, String.format("Failed withdraw, no statement with id %s returned for" +
                                " wallet %s with user %s and currency %s",
                        statementId, chosenAmount, user, chosenCurrency));
                rollback(connection);
                return new Object[0];
            }

            logger.log(Level.INFO, String.format("Successful withdraw for wallet %s with user %s and currency %s",
                    chosenAmount, user, chosenCurrency));
            connection.commit();
            StatementEntry statementEntry = opt.get();
            return new Object[]{updatedWallet, statementEntry};
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            rollback(connection);
            return new Object[0];
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }
    }

    public Optional<StatementEntry> insertIntoStatement(User user, UUID chosenWalletId, String statementId,
                                                        String chosenCardNumber,
                                                        String chosenCurrency, double chosenAmount,
                                                        String chosenOperation, String chosenStatus) {
        logger.log(Level.INFO, String.format("Start add new statement for user %s, with wallet %s, and statementId %s",
                user, chosenWalletId.toString(), statementId));
        String insertIntoStatement = """
                INSERT INTO statement (id, email, wallet_id, card_number, amount,
                currency, operation, status)
                VALUES (?,?,?,?,?,?,?,?)
                RETURNING *;
                """;

        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            StatementEntry statement = null;
            try (PreparedStatement stmt = connection.prepareStatement(insertIntoStatement)) {
                stmt.setString(1, statementId);
                stmt.setString(2, user.getEmail());
                stmt.setObject(3, chosenWalletId);
                stmt.setString(4, chosenCardNumber);
                stmt.setBigDecimal(5, BigDecimal.valueOf(chosenAmount));
                stmt.setString(6, chosenCurrency.toUpperCase());
                stmt.setString(7, chosenOperation);
                stmt.setString(8, chosenStatus);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String id = rs.getString("id");
                    String email = rs.getString("email");
                    UUID walletId = (UUID) rs.getObject("wallet_id");
                    String cardNumber = rs.getString("card_number");
                    String amount = rs.getObject("amount").toString();
                    String currency = rs.getString("currency");
                    String operation = rs.getString("operation");
                    String status = rs.getString("status");
                    LocalDateTime ldt = rs.getTimestamp("operation_time").toLocalDateTime();

                    statement = new StatementEntry(id, email, walletId, cardNumber,
                            amount, currency, operation, status, ldt);

                    logger.log(Level.INFO, String.format("Successfully add new statement for user %s, with wallet %s, and statementId %s",
                            user, chosenWalletId.toString(), statementId));
                    connection.commit();
                    return Optional.of(statement);
                } else {
                    logger.log(Level.INFO, String.format("Failed to add add new statement for user %s, with wallet %s, and statementId %s",
                            user, chosenWalletId.toString(), statementId));
                    rollback(connection);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            rollback(connection);
            return Optional.empty();
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }
    }
}
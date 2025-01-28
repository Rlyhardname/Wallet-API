package com.dimitrovsolutions.service;

import com.dimitrovsolutions.dao.CoreDao;
import com.dimitrovsolutions.dao.UserDaoImpl;
import com.dimitrovsolutions.dao.exceptions.BadCredentialsException;
import com.dimitrovsolutions.dao.exceptions.InsertionSqlException;
import com.dimitrovsolutions.dao.exceptions.InvalidDepositRequest;
import com.dimitrovsolutions.dao.exceptions.WalletWithCurrencyAlreadyExistsException;
import com.dimitrovsolutions.model.component.ExternalTransactionsUtil;
import com.dimitrovsolutions.model.component.StatementId;
import com.dimitrovsolutions.model.dto.WalletDto;
import com.dimitrovsolutions.model.entity.User;
import com.dimitrovsolutions.model.entity.Wallet;
import com.dimitrovsolutions.model.utils.MissingWalletException;
import com.dimitrovsolutions.model.utils.PasswordEncoder;
import com.dimitrovsolutions.model.utils.WalletMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class GlobalService {

    private final UserDaoImpl userDao;

    private final CoreDao coreDao;

    private final ExternalTransactionsUtil externalTransactionsUtil;

    private final StatementId id;

    public GlobalService(UserDaoImpl userDao, CoreDao coreDao, ExternalTransactionsUtil externalTransactionsUtil, StatementId id) {
        this.userDao = userDao;
        this.coreDao = coreDao;
        this.externalTransactionsUtil = externalTransactionsUtil;
        this.id = id;
    }

    public ResponseEntity<String> registerUser(String email, String password, String egn, String firstName, String lastName) {
        Optional<User> userOpt = userDao.fetchUserByEmail(email);
        if (userOpt.isPresent()) {
            return new ResponseEntity<>("Email already registered in system.", HttpStatus.BAD_GATEWAY);
        }

        String hashedPassword = PasswordEncoder.encode(password);
        try {
            userDao.addUser(email, hashedPassword, egn, firstName, lastName);
        } catch (SQLException e) {
            return new ResponseEntity<>("Internal server error, please registration process again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Successfully registered user.", HttpStatus.CREATED);
    }

    public ResponseEntity<String> addWallet(String email, String password, String currency) {
        User user;
        try {
            user = fetchUserByEmail(email);
            validatePassword(user.getPassword(), password);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        try {
            coreDao.addWallet(user, currency);
        } catch (WalletWithCurrencyAlreadyExistsException | InsertionSqlException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Successfully added wallet", HttpStatus.CREATED);
    }

    public List<WalletDto> getWallets(String email, String password) {
        User user;
        try {
            user = fetchUserByEmail(email);
            validatePassword(user.getPassword(), password);
        } catch (BadCredentialsException e) {
            return List.of();
        }

        return WalletMapper.mapTo(coreDao.fetchAllWallets(user.getAccountId()));
    }

    public ResponseEntity<?> getWallet(String email, String password, String currency) {
        User user;
        try {
            user = fetchUserByEmail(email);
            validatePassword(user.getPassword(), password);
            return new ResponseEntity<>(WalletMapper.mapTo(coreDao.fetchWalletByAccountIdAndCurrency(user.getAccountId(), currency).orElseThrow(
                    () -> new WalletNonExistingException(user.getAccountId().toString(), currency)
            )), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (WalletNonExistingException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Request money wire from card issuer/financial institution, if successful commence deposit and return a response
     * to issuer/financial institution about the status of the transaction as well as to client based on if the transaction
     * failed on the issuer/financial institution side, our side, or the deposit was successful.
     */
    public ResponseEntity<String> deposit(String email, String password, String cardNumber,
                                          String CSC, String currency, double amount) {
        User user;
        Wallet wallet;
        try {
            user = fetchUserByEmail(email);
            validatePassword(user.getPassword(), password);
            wallet = WalletMapper.mapTo(coreDao.fetchWalletByAccountIdAndCurrency(
                    user.getAccountId(), currency), currency);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (MissingWalletException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        String statementIdStub = id.increment();
        boolean externalWithdrawRequest = externalTransactionsUtil.requestMoneyFromCardIssuer(user.getFirstName(),
                user.getLastName(),
                user.getEgn(),
                statementIdStub,
                cardNumber,
                CSC,
                currency,
                amount);


        if (!externalWithdrawRequest) {
            coreDao.externalWithdrawDeclined(user, wallet.getId(), statementIdStub, cardNumber, currency, amount);
            return new ResponseEntity<>("Card issuer declined deposit operation", HttpStatus.EXPECTATION_FAILED);
        }

        try {
            boolean depositStatus = coreDao.deposit(user,
                    wallet,
                    statementIdStub,
                    cardNumber,
                    currency,
                    amount);

            if (depositStatus) {
                externalTransactionsUtil.confirmSuccessfulDepositFromCardIssuer(user.getFirstName(),
                        user.getLastName(),
                        user.getEgn(),
                        cardNumber,
                        statementIdStub,
                        CSC,
                        currency,
                        amount
                );

                return new ResponseEntity<>("Successfully deposited to wallet", HttpStatus.CREATED);
            }

            externalTransactionsUtil.reverseMoneyRequestFromCardIssuer(user.getFirstName(),
                    user.getLastName(),
                    user.getEgn(),
                    cardNumber,
                    statementIdStub,
                    CSC,
                    currency,
                    amount);

            return new ResponseEntity<>("Server error, please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidDepositRequest e) {
            externalTransactionsUtil.reverseMoneyRequestFromCardIssuer(user.getFirstName(),
                    user.getLastName(),
                    user.getEgn(),
                    cardNumber,
                    statementIdStub,
                    CSC,
                    currency,
                    amount);

            return new ResponseEntity<>("Internal server/account error," +
                    " please contact support for assistance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> withdraw(String email, String password, String cardNumber,
                                      String currency, double amount) {
        User user;
        Wallet wallet;
        try {
            user = fetchUserByEmail(email);
            validatePassword(user.getPassword(), password);
            wallet = WalletMapper.mapTo(coreDao.fetchWalletByAccountIdAndCurrency(user.getAccountId(), currency), currency);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (MissingWalletException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        String statementIdStub = id.increment();
        Object[] walletDtoStatementPair = coreDao.withdraw(user, wallet, statementIdStub, cardNumber, currency, amount);

        externalTransactionsUtil.sendMoneyToCardIssuer(user.getFirstName(), user.getLastName(), user.getEgn(), "stmt"
                , cardNumber, currency, amount);

        try {
            if (walletDtoStatementPair.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error, try again");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return new ResponseEntity<>(WalletMapper.mapTo((Wallet) walletDtoStatementPair[0])
                , HttpStatus.CREATED);
    }

    private User fetchUserByEmail(String email) {
        return userDao.fetchUserByEmail(email).orElseThrow(
                BadCredentialsException::new
        );
    }

    private void validatePassword(String hashedPassword, String plainTextPassword) {
        if (!PasswordEncoder.decodeMatches(plainTextPassword, hashedPassword)) {
            throw new BadCredentialsException();
        }
    }
}
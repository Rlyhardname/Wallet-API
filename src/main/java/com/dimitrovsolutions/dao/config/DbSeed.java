package com.dimitrovsolutions.dao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.dimitrovsolutions.dao.utils.JdbcUtils.closeConnection;
import static com.dimitrovsolutions.dao.utils.JdbcUtils.connect;

/**
 * On startup logs into postgres default database and creates new database with credentials
 * and database name and url from db.properties file. Adds all tables,constraints,triggers at
 * server launch if they're missing.
 */
@Configuration
public class DbSeed {

    @Bean
    DbSeed seedDataBaseService() {
        DbSeed seed = new DbSeed();
        seed.createDatabase();
        seed.seedDatabase();
        return seed;
    }


    private static String url = "jdbc:postgresql://localhost:5433/postgres";
    private static String user = "postgres";
    private static String password = DatabaseConfiguration.getPassword();

    private static final String CREATE_DATABASE = "CREATE DATABASE " + DatabaseConfiguration.getDatabaseName() + ";";// + DatabaseConfiguration.getDatabaseName();

    private static final String PG_CRYPTO = "CREATE EXTENSION IF NOT EXISTS pgcrypto;";

    private static final String CITEXT = "CREATE EXTENSION IF NOT EXISTS citext;";

    private static final String TABLE_USERS = "  CREATE TABLE IF NOT EXISTS Users(\n" +
            "            id UUID primary key DEFAULT gen_random_uuid(),\n" +
            "            email citext UNIQUE NOT NULL,\n" +
            "            first_name varchar(25) NOT NULL,\n" +
            "            last_name varchar(25) NOT NULL,\n" +
            "            egn varchar(10) UNIQUE CHECK (LENGTH(egn) = 10),\n" +
            "            password varchar not null,\n" +
            "            account_Id UUID DEFAULT gen_random_uuid()\n" +
            "            )";

    private static final String TABLE_ACCOUNT = "     CREATE TABLE IF NOT EXISTS account(\n" +
            "            id UUID not null,\n" +
            "            wallet_Id UUID not null references wallet(id),\n" +
            "            primary key(id,wallet_Id)\n" +
            "            )";

    private static final String TABLE_WALLET = "     CREATE TABLE IF NOT EXISTS wallet(\n" +
            "            ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),\n" +
            "            account_Id UUID NOT NULL,\n" +
            "            amount NUMERIC(15,2) DEFAULT 0.00 CHECK (amount>-0.01),\n" +
            "            currency citext NOT NULL,\n" +
            "            CONSTRAINT currency_exact_length CHECK (LENGTH(currency) = 3)\n" +
            "            );";

    private static final String FUNC_ADD_WALLET_TO_ACCOUNT = "  CREATE OR REPLACE FUNCTION add_wallet_to_account()\n" +
            "            RETURNS TRIGGER AS $$\n" +
            "            DECLARE\n" +
            "            \texisting_wallet wallet%ROWTYPE;\n" +
            "            BEGIN\n" +
            "            \tIF(TG_OP = 'INSERT') THEN\n" +
            "            \t\tSELECT * INTO existing_wallet\n" +
            "            \t\tFROM wallet w\n" +
            "            \t\tWHERE w.account_id = NEW.account_id AND w.currency = NEW.currency AND w.id != NEW.id;\n" +
            "            \n" +
            "            \t\tIF FOUND THEN\n" +
            "            \t\t\tRAISE EXCEPTION 'Account with id % and currency % alwaready exists',\n" +
            "            \t\t\t\texisting_wallet.id, NEW.currency;\n" +
            "            \t\tELSE\n" +
            "            \t\t\tINSERT INTO Account(id, wallet_id)\n" +
            "            \t\t\tvalues(NEW.account_Id, NEW.id);\n" +
            "            \t\tEND IF;\n" +
            "            \tEND IF;\n" +
            "            \treturn NEW;\n" +
            "            END;\n" +
            "            $$ LANGUAGE plpgsql;";

    private static final String WALLET_TRIGGER = """
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM pg_trigger
                    WHERE tgname = 'wallet_trigger'
                ) THEN
                    EXECUTE $q$
                        CREATE TRIGGER wallet_trigger
                        AFTER INSERT OR UPDATE ON wallet
                        FOR EACH ROW
                        EXECUTE FUNCTION add_wallet_to_account();
                    $q$;
                END IF;
            END
            $$;
            """;

    private static final String TABLE_STATEMENT = "     CREATE TABLE IF NOT EXISTS statement(\n" +
            "            id citext UNIQUE NOT NULL,\n" +
            "            email citext NOT NULL,\n" +
            "            wallet_id UUID,\n" +
            "            card_number varchar(19) CHECK (LENGTH(card_number) BETWEEN 8 AND 19),\n" +
            "            amount NUMERIC(15,2) NOT NULL,\n" +
            "            currency citext NOT NULL,\n" +
            "            operation VARCHAR NOT NULL,\n" +
            "            status VARCHAR NOT NULL,\n" +
            "            operation_time timestamp default NOW(),\n" +
            "            CONSTRAINT currency_exact_length CHECK (LENGTH(currency) = 3)\n" +
            "            )";


    public void seedDatabase() {
        Connection connection = null;
        try {
            connection = connect();
            try (Statement stmt = connection.createStatement()) {
                stmt.addBatch(PG_CRYPTO);
                stmt.addBatch(CITEXT);
                stmt.addBatch(TABLE_USERS);
                stmt.addBatch(TABLE_WALLET);
                stmt.addBatch(TABLE_ACCOUNT);
                stmt.addBatch(FUNC_ADD_WALLET_TO_ACCOUNT);
                stmt.addBatch(WALLET_TRIGGER);
                stmt.addBatch(TABLE_STATEMENT);
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    public void createDatabase() {

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(CREATE_DATABASE);
            } catch (SQLException ex) {
                // Will throw if db exists;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
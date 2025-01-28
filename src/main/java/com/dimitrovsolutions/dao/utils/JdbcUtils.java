package com.dimitrovsolutions.dao.utils;

import com.dimitrovsolutions.dao.config.DatabaseConfiguration;

import java.sql.*;

public class JdbcUtils {

    public static Connection connect() {
        try {
            String url = DatabaseConfiguration.getUrl();
            String user = DatabaseConfiguration.getUsername();
            String password = DatabaseConfiguration.getPassword();

            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.dimitrovsolutions.dao.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Responsible for loading properties from db.properties file, and returning them on demand.
 */
public class DatabaseConfiguration {
    public static final Properties properties = new Properties();

    static {
        try (InputStream in =
                     Objects.requireNonNull(DatabaseConfiguration.class.getClassLoader().getResource("db.properties")).openStream()) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("url");
    }

    public static String getDatabaseName() {
        return properties.getProperty("dbName");
    }

    public static String getUsername() {
        return properties.getProperty("username");
    }

    public static String getPassword() {
        return properties.getProperty("password");
    }
}
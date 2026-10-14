package com.aircraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database connection utility managing MySQL JDBC connections.
 * Supports externalized configuration via db.properties or environment variables.
 */
public class DatabaseConfig {

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "aircraft_maintenance_db";
    private static String username = "root";
    private static String password = "";
    private static boolean useSSL = false;
    private static boolean allowPublicKeyRetrieval = true;
    private static String serverTimezone = "UTC";

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try local file in current working directory
        File localFile = new File("db.properties");
        if (localFile.exists()) {
            try (InputStream in = new FileInputStream(localFile)) {
                props.load(in);
                loaded = true;
            } catch (Exception e) {
                System.err.println("Notice: Could not load local db.properties: " + e.getMessage());
            }
        }

        // 2. Try classpath resource
        if (!loaded) {
            try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception ignored) {
            }
        }

        // Apply properties if loaded
        if (loaded) {
            host = props.getProperty("db.host", host);
            port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)));
            database = props.getProperty("db.name", database);
            username = props.getProperty("db.username", username);
            password = props.getProperty("db.password", password);
            useSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", String.valueOf(useSSL)));
            allowPublicKeyRetrieval = Boolean.parseBoolean(props.getProperty("db.allowPublicKeyRetrieval", String.valueOf(allowPublicKeyRetrieval)));
            serverTimezone = props.getProperty("db.serverTimezone", serverTimezone);
        }

        // 3. Override with environment variables if present
        if (System.getenv("DB_HOST") != null) host = System.getenv("DB_HOST");
        if (System.getenv("DB_PORT") != null) {
            try { port = Integer.parseInt(System.getenv("DB_PORT")); } catch (NumberFormatException ignored) {}
        }
        if (System.getenv("DB_NAME") != null) database = System.getenv("DB_NAME");
        if (System.getenv("DB_USER") != null) username = System.getenv("DB_USER");
        if (System.getenv("DB_PASSWORD") != null) password = System.getenv("DB_PASSWORD");
    }

    public static String getJdbcUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b&serverTimezone=%s",
            host, port, database, useSSL, allowPublicKeyRetrieval, serverTimezone
        );
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
        }
        return DriverManager.getConnection(getJdbcUrl(), username, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void setCredentials(String user, String pass, String dbHost, int dbPort, String dbName) {
        username = user;
        password = pass;
        host = dbHost;
        port = dbPort;
        database = dbName;
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }
}

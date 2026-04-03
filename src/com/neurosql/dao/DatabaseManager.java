package com.neurosql.dao;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws Exception {
        Properties props = new Properties();

        // Try 1: standard classpath (works when Resources Root is set)
        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("db.properties");

        // Try 2: direct file path (works when classpath doesn't include resources)
        if (in == null) {
            java.io.File f = new java.io.File("resources/db.properties");
            if (f.exists()) {
                in = new java.io.FileInputStream(f);
                System.out.println("[DB] Loaded db.properties from file path.");
            }
        }

        // Try 3: one level up
        if (in == null) {
            java.io.File f = new java.io.File("../resources/db.properties");
            if (f.exists()) {
                in = new java.io.FileInputStream(f);
                System.out.println("[DB] Loaded db.properties from parent path.");
            }
        }

        if (in == null) {
            throw new RuntimeException(
                    "db.properties not found in any location.\n" +
                            "Looked in: classpath, resources/, ../resources/\n" +
                            "Make sure resources/db.properties exists and contains your password."
            );
        }

        props.load(in);
        in.close();

        this.connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
        this.connection.setAutoCommit(false);
        System.out.println("[DB] Connected to PostgreSQL successfully.");
    }

    public static synchronized DatabaseManager getInstance() throws Exception {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void batchInsertEEGSignals(int sessionId, Object[][] rows) throws SQLException {
        final int BATCH_SIZE = 1000;
        String sql = "INSERT INTO eeg_signals (session_id, ts_offset, channel_name, voltage) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < rows.length; i++) {
                ps.setInt(1, sessionId);
                ps.setDouble(2, (Double)  rows[i][0]);
                ps.setString(3, (String)  rows[i][1]);
                ps.setDouble(4, (Double)  rows[i][2]);
                ps.addBatch();
                if ((i + 1) % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    connection.commit();
                    System.out.printf("  Committed %,d rows...%n", i + 1);
                }
            }
            ps.executeBatch();
            connection.commit();
            System.out.printf("  Batch insert complete: %,d total rows.%n", rows.length);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("DB connection closed.");
        }
    }
}
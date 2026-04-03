package com.neurosql.etl;

import com.neurosql.dao.DatabaseManager;

public class EEGDataLoader {

    private static final String DEFAULT_CSV  = "python/eeg_data.csv";
    private static final String DEFAULT_NAME = "Arjun Sharma";
    private static final int    DEFAULT_AGE  = 34;
    private static final String DEFAULT_RISK = "HIGH";

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Neuro-SQL — EEG Data Loader  (ETL)    ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        String csvPath     = DEFAULT_CSV;
        String patientName = DEFAULT_NAME;
        int    patientAge  = DEFAULT_AGE;
        String riskLevel   = DEFAULT_RISK;

        System.out.println("[Loader] Using defaults for test run.");
        System.out.printf("[Loader] CSV     : %s%n", csvPath);
        System.out.printf("[Loader] Patient : %s (age %d, risk=%s)%n", patientName, patientAge, riskLevel);

        ETLResult result;
        try {
            ETLPipeline pipeline = new ETLPipeline();
            result = pipeline.run(csvPath, patientName, patientAge, riskLevel);
        } catch (Exception e) {
            System.err.println("\n[Loader] FATAL: " + e.getMessage());
            System.err.println("Check db.properties and ensure PostgreSQL is running.");
            e.printStackTrace();
            return;
        }

        System.out.println();
        if (result.isSuccess()) {
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.printf( "║  ETL COMPLETE                            ║%n");
            System.out.printf( "║  Patient ID  : %-26d║%n", result.getPatientId());
            System.out.printf( "║  Session ID  : %-26d║%n", result.getSessionId());
            System.out.printf( "║  Rows loaded : %-26s║%n", String.format("%,d", result.getRowsInserted()));
            System.out.printf( "║  Time taken  : %-26s║%n", String.format("%.2f s", result.getElapsedMs() / 1000.0));
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.printf("%nSession ID to give Partner B: %d%n", result.getSessionId());
        } else {
            System.err.println("ETL FAILED: " + result.getErrorMessage());
        }

        try { DatabaseManager.getInstance().closeConnection(); } catch (Exception ignored) {}
    }
}
package com.neurosql.etl;

import com.neurosql.dao.DatabaseManager;
import com.neurosql.dao.PatientDAO;
import com.neurosql.dao.SessionDAO;
import com.neurosql.model.Patient;

public class ETLPipeline {

    private final CSVParser  csvParser;
    private final PatientDAO patientDAO;
    private final SessionDAO sessionDAO;

    public ETLPipeline() throws Exception {
        this.csvParser  = new CSVParser();
        this.patientDAO = new PatientDAO();
        this.sessionDAO = new SessionDAO();
    }

    public ETLResult run(String csvPath, String patientName, int patientAge, String riskLevel) {
        long startMs = System.currentTimeMillis();

        // EXTRACT
        System.out.println("\n[ETL] Step 1/3 — EXTRACT: parsing CSV...");
        CSVParser.ParseResult parsed;
        try {
            parsed = csvParser.parse(csvPath);
        } catch (Exception e) {
            return ETLResult.fail("EXTRACT failed: " + e.getMessage());
        }

        parsed.printWarnings();
        if (parsed.getRowCount() == 0) {
            return ETLResult.fail("EXTRACT produced 0 valid rows.");
        }

        // TRANSFORM
        System.out.println("\n[ETL] Step 2/3 — TRANSFORM: validating data quality...");
        TransformSummary summary = buildSummary(parsed.getRows());
        summary.print();
        if (summary.getChannelCount() == 0) {
            return ETLResult.fail("TRANSFORM: no valid EEG channels found.");
        }

        // LOAD
        System.out.println("\n[ETL] Step 3/3 — LOAD: writing to PostgreSQL...");
        int patientId, sessionId;
        try {
            patientId = upsertPatient(patientName, patientAge, riskLevel);
            System.out.println("[ETL] Patient ID: " + patientId);
            sessionId = sessionDAO.insert(patientId);
            System.out.println("[ETL] Session ID: " + sessionId);
            DatabaseManager.getInstance().batchInsertEEGSignals(sessionId, parsed.getRows());
        } catch (Exception e) {
            return ETLResult.fail("LOAD failed: " + e.getMessage());
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        ETLResult result = ETLResult.ok(patientId, sessionId, parsed.getRowCount(), elapsedMs);
        System.out.println("\n[ETL] Complete — " + result);
        return result;
    }

    private int upsertPatient(String name, int age, String riskLevel) throws Exception {
        for (Patient p : patientDAO.findAll()) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                System.out.printf("[ETL] Reusing existing patient '%s' (id=%d)%n", name, p.getPatientId());
                return p.getPatientId();
            }
        }
        Patient p = new Patient();
        p.setName(name.trim());
        p.setAge(age);
        p.setHistoryRiskLevel(riskLevel.toUpperCase());
        int id = patientDAO.insert(p);
        System.out.printf("[ETL] Created new patient '%s' (id=%d)%n", name, id);
        return id;
    }

    private TransformSummary buildSummary(Object[][] rows) {
        java.util.Map<String, double[]> stats = new java.util.TreeMap<>();
        for (Object[] row : rows) {
            String ch = (String) row[1];
            double v  = (Double) row[2];
            stats.computeIfAbsent(ch, k -> new double[]{v, v, 0, 0});
            double[] s = stats.get(ch);
            if (v < s[0]) s[0] = v;
            if (v > s[1]) s[1] = v;
            s[2] += v;
            s[3]++;
        }
        return new TransformSummary(stats, rows.length);
    }

    private static class TransformSummary {
        private final java.util.Map<String, double[]> stats;
        private final int totalRows;

        TransformSummary(java.util.Map<String, double[]> stats, int totalRows) {
            this.stats = stats;
            this.totalRows = totalRows;
        }

        int getChannelCount() { return stats.size(); }

        void print() {
            System.out.printf("  %-10s %10s %10s %10s %10s%n",
                    "Channel", "Samples", "Min(µV)", "Max(µV)", "Avg(µV)");
            System.out.println("  " + "-".repeat(52));
            stats.forEach((ch, s) ->
                    System.out.printf("  %-10s %10.0f %10.2f %10.2f %10.2f%n",
                            ch, s[3], s[0], s[1], s[2] / s[3]));
            System.out.printf("  Total: %,d rows across %d channels%n", totalRows, stats.size());
        }
    }
}
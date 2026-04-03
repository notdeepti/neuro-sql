package com.neurosql.etl;

public class ETLResult {
    private final boolean success;
    private final int     patientId;
    private final int     sessionId;
    private final int     rowsInserted;
    private final long    elapsedMs;
    private final String  errorMessage;

    private ETLResult(boolean success, int patientId, int sessionId,
                      int rowsInserted, long elapsedMs, String errorMessage) {
        this.success      = success;
        this.patientId    = patientId;
        this.sessionId    = sessionId;
        this.rowsInserted = rowsInserted;
        this.elapsedMs    = elapsedMs;
        this.errorMessage = errorMessage;
    }

    public static ETLResult ok(int patientId, int sessionId, int rowsInserted, long elapsedMs) {
        return new ETLResult(true, patientId, sessionId, rowsInserted, elapsedMs, null);
    }

    public static ETLResult fail(String message) {
        return new ETLResult(false, -1, -1, 0, 0, message);
    }

    public boolean isSuccess()       { return success; }
    public int     getPatientId()    { return patientId; }
    public int     getSessionId()    { return sessionId; }
    public int     getRowsInserted() { return rowsInserted; }
    public long    getElapsedMs()    { return elapsedMs; }
    public String  getErrorMessage() { return errorMessage; }

    @Override
    public String toString() {
        if (!success) return "ETLResult[FAILED: " + errorMessage + "]";
        return String.format(
                "ETLResult[OK | patient=%d | session=%d | rows=%,d | time=%.2fs]",
                patientId, sessionId, rowsInserted, elapsedMs / 1000.0
        );
    }
}
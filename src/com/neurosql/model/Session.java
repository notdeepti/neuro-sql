package com.neurosql.model;

import java.time.LocalDateTime;

public class Session {
    private int           sessionId;
    private int           patientId;
    private LocalDateTime sessionDate;

    public int           getSessionId()   { return sessionId; }
    public int           getPatientId()   { return patientId; }
    public LocalDateTime getSessionDate() { return sessionDate; }

    public void setSessionId(int v)             { this.sessionId = v; }
    public void setPatientId(int v)             { this.patientId = v; }
    public void setSessionDate(LocalDateTime v) { this.sessionDate = v; }

    @Override
    public String toString() {
        return String.format("Session[id=%d, patientId=%d, date=%s]",
                sessionId, patientId, sessionDate);
    }
}
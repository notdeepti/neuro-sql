package com.neurosql.model;

public class Patient {
    private int    patientId;
    private String name;
    private int    age;
    private String historyRiskLevel;

    public int    getPatientId()        { return patientId; }
    public String getName()             { return name; }
    public int    getAge()              { return age; }
    public String getHistoryRiskLevel() { return historyRiskLevel; }

    public void setPatientId(int v)         { this.patientId = v; }
    public void setName(String v)           { this.name = v; }
    public void setAge(int v)               { this.age = v; }
    public void setHistoryRiskLevel(String v){ this.historyRiskLevel = v; }

    @Override
    public String toString() {
        return String.format("Patient[id=%d, name=%s, age=%d, risk=%s]",
                patientId, name, age, historyRiskLevel);
    }
}
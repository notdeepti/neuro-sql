package com.neurosql.model;

public class EEGSignal {
    private long   signalId;
    private int    sessionId;
    private double tsOffset;
    private String channelName;
    private double voltage;
    private double zScore;

    public long   getSignalId()    { return signalId; }
    public int    getSessionId()   { return sessionId; }
    public double getTsOffset()    { return tsOffset; }
    public String getChannelName() { return channelName; }
    public double getVoltage()     { return voltage; }
    public double getZScore()      { return zScore; }

    public void setSignalId(long v)     { this.signalId = v; }
    public void setSessionId(int v)     { this.sessionId = v; }
    public void setTsOffset(double v)   { this.tsOffset = v; }
    public void setChannelName(String v){ this.channelName = v; }
    public void setVoltage(double v)    { this.voltage = v; }
    public void setZScore(double v)     { this.zScore = v; }

    @Override
    public String toString() {
        return String.format("EEGSignal[ch=%s, t=%.4f, v=%.2f, z=%.2f]",
                channelName, tsOffset, voltage, zScore);
    }
}
package com.neurosql.dao;

import com.neurosql.model.EEGSignal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EEGSignalDAO {

    private final Connection conn;

    public EEGSignalDAO() throws Exception {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<EEGSignal> findBySession(int sessionId) throws SQLException {
        List<EEGSignal> list = new ArrayList<>();
        String sql = "SELECT * FROM eeg_signals "
                + "WHERE session_id = ? "
                + "ORDER BY ts_offset, channel_name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EEGSignal sig = new EEGSignal();
                sig.setSignalId(rs.getLong("signal_id"));
                sig.setSessionId(rs.getInt("session_id"));
                sig.setTsOffset(rs.getDouble("ts_offset"));
                sig.setChannelName(rs.getString("channel_name"));
                sig.setVoltage(rs.getDouble("voltage"));
                list.add(sig);
            }
        }
        return list;
    }

    public List<EEGSignal> findBySessionAndChannel(int sessionId, String channel)
            throws SQLException {
        List<EEGSignal> list = new ArrayList<>();
        String sql = "SELECT * FROM eeg_signals "
                + "WHERE session_id = ? AND channel_name = ? "
                + "ORDER BY ts_offset";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, channel);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EEGSignal sig = new EEGSignal();
                sig.setSignalId(rs.getLong("signal_id"));
                sig.setSessionId(rs.getInt("session_id"));
                sig.setTsOffset(rs.getDouble("ts_offset"));
                sig.setChannelName(rs.getString("channel_name"));
                sig.setVoltage(rs.getDouble("voltage"));
                list.add(sig);
            }
        }
        return list;
    }
}
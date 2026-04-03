package com.neurosql.dao;

import com.neurosql.model.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    private final Connection conn;

    public SessionDAO() throws Exception {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public int insert(int patientId) throws SQLException {
        String sql = "INSERT INTO sessions (patient_id) VALUES (?) RETURNING session_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("session_id");
                conn.commit();
                return id;
            }
        }
        throw new SQLException("Insert failed - no session_id returned.");
    }

    public List<Session> findByPatient(int patientId) throws SQLException {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT * FROM sessions WHERE patient_id = ? "
                + "ORDER BY session_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Session s = new Session();
                s.setSessionId(rs.getInt("session_id"));
                s.setPatientId(rs.getInt("patient_id"));
                s.setSessionDate(
                        rs.getTimestamp("session_date").toLocalDateTime());
                list.add(s);
            }
        }
        return list;
    }
}
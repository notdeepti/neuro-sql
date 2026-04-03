package com.neurosql.dao;

import com.neurosql.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    private final Connection conn;

    public PatientDAO() throws Exception {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public int insert(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (name, age, history_risk_level) "
                + "VALUES (?, ?, ?) RETURNING patient_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getHistoryRiskLevel());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("patient_id");
                conn.commit();
                return id;
            }
        }
        throw new SQLException("Insert failed - no patient_id returned.");
    }

    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM patients ORDER BY patient_id")) {
            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setHistoryRiskLevel(rs.getString("history_risk_level"));
                list.add(p);
            }
        }
        return list;
    }

    public Patient findById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setHistoryRiskLevel(rs.getString("history_risk_level"));
                return p;
            }
        }
        return null;
    }
}
package com.aircraft.dao;

import com.aircraft.model.Alert;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    public List<Alert> findAll() throws SQLException {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT al.*, a.registration_no, c.serial_number AS component_serial "
                   + "FROM ALERT al "
                   + "LEFT JOIN AIRCRAFT a ON al.aircraft_id = a.aircraft_id "
                   + "LEFT JOIN COMPONENT c ON al.component_id = c.component_id "
                   + "ORDER BY al.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public List<Alert> findOpen() throws SQLException {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT al.*, a.registration_no, c.serial_number AS component_serial "
                   + "FROM ALERT al "
                   + "LEFT JOIN AIRCRAFT a ON al.aircraft_id = a.aircraft_id "
                   + "LEFT JOIN COMPONENT c ON al.component_id = c.component_id "
                   + "WHERE al.status = 'OPEN' "
                   + "ORDER BY CASE al.severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, al.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public void updateStatus(int alertId, String newStatus) throws SQLException {
        String sql = "UPDATE ALERT SET status = ? WHERE alert_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, alertId);
            ps.executeUpdate();
        }
    }

    public void insert(Alert al) throws SQLException {
        String sql = "INSERT INTO ALERT (aircraft_id, component_id, alert_type, severity, message, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (al.getAircraftId() != null) ps.setInt(1, al.getAircraftId()); else ps.setNull(1, java.sql.Types.INTEGER);
            if (al.getComponentId() != null) ps.setInt(2, al.getComponentId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, al.getAlertType());
            ps.setString(4, al.getSeverity());
            ps.setString(5, al.getMessage());
            ps.setString(6, al.getStatus() != null ? al.getStatus() : "OPEN");
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    al.setAlertId(gk.getInt(1));
                }
            }
        }
    }

    public int countOpen() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ALERT WHERE status = 'OPEN'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Alert mapResultSet(ResultSet rs) throws SQLException {
        Alert al = new Alert();
        al.setAlertId(rs.getInt("alert_id"));
        int aid = rs.getInt("aircraft_id");
        al.setAircraftId(rs.wasNull() ? null : aid);
        int cid = rs.getInt("component_id");
        al.setComponentId(rs.wasNull() ? null : cid);
        al.setAlertType(rs.getString("alert_type"));
        al.setSeverity(rs.getString("severity"));
        al.setMessage(rs.getString("message"));
        Timestamp ts = rs.getTimestamp("created_at");
        al.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        al.setStatus(rs.getString("status"));
        al.setAircraftRegistration(rs.getString("registration_no"));
        al.setComponentSerial(rs.getString("component_serial"));
        return al;
    }
}

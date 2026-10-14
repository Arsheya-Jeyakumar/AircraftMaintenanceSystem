package com.aircraft.dao;

import com.aircraft.model.Failure;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FailureDAO {

    public List<Failure> findAll() throws SQLException {
        List<Failure> list = new ArrayList<>();
        String sql = "SELECT f.*, a.registration_no, c.serial_number AS component_serial, c.component_type "
                   + "FROM FAILURE f "
                   + "JOIN AIRCRAFT a ON f.aircraft_id = a.aircraft_id "
                   + "JOIN COMPONENT c ON f.component_id = c.component_id "
                   + "ORDER BY f.failure_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Failure f = new Failure();
                f.setFailureId(rs.getInt("failure_id"));
                f.setAircraftId(rs.getInt("aircraft_id"));
                f.setComponentId(rs.getInt("component_id"));
                Date fd = rs.getDate("failure_date");
                f.setFailureDate(fd != null ? fd.toLocalDate() : null);
                f.setFailureType(rs.getString("failure_type"));
                f.setSeverity(rs.getString("severity"));
                f.setDescription(rs.getString("description"));
                f.setResolved(rs.getBoolean("resolved"));
                f.setAircraftRegistration(rs.getString("registration_no"));
                f.setComponentSerial(rs.getString("component_serial"));
                f.setComponentType(rs.getString("component_type"));
                list.add(f);
            }
        }
        return list;
    }

    public List<Failure> findRecurring() throws SQLException {
        List<Failure> list = new ArrayList<>();
        String sql = "SELECT * FROM v_recurring_failures ORDER BY incident_count DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Failure f = new Failure();
                f.setComponentId(rs.getInt("component_id"));
                f.setComponentSerial(rs.getString("serial_number"));
                f.setComponentType(rs.getString("component_type"));
                f.setFailureType(rs.getString("failure_type"));
                f.setIncidentCount(rs.getInt("incident_count"));
                f.setSeverity(rs.getString("highest_severity"));
                Date ld = rs.getDate("latest_failure_date");
                f.setFailureDate(ld != null ? ld.toLocalDate() : null);
                f.setDescription("Affecting: " + rs.getString("affected_aircraft") + " | Unresolved: " + rs.getInt("unresolved_incidents"));
                f.setResolved(rs.getInt("unresolved_incidents") == 0);
                list.add(f);
            }
        }
        return list;
    }

    public void insert(Failure f) throws SQLException {
        String sql = "INSERT INTO FAILURE (aircraft_id, component_id, failure_date, failure_type, severity, description, resolved) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, f.getAircraftId());
            ps.setInt(2, f.getComponentId());
            ps.setDate(3, Date.valueOf(f.getFailureDate()));
            ps.setString(4, f.getFailureType());
            ps.setString(5, f.getSeverity());
            ps.setString(6, f.getDescription());
            ps.setBoolean(7, f.isResolved());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    f.setFailureId(gk.getInt(1));
                }
            }
        }
    }

    public void updateResolution(int failureId, boolean resolved) throws SQLException {
        String sql = "UPDATE FAILURE SET resolved = ? WHERE failure_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, resolved);
            ps.setInt(2, failureId);
            ps.executeUpdate();
        }
    }
}

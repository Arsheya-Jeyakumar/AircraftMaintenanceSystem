package com.aircraft.dao;

import com.aircraft.model.Maintenance;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    public List<Maintenance> findAll() throws SQLException {
        List<Maintenance> list = new ArrayList<>();
        String sql = "SELECT m.*, a.registration_no, a.model AS aircraft_model, "
                   + "c.serial_number AS component_serial, c.component_type, "
                   + "t.name AS technician_name, t.employee_no AS technician_employee_no "
                   + "FROM MAINTENANCE m "
                   + "JOIN AIRCRAFT a ON m.aircraft_id = a.aircraft_id "
                   + "LEFT JOIN COMPONENT c ON m.component_id = c.component_id "
                   + "LEFT JOIN TECHNICIAN t ON m.technician_id = t.technician_id "
                   + "ORDER BY m.scheduled_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public List<Maintenance> findOverdue() throws SQLException {
        List<Maintenance> list = new ArrayList<>();
        String sql = "SELECT * FROM v_overdue_maintenance ORDER BY days_overdue DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Maintenance m = new Maintenance();
                m.setMaintenanceId(rs.getInt("maintenance_id"));
                m.setMaintenanceType(rs.getString("maintenance_type"));
                Date sd = rs.getDate("scheduled_date");
                m.setScheduledDate(sd != null ? sd.toLocalDate() : null);
                m.setDaysOverdue(rs.getInt("days_overdue"));
                m.setStatus(rs.getString("maintenance_status"));
                m.setDescription(rs.getString("description"));
                m.setAircraftId(rs.getInt("aircraft_id"));
                m.setAircraftRegistration(rs.getString("registration_no"));
                m.setAircraftModel(rs.getString("aircraft_model"));
                int cid = rs.getInt("component_id");
                m.setComponentId(rs.wasNull() ? null : cid);
                m.setComponentSerial(rs.getString("component_serial"));
                m.setComponentType(rs.getString("component_type"));
                int tid = rs.getInt("technician_id");
                m.setTechnicianId(rs.wasNull() ? null : tid);
                m.setTechnicianName(rs.getString("technician_name"));
                m.setTechnicianEmployeeNo(rs.getString("technician_employee_no"));
                list.add(m);
            }
        }
        return list;
    }

    public void insert(Maintenance m) throws SQLException {
        String sql = "INSERT INTO MAINTENANCE (aircraft_id, component_id, maintenance_type, scheduled_date, description, status, technician_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getAircraftId());
            if (m.getComponentId() != null) ps.setInt(2, m.getComponentId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, m.getMaintenanceType());
            ps.setDate(4, Date.valueOf(m.getScheduledDate()));
            ps.setString(5, m.getDescription());
            ps.setString(6, m.getStatus() != null ? m.getStatus() : "SCHEDULED");
            if (m.getTechnicianId() != null) ps.setInt(7, m.getTechnicianId()); else ps.setNull(7, java.sql.Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    m.setMaintenanceId(gk.getInt(1));
                }
            }
        }
    }

    public void completeMaintenance(int maintenanceId, LocalDate completionDate) throws SQLException {
        String sql = "UPDATE MAINTENANCE SET completed_date = ?, status = 'COMPLETED' WHERE maintenance_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(completionDate != null ? completionDate : LocalDate.now()));
            ps.setInt(2, maintenanceId);
            ps.executeUpdate();
        }
    }

    public int countOverdue() throws SQLException {
        String sql = "SELECT COUNT(*) FROM MAINTENANCE WHERE scheduled_date < CURRENT_DATE AND status <> 'COMPLETED'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Maintenance mapResultSet(ResultSet rs) throws SQLException {
        Maintenance m = new Maintenance();
        m.setMaintenanceId(rs.getInt("maintenance_id"));
        m.setAircraftId(rs.getInt("aircraft_id"));
        int cid = rs.getInt("component_id");
        m.setComponentId(rs.wasNull() ? null : cid);
        m.setMaintenanceType(rs.getString("maintenance_type"));
        Date sd = rs.getDate("scheduled_date");
        m.setScheduledDate(sd != null ? sd.toLocalDate() : null);
        Date cd = rs.getDate("completed_date");
        m.setCompletedDate(cd != null ? cd.toLocalDate() : null);
        m.setDescription(rs.getString("description"));
        m.setStatus(rs.getString("status"));
        int tid = rs.getInt("technician_id");
        m.setTechnicianId(rs.wasNull() ? null : tid);
        m.setAircraftRegistration(rs.getString("registration_no"));
        m.setAircraftModel(rs.getString("aircraft_model"));
        m.setComponentSerial(rs.getString("component_serial"));
        m.setComponentType(rs.getString("component_type"));
        m.setTechnicianName(rs.getString("technician_name"));
        m.setTechnicianEmployeeNo(rs.getString("technician_employee_no"));
        return m;
    }
}

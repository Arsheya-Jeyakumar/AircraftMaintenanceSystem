package com.aircraft.dao;

import com.aircraft.model.Technician;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    public List<Technician> findAll() throws SQLException {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM TECHNICIAN ORDER BY name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public List<Technician> findActive() throws SQLException {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM TECHNICIAN WHERE status = 'ACTIVE' ORDER BY name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public void insert(Technician t) throws SQLException {
        String sql = "INSERT INTO TECHNICIAN (employee_no, name, qualification, specialization, phone, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getEmployeeNo());
            ps.setString(2, t.getName());
            ps.setString(3, t.getQualification());
            ps.setString(4, t.getSpecialization());
            ps.setString(5, t.getPhone());
            ps.setString(6, t.getStatus() != null ? t.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    t.setTechnicianId(gk.getInt(1));
                }
            }
        }
    }

    public void update(Technician t) throws SQLException {
        String sql = "UPDATE TECHNICIAN SET qualification = ?, specialization = ?, phone = ?, status = ? "
                   + "WHERE technician_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getQualification());
            ps.setString(2, t.getSpecialization());
            ps.setString(3, t.getPhone());
            ps.setString(4, t.getStatus());
            ps.setInt(5, t.getTechnicianId());
            ps.executeUpdate();
        }
    }

    private Technician mapResultSet(ResultSet rs) throws SQLException {
        Technician t = new Technician();
        t.setTechnicianId(rs.getInt("technician_id"));
        t.setEmployeeNo(rs.getString("employee_no"));
        t.setName(rs.getString("name"));
        t.setQualification(rs.getString("qualification"));
        t.setSpecialization(rs.getString("specialization"));
        t.setPhone(rs.getString("phone"));
        t.setStatus(rs.getString("status"));
        return t;
    }
}

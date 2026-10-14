package com.aircraft.dao;

import com.aircraft.model.Component;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ComponentDAO {

    public List<Component> findAll() throws SQLException {
        List<Component> list = new ArrayList<>();
        String sql = "SELECT * FROM COMPONENT ORDER BY serial_number";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public List<Component> findAvailable() throws SQLException {
        List<Component> list = new ArrayList<>();
        String sql = "SELECT * FROM COMPONENT WHERE current_status = 'AVAILABLE' ORDER BY component_type, serial_number";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public Component findById(int id) throws SQLException {
        String sql = "SELECT * FROM COMPONENT WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public void insert(Component c) throws SQLException {
        String sql = "INSERT INTO COMPONENT (serial_number, component_type, manufacturer, model, service_limit_hours, service_limit_cycles, current_status, current_location) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getSerialNumber());
            ps.setString(2, c.getComponentType());
            ps.setString(3, c.getManufacturer());
            ps.setString(4, c.getModel());
            if (c.getServiceLimitHours() != null) ps.setDouble(5, c.getServiceLimitHours()); else ps.setNull(5, java.sql.Types.DECIMAL);
            if (c.getServiceLimitCycles() != null) ps.setInt(6, c.getServiceLimitCycles()); else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, c.getCurrentStatus() != null ? c.getCurrentStatus() : "AVAILABLE");
            ps.setString(8, c.getCurrentLocation() != null ? c.getCurrentLocation() : "Warehouse");
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    c.setComponentId(gk.getInt(1));
                }
            }
        }
    }

    public void update(Component c) throws SQLException {
        String sql = "UPDATE COMPONENT SET component_type = ?, manufacturer = ?, model = ?, "
                   + "service_limit_hours = ?, service_limit_cycles = ?, current_status = ?, current_location = ? "
                   + "WHERE component_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getComponentType());
            ps.setString(2, c.getManufacturer());
            ps.setString(3, c.getModel());
            if (c.getServiceLimitHours() != null) ps.setDouble(4, c.getServiceLimitHours()); else ps.setNull(4, java.sql.Types.DECIMAL);
            if (c.getServiceLimitCycles() != null) ps.setInt(5, c.getServiceLimitCycles()); else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setString(6, c.getCurrentStatus());
            ps.setString(7, c.getCurrentLocation());
            ps.setInt(8, c.getComponentId());
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM COMPONENT";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Component mapResultSet(ResultSet rs) throws SQLException {
        Component c = new Component();
        c.setComponentId(rs.getInt("component_id"));
        c.setSerialNumber(rs.getString("serial_number"));
        c.setComponentType(rs.getString("component_type"));
        c.setManufacturer(rs.getString("manufacturer"));
        c.setModel(rs.getString("model"));
        double slh = rs.getDouble("service_limit_hours");
        c.setServiceLimitHours(rs.wasNull() ? null : slh);
        int slc = rs.getInt("service_limit_cycles");
        c.setServiceLimitCycles(rs.wasNull() ? null : slc);
        c.setCurrentStatus(rs.getString("current_status"));
        c.setCurrentLocation(rs.getString("current_location"));
        return c;
    }
}

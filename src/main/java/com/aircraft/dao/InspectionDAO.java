package com.aircraft.dao;

import com.aircraft.model.Inspection;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InspectionDAO {

    public List<Inspection> findAll() throws SQLException {
        List<Inspection> list = new ArrayList<>();
        String sql = "SELECT i.*, a.registration_no, c.serial_number AS component_serial, t.name AS inspector_name "
                   + "FROM INSPECTION i "
                   + "JOIN AIRCRAFT a ON i.aircraft_id = a.aircraft_id "
                   + "LEFT JOIN COMPONENT c ON i.component_id = c.component_id "
                   + "JOIN TECHNICIAN t ON i.inspector_id = t.technician_id "
                   + "ORDER BY i.inspection_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inspection i = new Inspection();
                i.setInspectionId(rs.getInt("inspection_id"));
                i.setAircraftId(rs.getInt("aircraft_id"));
                int cid = rs.getInt("component_id");
                i.setComponentId(rs.wasNull() ? null : cid);
                Date id = rs.getDate("inspection_date");
                i.setInspectionDate(id != null ? id.toLocalDate() : null);
                i.setInspectionType(rs.getString("inspection_type"));
                i.setResult(rs.getString("result"));
                i.setFindings(rs.getString("findings"));
                i.setInspectorId(rs.getInt("inspector_id"));
                i.setAircraftRegistration(rs.getString("registration_no"));
                i.setComponentSerial(rs.getString("component_serial"));
                i.setInspectorName(rs.getString("inspector_name"));
                list.add(i);
            }
        }
        return list;
    }

    public void insert(Inspection i) throws SQLException {
        String sql = "INSERT INTO INSPECTION (aircraft_id, component_id, inspection_date, inspection_type, result, findings, inspector_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getAircraftId());
            if (i.getComponentId() != null) ps.setInt(2, i.getComponentId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setDate(3, Date.valueOf(i.getInspectionDate()));
            ps.setString(4, i.getInspectionType());
            ps.setString(5, i.getResult());
            ps.setString(6, i.getFindings());
            ps.setInt(7, i.getInspectorId());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    i.setInspectionId(gk.getInt(1));
                }
            }
        }
    }
}

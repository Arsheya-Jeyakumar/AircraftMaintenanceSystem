package com.aircraft.dao;

import com.aircraft.model.Aircraft;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AircraftDAO {

    public List<Aircraft> findAll() throws SQLException {
        List<Aircraft> list = new ArrayList<>();
        String sql = "SELECT * FROM AIRCRAFT ORDER BY registration_no";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public Aircraft findById(int id) throws SQLException {
        String sql = "SELECT * FROM AIRCRAFT WHERE aircraft_id = ?";
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

    public Aircraft findByRegistration(String regNo) throws SQLException {
        String sql = "SELECT * FROM AIRCRAFT WHERE registration_no = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public void insert(Aircraft aircraft) throws SQLException {
        String sql = "INSERT INTO AIRCRAFT (registration_no, model, manufacturer, manufacture_date, total_flight_hours, total_cycles, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, aircraft.getRegistrationNo());
            ps.setString(2, aircraft.getModel());
            ps.setString(3, aircraft.getManufacturer());
            ps.setDate(4, aircraft.getManufactureDate() != null ? Date.valueOf(aircraft.getManufactureDate()) : null);
            ps.setDouble(5, aircraft.getTotalFlightHours());
            ps.setInt(6, aircraft.getTotalCycles());
            ps.setString(7, aircraft.getStatus() != null ? aircraft.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    aircraft.setAircraftId(gk.getInt(1));
                }
            }
        }
    }

    public void update(Aircraft aircraft) throws SQLException {
        String sql = "UPDATE AIRCRAFT SET model = ?, manufacturer = ?, manufacture_date = ?, "
                   + "total_flight_hours = ?, total_cycles = ?, status = ? WHERE aircraft_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, aircraft.getModel());
            ps.setString(2, aircraft.getManufacturer());
            ps.setDate(3, aircraft.getManufactureDate() != null ? Date.valueOf(aircraft.getManufactureDate()) : null);
            ps.setDouble(4, aircraft.getTotalFlightHours());
            ps.setInt(5, aircraft.getTotalCycles());
            ps.setString(6, aircraft.getStatus());
            ps.setInt(7, aircraft.getAircraftId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int aircraftId, String status) throws SQLException {
        String sql = "UPDATE AIRCRAFT SET status = ? WHERE aircraft_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, aircraftId);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM AIRCRAFT";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Aircraft mapResultSet(ResultSet rs) throws SQLException {
        Aircraft a = new Aircraft();
        a.setAircraftId(rs.getInt("aircraft_id"));
        a.setRegistrationNo(rs.getString("registration_no"));
        a.setModel(rs.getString("model"));
        a.setManufacturer(rs.getString("manufacturer"));
        Date mDate = rs.getDate("manufacture_date");
        a.setManufactureDate(mDate != null ? mDate.toLocalDate() : null);
        a.setTotalFlightHours(rs.getDouble("total_flight_hours"));
        a.setTotalCycles(rs.getInt("total_cycles"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}

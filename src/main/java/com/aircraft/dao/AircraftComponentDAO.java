package com.aircraft.dao;

import com.aircraft.model.AircraftComponent;
import com.aircraft.util.DatabaseConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AircraftComponentDAO {

    public List<AircraftComponent> getActiveInstallations() throws SQLException {
        List<AircraftComponent> list = new ArrayList<>();
        String sql = "SELECT * FROM v_aircraft_current_components ORDER BY aircraft_registration, component_type";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AircraftComponent ac = new AircraftComponent();
                ac.setInstallationId(rs.getInt("installation_id"));
                ac.setAircraftId(rs.getInt("aircraft_id"));
                ac.setComponentId(rs.getInt("component_id"));
                ac.setAircraftRegistration(rs.getString("aircraft_registration"));
                ac.setAircraftModel(rs.getString("aircraft_model"));
                ac.setComponentSerial(rs.getString("component_serial"));
                ac.setComponentType(rs.getString("component_type"));
                ac.setComponentManufacturer(rs.getString("component_manufacturer"));
                Date id = rs.getDate("installed_date");
                ac.setInstalledDate(id != null ? id.toLocalDate() : null);
                ac.setInstallationHours(rs.getDouble("installation_hours"));
                ac.setInstallationCycles(rs.getInt("installation_cycles"));
                ac.setOperationalHoursLogged(rs.getDouble("current_installation_hours"));
                ac.setOperationalCyclesLogged(rs.getInt("current_installation_cycles"));
                ac.setLifecycleState("CURRENTLY_INSTALLED");
                list.add(ac);
            }
        }
        return list;
    }

    public List<AircraftComponent> getLifecycleHistory(String serialFilter) throws SQLException {
        List<AircraftComponent> list = new ArrayList<>();
        String sql = "SELECT * FROM v_component_lifecycle_history";
        if (serialFilter != null && !serialFilter.trim().isEmpty()) {
            sql += " WHERE serial_number LIKE ? ORDER BY installed_date DESC";
        } else {
            sql += " ORDER BY serial_number, installed_date DESC";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (serialFilter != null && !serialFilter.trim().isEmpty()) {
                ps.setString(1, "%" + serialFilter.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AircraftComponent ac = new AircraftComponent();
                    ac.setInstallationId(rs.getInt("installation_id"));
                    ac.setComponentId(rs.getInt("component_id"));
                    ac.setComponentSerial(rs.getString("serial_number"));
                    ac.setComponentType(rs.getString("component_type"));
                    ac.setComponentManufacturer(rs.getString("component_manufacturer"));
                    ac.setAircraftId(rs.getInt("aircraft_id"));
                    ac.setAircraftRegistration(rs.getString("registration_no"));
                    ac.setAircraftModel(rs.getString("aircraft_model"));
                    Date id = rs.getDate("installed_date");
                    ac.setInstalledDate(id != null ? id.toLocalDate() : null);
                    Date rd = rs.getDate("removed_date");
                    ac.setRemovedDate(rd != null ? rd.toLocalDate() : null);
                    ac.setInstallationHours(rs.getDouble("installation_hours"));
                    double rmh = rs.getDouble("removal_hours");
                    ac.setRemovalHours(rs.wasNull() ? null : rmh);
                    ac.setInstallationCycles(rs.getInt("installation_cycles"));
                    int rmc = rs.getInt("removal_cycles");
                    ac.setRemovalCycles(rs.wasNull() ? null : rmc);
                    ac.setOperationalHoursLogged(rs.getDouble("operational_hours_logged"));
                    ac.setOperationalCyclesLogged(rs.getInt("operational_cycles_logged"));
                    ac.setInstallationReason(rs.getString("installation_reason"));
                    ac.setRemovalReason(rs.getString("removal_reason"));
                    ac.setLifecycleState(rs.getString("lifecycle_state"));
                    list.add(ac);
                }
            }
        }
        return list;
    }

    /**
     * Executes sp_install_component stored procedure inside a managed transaction.
     */
    public int installComponent(int aircraftId, int componentId, LocalDate installedDate,
                                double installHours, int installCycles, String reason) throws SQLException {
        String callSql = "{CALL sp_install_component(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, aircraftId);
            cs.setInt(2, componentId);
            cs.setDate(3, Date.valueOf(installedDate));
            cs.setDouble(4, installHours);
            cs.setInt(5, installCycles);
            cs.setString(6, reason);
            cs.registerOutParameter(7, java.sql.Types.INTEGER);
            cs.execute();
            return cs.getInt(7);
        }
    }

    /**
     * Executes sp_remove_component stored procedure inside a managed transaction.
     */
    public void removeComponent(int installationId, LocalDate removedDate, double removalHours,
                                int removalCycles, String reason, String newLocation, String newStatus) throws SQLException {
        String callSql = "{CALL sp_remove_component(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, installationId);
            cs.setDate(2, Date.valueOf(removedDate));
            cs.setDouble(3, removalHours);
            cs.setInt(4, removalCycles);
            cs.setString(5, reason);
            cs.setString(6, newLocation);
            cs.setString(7, newStatus);
            cs.execute();
        }
    }
}

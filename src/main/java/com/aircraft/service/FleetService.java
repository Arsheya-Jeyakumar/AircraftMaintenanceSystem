package com.aircraft.service;

import com.aircraft.dao.AircraftComponentDAO;
import com.aircraft.dao.AircraftDAO;
import com.aircraft.dao.ComponentDAO;
import com.aircraft.model.Aircraft;
import com.aircraft.model.AircraftComponent;
import com.aircraft.model.Component;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FleetService {

    private final AircraftDAO aircraftDAO = new AircraftDAO();
    private final ComponentDAO componentDAO = new ComponentDAO();
    private final AircraftComponentDAO aircraftComponentDAO = new AircraftComponentDAO();

    public List<Aircraft> getAllAircraft() throws SQLException {
        return aircraftDAO.findAll();
    }

    public void addAircraft(Aircraft a) throws SQLException {
        if (a.getRegistrationNo() == null || a.getRegistrationNo().trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        if (aircraftDAO.findByRegistration(a.getRegistrationNo().trim()) != null) {
            throw new IllegalArgumentException("Aircraft with registration " + a.getRegistrationNo() + " already exists.");
        }
        aircraftDAO.insert(a);
    }

    public void updateAircraft(Aircraft a) throws SQLException {
        aircraftDAO.update(a);
    }

    public void setAircraftStatus(int aircraftId, String status) throws SQLException {
        aircraftDAO.updateStatus(aircraftId, status);
    }

    public List<Component> getAllComponents() throws SQLException {
        return componentDAO.findAll();
    }

    public List<Component> getAvailableComponents() throws SQLException {
        return componentDAO.findAvailable();
    }

    public void addComponent(Component c) throws SQLException {
        if (c.getSerialNumber() == null || c.getSerialNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Component serial number cannot be empty.");
        }
        componentDAO.insert(c);
    }

    public void updateComponent(Component c) throws SQLException {
        componentDAO.update(c);
    }

    public List<AircraftComponent> getActiveInstallations() throws SQLException {
        return aircraftComponentDAO.getActiveInstallations();
    }

    public List<AircraftComponent> getLifecycleHistory(String serialFilter) throws SQLException {
        return aircraftComponentDAO.getLifecycleHistory(serialFilter);
    }

    public int installComponent(int aircraftId, int componentId, LocalDate installedDate,
                                double installHours, int installCycles, String reason) throws SQLException {
        if (installedDate == null) installedDate = LocalDate.now();
        if (installHours < 0 || installCycles < 0) {
            throw new IllegalArgumentException("Installation flight hours and cycles cannot be negative.");
        }
        return aircraftComponentDAO.installComponent(aircraftId, componentId, installedDate, installHours, installCycles, reason);
    }

    public void removeComponent(int installationId, LocalDate removedDate, double removalHours,
                                int removalCycles, String reason, String newLocation, String newStatus) throws SQLException {
        if (removedDate == null) removedDate = LocalDate.now();
        if (removalHours < 0 || removalCycles < 0) {
            throw new IllegalArgumentException("Removal flight hours and cycles cannot be negative.");
        }
        aircraftComponentDAO.removeComponent(installationId, removedDate, removalHours, removalCycles, reason, newLocation, newStatus);
    }

    public int getTotalAircraftCount() throws SQLException {
        return aircraftDAO.count();
    }

    public int getTotalComponentCount() throws SQLException {
        return componentDAO.count();
    }
}

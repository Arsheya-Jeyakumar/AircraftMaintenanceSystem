package com.aircraft.service;

import com.aircraft.dao.FailureDAO;
import com.aircraft.dao.InspectionDAO;
import com.aircraft.dao.MaintenanceDAO;
import com.aircraft.dao.TechnicianDAO;
import com.aircraft.model.Failure;
import com.aircraft.model.Inspection;
import com.aircraft.model.Maintenance;
import com.aircraft.model.Technician;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceService {

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final InspectionDAO inspectionDAO = new InspectionDAO();
    private final FailureDAO failureDAO = new FailureDAO();
    private final TechnicianDAO technicianDAO = new TechnicianDAO();

    public List<Maintenance> getAllMaintenance() throws SQLException {
        return maintenanceDAO.findAll();
    }

    public List<Maintenance> getOverdueMaintenance() throws SQLException {
        return maintenanceDAO.findOverdue();
    }

    public void scheduleMaintenance(Maintenance m) throws SQLException {
        if (m.getScheduledDate() == null) {
            throw new IllegalArgumentException("Scheduled date is required.");
        }
        if (m.getMaintenanceType() == null || m.getMaintenanceType().trim().isEmpty()) {
            throw new IllegalArgumentException("Maintenance type is required.");
        }
        maintenanceDAO.insert(m);
    }

    public void completeMaintenance(int maintenanceId, LocalDate completionDate) throws SQLException {
        maintenanceDAO.completeMaintenance(maintenanceId, completionDate != null ? completionDate : LocalDate.now());
    }

    public List<Inspection> getAllInspections() throws SQLException {
        return inspectionDAO.findAll();
    }

    public void recordInspection(Inspection i) throws SQLException {
        if (i.getInspectionDate() == null) {
            i.setInspectionDate(LocalDate.now());
        }
        inspectionDAO.insert(i);
    }

    public List<Failure> getAllFailures() throws SQLException {
        return failureDAO.findAll();
    }

    public List<Failure> getRecurringFailures() throws SQLException {
        return failureDAO.findRecurring();
    }

    public void logFailure(Failure f) throws SQLException {
        if (f.getFailureDate() == null) {
            f.setFailureDate(LocalDate.now());
        }
        if (f.getFailureType() == null || f.getFailureType().trim().isEmpty()) {
            throw new IllegalArgumentException("Failure type is required.");
        }
        failureDAO.insert(f);
    }

    public void resolveFailure(int failureId, boolean resolved) throws SQLException {
        failureDAO.updateResolution(failureId, resolved);
    }

    public List<Technician> getAllTechnicians() throws SQLException {
        return technicianDAO.findAll();
    }

    public List<Technician> getActiveTechnicians() throws SQLException {
        return technicianDAO.findActive();
    }

    public void addTechnician(Technician t) throws SQLException {
        technicianDAO.insert(t);
    }

    public int getOverdueCount() throws SQLException {
        return maintenanceDAO.countOverdue();
    }
}

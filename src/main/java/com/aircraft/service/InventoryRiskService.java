package com.aircraft.service;

import com.aircraft.dao.AlertDAO;
import com.aircraft.dao.InventoryDAO;
import com.aircraft.dao.RiskDAO;
import com.aircraft.dao.SparePartDAO;
import com.aircraft.model.Alert;
import com.aircraft.model.Inventory;
import com.aircraft.model.RiskSummary;
import com.aircraft.model.SparePart;

import java.sql.SQLException;
import java.util.List;

public class InventoryRiskService {

    private final SparePartDAO sparePartDAO = new SparePartDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final AlertDAO alertDAO = new AlertDAO();
    private final RiskDAO riskDAO = new RiskDAO();

    public List<SparePart> getAllSpareParts() throws SQLException {
        return sparePartDAO.findAll();
    }

    public List<SparePart> getLowStockParts() throws SQLException {
        return sparePartDAO.findLowStock();
    }

    public void addSparePart(SparePart sp) throws SQLException {
        if (sp.getPartNumber() == null || sp.getPartNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Part number cannot be empty.");
        }
        sparePartDAO.insert(sp);
    }

    public void updateSparePart(SparePart sp) throws SQLException {
        sparePartDAO.update(sp);
    }

    public List<Inventory> getAllInventory() throws SQLException {
        return inventoryDAO.findAll();
    }

    public void addInventoryRecord(Inventory inv) throws SQLException {
        inventoryDAO.insert(inv);
    }

    public void updateStockQuantity(int inventoryId, int newQuantity) throws SQLException {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        inventoryDAO.updateQuantity(inventoryId, newQuantity);
    }

    public List<Alert> getAllAlerts() throws SQLException {
        return alertDAO.findAll();
    }

    public List<Alert> getOpenAlerts() throws SQLException {
        return alertDAO.findOpen();
    }

    public void updateAlertStatus(int alertId, String newStatus) throws SQLException {
        alertDAO.updateStatus(alertId, newStatus);
    }

    public void createAlert(Alert al) throws SQLException {
        alertDAO.insert(al);
    }

    public List<RiskSummary> getRiskIntelligenceSummary() throws SQLException {
        return riskDAO.getRiskIntelligenceSummary();
    }

    public RiskSummary calculateRiskForComponent(int componentId) throws SQLException {
        return riskDAO.calculateComponentRisk(componentId);
    }

    public int getLowStockCount() throws SQLException {
        return sparePartDAO.countLowStock();
    }

    public int getOpenAlertsCount() throws SQLException {
        return alertDAO.countOpen();
    }

    public int getHighRiskCount() throws SQLException {
        return riskDAO.countHighRisk();
    }
}

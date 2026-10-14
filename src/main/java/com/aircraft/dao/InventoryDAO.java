package com.aircraft.dao;

import com.aircraft.model.Inventory;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<Inventory> findAll() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT i.*, sp.part_number, sp.part_name, sp.component_type, sp.minimum_stock "
                   + "FROM INVENTORY i "
                   + "JOIN SPARE_PART sp ON i.part_id = sp.part_id "
                   + "ORDER BY sp.part_number, i.location";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setInventoryId(rs.getInt("inventory_id"));
                inv.setPartId(rs.getInt("part_id"));
                inv.setLocation(rs.getString("location"));
                inv.setQuantityAvailable(rs.getInt("quantity_available"));
                Timestamp ts = rs.getTimestamp("last_updated");
                inv.setLastUpdated(ts != null ? ts.toLocalDateTime() : null);
                inv.setPartNumber(rs.getString("part_number"));
                inv.setPartName(rs.getString("part_name"));
                inv.setComponentType(rs.getString("component_type"));
                inv.setMinimumStock(rs.getInt("minimum_stock"));
                list.add(inv);
            }
        }
        return list;
    }

    public void insert(Inventory inv) throws SQLException {
        String sql = "INSERT INTO INVENTORY (part_id, location, quantity_available) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getPartId());
            ps.setString(2, inv.getLocation());
            ps.setInt(3, inv.getQuantityAvailable());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    inv.setInventoryId(gk.getInt(1));
                }
            }
        }
    }

    public void updateQuantity(int inventoryId, int newQuantity) throws SQLException {
        String sql = "UPDATE INVENTORY SET quantity_available = ? WHERE inventory_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, inventoryId);
            ps.executeUpdate();
        }
    }
}

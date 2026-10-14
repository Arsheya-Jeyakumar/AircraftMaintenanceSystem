package com.aircraft.dao;

import com.aircraft.model.SparePart;
import com.aircraft.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SparePartDAO {

    public List<SparePart> findAll() throws SQLException {
        List<SparePart> list = new ArrayList<>();
        String sql = "SELECT sp.*, COALESCE(SUM(i.quantity_available), 0) AS total_quantity_available "
                   + "FROM SPARE_PART sp "
                   + "LEFT JOIN INVENTORY i ON sp.part_id = i.part_id "
                   + "GROUP BY sp.part_id, sp.part_number, sp.part_name, sp.component_type, sp.supplier, sp.unit_cost, sp.minimum_stock "
                   + "ORDER BY sp.part_number";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SparePart sp = new SparePart();
                sp.setPartId(rs.getInt("part_id"));
                sp.setPartNumber(rs.getString("part_number"));
                sp.setPartName(rs.getString("part_name"));
                sp.setComponentType(rs.getString("component_type"));
                sp.setSupplier(rs.getString("supplier"));
                sp.setUnitCost(rs.getDouble("unit_cost"));
                sp.setMinimumStock(rs.getInt("minimum_stock"));
                sp.setTotalQuantityAvailable(rs.getInt("total_quantity_available"));
                sp.setStockDeficit(Math.max(0, sp.getMinimumStock() - sp.getTotalQuantityAvailable()));
                list.add(sp);
            }
        }
        return list;
    }

    public List<SparePart> findLowStock() throws SQLException {
        List<SparePart> list = new ArrayList<>();
        String sql = "SELECT * FROM v_low_stock_parts ORDER BY stock_deficit DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SparePart sp = new SparePart();
                sp.setPartId(rs.getInt("part_id"));
                sp.setPartNumber(rs.getString("part_number"));
                sp.setPartName(rs.getString("part_name"));
                sp.setComponentType(rs.getString("component_type"));
                sp.setSupplier(rs.getString("supplier"));
                sp.setUnitCost(rs.getDouble("unit_cost"));
                sp.setMinimumStock(rs.getInt("minimum_stock"));
                sp.setTotalQuantityAvailable(rs.getInt("total_quantity_available"));
                sp.setStockDeficit(rs.getInt("stock_deficit"));
                sp.setLocationBreakdown(rs.getString("location_breakdown"));
                list.add(sp);
            }
        }
        return list;
    }

    public void insert(SparePart sp) throws SQLException {
        String sql = "INSERT INTO SPARE_PART (part_number, part_name, component_type, supplier, unit_cost, minimum_stock) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sp.getPartNumber());
            ps.setString(2, sp.getPartName());
            ps.setString(3, sp.getComponentType());
            ps.setString(4, sp.getSupplier());
            ps.setDouble(5, sp.getUnitCost());
            ps.setInt(6, sp.getMinimumStock());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    sp.setPartId(gk.getInt(1));
                }
            }
        }
    }

    public void update(SparePart sp) throws SQLException {
        String sql = "UPDATE SPARE_PART SET part_name = ?, component_type = ?, supplier = ?, unit_cost = ?, minimum_stock = ? "
                   + "WHERE part_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getPartName());
            ps.setString(2, sp.getComponentType());
            ps.setString(3, sp.getSupplier());
            ps.setDouble(4, sp.getUnitCost());
            ps.setInt(5, sp.getMinimumStock());
            ps.setInt(6, sp.getPartId());
            ps.executeUpdate();
        }
    }

    public int countLowStock() throws SQLException {
        String sql = "SELECT COUNT(*) FROM v_low_stock_parts";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}

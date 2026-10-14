package com.aircraft.dao;

import com.aircraft.model.RiskSummary;
import com.aircraft.util.DatabaseConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RiskDAO {

    public List<RiskSummary> getRiskIntelligenceSummary() throws SQLException {
        List<RiskSummary> list = new ArrayList<>();
        String sql = "SELECT * FROM v_risk_intelligence_summary ORDER BY risk_score DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RiskSummary r = new RiskSummary();
                r.setComponentId(rs.getInt("component_id"));
                r.setSerialNumber(rs.getString("serial_number"));
                r.setComponentType(rs.getString("component_type"));
                r.setCurrentStatus(rs.getString("current_status"));
                r.setCurrentLocation(rs.getString("current_location"));
                r.setInstalledAircraft(rs.getString("installed_aircraft"));
                double slh = rs.getDouble("service_limit_hours");
                r.setServiceLimitHours(rs.wasNull() ? null : slh);
                r.setLifetimeHoursUsed(rs.getDouble("lifetime_hours_used"));
                int slc = rs.getInt("service_limit_cycles");
                r.setServiceLimitCycles(rs.wasNull() ? null : slc);
                r.setLifetimeCyclesUsed(rs.getInt("lifetime_cycles_used"));
                r.setServiceLimitPts(rs.getInt("service_limit_pts"));
                r.setOverdueMaintPts(rs.getInt("overdue_maint_pts"));
                r.setFailurePts(rs.getInt("failure_pts"));
                r.setLowStockPts(rs.getInt("low_stock_pts"));
                r.setRiskScore(rs.getInt("risk_score"));
                r.setRiskLevel(rs.getString("risk_level"));
                r.setRiskExplanation(rs.getString("risk_explanation"));
                list.add(r);
            }
        }
        return list;
    }

    public RiskSummary calculateComponentRisk(int componentId) throws SQLException {
        String callSql = "{CALL sp_generate_risk_score(?, ?, ?, ?)}";
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, componentId);
            cs.registerOutParameter(2, java.sql.Types.INTEGER);
            cs.registerOutParameter(3, java.sql.Types.VARCHAR);
            cs.registerOutParameter(4, java.sql.Types.VARCHAR);
            cs.execute();

            RiskSummary r = new RiskSummary();
            r.setComponentId(componentId);
            r.setRiskScore(cs.getInt(2));
            r.setRiskLevel(cs.getString(3));
            r.setRiskExplanation(cs.getString(4));
            return r;
        }
    }

    public int countHighRisk() throws SQLException {
        String sql = "SELECT COUNT(*) FROM v_risk_intelligence_summary WHERE risk_score >= 60";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}

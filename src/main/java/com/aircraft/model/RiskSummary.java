package com.aircraft.model;

public class RiskSummary {
    private int componentId;
    private String serialNumber;
    private String componentType;
    private String currentStatus;
    private String currentLocation;
    private String installedAircraft;
    private Double serviceLimitHours;
    private double lifetimeHoursUsed;
    private Integer serviceLimitCycles;
    private int lifetimeCyclesUsed;
    private int serviceLimitPts;
    private int overdueMaintPts;
    private int failurePts;
    private int lowStockPts;
    private int riskScore;
    private String riskLevel;
    private String riskExplanation;

    public RiskSummary() {}

    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getInstalledAircraft() { return installedAircraft; }
    public void setInstalledAircraft(String installedAircraft) { this.installedAircraft = installedAircraft; }

    public Double getServiceLimitHours() { return serviceLimitHours; }
    public void setServiceLimitHours(Double serviceLimitHours) { this.serviceLimitHours = serviceLimitHours; }

    public double getLifetimeHoursUsed() { return lifetimeHoursUsed; }
    public void setLifetimeHoursUsed(double lifetimeHoursUsed) { this.lifetimeHoursUsed = lifetimeHoursUsed; }

    public Integer getServiceLimitCycles() { return serviceLimitCycles; }
    public void setServiceLimitCycles(Integer serviceLimitCycles) { this.serviceLimitCycles = serviceLimitCycles; }

    public int getLifetimeCyclesUsed() { return lifetimeCyclesUsed; }
    public void setLifetimeCyclesUsed(int lifetimeCyclesUsed) { this.lifetimeCyclesUsed = lifetimeCyclesUsed; }

    public int getServiceLimitPts() { return serviceLimitPts; }
    public void setServiceLimitPts(int serviceLimitPts) { this.serviceLimitPts = serviceLimitPts; }

    public int getOverdueMaintPts() { return overdueMaintPts; }
    public void setOverdueMaintPts(int overdueMaintPts) { this.overdueMaintPts = overdueMaintPts; }

    public int getFailurePts() { return failurePts; }
    public void setFailurePts(int failurePts) { this.failurePts = failurePts; }

    public int getLowStockPts() { return lowStockPts; }
    public void setLowStockPts(int lowStockPts) { this.lowStockPts = lowStockPts; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskExplanation() { return riskExplanation; }
    public void setRiskExplanation(String riskExplanation) { this.riskExplanation = riskExplanation; }
}

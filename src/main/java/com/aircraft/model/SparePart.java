package com.aircraft.model;

public class SparePart {
    private int partId;
    private String partNumber;
    private String partName;
    private String componentType;
    private String supplier;
    private double unitCost;
    private int minimumStock;

    // Joined inventory aggregate fields
    private int totalQuantityAvailable;
    private int stockDeficit;
    private String locationBreakdown;

    public SparePart() {}

    public int getPartId() { return partId; }
    public void setPartId(int partId) { this.partId = partId; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public int getTotalQuantityAvailable() { return totalQuantityAvailable; }
    public void setTotalQuantityAvailable(int totalQuantityAvailable) { this.totalQuantityAvailable = totalQuantityAvailable; }

    public int getStockDeficit() { return stockDeficit; }
    public void setStockDeficit(int stockDeficit) { this.stockDeficit = stockDeficit; }

    public String getLocationBreakdown() { return locationBreakdown; }
    public void setLocationBreakdown(String locationBreakdown) { this.locationBreakdown = locationBreakdown; }

    @Override
    public String toString() {
        return partNumber + " - " + partName;
    }
}

package com.aircraft.model;

public class Component {
    private int componentId;
    private String serialNumber;
    private String componentType;
    private String manufacturer;
    private String model;
    private Double serviceLimitHours;
    private Integer serviceLimitCycles;
    private String currentStatus;
    private String currentLocation;

    public Component() {}

    public Component(int componentId, String serialNumber, String componentType, String manufacturer,
                     String model, Double serviceLimitHours, Integer serviceLimitCycles,
                     String currentStatus, String currentLocation) {
        this.componentId = componentId;
        this.serialNumber = serialNumber;
        this.componentType = componentType;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serviceLimitHours = serviceLimitHours;
        this.serviceLimitCycles = serviceLimitCycles;
        this.currentStatus = currentStatus;
        this.currentLocation = currentLocation;
    }

    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getServiceLimitHours() { return serviceLimitHours; }
    public void setServiceLimitHours(Double serviceLimitHours) { this.serviceLimitHours = serviceLimitHours; }

    public Integer getServiceLimitCycles() { return serviceLimitCycles; }
    public void setServiceLimitCycles(Integer serviceLimitCycles) { this.serviceLimitCycles = serviceLimitCycles; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    @Override
    public String toString() {
        return serialNumber + " - " + componentType + " (" + currentStatus + ")";
    }
}

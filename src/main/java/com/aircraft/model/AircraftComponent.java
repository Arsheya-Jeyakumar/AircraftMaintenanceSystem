package com.aircraft.model;

import java.time.LocalDate;

public class AircraftComponent {
    private int installationId;
    private int aircraftId;
    private int componentId;
    private LocalDate installedDate;
    private LocalDate removedDate;
    private double installationHours;
    private Double removalHours;
    private int installationCycles;
    private Integer removalCycles;
    private String installationReason;
    private String removalReason;

    // Joined reporting fields
    private String aircraftRegistration;
    private String aircraftModel;
    private String componentSerial;
    private String componentType;
    private String componentManufacturer;
    private double operationalHoursLogged;
    private int operationalCyclesLogged;
    private String lifecycleState;

    public AircraftComponent() {}

    public int getInstallationId() { return installationId; }
    public void setInstallationId(int installationId) { this.installationId = installationId; }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }

    public LocalDate getInstalledDate() { return installedDate; }
    public void setInstalledDate(LocalDate installedDate) { this.installedDate = installedDate; }

    public LocalDate getRemovedDate() { return removedDate; }
    public void setRemovedDate(LocalDate removedDate) { this.removedDate = removedDate; }

    public double getInstallationHours() { return installationHours; }
    public void setInstallationHours(double installationHours) { this.installationHours = installationHours; }

    public Double getRemovalHours() { return removalHours; }
    public void setRemovalHours(Double removalHours) { this.removalHours = removalHours; }

    public int getInstallationCycles() { return installationCycles; }
    public void setInstallationCycles(int installationCycles) { this.installationCycles = installationCycles; }

    public Integer getRemovalCycles() { return removalCycles; }
    public void setRemovalCycles(Integer removalCycles) { this.removalCycles = removalCycles; }

    public String getInstallationReason() { return installationReason; }
    public void setInstallationReason(String installationReason) { this.installationReason = installationReason; }

    public String getRemovalReason() { return removalReason; }
    public void setRemovalReason(String removalReason) { this.removalReason = removalReason; }

    public String getAircraftRegistration() { return aircraftRegistration; }
    public void setAircraftRegistration(String aircraftRegistration) { this.aircraftRegistration = aircraftRegistration; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public String getComponentSerial() { return componentSerial; }
    public void setComponentSerial(String componentSerial) { this.componentSerial = componentSerial; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getComponentManufacturer() { return componentManufacturer; }
    public void setComponentManufacturer(String componentManufacturer) { this.componentManufacturer = componentManufacturer; }

    public double getOperationalHoursLogged() { return operationalHoursLogged; }
    public void setOperationalHoursLogged(double operationalHoursLogged) { this.operationalHoursLogged = operationalHoursLogged; }

    public int getOperationalCyclesLogged() { return operationalCyclesLogged; }
    public void setOperationalCyclesLogged(int operationalCyclesLogged) { this.operationalCyclesLogged = operationalCyclesLogged; }

    public String getLifecycleState() { return lifecycleState; }
    public void setLifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; }
}

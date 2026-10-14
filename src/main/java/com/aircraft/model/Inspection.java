package com.aircraft.model;

import java.time.LocalDate;

public class Inspection {
    private int inspectionId;
    private int aircraftId;
    private Integer componentId;
    private LocalDate inspectionDate;
    private String inspectionType;
    private String result;
    private String findings;
    private int inspectorId;

    // Joined reporting fields
    private String aircraftRegistration;
    private String componentSerial;
    private String inspectorName;

    public Inspection() {}

    public int getInspectionId() { return inspectionId; }
    public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public Integer getComponentId() { return componentId; }
    public void setComponentId(Integer componentId) { this.componentId = componentId; }

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public String getInspectionType() { return inspectionType; }
    public void setInspectionType(String inspectionType) { this.inspectionType = inspectionType; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }

    public int getInspectorId() { return inspectorId; }
    public void setInspectorId(int inspectorId) { this.inspectorId = inspectorId; }

    public String getAircraftRegistration() { return aircraftRegistration; }
    public void setAircraftRegistration(String aircraftRegistration) { this.aircraftRegistration = aircraftRegistration; }

    public String getComponentSerial() { return componentSerial; }
    public void setComponentSerial(String componentSerial) { this.componentSerial = componentSerial; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
}

package com.aircraft.model;

import java.time.LocalDate;

public class Aircraft {
    private int aircraftId;
    private String registrationNo;
    private String model;
    private String manufacturer;
    private LocalDate manufactureDate;
    private double totalFlightHours;
    private int totalCycles;
    private String status;

    public Aircraft() {}

    public Aircraft(int aircraftId, String registrationNo, String model, String manufacturer,
                    LocalDate manufactureDate, double totalFlightHours, int totalCycles, String status) {
        this.aircraftId = aircraftId;
        this.registrationNo = registrationNo;
        this.model = model;
        this.manufacturer = manufacturer;
        this.manufactureDate = manufactureDate;
        this.totalFlightHours = totalFlightHours;
        this.totalCycles = totalCycles;
        this.status = status;
    }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public LocalDate getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }

    public double getTotalFlightHours() { return totalFlightHours; }
    public void setTotalFlightHours(double totalFlightHours) { this.totalFlightHours = totalFlightHours; }

    public int getTotalCycles() { return totalCycles; }
    public void setTotalCycles(int totalCycles) { this.totalCycles = totalCycles; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return registrationNo + " (" + model + ")";
    }
}

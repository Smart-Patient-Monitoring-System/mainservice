package com.example.mainservice.dto;

public class AdminDashboardDTO{
    private long doctorCount;
    private long specialdoctorCount;
    private long patientCount;

    public AdminDashboardDTO(long doctorCount, long patientCount, long specialdoctorCount) {
        this.doctorCount = doctorCount;

        this.patientCount = patientCount;

        this.specialdoctorCount = specialdoctorCount;
    }

    public long getDoctorCount() {
        return doctorCount;
    }

    public long getPatientCount() {
        return patientCount;
    }

    public long getSpecialDoctorCount() {
        return specialdoctorCount;
    }


}

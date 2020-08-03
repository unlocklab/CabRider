package com.example.cabbooking.rider.dto;

public class JoinDto {
    private String Join_id;
    private String rider_id;
    private String driver_id;
    private String trip_id;
    private String status;
    private long datetime;

    public JoinDto() {
    }

    public JoinDto(String join_id, String rider_id, String driver_id, String trip_id, String status, long datetime) {
        Join_id = join_id;
        this.rider_id = rider_id;
        this.driver_id = driver_id;
        this.trip_id = trip_id;
        this.status = status;
        this.datetime = datetime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJoin_id() {
        return Join_id;
    }

    public void setJoin_id(String join_id) {
        Join_id = join_id;
    }

    public String getRider_id() {
        return rider_id;
    }

    public void setRider_id(String rider_id) {
        this.rider_id = rider_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}

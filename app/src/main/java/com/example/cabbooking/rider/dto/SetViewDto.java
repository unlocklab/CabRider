package com.example.cabbooking.rider.dto;
/*
 * normal pojo classes with cunstructors and setter/getter
 * for Settings Screen
 * */
public class SetViewDto {
    private int rtype;
    private String value;

    public SetViewDto() {
    }

    public SetViewDto(int rtype, String value) {
        this.rtype = rtype;
        this.value = value;
    }

    public int getRtype() {
        return rtype;
    }

    public void setRtype(int rtype) {
        this.rtype = rtype;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

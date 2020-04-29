package com.example.cabbooking.rider.dto;

public class PriceDto {

    private String discount;
    private String price_id;
    private String price_pkm;

    public PriceDto() {
    }

    public PriceDto(String discount, String price_id, String price_pkm) {
        this.discount = discount;
        this.price_id = price_id;
        this.price_pkm = price_pkm;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getPrice_id() {
        return price_id;
    }

    public void setPrice_id(String price_id) {
        this.price_id = price_id;
    }

    public String getPrice_pkm() {
        return price_pkm;
    }

    public void setPrice_pkm(String price_pkm) {
        this.price_pkm = price_pkm;
    }
}

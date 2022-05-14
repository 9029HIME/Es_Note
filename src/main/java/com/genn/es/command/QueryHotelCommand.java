package com.genn.es.command;

import com.genn.es.common.CommonEsQuery;
import com.genn.es.enums.*;

public class QueryHotelCommand implements CommonEsQuery {
    @Match
    private String brandAndName;
    @Term
    private String city;
    @Term
    private String brand;
    @Term
    private String starName;
    @RangeFrom
    private String priceFrom;
    @RangeTo
    private String priceTo;
    @Location
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBrandAndName() {
        return brandAndName;
    }

    public void setBrandAndName(String brandAndName) {
        this.brandAndName = brandAndName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(String priceFrom) {
        this.priceFrom = priceFrom;
    }

    public String getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(String priceTo) {
        this.priceTo = priceTo;
    }
}

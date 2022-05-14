package com.genn.es.es;

import com.genn.es.common.GeoResult;
import com.genn.es.entity.Hotel;

public class HotelEsDTO extends GeoResult {
    private HotelEsDTO(){}

    private Long id;
    private String name;
    private String address;
    private String price;
    private String score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String location;
    private String pic;
    private String advertiseAmount;


    public static HotelEsDTO getEsDto(Hotel hotel){
        HotelEsDTO esDTO = new HotelEsDTO();
        esDTO.setName(hotel.getName());
        esDTO.setAddress(hotel.getAddress());
        esDTO.setPrice(hotel.getPrice());
        esDTO.setScore(hotel.getScore());
        esDTO.setId(hotel.getId());
        esDTO.setBrand(hotel.getBrand());
        esDTO.setCity(hotel.getCity());
        esDTO.setStarName(hotel.getStarName());
        esDTO.setBusiness(hotel.getBusiness());
        esDTO.setLocation(String.format("%s,%s",hotel.getLatitude(), hotel.getLongitude()));
        esDTO.setId(hotel.getId());
        return esDTO;
    }

    public String getAdvertiseAmount() {
        return advertiseAmount;
    }

    public void setAdvertiseAmount(String advertiseAmount) {
        this.advertiseAmount = advertiseAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}

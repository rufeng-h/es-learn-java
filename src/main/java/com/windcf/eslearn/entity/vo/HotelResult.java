package com.windcf.eslearn.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.windcf.eslearn.entity.repository.HotelDoc;
import lombok.Data;

/**
 * @author chunf
 */
@Data
public class HotelResult {
    /**
     * 酒店id
     */
    private Long id;

    /**
     * 酒店名称
     */
    private String name;

    /**
     * 酒店地址
     */
    private String address;

    /**
     * 酒店价格
     */
    private Integer price;

    /**
     * 酒店评分
     */
    private Integer score;

    /**
     * 酒店品牌
     */
    private String brand;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 酒店星级，1星到5星，1钻到5钻
     */
    private String starName;

    /**
     * 商圈
     */
    private String business;

    private String location;
    /**
     * 酒店图片
     */
    private String pic;

    private Double distance;

    @JsonProperty("isAD")
    private Boolean ad;

    public HotelResult(HotelDoc hotel, Double distance) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLocation();
        this.pic = hotel.getPic();
        this.distance = distance;
        this.ad = hotel.getAd();
    }
}

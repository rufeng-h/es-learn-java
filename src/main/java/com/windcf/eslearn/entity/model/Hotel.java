package com.windcf.eslearn.entity.model;

import java.io.Serializable;

import com.windcf.eslearn.entity.repository.HotelDoc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chunf
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hotel implements Serializable {
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

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 酒店图片
     */
    private String pic;

    public Hotel(HotelDoc hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        String[] arr = hotel.getLocation().split(", ");
        this.latitude = arr[0];
        this.longitude = arr[1];
        this.pic = hotel.getPic();
    }

    private static final long serialVersionUID = 1L;
}
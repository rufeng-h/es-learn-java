package com.windcf.eslearn.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

/**
 * @author chunf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "hotel")
public class HotelDoc {
    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", copyTo = "all")
    private String name;
    @Field(type = FieldType.Keyword, index = false)
    private String address;
    @Field(type = FieldType.Integer)
    private Integer price;
    @Field(type = FieldType.Integer)
    private Integer score;
    @Field(type = FieldType.Keyword, copyTo = "all")
    private String brand;
    @Field(type = FieldType.Keyword, copyTo = "all")
    private String city;
    @Field(type = FieldType.Keyword)
    private String starName;
    @Field(type = FieldType.Keyword)
    private String business;
    @GeoPointField
    private String location;
    @Field(type = FieldType.Keyword, index = false)
    private String pic;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
    }
}
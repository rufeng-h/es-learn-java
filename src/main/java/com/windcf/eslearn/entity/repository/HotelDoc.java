package com.windcf.eslearn.entity.repository;

import com.windcf.eslearn.entity.model.Hotel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author chunf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "hotel", writeTypeHint = WriteTypeHint.FALSE)
@Setting(settingPath = "/elasticsearch/hotel-settings.json")
public class HotelDoc {
    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "text_analyzer", searchAnalyzer = "ik_smart", copyTo = "all")
    private String name;
    @Field(type = FieldType.Keyword, index = false)
    private String address;
    @Field(type = FieldType.Integer)
    private Integer price;
    @Field(type = FieldType.Integer)
    private Integer score;
    @Field(type = FieldType.Keyword, copyTo = "all")
    private String brand;
    @Field(type = FieldType.Keyword)
    private String city;
    @Field(type = FieldType.Keyword)
    private String starName;
    @Field(type = FieldType.Keyword, copyTo = "all")
    private String business;
    @GeoPointField
    private String location;
    @Field(type = FieldType.Keyword, index = false)
    private String pic;

    @Field(type = FieldType.Boolean)
    private Boolean ad;

    @NonNull
    @CompletionField(analyzer = "completion_analyzer")
    private final Completion completion = new Completion();

    /**
     * 由其他属性copy而来，主要用于搜索功能，不需要储存数据
     */
    @Null
    @Nullable
    @Field(type = FieldType.Text, analyzer = "text_analyzer", searchAnalyzer = "ik_smart")
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final String all = null;

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
        this.ad = this.id % 3 == 0;

        List<String> input = new ArrayList<>();
        input.add(this.name);
        input.add(this.brand);
        if (this.business.contains("/")) {
            input.addAll(Arrays.asList(this.business.split("/")));
        } else {
            input.add(this.business);
        }
        this.completion.setInput(input.toArray(new String[0]));
    }
}
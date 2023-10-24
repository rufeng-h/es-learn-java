package com.windcf.eslearn.entity.param;

import com.windcf.eslearn.validation.ValidString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author chunf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchParam {
    @NotNull
    private String key;
    private Integer page = 1;
    private Integer size = 10;
    private Integer maxPrice;
    private Integer minPrice;
    @ValidString
    private String city;
    @ValidString
    private String brand;
    @ValidString
    private String location;
    private String sortBy = "default";
}

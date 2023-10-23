package com.windcf.eslearn.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * @author chunf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private long total;
    private Collection<HotelResult> hotels;
}

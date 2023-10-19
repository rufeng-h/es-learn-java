package com.windcf.eslearn.repository;

import com.windcf.eslearn.domain.HotelDoc;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * @author : chunf
 */
@org.springframework.stereotype.Repository
public interface HotelRepository extends Repository<HotelDoc, String> {
    List<HotelDoc> findByBrandAndScoreGreaterThanEqual(String brand, Integer score);

}

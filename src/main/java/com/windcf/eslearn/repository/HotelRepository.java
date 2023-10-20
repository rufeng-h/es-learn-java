package com.windcf.eslearn.repository;

import com.windcf.eslearn.domain.HotelDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : chunf
 */
@Repository
public interface HotelRepository extends ElasticsearchRepository<HotelDoc, String> {
    List<HotelDoc> findByBrandAndScoreGreaterThanEqual(String brand, Integer score);

    List<HotelDoc> findByAll(String value);

    List<HotelDoc> findByName(String name);
}

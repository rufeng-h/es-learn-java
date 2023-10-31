package com.windcf.eslearn.repository;

import com.windcf.eslearn.entity.repository.HotelDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    Page<HotelDoc> findByAll(String key, Pageable pageable);

    @NonNull
    Page<HotelDoc> findByAllAndBrandAndCityAndStarNameAndPriceBetween(@Nullable String all,
                                                                      @Nullable String brand,
                                                                      @Nullable String city,
                                                                      @Nullable String starName,
                                                                      @Nullable Integer minPrice,
                                                                      @Nullable Integer maxPrice,
                                                                      @NonNull Pageable pageable);
}

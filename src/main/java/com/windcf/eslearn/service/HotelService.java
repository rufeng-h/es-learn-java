package com.windcf.eslearn.service;

import com.windcf.eslearn.entity.model.Hotel;
import com.windcf.eslearn.entity.param.SearchParam;
import com.windcf.eslearn.entity.vo.SearchResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author chunf
 */
public interface HotelService {
    Hotel get(long id);

    List<Hotel> list();

    Integer loadEs();

    SearchResult search(SearchParam param);

    boolean delIndex(String index);

    Map<String, List<String>> filter(SearchParam param);

    List<String> suggestion(String key);
}

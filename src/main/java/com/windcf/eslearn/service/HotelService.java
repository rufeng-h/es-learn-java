package com.windcf.eslearn.service;

import com.windcf.eslearn.entity.model.Hotel;
import com.windcf.eslearn.entity.param.SearchParam;
import com.windcf.eslearn.entity.vo.SearchResult;

import java.util.List;

/**
 * @author chunf
 */
public interface HotelService {
    Hotel get(long id);

    List<Hotel> list();

    Integer loadEs();

    SearchResult search(SearchParam param);
}

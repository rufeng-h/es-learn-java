package com.windcf.eslearn.service;

import com.windcf.eslearn.domain.Hotel;

import java.util.List;

/**
 * @author chunf
 */
public interface HotelService {
    Hotel get(long id);

    List<Hotel> list();
}

package com.windcf.eslearn.service.impl;

import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.mapper.HotelMapper;
import com.windcf.eslearn.service.HotelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chunf
 */
@Service
public class HotelServiceImpl implements HotelService {
    private final HotelMapper hotelMapper;

    public HotelServiceImpl(HotelMapper hotelMapper) {
        this.hotelMapper = hotelMapper;
    }

    @Override
    public Hotel get(long id) {
        return hotelMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Hotel> list() {
        return hotelMapper.list();
    }
}

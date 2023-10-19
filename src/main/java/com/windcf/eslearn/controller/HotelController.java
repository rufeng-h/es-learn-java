package com.windcf.eslearn.controller;

import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.service.HotelService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * @author chunf
 */
@RestController
@Validated
public class HotelController {
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/hotel")
    public Hotel get(@NotNull Long id) {
        return hotelService.get(id);
    }
}

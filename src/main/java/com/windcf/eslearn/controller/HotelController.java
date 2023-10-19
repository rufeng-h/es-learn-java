package com.windcf.eslearn.controller;

import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.domain.HotelDoc;
import com.windcf.eslearn.repository.HotelRepository;
import com.windcf.eslearn.service.HotelService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author chunf
 */
@RestController
@Validated
public class HotelController {
    private final HotelService hotelService;
    private final HotelRepository hotelRepository;

    public HotelController(HotelService hotelService, HotelRepository hotelRepository) {
        this.hotelService = hotelService;
        this.hotelRepository = hotelRepository;
    }

    @GetMapping("/hotel")
    public Hotel get(@NotNull Long id) {
        return hotelService.get(id);
    }

    @GetMapping("/search")
    public List<HotelDoc> search(@NotNull String brand, @NotNull Integer score) {
        return hotelRepository.findByBrandAndScoreGreaterThanEqual(brand, score);
    }

    @GetMapping("/loadEs")
    public Integer loadEs() {
        return hotelService.loadEs();
    }
}

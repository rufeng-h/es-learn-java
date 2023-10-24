package com.windcf.eslearn.controller;

import com.windcf.eslearn.entity.model.Hotel;
import com.windcf.eslearn.entity.param.SearchParam;
import com.windcf.eslearn.entity.repository.HotelDoc;
import com.windcf.eslearn.entity.vo.SearchResult;
import com.windcf.eslearn.repository.HotelRepository;
import com.windcf.eslearn.service.HotelService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/hotel/list")
    public SearchResult list(@RequestBody @Validated SearchParam param) {
        return hotelService.search(param);
    }

    @PostMapping("/hotel/filters")
    public Map<String, List<String>> filter(@RequestBody @Validated SearchParam param) {
        return hotelService.filter(param);
    }

    @GetMapping("/findByAll")
    public List<HotelDoc> searchAll(@NotEmpty String value) {
        return hotelRepository.findByAll(value);
    }

    @GetMapping("/findByName")
    public List<HotelDoc> findByName(@NotEmpty String name) {
        return hotelRepository.findByName(name);
    }

    @DeleteMapping("/hotel")
    public Boolean delHotelIndex() {
        return hotelService.delIndex("hotel");
    }
}

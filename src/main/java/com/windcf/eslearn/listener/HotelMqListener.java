package com.windcf.eslearn.listener;

import com.windcf.eslearn.constant.HotelMqConstant;
import com.windcf.eslearn.service.HotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author : chunf
 */
@Component
public class HotelMqListener {
    private final HotelService hotelService;

    public HotelMqListener(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @RabbitListener(queues = HotelMqConstant.INSERT_QUEUE_NAME)
    public void insertOrUpdateHotel(Long id) {
        hotelService.updateById(id);
    }

    @RabbitListener(queues = HotelMqConstant.DELETE_QUEUE_NAME)
    public void delHotel(Long id) {
        hotelService.delById(id);
    }
}

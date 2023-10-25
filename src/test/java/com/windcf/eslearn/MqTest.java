package com.windcf.eslearn;

import com.windcf.eslearn.constant.HotelMqConstant;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
public class MqTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Test
    public void delDxlMessage() {
        Message message = rabbitTemplate.receive(HotelMqConstant.DEAD_LETTER_QUEUE_NAME);
        System.out.println(message);
    }
}

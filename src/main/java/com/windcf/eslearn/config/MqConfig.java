package com.windcf.eslearn.config;

import com.windcf.eslearn.constant.HotelMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : chunf
 */
@Configuration
public class MqConfig {
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(HotelMqConstant.EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue insertQueue() {
        Map<String, Object> params = new HashMap<>(2);
        params.put("x-dead-letter-exchange", HotelMqConstant.DEAD_LETTER_EXCHANHE_NAME);
        params.put("x-dead-letter-routing-key", HotelMqConstant.DEAD_LETTER_KEY);
        return new Queue(HotelMqConstant.INSERT_QUEUE_NAME, true, false, false, params);
    }

    @Bean
    public Queue deleteQueue() {
        Map<String, Object> params = new HashMap<>(2);
        params.put("x-dead-letter-exchange", HotelMqConstant.DEAD_LETTER_EXCHANHE_NAME);
        params.put("x-dead-letter-routing-key", HotelMqConstant.DEAD_LETTER_KEY);
        return new Queue(HotelMqConstant.DELETE_QUEUE_NAME, true, false, false, params);
    }

    @Bean
    public Binding insertBinding() {
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(HotelMqConstant.INSERT_KEY);
    }

    @Bean
    public Binding deleteBinding() {
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(HotelMqConstant.DELETE_KEY);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(HotelMqConstant.DEAD_LETTER_EXCHANHE_NAME, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(HotelMqConstant.DEAD_LETTER_QUEUE_NAME, true);
    }

    /**
     * 死信队列绑定死信交换机
     */
    @Bean
    public Binding dlcBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(HotelMqConstant.DEAD_LETTER_KEY);
    }
}

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
    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(HotelMqConstant.DLX_EXCHANGE_NAME, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(HotelMqConstant.DLX_QUEUE_NAME, true, false, false);
    }

    /**
     * 死信队列绑定死信交换机
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(HotelMqConstant.DLX_KEY);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(HotelMqConstant.EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue insertQueue() {
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", HotelMqConstant.DLX_EXCHANGE_NAME);
        params.put("x-dead-letter-routing-key", HotelMqConstant.DLX_KEY);
        return new Queue(HotelMqConstant.INSERT_QUEUE_NAME, true, false, false, params);
    }

    @Bean
    public Queue deleteQueue() {
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", HotelMqConstant.DLX_EXCHANGE_NAME);
        params.put("x-dead-letter-routing-key", HotelMqConstant.DLX_KEY);
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
}

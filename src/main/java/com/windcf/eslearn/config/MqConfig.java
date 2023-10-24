package com.windcf.eslearn.config;

import com.windcf.eslearn.constant.HotelMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return new Queue(HotelMqConstant.INSERT_QUEUE_NAME, true);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(HotelMqConstant.DELETE_QUEUE_NAME, true);
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

package com.windcf.eslearn.constant;

/**
 * @author chunf
 */
public class HotelMqConstant {
    public static final String EXCHANGE_NAME = "hotel.topic";
    public static final String INSERT_QUEUE_NAME = "hotel.insert.queue";
    public static final String DELETE_QUEUE_NAME = "hotel.delete.queue";
    public static final String INSERT_KEY = "hotel.insert";
    public static final String DELETE_KEY = "hotel.delete";
    public static final String DLX_EXCHANGE_NAME = "hotel.dlx";

    public static final String DLX_QUEUE_NAME = "hotel.dlx.queue";
    public static final String DLX_KEY = "hotel.dlx.key";
}

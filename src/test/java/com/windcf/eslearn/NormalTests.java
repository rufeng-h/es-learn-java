package com.windcf.eslearn;


import org.junit.jupiter.api.Test;

import java.util.Base64;

public class NormalTests {
    @Test
    void testBase64() {
        byte[] bytes = Base64.getDecoder().decode("eyJtYXRjaF9hbGwiOnt9fQ==".getBytes());
        String s = new String(bytes);
        System.out.println(s);
    }
}

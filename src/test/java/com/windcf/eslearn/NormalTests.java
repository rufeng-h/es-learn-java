package com.windcf.eslearn;


import org.junit.jupiter.api.Test;

import java.util.Base64;

public class NormalTests {
    @Test
    void testBase64() {
        String b64Id = "rO0ABXNyAA5qYXZhLmxhbmcuTG9uZzuL5JDMjyPfAgABSgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAAAAevFxOg==";
        byte[] bytes = Base64.getDecoder().decode(b64Id.getBytes());
        String s = new String(bytes);
        System.out.println(s);
    }
}

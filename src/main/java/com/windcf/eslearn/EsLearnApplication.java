package com.windcf.eslearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author chunf
 */
@SpringBootApplication
@EnableElasticsearchRepositories
public class EsLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsLearnApplication.class, args);
    }

}

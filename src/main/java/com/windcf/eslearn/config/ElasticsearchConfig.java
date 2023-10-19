package com.windcf.eslearn.config;

import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import java.util.Collections;

/**
 * @author chunf
 */
@Configuration
public class ElasticsearchConfig {
    @Configuration
    static class MyElasticsearchConfiguration extends ElasticsearchConfiguration {
        private final ElasticsearchProperties elasticsearchProperties;

        MyElasticsearchConfiguration(ElasticsearchProperties elasticsearchProperties) {
            this.elasticsearchProperties = elasticsearchProperties;
        }

        @Override
        @NonNull
        public ClientConfiguration clientConfiguration() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.elasticsearch+json;compatible-with=8"));
            headers.setAccept(Collections.singletonList(MediaType.valueOf("application/vnd.elasticsearch+json;compatible-with=8")));
            return ClientConfiguration.builder()
                    .connectedTo("localhost", "9200")
                    .withBasicAuth(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword())
                    .withPathPrefix(elasticsearchProperties.getPathPrefix())
                    .withDefaultHeaders(headers)
                    .build();
        }

    }
}
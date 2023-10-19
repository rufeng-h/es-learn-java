package com.windcf.eslearn.config;

import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

            List<String> uris = elasticsearchProperties.getUris();
            List<String> hostAndPort = new ArrayList<>();
            for (String s : uris) {
                URI uri = URI.create(s);
                hostAndPort.add(uri.getHost());
                hostAndPort.add(String.valueOf(uri.getPort()));
            }
            return ClientConfiguration.builder()
                    .connectedTo(hostAndPort.toArray(new String[0]))
                    .withBasicAuth(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword())
                    .withPathPrefix(elasticsearchProperties.getPathPrefix())
                    .withDefaultHeaders(headers)
                    .build();
        }

    }
}
package com.windcf.eslearn.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.AutoCloseableElasticsearchClient;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chunf
 */

@Configuration
public class MyElasticsearchConfiguration extends ElasticsearchConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    MyElasticsearchConfiguration(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @Override
    @NonNull
    @Bean
    public ClientConfiguration clientConfiguration() {
        List<String> uris = elasticsearchProperties.getUris();
        List<String> hostAndPort = new ArrayList<>();
        for (String s : uris) {
            URI uri = URI.create(s);
            hostAndPort.add(uri.getHost() + ":" + uri.getPort());
        }

        return ClientConfiguration
                .builder()
                .connectedTo(hostAndPort.toArray(new String[0]))
                .withBasicAuth(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword())
                .withPathPrefix(elasticsearchProperties.getPathPrefix())
                .withConnectTimeout(elasticsearchProperties.getConnectionTimeout())
                .withSocketTimeout(elasticsearchProperties.getSocketTimeout())
                .build();
    }

    @Override
    @Bean
    @NonNull
    public ElasticsearchClient elasticsearchClient(@NonNull RestClient restClient) {
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new AutoCloseableElasticsearchClient(transport);
    }

}
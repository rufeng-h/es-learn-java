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
        String[] hostAndPort = elasticsearchProperties.getUris()
                .stream()
                .map(s -> {
                    URI uri = URI.create(s);
                    return uri.getHost() + ":" + uri.getPort();
                }).toArray(String[]::new);

        return ClientConfiguration
                .builder()
                .connectedTo(hostAndPort)
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

    /**
     * not recommanded
     */
    @Override
    protected boolean writeTypeHints() {
        return false;
    }
}
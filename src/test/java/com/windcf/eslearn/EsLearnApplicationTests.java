package com.windcf.eslearn;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.cat.CountRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.domain.HotelDoc;
import com.windcf.eslearn.service.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
@ActiveProfiles("dev")
class EsLearnApplicationTests {
    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private ResourcePatternResolver resourceLoader;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private HotelService hotelService;


    @Test
    void contextLoads() {
    }

    @Test
    void testEs() throws IOException {

//        System.out.println(client.cat().indices().valueBody());
        boolean exists = client.indices().exists(ExistsRequest.of(builder -> builder.index("heima"))).value();
        if (exists) {
            DeleteIndexRequest request = DeleteIndexRequest.of(builder -> builder.index("heima"));
            System.out.println(client.indices().delete(request));
        }
        InputStream inputStream = resourceLoader.getResource("classpath:/hotel.json").getInputStream();
        CreateIndexRequest indexRequest = CreateIndexRequest.of(b -> b.index("hotel").mappings(TypeMapping.of(b1 -> b1.withJson(inputStream))));
        System.out.println(client.indices().create(indexRequest));
    }

    @Test
    void testJson() throws IOException {
        Hotel hotel = hotelService.get(61083);
        System.out.println(hotel);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest<HotelDoc> request = IndexRequest.of(b -> b.index("hotel").id(hotelDoc.getId().toString()).document(hotelDoc));
        System.out.println(client.index(request));
    }

    @Test
    void getDoc() throws IOException {
        System.out.println(client.get(GetRequest.of(b -> b.index("hotel").id("61083")), HotelDoc.class));
    }

    @Test
    void delDoc() throws IOException {
        System.out.println(client.delete(DeleteRequest.of(b -> b.index("hotel").id("61083"))));
    }

    @Test
    void count() throws IOException {
        System.out.println(client.cat().count(CountRequest.of(b -> b.index("hotel"))));
        System.out.println(elasticsearchOperations.count(Query.findAll(), HotelDoc.class));
        System.out.println(elasticsearchOperations.count(Query.findAll(), IndexCoordinates.of("hotel")));
    }

}

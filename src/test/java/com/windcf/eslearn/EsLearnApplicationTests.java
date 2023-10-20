package com.windcf.eslearn;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.domain.HotelDoc;
import com.windcf.eslearn.service.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
class EsLearnApplicationTests {
    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ResourcePatternResolver resourceLoader;

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
    void functionScore() {
        MatchQuery matchQuery = QueryBuilders.match().field("all").query("外滩").build();
        TermQuery termQuery = QueryBuilders.term().field("brand").value("如家").build();
        FunctionScore functionScore = FunctionScore.of(builder -> builder.filter(termQuery._toQuery()).weight(10.));
        FunctionScoreQuery functionScoreQuery = QueryBuilders
                .functionScore()
                .query(matchQuery._toQuery())
                .functions(functionScore)
                .boostMode(FunctionBoostMode.Multiply)
                .build();
        NativeQuery nativeQuery = new NativeQueryBuilder().withQuery(functionScoreQuery._toQuery()).build();
        List<SearchHit<HotelDoc>> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class).getSearchHits();
        for (SearchHit<HotelDoc> searchHit : searchHits) {
            System.out.println(searchHit.getScore());
//            System.out.println(searchHit.getContent());
        }
    }

    @Test
    void boolQuery() {
        List<Query> must = Collections.singletonList(QueryBuilders.match().field("name").query("如家").build()._toQuery());
        List<Query> mustNot = Collections.singletonList(QueryBuilders.range().field("price").gt(JsonData.of(400)).build()._toQuery());
        List<Query> filter = Collections.singletonList(QueryBuilders.geoDistance(
                builder -> builder
                        .distance("10km")
                        .field("location")
                        .location(GeoLocation.of(b -> b.latlon(LatLonGeoLocation.of(f -> f.lat(31.21).lon(121.5)))))));
        BoolQuery boolQuery = QueryBuilders.bool()
                .must(must)
                .mustNot(mustNot)
                .filter(filter)
                .build();
        NativeQuery nativeQuery = new NativeQueryBuilder().withQuery(boolQuery._toQuery()).build();
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);
        for (SearchHit<HotelDoc> searchHit : searchHits) {
            System.out.println(searchHit.getScore());
        }
    }
}

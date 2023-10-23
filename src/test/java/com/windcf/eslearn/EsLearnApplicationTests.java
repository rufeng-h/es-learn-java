package com.windcf.eslearn;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.windcf.eslearn.entity.repository.HotelDoc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@SpringBootTest
@ActiveProfiles("dev")
class EsLearnApplicationTests {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    void sortQueryByEsOperations() {
        Sort sort = Sort.by(Sort.Direction.DESC, "score");
        NativeQuery nativeQuery = new NativeQueryBuilder().withSort(sort)
                .withQuery(QueryBuilders.range(b -> b.field("price").gte(JsonData.of(300))))
                .build();
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);
        for (SearchHit<HotelDoc> searchHit : searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    @Test
    void sortQueryByEsClient() throws IOException {
        Query rangeQuery = QueryBuilders.range().field("price").gte(JsonData.of(300)).build()._toQuery();
        SortOptions sortOptions = SortOptionsBuilders.field(builder -> builder.field("score").order(SortOrder.Desc).mode(SortMode.Min));
        SearchRequest searchRequest = SearchRequest.of(builder -> builder.index("hotel").query(rangeQuery).sort(sortOptions));
        System.out.println(searchRequest.toString());

        SearchResponse<HotelDoc> searchResponse = elasticsearchClient.search(searchRequest, HotelDoc.class);
        for (Hit<HotelDoc> hit : searchResponse.hits().hits()) {
            System.out.println(hit.source());
        }
    }
}

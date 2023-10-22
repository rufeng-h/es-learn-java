package com.windcf.eslearn;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.windcf.eslearn.domain.HotelDoc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : chunf
 */
@SpringBootTest
@ActiveProfiles("dev")
class MyTests {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Test
    void testMatchAll() throws IOException {
        Query matchAllQuery = QueryBuilders.matchAll().build()._toQuery();
        SearchRequest searchRequest = new SearchRequest.Builder().index("hotel")
                .query(matchAllQuery).build();
        SearchResponse<HotelDoc> searchResponse = elasticsearchClient.search(searchRequest, HotelDoc.class);
        for (Hit<HotelDoc> hit : searchResponse.hits().hits()) {
            System.out.println(hit.source());
        }


        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(matchAllQuery)
                .build();
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);
        for (SearchHit<HotelDoc> searchHit : searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    @Test
    void testMatch() throws IOException {
        Query matchQuery = QueryBuilders.match().field("all").query("如家").build()._toQuery();
        SortOptions sortOptions = SortOptionsBuilders.field(builder -> builder.field("score").order(SortOrder.Desc));
        SearchRequest searchRequest = new SearchRequest.Builder().query(matchQuery)
                .index("hotel")
                .sort(sortOptions)
                .from(10)
                .size(5)
                .highlight(builder -> builder.requireFieldMatch(false).fields("brand", b -> b).fields("name", b -> b))
                .build();
        List<Hit<HotelDoc>> hits = elasticsearchClient.search(searchRequest, HotelDoc.class).hits().hits();
        for (Hit<HotelDoc> hit : hits) {
            System.out.println(hit.source());
            Map<String, List<String>> highlight = hit.highlight();
            for (Map.Entry<String, List<String>> entry : highlight.entrySet()) {
                System.out.println(entry.getKey() + " === " + String.join(", ", entry.getValue()));
            }
        }
        ArrayList<HighlightField> highlightFields = new ArrayList<>();
        HighlightField highlightBrand = new HighlightField("brand", HighlightFieldParameters.builder().build());
        HighlightField highlightName = new HighlightField("name", HighlightFieldParameters.builder().build());
        highlightFields.add(highlightName);
        highlightFields.add(highlightBrand);
        HighlightParameters parameters = HighlightParameters.builder().withRequireFieldMatch(false).build();

        NativeQuery nativeQuery = new NativeQueryBuilder().withQuery(matchQuery)
                .withSort(Sort.by(Sort.Direction.DESC, "score"))
                .withPageable(PageRequest.of(2, 5))
                .withHighlightQuery(new HighlightQuery(new Highlight(parameters, highlightFields), HotelDoc.class))
                .build();
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);
        for (SearchHit<HotelDoc> searchHit : searchHits) {
            System.out.println(searchHit.getContent());
            Map<String, List<String>> fields = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
                System.out.println(entry.getKey() + " === " + String.join(", ", entry.getValue()));
            }
        }
    }
}

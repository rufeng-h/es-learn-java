package com.windcf.eslearn;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.windcf.eslearn.entity.param.SearchParam;
import com.windcf.eslearn.entity.repository.HotelDoc;
import com.windcf.eslearn.repository.HotelRepository;
import com.windcf.eslearn.service.HotelService;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
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

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelService hotelService;

    @Test
    void testHotelService() {
        SearchParam param = new SearchParam();
        hotelService.filter(param);
    }

    @Test
    void testCreateIndex() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("hotel");
        Settings settings = elasticsearchOperations.indexOps(indexCoordinates).createSettings(HotelDoc.class);
        Document mapping = elasticsearchOperations.indexOps(indexCoordinates).createMapping(HotelDoc.class);
        elasticsearchOperations.indexOps(indexCoordinates).create(settings, mapping);

    }

    @Test
    void testSuggest() {
        CompletionSuggestionBuilder suggestionBuilder = new CompletionSuggestionBuilder("completion")
                .skipDuplicates(true)
                .size(10)
                .text("ru");
        SuggestBuilder suggestion = new SuggestBuilder().addSuggestion("title", suggestionBuilder);
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withSuggestBuilder(suggestion).build();
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(searchQuery, HotelDoc.class);
    }

    @Test
    void testNewSuggest() throws IOException {
        CompletionSuggester completionSuggester = CompletionSuggester.of(b -> b.field("completion").skipDuplicates(true).size(10));
        Suggester suggester = Suggester.of(b -> b.suggesters("title", builder -> builder.text("ru").completion(completionSuggester)));
        NativeQuery nativeQuery = new NativeQueryBuilder().withSuggester(suggester).build();

        // TODO BUG
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);
        System.out.println(searchHits.getSuggest());

        SearchRequest searchRequest = SearchRequest.of(b -> b.suggest(suggester));
        SearchResponse<HotelDoc> searchResponse = elasticsearchClient.search(searchRequest, HotelDoc.class);
        Map<String, List<Suggestion<HotelDoc>>> suggest = searchResponse.suggest();
        System.out.println(suggest.get("title"));
    }


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

    @Test
    void testSearch() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<HotelDoc> hotelDocs = hotelRepository.findByAllAndBrandAndCityAndStarNameAndPriceBetween(null, "如家", "深圳", null, 100, 300, pageRequest);
        System.out.println(hotelDocs.getTotalElements());
    }

    @Test
    void testBucketAgg() {
        Aggregation terms = AggregationBuilders.terms(builder -> builder.field("brand")
                .order(List.of(NamedValue.of("_count", SortOrder.Asc)))
                .size(20));
        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(QueryBuilders.range(b -> b.field("price").lte(JsonData.of(200))))
                .withAggregation("brandAgg", terms)
                .build();
        nativeQuery.setMaxResults(0);
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);

        @SuppressWarnings("unchecked") AggregationsContainer<List<ElasticsearchAggregation>> aggregations = (AggregationsContainer<List<ElasticsearchAggregation>>) searchHits.getAggregations();
        assert aggregations != null;
        System.out.println(aggregations.getClass());
        for (ElasticsearchAggregation aggregation : aggregations.aggregations()) {
            Aggregate aggregate = aggregation.aggregation().getAggregate();
            List<StringTermsBucket> stringTermsBuckets = aggregate.sterms().buckets().array();
            for (StringTermsBucket bucket : stringTermsBuckets) {
                System.out.println(bucket.key().stringValue() + ": " + bucket.docCount());
            }
        }

    }

    /**
     * 子聚合
     */
    @Test
    void testMetricAgg() {
        Aggregation statsAggregation = AggregationBuilders.stats().field("score").build()._toAggregation();
        Aggregation terms = new Aggregation.Builder().aggregations("scoreStat", statsAggregation).terms(builder -> builder.field("brand")
                .order(List.of(NamedValue.of("_count", SortOrder.Asc)))
                .size(20)).build();

        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withAggregation("brandAgg", terms)
                .build();
        nativeQuery.setMaxResults(0);
        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(nativeQuery, HotelDoc.class);
        @SuppressWarnings("unchecked") AggregationsContainer<List<ElasticsearchAggregation>> aggregations = (AggregationsContainer<List<ElasticsearchAggregation>>) searchHits.getAggregations();
        assert aggregations != null;
        for (ElasticsearchAggregation aggregation : aggregations.aggregations()) {
            StringTermsAggregate sterms = aggregation.aggregation().getAggregate().sterms();
            List<StringTermsBucket> termsBuckets = sterms.buckets().array();
            for (StringTermsBucket bucket : termsBuckets) {
                System.out.println(bucket);
            }
        }
    }

    @Test
    void testAnalyzer() throws IOException {
        AnalyzeRequest analyzeRequest = new AnalyzeRequest.Builder().analyzer("pinyin").text("如家酒店真不错").build();
        AnalyzeResponse analyzeResponse = elasticsearchClient.indices().analyze(analyzeRequest);
        System.out.println(analyzeResponse.tokens());

        ElasticsearchTemplate elasticsearchTemplate = (ElasticsearchTemplate) elasticsearchOperations;
        AnalyzeResponse response = elasticsearchTemplate.execute(client -> client.indices().analyze(analyzeRequest));
        System.out.println(response.tokens());
    }

    @Test
    void testUpdateById() {
        hotelService.updateById(2062643514L);
    }
}

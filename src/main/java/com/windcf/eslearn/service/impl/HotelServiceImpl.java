package com.windcf.eslearn.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import com.windcf.eslearn.entity.model.Hotel;
import com.windcf.eslearn.entity.param.SearchParam;
import com.windcf.eslearn.entity.repository.HotelDoc;
import com.windcf.eslearn.entity.vo.HotelResult;
import com.windcf.eslearn.entity.vo.SearchResult;
import com.windcf.eslearn.mapper.HotelMapper;
import com.windcf.eslearn.service.HotelService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chunf
 */
@Service
public class HotelServiceImpl implements HotelService {
    private final HotelMapper hotelMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    public HotelServiceImpl(HotelMapper hotelMapper, ElasticsearchOperations elasticsearchOperations) {
        this.hotelMapper = hotelMapper;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Hotel get(long id) {
        return hotelMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Hotel> list() {
        return hotelMapper.list();
    }

    @Override
    public Integer loadEs() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("hotel");
        boolean exists = elasticsearchOperations.indexOps(indexCoordinates).exists();
        if (!exists) {
            elasticsearchOperations.indexOps(indexCoordinates).createMapping(HotelDoc.class);
        }

        List<Hotel> docs = this.list();
        List<IndexQuery> indexQueries = docs.stream().map(HotelDoc::new)
                .map(doc -> new IndexQueryBuilder()
                        .withIndex("hotel")
                        .withId(doc.getId().toString())
                        .withObject(doc).build())
                .collect(Collectors.toList());
        List<IndexedObjectInformation> informations = elasticsearchOperations.bulkIndex(indexQueries, indexCoordinates);
        return informations.size();
    }

    @Override
    public SearchResult search(SearchParam param) {
        PageRequest pageRequest = PageRequest.of(param.getPage() - 1, param.getSize());
        String searchKey = param.getKey();
        List<Query> queries = new ArrayList<>();
        if (StringUtils.hasText(searchKey)) {
            queries.add(QueryBuilders.match().field("all").query(searchKey).build()._toQuery());
        }
        if (StringUtils.hasText(param.getBrand())) {
            queries.add(QueryBuilders.term().field("brand").value(param.getBrand()).build()._toQuery());
        }
        if (StringUtils.hasText(param.getCity())) {
            queries.add(QueryBuilders.term().field("city").value(param.getCity()).build()._toQuery());
        }
        if (param.getMinPrice() != null || param.getMaxPrice() != null) {
            RangeQuery.Builder builder = QueryBuilders.range().field("price");
            if (param.getMinPrice() != null) {
                builder.gte(JsonData.of(param.getMinPrice()));
            }
            if (param.getMaxPrice() != null) {
                builder.lte(JsonData.of(param.getMaxPrice()));
            }
            queries.add(builder.build()._toQuery());
        }
        Query functionFilter = QueryBuilders.term().field("ad").value(true).build()._toQuery();
        Query boolQuery = QueryBuilders.bool().must(queries).build()._toQuery();

        Query functionScore = QueryBuilders.functionScore(
                b -> b.query(boolQuery).functions(
                        b1 -> b1.filter(functionFilter).weight(5.0)).boostMode(FunctionBoostMode.Multiply));

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withSort(Sort.by(prepareOrders(param)))
                .withQuery(functionScore)
                .withPageable(pageRequest);

        SearchHits<HotelDoc> searchHits = elasticsearchOperations.search(builder.build(), HotelDoc.class);
        ArrayList<HotelResult> hotels = new ArrayList<>();
        for (SearchHit<HotelDoc> hit : searchHits.getSearchHits()) {
            List<Object> values = hit.getSortValues();
            hotels.add(new HotelResult(hit.getContent(), Double.parseDouble((String) values.get(values.size() - 1))));
        }
        return new SearchResult(searchHits.getTotalHits(), hotels);
    }

    @Override
    public boolean delIndex(String index) {
        return elasticsearchOperations.indexOps(IndexCoordinates.of(index)).delete();
    }

    @NonNull
    private List<Sort.Order> prepareOrders(SearchParam param) {
        List<Sort.Order> orders = new ArrayList<>();
        if (!StringUtils.hasText(param.getLocation())) {
            throw new IllegalArgumentException("错误的参数：location");
        }
        if ("score".equals(param.getSortBy())) {
            orders.add(Sort.Order.desc("score"));
        }
        if ("price".equals(param.getSortBy())) {
            orders.add(Sort.Order.asc("price"));
        }
        String[] s = param.getLocation().split(", ");
        GeoPoint geoPoint = new GeoPoint(Double.parseDouble(s[1]), Double.parseDouble(s[0]));
        orders.add(new GeoDistanceOrder("location", geoPoint).withUnit("km").with(Sort.Direction.ASC));
        return orders;
    }
}

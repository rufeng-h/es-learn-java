package com.windcf.eslearn.service.impl;

import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.domain.HotelDoc;
import com.windcf.eslearn.mapper.HotelMapper;
import com.windcf.eslearn.service.HotelService;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

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
        List<Hotel> docs = this.list();
        List<IndexQuery> indexQueries = docs.stream().map(HotelDoc::new)
                .map(doc -> new IndexQueryBuilder()
                        .withIndex("hotel")
                        .withId(doc.getId().toString())
                        .withObject(doc).build())
                .collect(Collectors.toList());
        List<IndexedObjectInformation> informations = elasticsearchOperations.bulkIndex(indexQueries, IndexCoordinates.of("hotel"));
        return informations.size();
    }
}

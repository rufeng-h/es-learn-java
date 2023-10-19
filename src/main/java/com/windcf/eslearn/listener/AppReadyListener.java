package com.windcf.eslearn.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.CountRequest;
import co.elastic.clients.elasticsearch.cat.count.CountRecord;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.windcf.eslearn.domain.Hotel;
import com.windcf.eslearn.domain.HotelDoc;
import com.windcf.eslearn.service.HotelService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chunf
 */
@Component
public class AppReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Log logger = LogFactory.getLog(AppReadyListener.class);
    private final HotelService hotelService;
    private final ElasticsearchClient esClient;

    public AppReadyListener(HotelService hotelService, ElasticsearchClient esClient) {
        this.hotelService = hotelService;
        this.esClient = esClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        boolean bulk;
        try {
            CountRecord record = esClient.cat().count(CountRequest.of(b -> b.index("hotel"))).valueBody().get(0);
            bulk = record.count() != null && Long.parseLong(record.count()) == 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!bulk) {
            return;
        }
        List<Hotel> hotels = hotelService.list();
        List<HotelDoc> hotelDocs = hotels.stream().map(HotelDoc::new).collect(Collectors.toList());
        List<BulkOperation> bulkOperations = hotelDocs.stream()
                .map(doc -> BulkOperation.of(b -> b.create(builder -> builder.document(doc).index("hotel").id(doc.getId().toString()))))
                .collect(Collectors.toList());
        try {
            esClient.bulk(BulkRequest.of(b -> b.index("hotel").operations(bulkOperations)));
            logger.info("add " + bulkOperations.size() + " docs to es!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

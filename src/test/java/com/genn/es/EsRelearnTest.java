package com.genn.es;

import com.genn.es.Repository.HotelMapper;
import com.genn.es.entity.Hotel;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class EsRelearnTest {
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Test
    public void testSelect(){
        Hotel hotel = hotelMapper.selectById(38665L);
        System.out.println(hotel);
    }

    String createHotelDSL = "{\"mappings\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\",\"copy_to\":\"brandAndName\"},\"address\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"},\"price\":{\"type\":\"double\"},\"score\":{\"type\":\"double\"},\"brand\":{\"type\":\"keyword\",\"copy_to\":\"brandAndName\"},\"city\":{\"type\":\"keyword\"},\"starName\":{\"type\":\"keyword\"},\"business\":{\"type\":\"text\"},\"location\":{\"type\":\"geo_point\"},\"brandAndName\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"}}}}";

    @Test
    public void testCreateIndex() throws IOException {
        String indexName = "hotel";
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.source(createHotelDSL, XContentType.JSON);
        restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }
}

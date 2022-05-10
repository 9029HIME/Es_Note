package com.genn.es;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.genn.es.Repository.HotelMapper;
import com.genn.es.entity.Hotel;
import com.genn.es.es.HotelEsDTO;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

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

    /**
     * 新建索引
     * @throws IOException
     */
    @Test
    public void testCreateIndex() throws IOException {
        String indexName = "hotel";
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.source(createHotelDSL, XContentType.JSON);
        restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 新建文档
     * @throws IOException
     */
    @Test
    public void testCreateDocument() throws IOException {
        Hotel hotel = hotelMapper.selectById(38665L);
        HotelEsDTO esDto = HotelEsDTO.getEsDto(hotel);
        String indexName = "hotel";

        IndexRequest request = new IndexRequest(indexName).id(String.valueOf(esDto.getId()));
        request.source(JSONObject.toJSONString(esDto), XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSONString(index));
    }

    /**
     * 批量插入数据库的hotel到ES
     */
    @Test
    public void testBulkInsert() throws IOException {
        List<Hotel> hotels = hotelMapper.selectList(null);
        BulkRequest bulkRequest = new BulkRequest();
        for (Hotel hotel : hotels) {
            HotelEsDTO esDto = HotelEsDTO.getEsDto(hotel);
            bulkRequest.add(new IndexRequest("hotel")
                    .id(String.valueOf(esDto.getId()))
                    .source(JSONObject.toJSONString(esDto),XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSONString(bulk));
    }
}

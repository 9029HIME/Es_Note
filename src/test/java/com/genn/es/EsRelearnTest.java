package com.genn.es;

import com.alibaba.fastjson.JSONObject;
import com.genn.es.repository.HotelMapper;
import com.genn.es.dto.EsResponse;
import com.genn.es.entity.Hotel;
import com.genn.es.es.HotelEsDTO;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
public class EsRelearnTest {
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Test
    public void testSelect() {
        Hotel hotel = hotelMapper.selectById(38665L);
        System.out.println(hotel);
    }

    String createHotelDSL = "{\"mappings\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\",\"copy_to\":\"brandAndName\"},\"address\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"},\"price\":{\"type\":\"double\"},\"score\":{\"type\":\"double\"},\"brand\":{\"type\":\"keyword\",\"copy_to\":\"brandAndName\"},\"city\":{\"type\":\"keyword\"},\"starName\":{\"type\":\"keyword\"},\"business\":{\"type\":\"text\"},\"location\":{\"type\":\"geo_point\"},\"brandAndName\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\"}}}}";

    /**
     * 新建索引
     *
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
     *
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
                    .source(JSONObject.toJSONString(esDto), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSONString(bulk));
    }

    @Test
    public void TestNormalQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().
                query(
                        QueryBuilders.matchQuery(
                                "name", "上海"
                        )
                );
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        EsResponse<HotelEsDTO> hotelEsDTOEsResponse = analysisResponse(response, HotelEsDTO.class);
        System.out.println(JSONObject.toJSONString(hotelEsDTOEsResponse));
    }

    public <T> EsResponse<T> analysisResponse(SearchResponse response, Class<T> returnType) {
        EsResponse result = new EsResponse();
        SearchHit[] hits = response.getHits().getHits();
        TotalHits totalHits = response.getHits().getTotalHits();
        result.setTotal(totalHits.value);

        List<T> payload = new LinkedList<>();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            T t = JSONObject.parseObject(sourceAsString, returnType);
            payload.add(t);
        }
        result.setPayload(payload);
        return result;
    }

    @Test
    public void testMetric() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        // 我不希望返回文档，我只想知道评分的聚合
        request.source().size(0)
                .aggregation(
                        AggregationBuilders.stats("MyScore").field("score")
                );
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        Aggregations aggregations = response.getAggregations();
        // 根据上面的聚合自定义名称，获取对应的聚合结果，注意要用ES的aggregations.metrics.Stats接收
        Stats stats = aggregations.get("MyScore");
        System.out.println(JSONObject.toJSONString(stats));
    }

    @Test
    public void testBucket() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        // 我不希望返回文档，我只想知道品牌的聚合
        request.source().size(0)
                .aggregation(
                        AggregationBuilders.terms("MyBrand").field("brand")
                );
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        Aggregations aggregations = response.getAggregations();
        // 根据上面的聚合自定义名称，获取对应的聚合结果，注意用ES的aggregations.metrics.Terms接收
        Terms brandTerms = aggregations.get("MyBrand");
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println(String.format("品牌%s包含的文档数：%s", keyAsString, docCount));
        }
    }
}

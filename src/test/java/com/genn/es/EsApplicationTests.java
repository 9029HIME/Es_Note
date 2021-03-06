package com.genn.es;

import com.alibaba.fastjson.JSONObject;
import com.genn.es.entity.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class EsApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    //创建空索引
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("boot_user");
        IndicesClient indices = restHighLevelClient.indices();
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    //获取索引
    @Test
    public void getIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("boot_index01");
        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println(getIndexResponse);
    }

    //删除索引
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("boot_index01");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest,RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }


    //新建文档
    @Test
    public void createDoc() throws IOException {
        User user = new User();
        user.setAge(18);
        user.setUsername("kjg");
        user.setId(1L);

        IndexRequest request = new IndexRequest("boot_user");
        request.id(String.valueOf(user.getId()));
        request.source(JSONObject.toJSONString(user), XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);

        System.out.println(index);
        //即created,update,noop
        System.out.println(index.status());

    }

    //获取文档
    @Test
    public void getDoc() throws IOException{
        GetRequest getRequest = new GetRequest("boot_user","1");
        //不需要过滤
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none");

        GetResponse response = restHighLevelClient.get(getRequest,RequestOptions.DEFAULT);

        //文档的实际内容
        System.out.println(response.getSourceAsString());

    }

    //更新文档
    @Test
    public void updateDoc() throws IOException{
        UpdateRequest request = new UpdateRequest("boot_user","1");

        User user = new User();
        user.setUsername("更改名");

        request.doc(JSONObject.toJSONString(user),XContentType.JSON);
        UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);

        System.out.println(response.status());
    }

    //删除文档
    @Test
    public void deleteDoc() throws IOException{
        DeleteRequest request = new DeleteRequest("boot_user","1");

        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);

        System.out.println(response.status());
    }


    //查询文档
    @Test
    public void searchDoc() throws IOException {
        SearchRequest request = new SearchRequest("boot_user");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 查询具体条件，里面还有很多API对应条件，如bool,match,sort,from,size

        // MatchQueryBuilder matchQueryBuilder = QueryBuilders.
        // matchQuery("username","更改名");

        // QueryBuilders.boolQuery().should();

        // 这里只用term查询
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("username","更改名");


        searchSourceBuilder.query(termQueryBuilder)
                .from(0).size(10).sort("id");
        request.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        //实际结果是存放再HITS里，跟KIBANA查询的json类似
        System.out.println(JSONObject.toJSONString(response.getHits().getHits()));

    }



    @Test
    void contextLoads() {
    }

}

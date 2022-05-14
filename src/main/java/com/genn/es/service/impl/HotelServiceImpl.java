package com.genn.es.service.impl;

import com.genn.es.command.QueryHotelCommand;
import com.genn.es.common.CommonEsQuery;
import com.genn.es.dto.EsResponse;
import com.genn.es.entity.Hotel;
import com.genn.es.enums.*;
import com.genn.es.es.HotelEsDTO;
import com.genn.es.service.IHotelService;
import com.genn.es.utils.EsUtil;
import io.netty.util.internal.StringUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;

@Service
public class HotelServiceImpl implements IHotelService {
    @Autowired
    private RestHighLevelClient esClient;


    @Override
    public EsResponse<HotelEsDTO> keywordQuery(String keyword) throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .query(
                        QueryBuilders.matchQuery(
                                "brandAndName", keyword
                        )
                );
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
        EsResponse<HotelEsDTO> result = EsUtil.analysisResponse(response, HotelEsDTO.class);
        return result;
    }

    @Override
    public EsResponse<HotelEsDTO> multiQuery(QueryHotelCommand command) throws IllegalAccessException, IOException {
        SearchRequest request = dynamicQuery("hotel", command);
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
//        EsResponse<HotelEsDTO> result = EsUtil.analysisResponse(response, HotelEsDTO.class);
        // 03-23：整理好经纬度距离并返回
        EsResponse<HotelEsDTO> result = EsUtil.analysisGeoResponse(response, HotelEsDTO.class);
        return result;
    }

    public SearchRequest dynamicQuery(String indexName, Object queryParam) throws IllegalAccessException {
        if (!(queryParam instanceof CommonEsQuery)) {
            throw new RuntimeException("error param");
        }
        SearchRequest request = new SearchRequest(indexName);
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        request.source().query(boolQueryBuilder);

        //03-24，要实现广告排序效果
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                // 使用下面for循环动态拼接的复合查询
                boolQueryBuilder,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                // 定义过滤条件：有advertiseAmount值的才参与重新算分
                                QueryBuilders.existsQuery("advertiseAmount"),
                                // 定义算分函数，直接将advertiseAmount作为重算分值
                                ScoreFunctionBuilders.fieldValueFactorFunction("advertiseAmount")
                        )
                }
                // 加权模式，使用乘法加权，算出最终结果
        ).boostMode(CombineFunction.MULTIPLY);
        request.source().query(functionScoreQueryBuilder);

        Class<?> aClass = queryParam.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value = (String) field.get(queryParam);
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            if (field.isAnnotationPresent(Match.class)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery(
                                name, value
                        )
                );
            } else if (field.isAnnotationPresent(Term.class)) {
                boolQueryBuilder.must(
                        QueryBuilders.termQuery(
                                name, value
                        )
                );
            } else if (field.isAnnotationPresent(RangeFrom.class)) {
                boolQueryBuilder.must(
                        QueryBuilders.rangeQuery("price").gte(value)
                );
            } else if (field.isAnnotationPresent(RangeTo.class)) {
                boolQueryBuilder.must(
                        QueryBuilders.rangeQuery("price").lte(value)
                );
            } else if (field.isAnnotationPresent(Location.class)) {
                String[] split = value.split(",");
                String longitude = split[0];
                String latitude = split[1];
                request.source().sort(
                        SortBuilders.geoDistanceSort("location", Double.valueOf(latitude), Double.valueOf(longitude))
                                .order(SortOrder.ASC)
                                .unit(DistanceUnit.KILOMETERS)
                );
            }
        }
        return request;
    }
}

package com.genn.es.utils;

import com.alibaba.fastjson.JSONObject;
import com.genn.es.common.GeoResult;
import com.genn.es.dto.EsResponse;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.LinkedList;
import java.util.List;

public class EsUtil {

    public static <T> EsResponse<T> analysisResponse(SearchResponse response, Class<T> returnType){
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

    public static <T extends GeoResult> EsResponse<T> analysisGeoResponse(SearchResponse response, Class<T> returnType){
        EsResponse result = new EsResponse();
        SearchHit[] hits = response.getHits().getHits();
        TotalHits totalHits = response.getHits().getTotalHits();
        result.setTotal(totalHits.value);

        List<T> payload = new LinkedList<>();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            T t = JSONObject.parseObject(sourceAsString, returnType);

            Object[] sortValues = hit.getSortValues();
            if(sortValues.length > 0){
                String sortValue = String.valueOf(sortValues[0]);
                t.setSortValue(sortValue);
            }
            payload.add(t);
        }
        result.setPayload(payload);
        return result;
    }

}

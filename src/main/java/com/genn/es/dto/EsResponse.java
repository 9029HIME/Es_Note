package com.genn.es.dto;

import java.util.List;

public class EsResponse<T> {
    private Long total;
    private List<T> payload;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getPayload() {
        return payload;
    }

    public void setPayload(List<T> payload) {
        this.payload = payload;
    }
}

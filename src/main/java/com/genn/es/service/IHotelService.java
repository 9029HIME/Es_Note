package com.genn.es.service;

import com.genn.es.command.QueryHotelCommand;
import com.genn.es.dto.EsResponse;
import com.genn.es.entity.Hotel;
import com.genn.es.es.HotelEsDTO;

import java.io.IOException;

public interface IHotelService {
    EsResponse<HotelEsDTO> keywordQuery(String keyword) throws IOException;

    EsResponse<HotelEsDTO> multiQuery(QueryHotelCommand command) throws IllegalAccessException, IOException;
}

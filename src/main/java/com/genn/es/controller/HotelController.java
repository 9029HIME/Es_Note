package com.genn.es.controller;

import com.genn.es.command.QueryHotelCommand;
import com.genn.es.dto.EsResponse;
import com.genn.es.entity.Hotel;
import com.genn.es.es.HotelEsDTO;
import com.genn.es.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @GetMapping("/keywordQuery")
    public EsResponse<HotelEsDTO> keywordQuery(@RequestParam(value = "keyword",required = true) String keyword) throws IOException {
        return hotelService.keywordQuery(keyword);
    }

    @PostMapping("/multiQuery")
    public EsResponse<HotelEsDTO> multiQuery(@RequestBody QueryHotelCommand command) throws IOException, IllegalAccessException {
        return hotelService.multiQuery(command);
    }
}

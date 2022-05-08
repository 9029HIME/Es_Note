package com.genn.es;

import com.genn.es.Repository.HotelMapper;
import com.genn.es.entity.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EsRelearnTest {
    @Autowired
    private HotelMapper hotelMapper;

    @Test
    public void testSelect(){
        Hotel hotel = hotelMapper.selectById(38665L);
        System.out.println(hotel);
    }
}

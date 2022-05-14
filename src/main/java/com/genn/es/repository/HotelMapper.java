package com.genn.es.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.genn.es.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
}

package com.sky.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 通过菜品id获取关联套餐id
     * @param dishIds
     * @return
     */
    List<Long> getsetmealIdsByDishIds(List<Long> dishIds);
}

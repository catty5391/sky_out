package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 通过菜品id获取关联套餐id
     * @param dishIds
     * @return
     */
    List<Long> getsetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBysetMealId(List<Long> ids);

    @Select("select * from sky_take_out.setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

}

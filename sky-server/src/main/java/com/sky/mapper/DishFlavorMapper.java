package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.DishFlavor;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量增加新口味
     * @param flavors
     */
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据菜品id删除flavor
     * @param dishId
     */
    @Delete("delete from sky_take_out.dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    @Select("select * from sky_take_out.dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getFlavorsByDishId(Long dishId);

    void deleteByDishIds(List<Long> dishIds);
}

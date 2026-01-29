package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量增加新口味
     * @param flavors
     */
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);
}

package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 增加新菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    @Select("select * from dish where id = #{id}")
    Dish findById(Long id);

    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where name = #{name} and id != #{id}")
    Dish findByName(Dish dish);

    void deleteByIds(List<Long> ids);

    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> findByCategoryId(Integer categoryId);
}

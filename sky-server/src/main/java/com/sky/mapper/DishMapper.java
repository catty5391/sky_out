package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
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

    @Select("select d.*, c.name category_name from dish d left join sky_take_out.category c on d.category_id = c.id where d.id = #{id}")
    DishVO findById(Long id);

    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where name = #{name} and id != #{id}")
    Dish findByName(Dish dish);

    void deleteByIds(List<Long> ids);

    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> findByCategoryId(Integer categoryId);

    @Select("select d.* from dish d INNER JOIN setmeal_dish s on d.id = s.dish_id WHERE setmeal_id = #{setmealId}")
    List<Dish> getDishesBySetmealId(Long setmealId);

    List<Dish> list(Dish dish);

    @Select("select count(*) from dish where status = #{status}")
    Integer getAmountByStatus(int status);
}

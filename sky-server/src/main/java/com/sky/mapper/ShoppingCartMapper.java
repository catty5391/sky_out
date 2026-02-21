package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    @Insert("insert into sky_take_out.shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
                                            "VALUES(#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void add(ShoppingCart shoppingCart);

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update sky_take_out.shopping_cart set number = #{number} where id = #{id}")
    void changeNum(ShoppingCart shoppingCart);


    void delete(ShoppingCart shoppingCart);

    @Delete("delete from sky_take_out.shopping_cart where user_id = #{userId}")
    void clean(Long userId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}

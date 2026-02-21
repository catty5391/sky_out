package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Resource
    ShoppingCartMapper shoppingCartMapper;

    @Resource
    DishMapper dishMapper;

    @Resource
    SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        shoppingCart.setUserId(userId);
        //判断是否存在
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);


        //如果存在数量加一
        if(shoppingCarts != null && !shoppingCarts.isEmpty()){
            ShoppingCart cart = shoppingCarts.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.changeNum(cart);
            return;
        }
        //不存在则喜加一
        //判断是套餐还是菜品
        Long dishId = shoppingCartDTO.getDishId();
        if(dishId != null){
            //到菜品表查询
            DishVO dish = dishMapper.findById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartMapper.add(shoppingCart);
            return;
        }

        Long setmealId = shoppingCartDTO.getSetmealId();
        SetmealVO setmeal = setmealMapper.getByIdWithCategoryName(setmealId);
        shoppingCart.setName(setmeal.getName());
        shoppingCart.setAmount(setmeal.getPrice());
        shoppingCart.setImage(setmeal.getImage());
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setNumber(1);
        shoppingCartMapper.add(shoppingCart);

    }

    @Override
    public List<ShoppingCart> listShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        // 判断Num是否为1如果为1，直接删除，如果不为1，Num - 1
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        ShoppingCart cart = list.get(0);
        if(cart.getNumber() != 1){
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.changeNum(cart);
        }else{
            shoppingCartMapper.delete(shoppingCart);
        }
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }
}

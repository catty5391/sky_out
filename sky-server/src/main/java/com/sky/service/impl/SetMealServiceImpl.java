package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.sky.entity.Setmeal;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {
    @Resource
    SetmealMapper setmealMapper;

    @Resource
    SetmealDishMapper setmealDishMapper;

    @Override
    public void addSetMeal(SetmealDTO setmealDTO) {
        // 1. 新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        // 2. 更新setmeal_dish 表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }
}

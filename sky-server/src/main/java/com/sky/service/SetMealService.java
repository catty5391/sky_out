package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

public interface SetMealService {
    void addSetMeal(SetmealDTO setmealDTO);

    PageResult<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);
}

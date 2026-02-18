package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.exception.UpdateNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.sky.entity.Setmeal;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {
    @Resource
    SetmealMapper setmealMapper;

    @Resource
    SetmealDishMapper setmealDishMapper;

    @Resource
    DishMapper dishMapper;

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

    @Override
    public PageResult<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);
        Page<SetmealVO> setmeals = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult<>(setmeals.getTotal(), setmeals.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        // 0. 检查套餐是否处于出售状态
        for(Long id : ids){
            Integer status = setmealMapper.getStatusById(id);
            if ( status == 1)  throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 1. 批量删除套餐表中相关数据
        setmealMapper.deleteBatch(ids);
        // 2. 批量删除套餐-菜品表中相关数据
        setmealDishMapper.deleteBysetMealId(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        // 1. 联查
        SetmealVO setmealVO = setmealMapper.getByIdWithCategoryName(id);
        // 2. 补充setmealVO 的setmealDishes属性
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        // 1. 检查name，确保与表中其它数据不冲突
        Setmeal setmealTemp = setmealMapper.getByName(setmealDTO);
        if(setmealTemp != null) throw new UpdateNotAllowedException(MessageConstant.Setmeal_Repeated);

        // 2. 更新setmeal 表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 3. 更新setmeal-dish 表
        Long id = setmealDTO.getId();
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        setmealDishMapper.deleteBysetMealId(ids);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        // 对于包含停售菜品的套餐，不能起售
        if(status.equals(StatusConstant.ENABLE)){
            List<Dish> dishes = dishMapper.getDishesBySetmealId(id);
            dishes.forEach(dish -> {
                if(dish.getStatus().equals(StatusConstant.DISABLE)) throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            });
        }

        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}

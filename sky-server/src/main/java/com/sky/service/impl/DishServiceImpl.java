package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.UpdateNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Resource
    DishMapper dishMapper;

    @Resource
    DishFlavorMapper dishFlavorMapper;

    @Resource
    SetmealDishMapper setmealDishMapper;

    @Resource
    CategoryMapper categoryMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    public void save(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 1. 保存菜品
        dishMapper.insert(dish);

        // 2. 保存口味
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach((flavor) -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);

        }

    }

    @Override
    public PageResult<DishVO> page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishFlavorMapper.page(dishPageQueryDTO);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        // 1. 是否处于起售状态
        for (Long id:ids){
            DishVO dishVO = dishMapper.findById(id);
            if (dishVO.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 2. 是否有套餐关联
        List<Long> setmealIds = setmealDishMapper.getsetmealIdsByDishIds(ids);
        if(setmealIds != null && !setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 3. 删除菜品 + 菜品对应口味
//        for(Long id: ids){
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);

    }

    @Override
    public void updateStatus(Dish dish) {
        dishMapper.update(dish);
    }

    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 1. 更新菜品, 检查是否名字是否与现有的重复,并将起售的菜品改为停售
        dish.setStatus(0);
        if(dishMapper.findByName(dish) != null){
            throw new UpdateNotAllowedException(MessageConstant.Dish_Repeated);
        }
        dishMapper.update(dish);
        // 2. 更新口味
        Long dishId = dish.getId();
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        dishFlavors.forEach((dishFlavor) ->{
            dishFlavor.setDishId(dishId);
        });
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        dishFlavorMapper.insertBatch(dishFlavors);
    }

    @Override
    public DishVO findById(Long id) {
        //
        DishVO dishVO = dishMapper.findById(id);

        // 3. 将flavor放到dishVo中
        List<DishFlavor> dishFlavors = dishFlavorMapper.getFlavorsByDishId(id);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public List<Dish> findByCategoryId(Integer categoryId) {
        List<Dish> dishes = dishMapper.findByCategoryId(categoryId);
        return dishes;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}

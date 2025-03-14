package com.siki.mapper;

import com.siki.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量新增套餐菜品
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id删除菜品
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

//    /**
//     * 根据菜品查询套餐id
//     * @param setmealDish
//     * @return
//     */
//    List<Long> getSetmealIdsByDishId(SetmealDish setmealDish);
}

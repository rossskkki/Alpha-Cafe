package com.siki.mapper;

import com.github.pagehelper.Page;
import com.siki.annotation.AutoFill;
import com.siki.dto.DishPageQueryDTO;
import com.siki.entity.Dish;
import com.siki.enumeration.OperationType;
import com.siki.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

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
     * 新增菜品
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 删除菜品
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} and status = 1")
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 根据菜品id查询菜品状态
     * @param dishId
     * @return
     */
    @Select("select status from dish where id = #{dishId}")
    Integer getStatusById(Long dishId);

    /**
     * 修改菜品状态
     * @param id
     * @param status
     */
    @Update("update dish set status = #{status} where id = #{id}")
    void updateStatus(Long id, Integer status);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    @Select("select * from dish where is_hot = 1")
    List<Dish> hotDishDisplay();
}

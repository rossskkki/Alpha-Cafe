package com.siki.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.siki.constant.MessageConstant;
import com.siki.constant.StatusConstant;
import com.siki.dto.SetmealDTO;
import com.siki.dto.SetmealPageQueryDTO;
import com.siki.entity.Setmeal;
import com.siki.entity.SetmealDish;
import com.siki.exception.DeletionNotAllowedException;
import com.siki.exception.SetmealEnableFailedException;
import com.siki.mapper.DishMapper;
import com.siki.mapper.SetmealDishMapper;
import com.siki.mapper.SetmealMapper;
import com.siki.result.PageResult;
import com.siki.service.SetmealService;
import com.siki.vo.DishItemVO;
import com.siki.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向套餐表插入一条数据
        setmealMapper.insert(setmeal);
        //获取套餐id
        Long setmealId = setmeal.getId();
        //向套餐表插入多条数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
        //菜品是否停售
        for (SetmealDish setmealDish : setmealDishes) {
            Long dishId = setmealDish.getDishId();
            Integer status = dishMapper.getStatusById(dishId);
            //如果有菜品停售，则停售套餐
            if (status == StatusConstant.DISABLE) {
                setmealMapper.updateStatus(setmealId, StatusConstant.DISABLE);
                break;
            }
        }

    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //套餐是否起售中
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //根据套餐id批量删除套餐
        setmealMapper.deleteByIds(ids);
        //根据套餐id批量删除套餐菜品
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 更新套餐状态
     * @param setmealId
     * @param status
     */
    public void updateStatus(Long setmealId, Integer status) {
        if (status == StatusConstant.ENABLE) {
            //套餐是否有菜品停售
            List<SetmealDish> dishes = setmealDishMapper.getBySetmealId(setmealId);
            for (SetmealDish setmealDish : dishes) {
                Long dishId = setmealDish.getDishId();
                Integer dishStatus = dishMapper.getStatusById(dishId);
                if (dishStatus == StatusConstant.DISABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
            setmealMapper.updateStatus(setmealId, status);
        }
        if (status == StatusConstant.DISABLE) {
            setmealMapper.updateStatus(setmealId, status);
        }
    }

    /**
     * 更新套餐
     * @param setmealDTO
     */
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //修改套餐
        setmealMapper.update(setmeal);
        //根据套餐id删除套餐菜品
        List<Long> setmealId = new ArrayList<>();
        setmealId.add(setmeal.getId());
        setmealDishMapper.deleteBySetmealIds(setmealId);
        //新增套餐菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
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

    @Override
    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) {
            return null;
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }
}

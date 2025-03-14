package com.siki.service;

import com.siki.dto.ShoppingCartDTO;
import com.siki.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 添加商品到购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查询购物车商品
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void clean();

    /**
     * 更新购物车商品数量
     * @param shoppingCartDTO
     */
    void sub(ShoppingCartDTO shoppingCartDTO);
}

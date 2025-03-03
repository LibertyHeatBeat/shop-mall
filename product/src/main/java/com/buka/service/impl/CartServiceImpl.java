package com.buka.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.buka.constant.CacheKey;
import com.buka.dto.CartItemDto;
import com.buka.exception.BizException;
import com.buka.enums.BizCodeEnum;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.CartItemDO;
import com.buka.model.ProductDO;
import com.buka.service.CartService;
import com.buka.service.ProductService;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import com.buka.model.CartDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lhb
 * @version 1.0
 * @description: 购物车实现类
 * @date 2025/2/18 下午12:07
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductService productService;



    /**
    * @Author: lhb
    * @Description: 向购物车中添加商品
    * @DateTime: 下午12:11 2025/2/18
    * @Params: [cartItemDto]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData addCart(CartItemDto cartItemDto) {
        // 获取当前用户
        Integer buyNum = cartItemDto.getBuyNum();
        long productId = cartItemDto.getProductId();
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        Object o = myCartOps.get(productId+"");
        String result="";
        if (o!=null){
            result=(String) o;
        }
        // 判断购物车中是否存在该商品
        if (StringUtils.isBlank(result)){
            CartItemDO cartItemDO=new CartItemDO();
            ProductDO productDO = productService.getById(productId);
            if (productDO==null) {
                throw new BizException(BizCodeEnum.SYSTEM_ERROR);
            }
            cartItemDO.setProductId(productId);
            cartItemDO.setProductPrice(productDO.getAmount());
            cartItemDO.setProductImage(productDO.getCoverImg());
            cartItemDO.setBuyNum(buyNum);
            cartItemDO.setProductTitle(productDO.getTitle());
            myCartOps.put(productId+"", JSON.toJSONString(cartItemDO));
        }else {
            CartItemDO cartItemDO = JSON.parseObject(result, CartItemDO.class);
            cartItemDO.setBuyNum(cartItemDO.getBuyNum()+buyNum);
            myCartOps.put(productId+"",JSON.toJSONString(cartItemDO));
        }
        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description: 清空购物车实现类
    * @DateTime: 上午8:56 2025/2/19
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData clearCart() {
        String cartKey = getCartKey();
        redisTemplate.delete(cartKey);
        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description: 产看购物车实现方法
    * @DateTime: 上午8:58 2025/2/19
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData myCart() {
        List<CartItemDO> cartItemDOList = buildCartItem(false);
        CartDO cartDO = new CartDO();
        cartDO.setCartItem(cartItemDOList);
        return JsonData.buildSuccess(cartDO);
    }

    @Override
    public JsonData changeCart(CartItemDto cartItemDto) {
        // 获取购物车数据
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        Object o = myCartOps.get(cartItemDto.getProductId()+"");
        // 判断购物车中是否存在该商品
        if (o==null){
            throw new BizException(BizCodeEnum.CART_ITEM_NOT_EXIST);
        }
        // 获取购物车中的商品
        CartItemDO cartItemDO = JSON.parseObject((String) o, CartItemDO.class);
        cartItemDO.setBuyNum(cartItemDto.getBuyNum());
        // 更新购物车
        myCartOps.put(cartItemDto.getProductId()+"",JSON.toJSONString(cartItemDO));
        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description: 删除购物车中的指定商品
    * @DateTime: 上午9:41 2025/2/19
    * @Params: [productId]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData deleteCart(long productId) {
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        myCartOps.delete(productId+"");
        return JsonData.buildSuccess();
    }


    /**
    * @Author: lhb
    * @Description: 获取当前用户的购物车数据操作对象
    * @DateTime: 下午12:11 2025/2/18
    * @Params: []
    * @Return org.springframework.data.redis.core.BoundHashOperations<java.lang.String,java.lang.Object,java.lang.Object>
    */
    private BoundHashOperations<String, Object, Object> getMyCartOps() {
        String cartKey = getCartKey();
        // 获取购物车数据
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = redisTemplate.boundHashOps(cartKey);
        return stringObjectObjectBoundHashOperations;
    }

    /**
    * @Author: lhb
    * @Description: 获取购物车的缓存键
    * @DateTime: 下午12:11 2025/2/18
    * @Params: []
    * @Return java.lang.String
    */
    private String getCartKey() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String cartKey = String.format(CacheKey.CART_KEY,loginUser.getId());
        return cartKey;
    }

    /**
    * @Author: lhb
    * @Description: 构建购物车
    * @DateTime: 上午9:02 2025/2/19
    * @Params: [b]
    * @Return java.util.List<com.buka.model.CartItemDO>
    */
    private List<CartItemDO> buildCartItem(boolean b) {
        // 获取购物车数据
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        List<Object> values = myCartOps.values();
        List<CartItemDO> list = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        // 遍历购物车数据
        for (Object value : values) {
            CartItemDO cartItemDO = JSON.parseObject((String) value, CartItemDO.class);
            list.add(cartItemDO);
            ids.add(cartItemDO.getProductId());
        }
        if (b) {
            setProductLatestPrice(list, ids);
        }
        return list;
    }

    /**
    * @Author: lhb
    * @Description: 根据商品ID设置购物项的最新价格
    * @DateTime: 上午9:02 2025/2/19
    * @Params: [list, ids]
    * @Return void
    */
    private void setProductLatestPrice(List<CartItemDO> list, List<Long> ids) {
        List<ProductDO> productDOS = productService.listByIds(ids);
        Map<Long, ProductDO> map =
                productDOS.stream()
                        .collect(Collectors.toMap(ProductDO::getId, Function.identity()));
        for (CartItemDO cartItemDO : list) {
            ProductDO productDO = map.get(cartItemDO.getProductId());
            cartItemDO.setProductPrice(productDO.getAmount());
            cartItemDO.setProductTitle(productDO.getTitle());
            cartItemDO.setProductImage(productDO.getCoverImg());
        }
    }
    @Override
    public JsonData confirmOrderCartItems(List<Long> productIdList) {
        List<CartItemDO> cartItemDOList = buildCartItem(true);
        List<CartItemDO> collect = cartItemDOList.stream().filter(obj -> {
            if (productIdList.contains(obj.getProductId())) {
                this.deleteCart(obj.getProductId());
                return true;
            }
            return false;
        }).collect(Collectors.toList());


        return JsonData.buildSuccess(collect);
    }

}

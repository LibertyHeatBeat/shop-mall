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
        // 获取购买数量和产品ID
        Integer buyNum = cartItemDto.getBuyNum();
        long productId = cartItemDto.getProductId();

        // 获取当前用户的购物车操作对象
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();

        // 检查购物车中是否已存在该商品
        Object o = myCartOps.get(productId+"");
        String result="";

        if (o!=null){
            result=(String) o;
        }

        // 如果购物车中没有该商品信息，则创建新的商品信息对象
        if (StringUtils.isBlank(result)){
            //购物车没有这个商品
            CartItemDO cartItemDO=new CartItemDO();
            // 根据产品ID获取产品详细信息
            ProductDO productDO = productService.getById(productId);
            // 如果产品不存在，抛出业务异常
            if (productDO==null) throw new BizException(BizCodeEnum.SYSTEM_ERROR);

            // 设置商品信息到购物车项中
            cartItemDO.setProductId(productId);
            cartItemDO.setProductPrice(productDO.getAmount());
            cartItemDO.setProductImage(productDO.getCoverImg());
            cartItemDO.setBuyNum(buyNum);
            cartItemDO.setProductTitle(productDO.getTitle());

            // 将新的购物车项添加到购物车中
            myCartOps.put(productId+"", JSON.toJSONString(cartItemDO));
        }else {
            // 如果购物车中已存在该商品信息，则更新商品数量
            CartItemDO cartItemDO = JSON.parseObject(result, CartItemDO.class);
            cartItemDO.setBuyNum(cartItemDO.getBuyNum()+buyNum);
            // 更新购物车中的商品信息
            myCartOps.put(productId+"",JSON.toJSONString(cartItemDO));
        }

        // 返回成功响应
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
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        Object o = myCartOps.get(cartItemDto.getProductId()+"");
        if (o==null)throw new BizException(BizCodeEnum.CART_ITEM_NOT_EXIST);
        CartItemDO cartItemDO = JSON.parseObject((String) o, CartItemDO.class);
        cartItemDO.setBuyNum(cartItemDto.getBuyNum());
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
        // 获取购物车的键，这个键是根据当前用户确定的，确保每个用户的购物车数据是独立的
        String cartKey = getCartKey();

        // 使用获取到的购物车键，从Redis中获取一个与该键绑定的哈希操作对象
        // 这个对象提供了丰富的操作方法，用于处理购物车中的商品信息
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = redisTemplate.boundHashOps(cartKey);

        // 返回与购物车键绑定的哈希操作对象，供其他方法使用，执行实际的数据操作
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
    * @Description:
    * @DateTime: 上午9:02 2025/2/19
    * @Params: [b]
    * @Return java.util.List<com.buka.model.CartItemDO>
    */
    private List<CartItemDO> buildCartItem(boolean b) {
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        List<Object> values = myCartOps.values();
        List<CartItemDO> list = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
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
}

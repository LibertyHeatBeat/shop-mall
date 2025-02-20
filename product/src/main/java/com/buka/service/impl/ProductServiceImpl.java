package com.buka.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.model.ProductDO;
import com.buka.mapper.ProductMapper;
import com.buka.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.JsonData;
import com.buka.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lhb
 * @since 2025-02-17
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    /**
    * @Author: lhb
    * @Description: 分页查询
    * @DateTime: 下午4:14 2025/2/17
    * @Params: [page, size]
    * @Return java.lang.Object
    */
    @Override
    public Object pageProduct(Long page, Long size) {
        Page<ProductDO> page1=new Page<>(page,size);
        this.page(page1);
        List<ProductDO> records = page1.getRecords();
        List<ProductVO> collect = records.stream().map(obj -> {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(obj, productVO);
            return productVO;
        }).collect(Collectors.toList());
        long total = page1.getTotal();
        long pages1 = page1.getPages();
        Map<String, Object> map=new HashMap<>();
        map.put("data",collect);
        map.put("total",total);
        map.put("pages",pages1);
        return JsonData.buildSuccess(map);
    }

    /**
    * @Author: lhb
    * @Description: TODO
    * @DateTime: 下午4:16 2025/2/17
    * @Params: [productId]
    * @Return java.lang.Object
    */
    @Override
    public Object detail(Long productId) {
        ProductDO one = getById(productId);
        ProductVO productVO=new ProductVO();
        BeanUtils.copyProperties(one,productVO);
        productVO.setStock(one.getStock()-one.getLockStock());
        return JsonData.buildSuccess(productVO);
    }
}

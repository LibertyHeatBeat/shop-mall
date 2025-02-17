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
        // 创建Page对象，用于存储分页查询的结果
        Page<ProductDO> page1=new Page<>(page,size);

        // 执行分页查询
        this.page(page1);

        // 获取查询结果中的产品记录
        List<ProductDO> records = page1.getRecords();
        // 将查询结果转换为ProductVO对象的列表
        List<ProductVO> collect = records.stream().map(obj -> {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(obj, productVO);
            return productVO;
        }).collect(Collectors.toList());
        // 获取总记录数
        long total = page1.getTotal();
        // 获取总页数
        long pages1 = page1.getPages();

        // 创建一个Map对象来存储分页查询的结果和相关信息
        Map<String, Object> map=new HashMap<>();
        map.put("data",collect);
        map.put("total",total);
        map.put("pages",pages1);

        // 返回包含分页查询结果的JsonData对象
        return JsonData.buildSuccess(map);
    }

    /**
    * @Author: lhb
    * @Description: 轮播图
    * @DateTime: 下午4:16 2025/2/17
    * @Params: [productId]
    * @Return java.lang.Object
    */
    @Override
    public Object detail(Long productId) {
        // 通过产品ID获取产品实体
        ProductDO one = getById(productId);
        // 创建一个产品视图对象
        ProductVO productVO=new ProductVO();
        // 将产品实体的属性复制到视图对象中
        BeanUtils.copyProperties(one,productVO);
        // 计算并设置产品的可用库存量
        productVO.setStock(one.getStock()-one.getLockStock());

        // 返回包含产品视图对象的成功Json数据
        return JsonData.buildSuccess(productVO);
    }
}

package com.buka.controller;


import com.buka.request.LockProductRequest;
import com.buka.service.ProductService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lhb
 * @since 2025-02-17
 */
@RestController
@RequestMapping("/api/product/v1")
public class ProductController {
    @Autowired
    private ProductService productService;
    /**
    * @Author: lhb
    * @Description: 分页查询
    * @DateTime: 下午4:13 2025/2/17
    * @Params: [page, size]
    * @Return JsonData
    */
    @GetMapping("/page_product")
    public JsonData pageProduct(@RequestParam(value = "page", defaultValue = "1") Long page,
                                @RequestParam(value = "size", defaultValue = "10") Long size) {
        return JsonData.buildSuccess(productService.pageProduct(page, size));
    }
    /**
    * @Author: lhb
    * @Description: 根据ID查看商品详情
    * @DateTime: 下午4:14 2025/2/17
    * @Params: [productId]
    * @Return com.buka.util.JsonData
    */
    @GetMapping("/detail/{product_id}")
    public JsonData detail(@PathVariable("product_id") Long productId) {
        return JsonData.buildSuccess(productService.detail(productId));
    }

    @PostMapping("/lock_product")
    public JsonData lockProduct(@RequestBody LockProductRequest lockProductRequest){
        return productService.lockPeoduct(lockProductRequest);
    }
    

}


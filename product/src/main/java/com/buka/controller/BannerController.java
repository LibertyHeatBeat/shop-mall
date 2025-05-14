package com.buka.controller;


import com.buka.service.BannerService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  商品轮播图管理
 * </p>
 *
 * @author lhb
 * @since 2025-02-17
 */
@RestController
@RequestMapping("/api/banner/v1")
public class BannerController {
    @Autowired
    private BannerService bannerService;

    /**
    * @Author: lhb
    * @Description: 商品轮播图
    * @DateTime: 下午8:00 2025/4/20
    * @Params: []
    * @Return com.buka.util.JsonData
    */
    @GetMapping("list_banner")
    public JsonData listBanner() {
        return  JsonData.buildSuccess(bannerService.list());
    }
}


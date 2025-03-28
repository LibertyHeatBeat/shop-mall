package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.config.RabbitMQConfig;
import com.buka.enums.ProductOrderStateEnum;
import com.buka.enums.StockTaskStateEnum;
import com.buka.es.ProductDocument;
import com.buka.exception.BizException;
import com.buka.feign.ProductOrderFeignSerivce;
import com.buka.model.ProductDO;
import com.buka.mapper.ProductMapper;
import com.buka.model.ProductMessage;
import com.buka.model.ProductTaskDO;
import com.buka.request.LockProductRequest;
import com.buka.request.OrderItemRequest;
import com.buka.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.service.ProductTaskService;
import com.buka.util.JsonData;
import com.buka.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
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
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {
    @Autowired
    private ProductTaskService productTaskService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private ProductOrderFeignSerivce productOrderFeignSerivce;
    @Autowired
    private ElasticsearchRestTemplate  elasticsearchRestTemplate;

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

    /**
    * @Author: lhb
    * @Description: 锁定商品库存，并生成库存锁定任务。
     *该方法会遍历订单中的每个商品，依次执行以下操作：
     *1. 查询商品信息；
     *2. 更新商品的锁定库存；
     *3. 生成库存锁定任务记录；
     *4. 发送延迟消息，用于后续库存释放操作。
    * @DateTime: 下午4:04 2025/3/7
    * @Params: [lockProductRequest]
    * @Return com.buka.util.JsonData
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData lockPeoduct(LockProductRequest lockProductRequest) {
        String orderOutTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> orderItemRequest = lockProductRequest.getOrderItemRequest();
        //遍历订单中的每个商品，执行库存锁定操作
        for(OrderItemRequest itemRequest : orderItemRequest){
            //查询商品信息
            ProductDO productDO = getById(itemRequest.getProductId());
            //更新商品的锁定库存
            LambdaUpdateWrapper<ProductDO> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.set(ProductDO::getLockStock , productDO.getLockStock() + itemRequest.getBuyNum());
            queryWrapper.eq(ProductDO::getId,itemRequest.getProductId());
            boolean result = update(queryWrapper);
            //判断库存是否扣减失败，如果失败，抛出异常
            if (!result){
                throw new BizException(23001,"库存扣减失败");
            }
            // 生成库存锁定任务记录
            ProductTaskDO productTaskDO = new ProductTaskDO();
            productTaskDO.setProductId(itemRequest.getProductId());
            productTaskDO.setBuyNum(itemRequest.getBuyNum());
            productTaskDO.setProductName(productDO.getTitle());
            productTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
            productTaskDO.setOutTradeNo(orderOutTradeNo);
            productTaskDO.setCreateTime(new Date());
            productTaskService.save(productTaskDO);

            // 发送延迟消息，用于后续库存释放操作
            ProductMessage productMessage = new ProductMessage();
            productMessage.setOutTradeNo(orderOutTradeNo);
            productMessage.setTaskId(productTaskDO.getId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getStockReleaseDelayRoutingKey(), productMessage);
        }
        // 返回操作成功的结果

        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description: 释放产品库存
     * 该函数用于根据产品消息释放相应的库存。首先会检查工作单是否存在，然后根据订单状态决定是否释放库存。
     * 如果订单状态为NEW，则返回false，表示需要重新投递消息；如果订单状态为PAY，则修改任务状态为FINISH并返回true。
     * 如果订单不存在或已被取消，则回滚库存并修改任务状态为CANCEL。
    * @DateTime: 下午2:06 2025/3/8
    * @Params: [productMessage]
    * @Return boolean
    */
    @Override
    public boolean releaseProductStock(ProductMessage productMessage) {
        ProductTaskDO productTaskDO = productTaskService.getById(productMessage.getTaskId());
        if(productTaskDO == null){
            log.error("工作单不存在");
        }
        if (productTaskDO.getLockState().equalsIgnoreCase(StockTaskStateEnum.LOCK.name())){
            JsonData jsonData = productOrderFeignSerivce.queryProductOrderState(productMessage.getOutTradeNo());
            if(jsonData.getCode()==0){
                String data = jsonData.getData().toString();
                if (data.equalsIgnoreCase(ProductOrderStateEnum.NEW.name())){
                    log.warn("订单状态是NEW,返回给消息队列，重新投递:{}", productMessage);
                    return false;
                }
                if (data.equalsIgnoreCase(ProductOrderStateEnum.PAY.name())){
                    productTaskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    productTaskService.updateById(productTaskDO);
                    return true;
                }
            }
            // 如果订单不存在或已被取消，回滚库存并修改任务状态为CANCEL
            log.warn("订单不存在，或者订单被取消，确认消息,修改task状态为CANCEL,恢复商品库存,message:{}", productMessage);
            ProductDO productDO = getById(productTaskDO.getProductId());
            LambdaUpdateWrapper<ProductDO> lambdaUpdateWrapper =  new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ProductDO::getId, productTaskDO.getProductId());
            lambdaUpdateWrapper.set(ProductDO::getLockStock, productDO.getLockStock() - productTaskDO.getBuyNum());
            update(lambdaUpdateWrapper);
            // 修改任务状态为CANCEL
            productTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            productTaskService.updateById(productTaskDO);

            return true;
        }
        return true;
    }

    /**
    * @Author: lhb
    * @Description: 添加商品信息并同步到Elasticsearch。
     * 该方法首先设置商品的创建时间和锁定库存数量，然后保存商品信息到数据库，
     * 最后将商品信息同步到Elasticsearch。
    * @DateTime: 上午10:44 2025/3/28
    * @Params: [productDO]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData addProduct(ProductDO productDO) {
        productDO.setCreateTime(new Date());
        productDO.setLockStock(0);
        save(productDO);
        log.info("保存商品信息");
        // 同步商品信息到Elasticsearch
        syncToES(productDO);
        return JsonData.buildSuccess();
    }

    /**
    * @Author: lhb
    * @Description: 根据关键字和价格范围搜索产品，并返回搜索结果。
    * @DateTime: 上午10:54 2025/3/28
    * @Params: [keyword, minPrice, maxPrice]
    * @Return com.buka.util.JsonData
    */
    @Override
    public JsonData searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        // 构建布尔查询，用于组合多个查询条件
        BoolQueryBuilder builder = new BoolQueryBuilder();

        // 如果关键字不为空，则添加标题匹配查询
        if (keyword != null && !keyword.isEmpty()) {
            builder.must(QueryBuilders.matchQuery("title", keyword));
        }

        // 添加价格范围查询
        if (minPrice != null) {
            builder.filter(QueryBuilders.rangeQuery("amount").gte(minPrice.doubleValue()));
        }
        if (maxPrice != null) {
            builder.filter(QueryBuilders.rangeQuery("amount").lte(maxPrice.doubleValue()));
        }

        // 构建高亮显示，用于在搜索结果中突出显示匹配的关键字
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .preTags("<em>")
                .postTags("</em>");

        // 构建完整的搜索查询，包含查询条件和高亮显示设置
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(builder)
                .withHighlightBuilder(highlightBuilder)
                .build();

        // 执行搜索并获取搜索结果
        SearchHits<ProductDocument> search = elasticsearchRestTemplate.search(searchQuery, ProductDocument.class);

        // 将搜索结果转换为产品文档列表
        List<ProductDocument> documents = search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // 返回包含搜索结果的JsonData对象
        return JsonData.buildSuccess(documents);
    }

    /**
    * @Author: lhb
    * @Description: 异步将商品信息同步到Elasticsearch中。
     * 该方法会将传入的商品对象转换为Elasticsearch文档格式，并保存到Elasticsearch中。
     * 如果同步过程中发生异常，会记录日志以便后续补偿处理。
    * @DateTime: 上午10:46 2025/3/28
    * @Params: [productDO]
    * @Return void
    */
    private void syncToES(ProductDO productDO) {
        try {
            // 将ProductDO对象转换为ProductDocument对象
            ProductDocument productDocument = new ProductDocument();
            BeanUtils.copyProperties(productDO, productDocument);

            // 格式化创建时间并设置到ProductDocument中
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss");
            String format = simpleDateFormat.format(productDO.getCreateTime());
            productDocument.setCreateTime(format);

            // 将ProductDocument保存到Elasticsearch中
            elasticsearchRestTemplate.save(productDocument);
        } catch (Exception e) {
            // 记录日志，后续补偿处理
            log.error("同步商品到ES失败，productId={}", productDO.getId(), e);
        }
    }
}

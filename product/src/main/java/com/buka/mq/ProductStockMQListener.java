package com.buka.mq;

import com.buka.model.ProductMessage;
import com.buka.service.ProductService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/7 下午4:10
 */
@Component
@Slf4j
@RabbitListener(queues = "${mqconfig.stock_release_routing_key}")
public class ProductStockMQListener {
    @Autowired
    private ProductService productService;
    @RabbitHandler
    public void handle(ProductMessage productMessage, Message message, Channel channel) throws IOException {
        // 记录接收到的消息内容
        log.info("监听到消息：releaseProductStock消息内容：{}", productMessage);

        // 获取消息的唯一标识符（deliveryTag），用于后续确认或拒绝消息
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 调用商品服务，尝试释放商品库存
        boolean flag = productService.releaseProductStock(productMessage);

        // 根据库存释放结果，确认或拒绝消息
        if (flag) {
            // 库存释放成功，确认消息已被消费
            channel.basicAck(deliveryTag, false);
        } else {
            // 库存释放失败，拒绝消息并记录错误日志
            channel.basicReject(deliveryTag, true);
            log.error("释放商品库存失败 flag=false,{}", productMessage);
        }
    }
}

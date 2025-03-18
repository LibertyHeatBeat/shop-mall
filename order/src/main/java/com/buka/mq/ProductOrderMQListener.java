package com.buka.mq;

import com.buka.model.OrderMessage;
import com.buka.service.ProductOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lhb
 * @version 1.0
 * @description: 监听消息
 * @date 2025/3/18 下午4:15
 */
@Slf4j
@Component
@RabbitListener(queues = "${order_close_queue}")
public class ProductOrderMQListener {
    @Autowired
    private ProductOrderService productOrderService;

    @RabbitHandler
    public void setProductOrderService(OrderMessage orderMessage, Message message, Channel channel){
        log.info("订单服务收到消息：{}", orderMessage);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = productOrderService.closeProductOrder(orderMessage.getOutTradeNo());
        try {
            if (flag){
                channel.basicAck(deliveryTag,false);
            }else {
                channel.basicReject(deliveryTag,true);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("订单服务处理消息失败：{}",orderMessage);
        }
    }
}

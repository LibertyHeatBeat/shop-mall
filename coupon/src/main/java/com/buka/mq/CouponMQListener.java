package com.buka.mq;

import com.buka.model.CouponRecordMessage;
import com.buka.service.CouponRecordService;
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
 * @description: TODO
 * @date 2025/3/9 下午6:56
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.coupon_release_queue}")
public class CouponMQListener {
    @Autowired
    private CouponRecordService couponRecordService;
    /**
    * @Author: lhb
    * @Description: 处理RabbitMQ消息的方法，用于释放优惠券记录。
     *      * 该方法监听RabbitMQ队列中的消息，并根据消息内容执行相应的业务逻辑。
    * @DateTime: 下午6:59 2025/3/9
    * @Params: [recordMessage, message, channel]
    * @Return void
    */
    @RabbitHandler
    public void process(CouponRecordMessage recordMessage, Message message, Channel channel) throws Exception {
        // 记录接收到的消息内容
        log.info("监听到消息：releaseCouponRecord消息内容：{}", recordMessage);

        // 获取消息的唯一标识符（deliveryTag），用于后续确认消息消费状态
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 调用服务层方法，尝试释放优惠券记录
        boolean flag = couponRecordService.releaseCouponRecord(recordMessage);

        // 根据业务处理结果，确认消息的消费状态
        if (flag) {
            // 业务处理成功，确认消息消费成功
            channel.basicAck(deliveryTag, false);
        } else {
            // 业务处理失败，拒绝消息并重新入队
            channel.basicReject(deliveryTag, true);
        }
    }
}

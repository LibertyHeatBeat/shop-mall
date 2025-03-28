package com.buka.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
/**
 * @author lhb
 * @version 1.0
 * @description: TODO
 * @date 2025/3/9 下午4:57
 */
@Configuration
@Data
public class RabbitMQConfig {


    /**
     * 交换机
     */
    @Value("${mqconfig.coupon_event_exchange}")
    private String eventExchange;


    /**
     * 第一个队列  延迟队列，
     */
    @Value("${mqconfig.coupon_release_delay_queue}")
    private String couponReleaseDelayQueue;

    /**
     * 第一个队列的路由key
     * 进入队列的路由key
     */
    @Value("${mqconfig.coupon_release_delay_routing_key}")
    private String couponReleaseDelayRoutingKey;


    /**
     * 第二个队列，被监听恢复库存的队列
     */
    @Value("${mqconfig.coupon_release_queue}")
    private String couponReleaseQueue;

    /**
     * 第二个队列的路由key
     *
     * 即进入死信队列的路由key
     */
    @Value("${mqconfig.coupon_release_routing_key}")
    private String couponReleaseRoutingKey;

    /**
     * 过期时间
     */
    @Value("${mqconfig.ttl}")
    private Integer ttl;


    /**
     * 消息转换器
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    /**
     * 创建交换机 Topic类型，也可以用dirct路由
     * 一般一个微服务一个交换机
     * @return
     */
    @Bean
    public Exchange couponEventExchange(){
        return new TopicExchange(eventExchange,true,false);
    }


    /**
     * 延迟队列
     */
    @Bean
    public Queue couponReleaseDelayQueue(){

        Map<String,Object> args = new HashMap<>(3);
        args.put("x-message-ttl",ttl);
        args.put("x-dead-letter-routing-key",couponReleaseRoutingKey);
        args.put("x-dead-letter-exchange",eventExchange);

        return new Queue(couponReleaseDelayQueue,true,false,false,args);
    }


    /**
     * 死信队列，普通队列，用于被监听
     */
    @Bean
    public Queue couponReleaseQueue(){

        return new Queue(couponReleaseQueue,true,false,false);

    }


    /**
     * 第一个队列，即延迟队列的绑定关系建立
     * @return
     */
    @Bean
    public Binding couponReleaseDelayBinding(){

        return new Binding(couponReleaseDelayQueue,Binding.DestinationType.QUEUE,eventExchange,couponReleaseDelayRoutingKey,null);
    }

    /**
     * 死信队列绑定关系建立
     * @return
     */
    @Bean
    public Binding couponReleaseBinding(){

        return new Binding(couponReleaseQueue,Binding.DestinationType.QUEUE,eventExchange,couponReleaseRoutingKey,null);
    }

}

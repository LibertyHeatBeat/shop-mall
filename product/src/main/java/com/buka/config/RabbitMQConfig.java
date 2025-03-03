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
 * @description: RabbitMQ配置类
 * @date 2025/3/3 下午6:52
 */
@Configuration
@Data
public class RabbitMQConfig {
    /**
     * 交换机
     */
    @Value("${mqconfig.stock_event_exchange}")
    private String eventExchange;


    /**
     * 第一个队列延迟队列，
     */
    @Value("${mqconfig.stock_release_delay_queue}")
    private String stockReleaseDelayQueue;

    /**
     * 第一个队列的路由key
     * 进入队列的路由key
     */
    @Value("${mqconfig.stock_release_delay_routing_key}")
    private String stockReleaseDelayRoutingKey;


    /**
     * 第二个队列，被监听恢复库存的队列
     */
    @Value("${mqconfig.stock_release_queue}")
    private String stockReleaseQueue;

    /**
     * 第二个队列的路由key
     *
     * 即进入死信队列的路由key
     */
    @Value("${mqconfig.stock_release_routing_key}")
    private String stockReleaseRoutingKey;

    /**
     * 过期时间
     */
    @Value("${mqconfig.ttl}")
    private Integer ttl;


    //交换机
    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange(eventExchange,true,false);
    }


    //普通队列
    @Bean
    public Queue stockReleaseDelayQueue(){
        Map<String,Object> map=new HashMap<>();
        map.put("x-message-ttl",ttl);
        map.put("x-dead-letter-exchange",eventExchange);
        map.put("x-dead-letter-routing-key",stockReleaseRoutingKey);
        return new Queue(stockReleaseDelayQueue,true,false,false,map);
    }


    /**
     * 死信队列，普通队列，用于被监听
     */
    @Bean
    public Queue stockReleaseQueue(){

        return new Queue(stockReleaseQueue,true,false,false);

    }

    /**
     * 第一个队列，即延迟队列的绑定关系建立
     * @return
     */
    @Bean
    public Binding stockReleaseDelayBinding(){

        return new Binding(stockReleaseDelayQueue,Binding.DestinationType.QUEUE,eventExchange,stockReleaseDelayRoutingKey,null);
    }

    /**
     * 死信队列绑定关系建立
     * @return
     */
    @Bean
    public Binding stockReleaseBinding(){

        return new Binding(stockReleaseQueue,Binding.DestinationType.QUEUE,eventExchange,stockReleaseRoutingKey,null);
    }



    /**
     * 消息转换器
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}

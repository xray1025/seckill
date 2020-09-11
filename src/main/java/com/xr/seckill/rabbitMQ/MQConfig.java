package com.xr.seckill.rabbitMQ;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String TOPIC_EXCHANGE = "topicExchage";
    public static final String FANOUT_EXCHANGE = "fanoutExchage";


    /**
     * Direct模式 交换机Exchange
     * 发送者先发送到交换机上，然后交换机作为路由再将信息发到队列，
     * */
    @Bean
    public Queue queue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    /**
     * Topic模式 交换机Exchange
     * */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }
    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    //绑定
    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
    }


//    /**
//     * Fanout 广播
//     */
//    @Bean
//    public FanoutExchange fanoutExchange(){
//        return new FanoutExchange(FANOUT_EXCHANGE);
//    }
//    //绑定
//    @Bean
//    public Binding fanoutBinding1() {
//        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
//    }
//    @Bean
//    public Binding fanoutBinding2() {
//        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
//    }

}

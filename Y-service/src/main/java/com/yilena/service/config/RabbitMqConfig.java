package com.yilena.service.config;

import com.yilena.service.constant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());  // 设置转换器
        return factory;
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(MqConstant.CRUD_EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(MqConstant.USER_QUEUE);
    }

    @Bean
    public Queue followQueue() {
        return new Queue(MqConstant.FOLLOW_QUEUE);
    }

    @Bean
    public Queue collectionQueue() {
        return new Queue(MqConstant.COLLECTION_QUEUE);
    }

    @Bean
    public Queue favoritesQueue() {
        return new Queue(MqConstant.FAVORITES_QUEUE);
    }

    @Bean
    public Queue videoQueue() {
        return new Queue(MqConstant.VIDEO_QUEUE);
    }

    @Bean
    public Queue postQueue() {
        return new Queue(MqConstant.POST_QUEUE);
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(MqConstant.CHAT_QUEUE);
    }

    @Bean
    public Queue historyQueue() {
        return new Queue(MqConstant.HISTORY_QUEUE);
    }

    @Bean
    public Queue hotSearchQueue() {
        return new Queue(MqConstant.HOT_SEARCH_QUEUE);
    }

    @Bean
    public Queue videoFavoritesQueue() {
        return new Queue(MqConstant.VIDEO_FAVORITES_QUEUE);
    }

    @Bean
    public Binding UserBinding(DirectExchange directExchange, Queue userQueue) {
        return BindingBuilder.bind(userQueue)
                .to(directExchange)
                .with(MqConstant.USER_BINDING_KEY);
    }

    @Bean
    public Binding FollowBinding(DirectExchange directExchange, Queue followQueue) {
        return BindingBuilder.bind(followQueue)
                .to(directExchange)
                .with(MqConstant.FOLLOW_BINDING_KEY);
    }

    @Bean
    public Binding CollectionBinding(DirectExchange directExchange, Queue collectionQueue) {
        return BindingBuilder.bind(collectionQueue)
                .to(directExchange)
                .with(MqConstant.COLLECTION_BINDING_KEY);
    }

    @Bean
    public Binding FavoritesBinding(DirectExchange directExchange, Queue favoritesQueue) {
        return BindingBuilder.bind(favoritesQueue)
                .to(directExchange)
                .with(MqConstant.FAVORITES_BINDING_KEY);
    }

    @Bean
    public Binding VideoBinding(DirectExchange directExchange, Queue videoQueue) {
        return BindingBuilder.bind(videoQueue)
                .to(directExchange)
                .with(MqConstant.VIDEO_BINDING_KEY);
    }

    @Bean
    public Binding PostBinding(DirectExchange directExchange, Queue postQueue) {
        return BindingBuilder.bind(postQueue)
                .to(directExchange)
                .with(MqConstant.POST_BINDING_KEY);
    }

    @Bean
    public Binding ChatBinding(DirectExchange directExchange, Queue chatQueue) {
        return BindingBuilder.bind(chatQueue)
                .to(directExchange)
                .with(MqConstant.CHAT_BINDING_KEY);
    }

    @Bean
    public Binding HistoryBinding(DirectExchange directExchange, Queue historyQueue) {
        return BindingBuilder.bind(historyQueue)
                .to(directExchange)
                .with(MqConstant.HISTORY_BINDING_KEY);
    }

    @Bean
    public Binding HotSearchBinding(DirectExchange directExchange, Queue hotSearchQueue) {
        return BindingBuilder.bind(hotSearchQueue)
                .to(directExchange)
                .with(MqConstant.HOT_SEARCH_BINDING_KEY);
    }

    @Bean
    public Binding VideoFavoritesBinding(DirectExchange directExchange, Queue videoFavoritesQueue) {
        return BindingBuilder.bind(videoFavoritesQueue)
                .to(directExchange)
                .with(MqConstant.VIDEO_FAVORITES_BINDING_KEY);
    }
}

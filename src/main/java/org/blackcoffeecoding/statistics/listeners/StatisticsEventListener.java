package org.blackcoffeecoding.statistics.listeners;

import com.rabbitmq.client.Channel; // <-- Важный импорт
import org.blackcoffeecoding.device.events.DeviceCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders; // <-- Важный импорт
import org.springframework.messaging.handler.annotation.Header; // <-- Важный импорт
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatisticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(StatisticsEventListener.class);

    private final Map<String, Integer> stats = new ConcurrentHashMap<>();

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "statistics-queue", durable = "true"),
            exchange = @Exchange(name = "devices-exchange", type = "topic", durable = "true"),
            key = "device.created"
    ))
    public void handleDeviceCreated(DeviceCreatedEvent event,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {

            stats.merge(event.category(), 1, Integer::sum);

            log.info("STATS: Учтено новое устройство: {}. Категория: {}. Всего в категории: {}",
                    event.name(), event.category(), stats.get(event.category()));

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Ошибка при обновлении статистики", e);

            channel.basicNack(deliveryTag, false, false);
        }
    }
}
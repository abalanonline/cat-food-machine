/*
 * Copyright 2020 Aleksei Balan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ab.catfood;

import ab.catfood.api.Meow;
import ab.catfood.api.MeowPub;
import ab.catfood.api.MeowSub;
import ab.catfood.api.Queue;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitConsumerProperties;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitProducerProperties;
import org.springframework.cloud.stream.binder.rabbit.provisioning.RabbitExchangeQueueProvisioner;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class LegacyWheel<M> implements MeowPub<M>, MeowSub<M> {

  @Autowired
  AmqpTemplate amqpTemplate;

  private RabbitExchangeQueueProvisioner provisioningProvider;

  private SimpleMessageListenerContainer container;

  public LegacyWheel(@Autowired ConnectionFactory rabbitConnectionFactory) {
    container = new SimpleMessageListenerContainer(rabbitConnectionFactory);
    provisioningProvider = new RabbitExchangeQueueProvisioner(rabbitConnectionFactory);
  }

  @SneakyThrows
  public byte[] serialize(M object) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(object);
    return byteArrayOutputStream.toByteArray();
  }

  @Override
  public void pub(Queue queue, Meow<M> meow) {
    provisioningProvider.provisionProducerDestination(
        queue.getQueueName(),
        new ExtendedProducerProperties<>(new RabbitProducerProperties()));

    MessageProperties properties = new MessageProperties();
    properties.getHeaders().putAll(meow.getHeaders());
    amqpTemplate.send(queue.getQueueName(), new Message(serialize(meow.getData()), properties));
  }

  @SneakyThrows
  public M deserialize(byte[] bytes) {
    return (M) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
  }

  @Override
  public void sub(Queue queue, BiConsumer<Map<String, String>, M> consumer) {
    RabbitConsumerProperties rabbitConsumerProperties = new RabbitConsumerProperties();
    rabbitConsumerProperties.setQueueNameGroupOnly(true);
    provisioningProvider.provisionConsumerDestination(
        queue.getQueueName(), queue.getQueueName(),
        new ExtendedConsumerProperties<>(rabbitConsumerProperties));

    container.addQueueNames(queue.getQueueName());
    container.setMessageListener((MessageListener) message -> {
      consumer.accept(
          message.getMessageProperties().getHeaders().entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))),
          deserialize(message.getBody()));
    });
    container.start();
  }

}

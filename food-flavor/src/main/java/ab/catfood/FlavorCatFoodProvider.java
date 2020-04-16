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
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@Service
public class FlavorCatFoodProvider<M> implements MeowPub<M>, MeowSub<M>, MessageListener {

  JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

  private JmsListenerContainerFactory<?> jmsListenerContainerFactory;

  private JmsTemplate jmsTemplate;

  BiConsumer<Map<String, String>, M> consumer;

  public FlavorCatFoodProvider(ConnectionFactory jmsConnectionFactory, JmsListenerEndpointRegistry jmsListenerEndpointRegistry) {

    // create JmsTemplate
    jmsTemplate = new JmsTemplate(jmsConnectionFactory);
    jmsTemplate.setPubSubDomain(false);

    // create JmsListenerContainerFactory
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setPubSubDomain(false);
    factory.setConnectionFactory(jmsConnectionFactory);
    //factory.setSessionTransacted(true);
    //factory.setAutoStartup(true);
    jmsListenerContainerFactory = factory;

    this.jmsListenerEndpointRegistry = jmsListenerEndpointRegistry;
  }

  @SneakyThrows
  @Override
  public void onMessage(Message message) {
    Map<String, String> headers = new HashMap<>();
    final Enumeration<String> propertyNames = (Enumeration<String>) message.getPropertyNames();
    while (propertyNames.hasMoreElements()) {
      String name = propertyNames.nextElement();
      headers.put(name, message.getStringProperty(name));
    }
    consumer.accept(headers, (M) jmsTemplate.getMessageConverter().fromMessage(message));
  }

  @Override
  public void pub(Queue queue, Meow<M> meow) {
    jmsTemplate.convertAndSend(queue.getQueueName(), meow.getData(), message -> {
      Map<String, String> headers = meow.getHeaders();
      for (Map.Entry<String,String> header : headers.entrySet()) {
        message.setStringProperty(header.getKey(), header.getValue());
      }
      return message;
    });
  }

  @Override
  public void sub(Queue queue, BiConsumer<Map<String, String>, M> consumer) {
    this.consumer = consumer;
    SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setMessageListener(this);
    endpoint.setDestination(queue.getQueueName());
    endpoint.setId(this.getClass().getName() + "#" + UUID.randomUUID().toString());
    //MessageListenerContainer listenerContainer = jmsListenerContainerFactory.createListenerContainer(endpoint);
    jmsListenerEndpointRegistry.registerListenerContainer(endpoint, jmsListenerContainerFactory, true);
  }

}

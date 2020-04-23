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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

@Slf4j
@Service
public class AromaCatFoodMessageSupport extends MessageProducerSupport implements MessageHandler, MessageChannel, MessageListener {

  JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

  private JmsListenerContainerFactory<?> jmsListenerContainerFactory;

  private JmsTemplate jmsTemplate;

  ProducerDestination producerDestination;

  ConsumerDestination consumerDestination;

  public AromaCatFoodMessageSupport(ConnectionFactory jmsConnectionFactory, JmsListenerEndpointRegistry jmsListenerEndpointRegistry) {

    // create JmsTemplate
    jmsTemplate = new JmsTemplate(jmsConnectionFactory);
    jmsTemplate.setPubSubDomain(true);
    //jmsTemplate.setMessageConverter // we don't need it for byte arrays

    // create JmsListenerContainerFactory
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setPubSubDomain(true);
    factory.setConnectionFactory(jmsConnectionFactory);
    //factory.setSessionTransacted(true);
    //factory.setAutoStartup(true);
    jmsListenerContainerFactory = factory;

    this.jmsListenerEndpointRegistry = jmsListenerEndpointRegistry;
    setOutputChannel(this); // set to mock before outputChannel is configured
  }

  @SneakyThrows
  @Override
  public void onMessage(javax.jms.Message message) {
    Map<String, Object> headers = new HashMap<>();
    final Enumeration<String> propertyNames = (Enumeration<String>) message.getPropertyNames();
    while (propertyNames.hasMoreElements()) {
      String name = propertyNames.nextElement();
      headers.put(name, message.getStringProperty(name));
    }
    // FIXME need to generify headers serialization, UUIDs are not accepted by JmsTemplate
    //  though they are protected in MessageHeaders and this code is useless
    headers.put(MessageHeaders.ID, UUID.fromString((String) headers.get(MessageHeaders.ID)));
    headers.put(MessageHeaders.TIMESTAMP, new Long((String) headers.get(MessageHeaders.TIMESTAMP)));
    sendMessage(new GenericMessage<>(jmsTemplate.getMessageConverter().fromMessage(message), headers));
  }

  @Override
  public void handleMessage(Message<?> message) {
    jmsTemplate.convertAndSend(this.producerDestination.getName(), message.getPayload(), m -> {
      for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
        m.setStringProperty(header.getKey(), header.getValue().toString());
      }
      // FIXME a lot of excessive debugging here
      m.setStringProperty("debug-" + MessageHeaders.ID, String.valueOf(message.getHeaders().get(MessageHeaders.ID)));
      m.setStringProperty("debug-" + MessageHeaders.TIMESTAMP, String.valueOf(message.getHeaders().get(MessageHeaders.TIMESTAMP)));
      return m;
    });
  }

  @Override // dummy MessageChannel
  public boolean send(Message<?> message) {
    throw new IllegalStateException("Output MessageChannel used before initializing");
  }

  @Override // dummy MessageChannel
  public boolean send(Message<?> message, long l) {
    throw new IllegalStateException("Output MessageChannel used before initializing");
  }

  public void setProducerDestination(ProducerDestination producerDestination) {
    this.producerDestination = producerDestination;
  }

  public void setConsumerDestination(ConsumerDestination consumerDestination) {
    this.consumerDestination = consumerDestination;

    SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setMessageListener(this);
    endpoint.setDestination(this.consumerDestination.getName());
    endpoint.setId(this.getClass().getName() + "#" + UUID.randomUUID().toString());
    //MessageListenerContainer listenerContainer = jmsListenerContainerFactory.createListenerContainer(endpoint);
    jmsListenerEndpointRegistry.registerListenerContainer(endpoint, jmsListenerContainerFactory, true);
  }
}

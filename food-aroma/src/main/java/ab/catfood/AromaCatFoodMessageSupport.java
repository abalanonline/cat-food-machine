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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import javax.jms.MessageListener;
import java.util.UUID;

@Slf4j
@Service
public class AromaCatFoodMessageSupport extends MessageProducerSupport implements MessageHandler, Runnable, MessageChannel, MessageListener {

  @Autowired
  JmsTemplate jmsTemplate;

  @Autowired
  JmsListenerContainerFactory<?> jmsListenerContainerFactory;
  // let Spring choose Simple or Default implementation

  @Autowired
  JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

  ProducerDestination producerDestination;

  ConsumerDestination consumerDestination;

  public AromaCatFoodMessageSupport() {
    setOutputChannel(this); // set to mock before outputChannel is configured
  }

  @Override
  public void run() { // clumsy receiver, not used
    while (true) {
      try {
        final Message<?> message = (Message<?>) jmsTemplate.receiveAndConvert(this.consumerDestination.getName());
        sendMessage(message);
      } catch (RuntimeException e) {
        log.error("Message receive error, receiver shut down", e);
        break; // exiting
      }
    }
  }

  @SneakyThrows
  @Override
  public void onMessage(javax.jms.Message message) {
    sendMessage((Message<?>) jmsTemplate.getMessageConverter().fromMessage(message));
  }

  @Override
  public void handleMessage(Message<?> message) {
    jmsTemplate.convertAndSend(this.producerDestination.getName(), message);
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
    jmsTemplate.setPubSubDomain(true); // FIXME JMS singleton switched to pub/sub
    this.producerDestination = producerDestination;
  }

  public void setConsumerDestination(ConsumerDestination consumerDestination) {
    jmsTemplate.setPubSubDomain(true); // FIXME JMS singleton switched to pub/sub
    this.consumerDestination = consumerDestination;
    //new Thread(this, AromaCatFoodMessageSupport.class.getSimpleName()).start();

    SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setMessageListener(this);
    endpoint.setDestination(this.consumerDestination.getName());
    endpoint.setId(this.getClass().getName() + "#" + UUID.randomUUID().toString());
    jmsListenerEndpointRegistry.registerListenerContainer(endpoint, jmsListenerContainerFactory, true);
  }
}

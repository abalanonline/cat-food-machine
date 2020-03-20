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

package ab.catmachine;

import ab.catmachine.api.Meow;
import ab.catmachine.api.MeowPub;
import ab.catmachine.api.MeowSub;
import ab.catmachine.api.Queue;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class MeowSqs implements ConnectionFactory, MeowPub<CatFood>, MeowSub<CatFood> {

  private final ConnectionFactory connectionFactory;

  @Autowired
  private JmsTemplate jmsTemplate;

  public MeowSqs() {
    // get configuration from local .aws
    this.connectionFactory = new SQSConnectionFactory(new ProviderConfiguration());
  }

  @Override
  public Connection createConnection() throws JMSException {
    return connectionFactory.createConnection();
  }

  @Override
  public Connection createConnection(String s, String s1) throws JMSException {
    return connectionFactory.createConnection(s, s1);
  }

  @Override
  public void pub(Queue queue, Meow<CatFood> meow) {
    jmsTemplate.convertAndSend(queue.getQueueName(), meow.getData());
  }

  @Override
  public void sub(Queue queue, BiConsumer<Map<String, String>, CatFood> consumer) {
    //jmsTemplate.receiveAndConvert("");
    // TODO ???
  }

}

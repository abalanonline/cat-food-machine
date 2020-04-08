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

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.springframework.stereotype.Service;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@Service
public class MeowRabbit implements ConnectionFactory {

  private final ConnectionFactory connectionFactory;

  public MeowRabbit() {
    this.connectionFactory = new RMQConnectionFactory();
  }

  @Override
  public Connection createConnection() throws JMSException {
    return connectionFactory.createConnection();
  }

  @Override
  public Connection createConnection(String s, String s1) throws JMSException {
    return connectionFactory.createConnection(s, s1);
  }

}

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

import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.stereotype.Service;

@Service
public class EmptyBinder<T> implements Binder<T, ConsumerProperties, ProducerProperties>, Binding<T> {

  @Override
  public Binding<T> bindConsumer(String name, String group, T inboundBindTarget, ConsumerProperties consumerProperties) {
    return this;
  }

  @Override
  public Binding<T> bindProducer(String name, T outboundBindTarget, ProducerProperties producerProperties) {
    return this;
  }

  @Override
  public void unbind() {
  }

}

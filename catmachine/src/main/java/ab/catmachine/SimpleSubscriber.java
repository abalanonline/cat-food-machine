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

import ab.catfood.api.MeowSub;
import ab.catfood.api.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class SimpleSubscriber implements BiConsumer<Map<String, String>, CatFood> {

  //@Value("${catmachine.queue}") // FIXME failed to make it work
  private String queueName = "test-ctm-queue";

  @Autowired
  public SimpleSubscriber(MeowSub<CatFood> sub) {
    Queue queue = new Queue(queueName);
    //sub.sub(queue, this);
  }

  @Override
  public void accept(Map<String, String> stringStringMap, CatFood catFood) {
    log.info("Catmachine idle " + catFood.getUuid());
  }

}

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

import ab.catfood.api.Meow;
import ab.catfood.api.MeowPub;
import ab.catfood.api.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Component
public class Timer {

	@Autowired
	MeowPub<CatFood> pub;

	@Value("${catmachine.queue}")
	private String queueName;

	@Scheduled(fixedRate = 10000)
	public void click() {
		log.info("Catmachine idle");
		Queue queue = new Queue(queueName);
		Meow<CatFood> meow = new Meow<>(new HashMap<>(), new CatFood(UUID.randomUUID()));
		pub.pub(queue, meow);
	}
}

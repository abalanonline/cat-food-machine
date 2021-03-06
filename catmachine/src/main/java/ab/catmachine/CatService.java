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
import ab.catfood.api.MeowSub;
import ab.catfood.api.Queue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bus.ServiceMatcher;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.bus.event.UnknownRemoteApplicationEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.jms.ConnectionFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

/**
 * Cat as a service (CaaS) is a category of companion animal services
 * that provides a cat that can be watched and played with
 */
@Slf4j
@Service
public class CatService implements BiConsumer<Map<String, String>, CatFood> {

  String[] popularCatNames = {
      "Max", "Tiger", "Chloe", "Tigger", "Kitty", "Molly", "Baby", "Smokey", "Lucy", "Charlie", "Angel", "Blackie",
      "Bella", "Simon", "Samantha", "Oliver", "Shadow", "Jack", "Princess", "Tom", "Sam", "Sophie", "Sammy", "Misty",
      "Buddy", "Patches", "Casper", "Oreo", "Cleo", "Sassy", "Sylvester", "Lucky", "Muffin", "Simba", "Whiskers",
      "Fluffy", "Lily", "Fraidy", "Oscar", "Maggie", "Patch", "Scaredy"};

  String[] popularCatBehaviours = {
      "jumped", "kneaded", "twitched ears", "opened mouth", "laid down", "leaped", "stretched", "twitched tail",
      "drooled", "curled up", "grumped", "bounced", "closed eyes", "scratched", "curl tail", "blinked", "poked",
      "licked", "flatten ears", "rubbed", "got excited"};

  public static final Queue QUEUE = new Queue("test-ctm-queue");

  @Autowired
  private ServiceMatcher serviceMatcher;

  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  public void somethingACatDoes(String something) {
    String threadName = Thread.currentThread().getName();
    if (threadName.startsWith("DefaultMessageListenerContainer-")) {
      threadName = threadName.substring(32);
      if ("1".equals(threadName)) {
        threadName = "";
      } else {
        threadName = "-yarn" + threadName;
      }
    } else {
      threadName = "";
    }
    somethingASomeoneDoes(something, firstName + threadName);
  }

  public void somethingASomeoneDoes(String something, String someone) {
    new Thread(() -> applicationEventPublisher.publishEvent(new StringEvent(someone + " " + something))).start();
  }

  @EventListener
  public void onRefreshScopeRefreshed(final RefreshScopeRefreshedEvent event) {
    somethingACatDoes(randomBehaviour() + " and meowed");
  }

  @EventListener
  public void onRemoteApplicationEvent(final RemoteApplicationEvent event) {
    final String string;
    try {
      string = new StringEvent(event).getString();
    } catch (Exception e) {
      return; // could not create StringEvent - do nothing
    }
    log.info(string);
    if (string.contains("meow")) runCatMachine();
  }

  @SneakyThrows
  public void runCatMachine() {
    Thread.sleep(500 + ThreadLocalRandom.current().nextInt(500));
    for (int i = 0; i < 4; i++) {
      final CatFood catFood = new CatFood(UUID.randomUUID());
      Meow<CatFood> meow = new Meow<>(new HashMap<>(), catFood);
      somethingASomeoneDoes("delivered " + catFood, "Catmachine");
      pub.pub(QUEUE, meow);
      Thread.sleep(ThreadLocalRandom.current().nextInt(500));
    }
  }

  private final String firstName;

  private Instant chatteringTime;

  @Autowired
  private MeowPub<CatFood> pub;

  @Autowired
  public CatService(MeowSub<CatFood> sub) {
    this.firstName = popularCatNames[ThreadLocalRandom.current().nextInt(popularCatNames.length)]
        + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
    this.chatteringTime = Instant.now().plus(Duration.ofSeconds(4 + ThreadLocalRandom.current().nextInt(4)));
    sub.sub(QUEUE, this);
  }

  public String randomBehaviour() {
    return popularCatBehaviours[ThreadLocalRandom.current().nextInt(popularCatBehaviours.length)];
  }

  @Override
  public void accept(Map<String, String> stringStringMap, CatFood catFood) {
    somethingACatDoes("picked " + catFood);
    try {
      consumeFood(catFood);
      somethingACatDoes("consumed " + catFood + " and " + randomBehaviour());
    } catch (Exception e) {
      somethingACatDoes("could not make it: " + e.getMessage());
      throw e;
    }
  }

  @SneakyThrows
  private void consumeFood(CatFood catFood) {
    //final boolean debug = true; if (debug) { Thread.sleep(500); return; }
    // inedible yarn have zero consumption time creating bursts of exceptions
    int msToConsume = catFood.getFoodType() * 500;
    //msToConsume += 5000;
    final Instant newChatteringTime = Instant.now().plus(Duration.ofMillis(5000 + msToConsume));
    if (newChatteringTime.isAfter(chatteringTime)) {
      this.chatteringTime = newChatteringTime;
    }
    Thread.sleep(msToConsume);
    if (catFood.getFoodType() == 0 || catFood.getTexture() == 0) throw new IllegalStateException("inedible");
    //if (catFood.getFoodType() < 10) throw new IllegalStateException("test");
  }

  @Scheduled(fixedRate = 1000)
  public void passTime() {
    if (Instant.now().isAfter(chatteringTime)) {
      log.debug("chatter at " + connectionFactory);
      somethingACatDoes(ThreadLocalRandom.current().nextBoolean() ? "meows loudly" : "chatter softly");
      chatteringTime = Instant.now().plus(Duration.ofSeconds(10));
    }
  }

  /**
   * It is clumsy but most reliable code I could make so far
   */
  public class StringEvent extends RemoteApplicationEvent {
    @Getter
    private final String string;

    public StringEvent(String string) {
      super(new Object(), serviceMatcher.getServiceId(), null);
      this.string = string;
    }

    public StringEvent(RemoteApplicationEvent event) {
      super(new Object(), serviceMatcher.getServiceId(), null);
      final UnknownRemoteApplicationEvent unknownRemoteApplicationEvent = (UnknownRemoteApplicationEvent) event;
      final String typeInfo = unknownRemoteApplicationEvent.getTypeInfo();
      Assert.isTrue(this.getClass().getName().endsWith(typeInfo), "not deserializable");
      try {
        HashMap payload = new ObjectMapper().readValue(unknownRemoteApplicationEvent.getPayload(), HashMap.class);
        this.string = String.valueOf(payload.get("string"));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

  }

}

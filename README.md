# cat-food-machine

delivers delicious food to cats

## Methods of delivering

* Topic with unshared non-durable subscriptions for aroma and sound
* Queue with (shared durable) consumers for food

Rated R: strong language and disturbing architecture

```text
Tigger38 bounced and meowed
Tigger38 catmachine delivered chopped chicken 14
Tigger38 catmachine delivered blended pork 39
Tigger38 catmachine delivered inedible whitefish 4f
Shadow12 picked chopped chicken 14
Tigger38 picked blended pork 39
Oscar08 picked inedible whitefish 4f
Tigger38 consumed blended pork 39 and drooled
Shadow12 consumed chopped chicken 14 and stretched
Listener threw exception
Caused by: java.lang.IllegalStateException: inedible
Shadow12 picked inedible whitefish 4f
Listener threw exception
Caused by: java.lang.IllegalStateException: inedible

```

## Multiyarning

```text
Shadow35 meows loudly
Catmachine delivered pate beef 38
Shadow35 picked pate beef 38
Catmachine delivered solid liver 1C
Catmachine delivered shredded pork 59
Shadow35-yarn2 picked shredded pork 59
Catmachine delivered solid salmon 11
Shadow35 consumed pate beef 38 and bounced
Shadow35 picked solid liver 1C
Shadow35-yarn2 consumed shredded pork 59 and leaped
```

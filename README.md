# Spring Batch Remote Chunking Example

## Example of spring batch distribution using RabbitMQ

Implementation of Remote Chunking in order to scale processing. 

## How to run:

* Run `docker-compose up -d`
* Load ENV variables from `env-consumer` and `env-producer` files.
* Run disarable amount of `me.flash.distributedbatch.consumer.ConsumerApplication` application instances:
 `gradle consumer:bootRun`.
* Run `me.flash.distributedbatch.producer.ProducerApplication` application: `gradle producer:bootRun`.
* Profit

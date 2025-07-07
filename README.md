# Learn Microservices with Spring Boot 3 - (Spring Cloud and Kafka)
This repository contains the source code of the practical use case described in the book [Learn Microservices with Spring Boot 3 (3rd Edition)](https://link.springer.com/book/10.1007/978-1-4842-9757-5).
And I made some changes to logback kafka appender to add headers https://github.com/danielwegener/logback-kafka-appender and add micrometer zipkin trace support.

## Features

The figure below shows a high-level overview of the final version of our system.

![Logical View - Chapter 8 (Final)](resources/microservice_patterns-Config-Server-1.png)

The main concepts included in this project are:

* Why do we need Centralized Logs and Distributed tracing?
* Why would I create Docker images for my applications?
* Building a simple logger application with Spring Boot and Kafka using logback-kafka-appender.
* Distributed traces with Micrometer and Zipkin.
* Building Docker images for Spring Boot applications with Cloud Native Buildpacks.
* Container Platforms, Application Platforms, and Cloud Services.

## Running the app

### Building the images yourself

First, build the application images with:

```bash
multiplication$ ./mvnw spring-boot:build-image
gamification$ ./mvnw spring-boot:build-image
gateway$ ./mvnw spring-boot:build-image
logs$ ./mvnw spring-boot:build-image
```

or

```bash
multiplication$ docker build -t  multiplication:0.0.1-SNAPSHOT .
gamification$ docker build -t  gamification:0.0.1-SNAPSHOT .
gateway$ docker build -t  gateway:0.0.1-SNAPSHOT .
logs$ docker build -t  logs:0.0.1-SNAPSHOT .
```

Then, build the consul importer from the `docker/consul` folder:

```bash
$ consul agent -node=learnmicro -dev
docker/consul$ consul kv export config/ > consul-kv-docker.json
docker/consul$ docker build -t consul-importer:1.0 .
```

And the UI server (first you have to build it with `npm run build`):

```bash
challenges-frontend$ npm install
challenges-frontend$ npm run build
challenges-frontend$ docker build -t challenges-frontend:1.0 .
```

Once you have all the images ready, run:

```bash
docker$ docker-compose up
```

See the figure below for a diagram showing the container view.

![Container View](resources/microservice_patterns-View-Containers.png)

Once the backend and the frontend are started, you can navigate to `http://localhost:3000` in your browser and start resolving multiplication challenges.

## Playing with Docker Compose

After the system is up and running, you can quickly scale up and down instances of both Multiplication and Gamification services. For example, you can run:

```bash
docker$ docker-compose up --scale multiplication=2 --scale gamification=2
```

And you'll get two instances of each of these services with proper Load Balancing and Service Discovery.


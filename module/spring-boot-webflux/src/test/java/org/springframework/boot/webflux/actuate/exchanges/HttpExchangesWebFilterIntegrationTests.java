/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.webflux.actuate.exchanges;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.Include;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Integration tests for {@link HttpExchangesWebFilter}.
 *
 * @author Andy Wilkinson
 */
class HttpExchangesWebFilterIntegrationTests {

	private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
		.withUserConfiguration(Config.class);

	@Test
	void exchangeForNotFoundResponseHas404Status() {
		this.contextRunner.run((context) -> {
			WebTestClient.bindToApplicationContext(context)
				.build()
				.get()
				.uri("/")
				.exchange()
				.expectStatus()
				.isNotFound();
			HttpExchangeRepository repository = context.getBean(HttpExchangeRepository.class);
			assertThat(repository.findAll()).hasSize(1);
			assertThat(repository.findAll().get(0).getResponse().getStatus()).isEqualTo(404);
		});
	}

	@Test
	void exchangeForMonoErrorWithRuntimeExceptionHas500Status() {
		this.contextRunner.run((context) -> {
			WebTestClient.bindToApplicationContext(context)
				.build()
				.get()
				.uri("/mono-error")
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			HttpExchangeRepository repository = context.getBean(HttpExchangeRepository.class);
			assertThat(repository.findAll()).hasSize(1);
			assertThat(repository.findAll().get(0).getResponse().getStatus()).isEqualTo(500);
		});
	}

	@Test
	void exchangeForThrownRuntimeExceptionHas500Status() {
		this.contextRunner.run((context) -> {
			WebTestClient.bindToApplicationContext(context)
				.build()
				.get()
				.uri("/thrown")
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			HttpExchangeRepository repository = context.getBean(HttpExchangeRepository.class);
			assertThat(repository.findAll()).hasSize(1);
			assertThat(repository.findAll().get(0).getResponse().getStatus()).isEqualTo(500);
		});
	}

	@Configuration(proxyBeanMethods = false)
	@EnableWebFlux
	static class Config {

		@Bean
		HttpExchangesWebFilter httpExchangesWebFilter(HttpExchangeRepository repository) {
			return new HttpExchangesWebFilter(repository, EnumSet.allOf(Include.class));
		}

		@Bean
		HttpExchangeRepository httpExchangeRepository() {
			return new InMemoryHttpExchangeRepository();
		}

		@Bean
		HttpHandler httpHandler(ApplicationContext applicationContext) {
			return WebHttpHandlerBuilder.applicationContext(applicationContext).build();
		}

		@Bean
		RouterFunction<ServerResponse> router() {
			return route(GET("/mono-error"), (request) -> Mono.error(new RuntimeException())).andRoute(GET("/thrown"),
					(HandlerFunction<ServerResponse>) (request) -> {
						throw new RuntimeException();
					});
		}

	}

}

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

package org.springframework.boot.session.autoconfigure;

import java.time.Duration;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SessionProperties}.
 *
 * @author Stephane Nicoll
 */
class SessionPropertiesTests {

	private final SessionProperties properties = new SessionProperties();

	@Test
	@SuppressWarnings("unchecked")
	void determineTimeoutWithTimeoutIgnoreFallback() {
		this.properties.setTimeout(Duration.ofMinutes(1));
		Supplier<Duration> fallback = mock(Supplier.class);
		assertThat(this.properties.determineTimeout(fallback)).isEqualTo(Duration.ofMinutes(1));
		then(fallback).shouldHaveNoInteractions();
	}

	@Test
	void determineTimeoutWithNoTimeoutUseFallback() {
		this.properties.setTimeout(null);
		Duration fallback = Duration.ofMinutes(2);
		assertThat(this.properties.determineTimeout(() -> fallback)).isSameAs(fallback);
	}

	@Test
	void defaultFilterOrderIsCloseToHighestPrecedence() {
		assertThat(this.properties.getServlet().getFilterOrder()).isCloseTo(Ordered.HIGHEST_PRECEDENCE,
				Assertions.within(50));
	}

}

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

package org.springframework.boot.docs.io.quartz

import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean

class MySampleJob : QuartzJobBean() {

	// @fold:on // fields ...
	private var myService: MyService? = null

	private var name: String? = null
	// @fold:off

	// Inject "MyService" bean
	fun setMyService(myService: MyService?) {
		this.myService = myService
	}

	// Inject the "name" job data property
	fun setName(name: String?) {
		this.name = name
	}

	override fun executeInternal(context: JobExecutionContext) {
		myService!!.someMethod(context.fireTime, name)
	}

}


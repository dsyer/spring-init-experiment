/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.init;

import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityRequestMatcherProviderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * @author Dave Syer
 *
 */
@SelectedAutoConfiguration(root = ConfigurationPropertiesAutoConfiguration.class, values = {
		PropertyPlaceholderAutoConfiguration.class })
@SelectedAutoConfiguration(root = WebFluxAutoConfiguration.class, values = {
		ReactiveWebServerFactoryAutoConfiguration.class,
		ErrorWebFluxAutoConfiguration.class, HttpHandlerAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class })
@SelectedAutoConfiguration(root = WebMvcAutoConfiguration.class, values = {
		ServletWebServerFactoryAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
		DispatcherServletAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class })
@SelectedAutoConfiguration(root = ReactiveSecurityAutoConfiguration.class, values = {
		ReactiveUserDetailsServiceAutoConfiguration.class,
		WebFluxAutoConfiguration.class })
@SelectedAutoConfiguration(root = SecurityAutoConfiguration.class, values = {
		UserDetailsServiceAutoConfiguration.class,
		SecurityRequestMatcherProviderAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class, WebMvcAutoConfiguration.class })
@SelectedAutoConfiguration(root = JdbcTemplateAutoConfiguration.class, values = {
		DataSourceAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class })
@SelectedAutoConfiguration(root = JacksonAutoConfiguration.class, values = {
		ConfigurationPropertiesAutoConfiguration.class })
@SelectedAutoConfiguration(root = MustacheAutoConfiguration.class, values = {
		ConfigurationPropertiesAutoConfiguration.class })
public class SpringBootSelectedAutoConfigurations {

}

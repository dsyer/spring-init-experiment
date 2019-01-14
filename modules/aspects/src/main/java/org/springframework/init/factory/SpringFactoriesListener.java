/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.init.factory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
public class SpringFactoriesListener implements SmartApplicationListener {

	private static Log logger = LogFactory.getLog(SpringFactoriesListener.class);

	private static Map<String, Map<Class<?>, Set<String>>> mappings = new HashMap<>();

	private static Set<String> mapped = new HashSet<>();

	public static Collection<String> loadFactoryNames(Class<?> key) {
		for (Map<Class<?>, Set<String>> map : mappings.values()) {
			if (map.containsKey(key)) {
				return map.get(key);
			}
		}
		return PropertiesFileFactories.loadFactoryNames(key, null);
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationEnvironmentPreparedEvent prepared = (ApplicationEnvironmentPreparedEvent) event;
		SpringApplication application = prepared.getSpringApplication();
		Map<Class<?>, Set<String>> result = new LinkedHashMap<>();
		for (Class<?> type : sources(application)) {
			result.putAll(computeFactories(new StandardAnnotationMetadata(type)));
		}
	}

	private Set<Class<?>> sources(SpringApplication application) {
		Set<Object> objects = application.getAllSources();
		Set<Class<?>> result = new LinkedHashSet<>();
		for (Object object : objects) {
			if (object instanceof String) {
				object = ClassUtils.resolveClassName((String) object, null);
			}
			Class<?> source = (Class<?>) object;
			if (AnnotatedElementUtils.hasAnnotation(source, SpringFactories.class)) {
				result.add(source);
			}
		}
		return result;
	}

	private Map<Class<?>, Set<String>> computeFactories(AnnotationMetadata metadata) {
		String[] values = (String[]) metadata
				.getAnnotationAttributes(SpringFactories.class.getName(), true)
				.get("value");
		Map<Class<?>, Set<String>> result = new LinkedHashMap<>();
		for (String value : values) {
			StopWatch stop = new StopWatch("factories");
			if (!mapped.contains(value)) {
				mapped.add(value);
				if (!ClassUtils.isPresent(value, null)) {
					continue;
				}
				Class<?> resolved = ClassUtils.resolveClassName(value, null);
				if (AnnotationUtils.isAnnotationDeclaredLocally(SpringFactory.class,
						resolved)) {
					stop.start(value);
					for (SpringFactory selected : findSelected(resolved)) {
						Class<?> key = selected.key();
						AnnotationAttributes attrs = AnnotationUtils
								.getAnnotationAttributes(selected, true, false);
						String[] mapped = attrs.getStringArray("value");
						SpringFactoriesListener.mappings
								.computeIfAbsent(value, k -> new LinkedHashMap<>())
								.computeIfAbsent(key, k -> new LinkedHashSet<>())
								.addAll(Arrays.asList(mapped));
					}
					stop.stop();
				}
				if (stop.getTaskCount() > 0) {
					logger.info("Initialized autoconfig mappings: " + stop);
				}
			}
			if (SpringFactoriesListener.mappings.containsKey(value)) {
				result.putAll(SpringFactoriesListener.mappings.get(value));
			}
		}
		return result;

	}

	private Collection<SpringFactory> findSelected(Class<?> type) {
		if (!AnnotationUtils.isAnnotationDeclaredLocally(SpringFactory.class, type)) {
			return Collections.emptySet();
		}
		Set<SpringFactory> list = new LinkedHashSet<>();
		SpringFactory configs = AnnotationUtils.findAnnotation(type, SpringFactory.class);
		list.add(configs);
		for (Class<?> depend : configs.classes()) {
			list.addAll(findSelected(depend));
		}
		return list;
	}

	private static class PropertiesFileFactories {

		public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
		private static final Map<ClassLoader, MultiValueMap<String, String>> cache = new ConcurrentReferenceHashMap<>();

		public static List<String> loadFactoryNames(Class<?> factoryClass,
				@Nullable ClassLoader classLoader) {
			String factoryClassName = factoryClass.getName();
			return loadSpringFactories(classLoader).getOrDefault(factoryClassName,
					Collections.emptyList());
		}

		private static Map<String, List<String>> loadSpringFactories(
				@Nullable ClassLoader classLoader) {
			MultiValueMap<String, String> result = cache.get(classLoader);
			if (result != null) {
				return result;
			}

			try {
				Enumeration<URL> urls = (classLoader != null
						? classLoader.getResources(FACTORIES_RESOURCE_LOCATION)
						: ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
				result = new LinkedMultiValueMap<>();
				while (urls.hasMoreElements()) {
					URL url = urls.nextElement();
					UrlResource resource = new UrlResource(url);
					Properties properties = PropertiesLoaderUtils
							.loadProperties(resource);
					for (Map.Entry<?, ?> entry : properties.entrySet()) {
						String factoryClassName = ((String) entry.getKey()).trim();
						for (String factoryName : StringUtils
								.commaDelimitedListToStringArray(
										(String) entry.getValue())) {
							result.add(factoryClassName, factoryName.trim());
						}
					}
				}
				cache.put(classLoader, result);
				return result;
			}
			catch (IOException ex) {
				throw new IllegalArgumentException(
						"Unable to load factories from location ["
								+ FACTORIES_RESOURCE_LOCATION + "]",
						ex);
			}
		}

	}
}

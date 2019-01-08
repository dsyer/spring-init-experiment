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
package org.springframework.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.init.SelectedAutoConfiguration.SelectedAutoConfigurations;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;

/**
 * @author Dave Syer
 *
 */
public class EnableSelectedAutoConfigurationImportSelector
		extends AutoConfigurationImportSelector {
	
	private static Log logger = LogFactory.getLog(EnableSelectedAutoConfigurationImportSelector.class);

	private static final String[] NO_IMPORTS = {};

	private static Map<String, Set<String>> mappings;

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		if (!isEnabled(metadata)) {
			return NO_IMPORTS;
		}
		// Don't use super.selectImports() because it does too much work with the
		// annotation metadata
		return computeImports(metadata).toArray(new String[0]);
	}

	@Override
	protected boolean isEnabled(AnnotationMetadata metadata) {
		return super.isEnabled(metadata) && getEnvironment().getProperty(
				EnableSelectedAutoConfiguration.ENABLED_OVERRIDE_PROPERTY, Boolean.class,
				true);
	}

	@Override
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
			AnnotationAttributes attributes) {
		return computeImports(metadata);
	}

	private List<String> computeImports(AnnotationMetadata metadata) {
		String[] values = (String[]) metadata.getAnnotationAttributes(
				EnableSelectedAutoConfiguration.class.getName(), true).get("value");
		Set<String> result = new LinkedHashSet<>();
		for (String value : values) {
			if (mappings == null) {
				StopWatch stop = new StopWatch("selected");
				stop.start();
				mappings = new HashMap<>();
				for (String factory : SpringFactoriesLoader
						.loadFactoryNames(EnableSelectedAutoConfiguration.class, null)) {
					if (ClassUtils.isPresent(factory, null)) {
						Class<?> type = ClassUtils.resolveClassName(factory, null);
						if (AnnotationUtils.isAnnotationDeclaredLocally(
								SelectedAutoConfigurations.class, type)) {
							SelectedAutoConfigurations configs = AnnotationUtils
									.findAnnotation(type,
											SelectedAutoConfigurations.class);
							for (SelectedAutoConfiguration selected : configs.value()) {
								AnnotationAttributes attrs = AnnotationUtils
										.getAnnotationAttributes(selected, true, false);
								String[] mapped = attrs.getStringArray("values");
								mappings.computeIfAbsent(attrs.getString("root"),
										k -> new LinkedHashSet<>())
										.addAll(Arrays.asList(mapped));
							}
						}
					}
				}
				computeCrossReferences();
				stop.stop();
				logger.info("Initialized autoconfig mappings: " + stop);
			}
			if (mappings.containsKey(value)) {
				result.addAll(mappings.get(value));
			}
			result.add(value);
		}
		return new ArrayList<>(result);
	}

	private void computeCrossReferences() {
		for (Set<String> mapping : mappings.values()) {
			extend(mapping, new HashSet<>());
		}
	}
	
	private boolean extend(Set<String> mapping, Set<String> seen) {
		int count = mapping.size();
		for (String mapped : new ArrayList<>(mapping)) {
			if (seen.contains(mapped)) {
				continue;
			}
			seen.add(mapped);
			if (mappings.containsKey(mapped)) {
				mapping.addAll(mappings.get(mapped));
			}
		}
		boolean result = mapping.size() > count;
		if (result) {
			result = extend(mapping, seen);
		}
		return result;
	}

	@Override
	protected Class<?> getAnnotationClass() {
		return EnableSelectedAutoConfiguration.class;
	}
}

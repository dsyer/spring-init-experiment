/*
 * Copyright 2020-2020 the original author or authors.
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
package org.springframework.init.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import com.squareup.javapoet.JavaFile;

/**
 * @author Dave Syer
 *
 */
public class InitializerGenerator {

	private Class<?>[] classes;

	private Class<?> base;

	private CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

	public InitializerGenerator(Class<?> base, Class<?>... classes) {
		this.base = base;
		this.classes = classes;
	}

	public Set<JavaFile> generate() {

		Map<String, Set<Class<?>>> packages = new HashMap<>();
		for (Class<?> type : classes) {
			packages.computeIfAbsent(ClassUtils.getPackageName(type), key -> new HashSet<>()).add(type);
		}

		Set<JavaFile> files = new HashSet<>();
		return files;
	}

}

/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.reactive.accept;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Builder for a composite {@link RequestedContentTypeResolver} that delegates
 * to other resolvers each implementing a different strategy to determine the
 * requested content type -- e.g. Accept header, query parameter, or other.
 *
 *委托的复合{@link RequestedContentTypeResolver}的构建器
 *其他解析器每个实施不同的策略来确定
 *请求的内容类型 - 例如接受标头，查询参数或其他。
 *
 * <p>Use builder methods to add resolvers in the desired order. For a given
 * request he first resolver to return a list that is not empty and does not
 * consist of just {@link MediaType#ALL}, will be used.
 *
 * <p>使用构建器方法以所需顺序添加解析器。对于给定的
 *请求他首先解析器返回一个非空的列表而不是空的列表
 *将仅使用{@link MediaType＃ALL}。
 *
 * <p>By default, if no resolvers are explicitly configured, the builder will
 * add {@link HeaderContentTypeResolver}.
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
public class RequestedContentTypeResolverBuilder {

	private final List<Supplier<RequestedContentTypeResolver>> candidates = new ArrayList<>();


	/**
	 * Add a resolver to get the requested content type from a query parameter.
	 * By default the query parameter name is {@code "format"}.
	 */
	public ParameterResolverConfigurer parameterResolver() {
		ParameterResolverConfigurer parameterBuilder = new ParameterResolverConfigurer();
		this.candidates.add(parameterBuilder::createResolver);
		return parameterBuilder;
	}

	/**
	 * Add resolver to get the requested content type from the
	 * {@literal "Accept"} header.
	 */
	public void headerResolver() {
		this.candidates.add(HeaderContentTypeResolver::new);
	}

	/**
	 * Add resolver that returns a fixed set of media types.
	 * @param mediaTypes the media types to use
	 */
	public void fixedResolver(MediaType... mediaTypes) {
		this.candidates.add(() -> new FixedContentTypeResolver(Arrays.asList(mediaTypes)));
	}

	/**
	 * Add a custom resolver.
	 * @param resolver the resolver to add
	 */
	public void resolver(RequestedContentTypeResolver resolver) {
		this.candidates.add(() -> resolver);
	}

	/**
	 * Build a {@link RequestedContentTypeResolver} that delegates to the list
	 * of resolvers configured through this builder.
	 *
	 * 构建一个委托给列表的{@link RequestedContentTypeResolver} 通过此构建器配置的解析程序。
	 */
	public RequestedContentTypeResolver build() {

		List<RequestedContentTypeResolver> resolvers =
				this.candidates.isEmpty() ?
						Collections.singletonList(new HeaderContentTypeResolver()) :
						this.candidates.stream().map(Supplier::get).collect(Collectors.toList());

		return exchange -> {
			for (RequestedContentTypeResolver resolver : resolvers) {
				// ------------- 关键方法 -----------

				List<MediaType> mediaTypes = resolver.resolveMediaTypes(exchange);
				if (mediaTypes.equals(RequestedContentTypeResolver.MEDIA_TYPE_ALL_LIST)) {
					continue;
				}
				return mediaTypes;
			}
			return RequestedContentTypeResolver.MEDIA_TYPE_ALL_LIST;
		};
	}


	/**
	 * Helper to create and configure {@link ParameterContentTypeResolver}.
	 */
	public static class ParameterResolverConfigurer {

		private final Map<String, MediaType> mediaTypes = new HashMap<>();

		@Nullable
		private String parameterName;

		/**
		 * Configure a mapping between a lookup key (extracted from a query
		 * parameter value) and a corresponding {@code MediaType}.
		 * @param key the lookup key
		 * @param mediaType the MediaType for that key
		 */
		public ParameterResolverConfigurer mediaType(String key, MediaType mediaType) {
			this.mediaTypes.put(key, mediaType);
			return this;
		}

		/**
		 * Map-based variant of {@link #mediaType(String, MediaType)}.
		 * @param mediaTypes the mappings to copy
		 */
		public ParameterResolverConfigurer mediaType(Map<String, MediaType> mediaTypes) {
			this.mediaTypes.putAll(mediaTypes);
			return this;
		}

		/**
		 * Set the name of the parameter to use to determine requested media types.
		 * <p>By default this is set to {@literal "format"}.
		 */
		public ParameterResolverConfigurer parameterName(String parameterName) {
			this.parameterName = parameterName;
			return this;
		}

		/**
		 * Private factory method to create the resolver.
		 */
		private RequestedContentTypeResolver createResolver() {
			ParameterContentTypeResolver resolver = new ParameterContentTypeResolver(this.mediaTypes);
			if (this.parameterName != null) {
				resolver.setParameterName(this.parameterName);
			}
			return resolver;
		}
	}

}

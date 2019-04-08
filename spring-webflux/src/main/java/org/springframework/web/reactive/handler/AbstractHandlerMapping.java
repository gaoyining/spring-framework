/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.web.reactive.handler;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.*;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Abstract base class for {@link org.springframework.web.reactive.HandlerMapping}
 * implementations.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Brian Clozel
 * @since 5.0
 */
public abstract class AbstractHandlerMapping extends ApplicationObjectSupport implements HandlerMapping, Ordered {

	private static final WebHandler REQUEST_HANDLED_HANDLER = exchange -> Mono.empty();


	private final PathPatternParser patternParser;

	private final UrlBasedCorsConfigurationSource globalCorsConfigSource;

	private CorsProcessor corsProcessor = new DefaultCorsProcessor();

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered


	public AbstractHandlerMapping() {
		  this.patternParser = new PathPatternParser();
		  this.globalCorsConfigSource = new UrlBasedCorsConfigurationSource(this.patternParser);
	}


	/**
	 * Shortcut method for setting the same property on the underlying pattern
	 * parser in use. For more details see:
	 *
	 * 在正在使用的基础模式解析器上设置相同属性的快捷方法。有关更多详情，请参阅	 * <ul>
	 *
	 * <li>{@link #getPathPatternParser()} -- the underlying pattern parser
	 * <li>{@link PathPatternParser#setCaseSensitive(boolean)} -- the case
	 * sensitive slash option, including its default value.
	 * </ul>
	 * <p><strong>Note:</strong> aside from
	 */
	public void setUseCaseSensitiveMatch(boolean caseSensitiveMatch) {
		this.patternParser.setCaseSensitive(caseSensitiveMatch);
	}

	/**
	 * Shortcut method for setting the same property on the underlying pattern
	 * parser in use. For more details see:
	 * <ul>
	 * <li>{@link #getPathPatternParser()} -- the underlying pattern parser
	 * <li>{@link PathPatternParser#setMatchOptionalTrailingSeparator(boolean)} --
	 * the trailing slash option, including its default value.
	 * </ul>
	 */
	public void setUseTrailingSlashMatch(boolean trailingSlashMatch) {
		this.patternParser.setMatchOptionalTrailingSeparator(trailingSlashMatch);
	}

	/**
	 * Return the {@link PathPatternParser} instance that is used for
	 * {@link #setCorsConfigurations(Map) CORS configuration checks}.
	 * Sub-classes can also use this pattern parser for their own request
	 * mapping purposes.
	 */
	public PathPatternParser getPathPatternParser() {
		return this.patternParser;
	}

	/**
	 * Set "global" CORS configuration based on URL patterns. By default the
	 * first matching URL pattern is combined with handler-level CORS
	 * configuration if any.
	 */
	public void setCorsConfigurations(Map<String, CorsConfiguration> corsConfigurations) {
		this.globalCorsConfigSource.setCorsConfigurations(corsConfigurations);
	}

	/**
	 * Configure a custom {@link CorsProcessor} to use to apply the matched
	 * {@link CorsConfiguration} for a request.
	 * <p>By default an instance of {@link DefaultCorsProcessor} is used.
	 */
	public void setCorsProcessor(CorsProcessor corsProcessor) {
		Assert.notNull(corsProcessor, "CorsProcessor must not be null");
		this.corsProcessor = corsProcessor;
	}

	/**
	 * Return the configured {@link CorsProcessor}.
	 */
	public CorsProcessor getCorsProcessor() {
		return this.corsProcessor;
	}

	/**
	 * Specify the order value for this HandlerMapping bean.
	 * <p>The default value is {@code Ordered.LOWEST_PRECEDENCE}, meaning non-ordered.
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	@Override
	public Mono<Object> getHandler(ServerWebExchange exchange) {
		// -------- 关键方法，子类实现 ---------
		// 查找给定请求的处理程序
		return getHandlerInternal(exchange).map(handler -> {
			if (CorsUtils.isCorsRequest(exchange.getRequest())) {
				CorsConfiguration configA = this.globalCorsConfigSource.getCorsConfiguration(exchange);
				CorsConfiguration configB = getCorsConfiguration(handler, exchange);
				CorsConfiguration config = (configA != null ? configA.combine(configB) : configB);
				if (!getCorsProcessor().process(config, exchange) ||
						CorsUtils.isPreFlightRequest(exchange.getRequest())) {
					return REQUEST_HANDLED_HANDLER;
				}
			}
			return handler;
		});
	}

	/**
	 * Look up a handler for the given request, returning an empty {@code Mono}
	 * if no specific one is found. This method is called by {@link #getHandler}.
	 * <p>On CORS pre-flight requests this method should return a match not for
	 * the pre-flight request but for the expected actual request based on the URL
	 * path, the HTTP methods from the "Access-Control-Request-Method" header, and
	 * the headers from the "Access-Control-Request-Headers" header.
	 *
	*查找给定请求的处理程序，返回空{@code Mono}
	*如果没有找到具体的。此方法由{@link #getHandler}调用。
	* <p>在CORS飞行前请求中，此方法应返回不匹配的匹配项
	*飞行前请求，但基于URL的预期实际请求
	* path，来自“Access-Control-Request-Method”标头的HTTP方法，以及
	*来自“Access-Control-Request-Headers”标头的标头。	 * @param exchange current exchange
	 *
	 * @return {@code Mono} for the matching handler, if any
	 */
	protected abstract Mono<?> getHandlerInternal(ServerWebExchange exchange);

	/**
	 * Retrieve the CORS configuration for the given handler.
	 *
	 * 检索给定处理程序的CORS配置。
	 *
	 * @param handler the handler to check (never {@code null})
	 * @param exchange the current exchange
	 * @return the CORS configuration for the handler, or {@code null} if none
	 */
	@Nullable
	protected CorsConfiguration getCorsConfiguration(Object handler, ServerWebExchange exchange) {
		if (handler instanceof CorsConfigurationSource) {
			return ((CorsConfigurationSource) handler).getCorsConfiguration(exchange);
		}
		return null;
	}

}

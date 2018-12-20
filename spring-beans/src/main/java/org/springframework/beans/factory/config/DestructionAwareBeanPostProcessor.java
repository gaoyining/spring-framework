/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Subinterface of {@link BeanPostProcessor} that adds a before-destruction callback.
 *
 * <p>The typical usage will be to invoke custom destruction callbacks on
 * specific bean types, matching corresponding initialization callbacks.
 *
 * {@link BeanPostProcessor}的子接口，它添加了一个破坏前的回调。
 * <p>典型的用法是调用特定bean类型的自定义销毁回调，匹配相应的初始化回调。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given bean instance before
	 * its destruction. Can invoke custom destruction callbacks.
	 * <p>Like DisposableBean's {@code destroy} and a custom destroy method,
	 * this callback just applies to singleton beans in the factory (including
	 * inner beans).
	 *
	 * 在销毁之前将此BeanPostProcessor应用于给定的bean实例。 可以调用自定义销毁回调。
	 * <p>与DisposableBean的{@code destroy}和自定义destroy方法类似，此回调仅适用于工厂中的单例bean（包括内部bean）。
	 *
	 *
	 * @param bean the bean instance to be destroyed
	 * @param beanName the name of the bean
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setDestroyMethodName
	 */
	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

	/**
	 * Determine whether the given bean instance requires destruction by this
	 * post-processor.
	 * <p><b>NOTE:</b> Even as a late addition, this method has been introduced on
	 * {@code DestructionAwareBeanPostProcessor} itself instead of on a SmartDABPP
	 * subinterface. This allows existing {@code DestructionAwareBeanPostProcessor}
	 * implementations to easily provide {@code requiresDestruction} logic while
	 * retaining compatibility with Spring <4.3, and it is also an easier onramp to
	 * declaring {@code requiresDestruction} as a Java 8 default method in Spring 5.
	 * <p>If an implementation of {@code DestructionAwareBeanPostProcessor} does
	 * not provide a concrete implementation of this method, Spring's invocation
	 * mechanism silently assumes a method returning {@code true} (the effective
	 * default before 4.3, and the to-be-default in the Java 8 method in Spring 5).
	 * @param bean the bean instance to check
	 * @return {@code true} if {@link #postProcessBeforeDestruction} is supposed to
	 * be called for this bean instance eventually, or {@code false} if not needed
	 *
	 * 确定给定的b​​ean实例是否需要通过此后处理器进行销毁。
	 * <p> <b>注意：</ b>即使作为后期添加，此方法也已引入
	 * {@code DestructionAwareBeanPostProcessor}本身而不是SmartDABPP子接口。这允许现有的{@code DestructionAwareBeanPostProcessor}
	 * 实现轻松提供{@code requiresDestruction}逻辑，同时保持与Spring <4.3的兼容性，
	 * 并且在Spring 5中将{@code requiresDestruction}声明为Java 8默认方法也是一种更容易的onramp。
	 * <p>如果{@code DestructionAwareBeanPostProcessor}的实现没有提供此方法的具体实现，
	 * 则Spring的调用机制默认假定一个返回{@code true}的方法（4.3之前的有效默认值，以及默认的默认值）
	 * Spring 5中的Java 8方法。 @param bean要检查的bean实例
	 * @return {@code true}如果最终应该为这个bean实例调用{@link #postProcessBeforeDestruction}，或者如果不需要{@code false}
	 * @since 4.3
	 */
	default boolean requiresDestruction(Object bean) {
		return true;
	}

}

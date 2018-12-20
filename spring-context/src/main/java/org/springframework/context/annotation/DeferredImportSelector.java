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

package org.springframework.context.annotation;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * A variation of {@link ImportSelector} that runs after all {@code @Configuration} beans
 * have been processed. This type of selector can be particularly useful when the selected
 * imports are {@code @Conditional}.
 *
 * <p>Implementations can also extend the {@link org.springframework.core.Ordered}
 * interface or use the {@link org.springframework.core.annotation.Order} annotation to
 * indicate a precedence against other {@link DeferredImportSelector}s.
 *
 * <p>Implementations may also provide an {@link #getImportGroup() import group} which
 * can provide additional sorting and filtering logic across different selectors.
 *
 * 在处理完所有{@code @Configuration} bean之后运行的{@link ImportSelector}的变体。
 * 当所选导入为{@code @Conditional}时，此类选择器特别有用。
 * <p>实现还可以扩展{@link org.springframework.core.Ordered}接口
 * 或使用{@link org.springframework.core.annotation.Order}注释来指示其他{@link DeferredImportSelector}的优先级。
 * <p>实现还可以提供{@link #getImportGroup（）导入组}，它可以跨不同的选择器提供额外的排序和过滤逻辑。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 4.0
 */
public interface DeferredImportSelector extends ImportSelector {

	/**
	 * Return a specific import group or {@code null} if no grouping is required.
	 *
	 * 如果不需要分组，则返回特定的导入组或{@code null}。
	 *
	 * @return the import group class or {@code null}
	 */
	@Nullable
	default Class<? extends Group> getImportGroup() {
		return null;
	}


	/**
	 * Interface used to group results from different import selectors.
	 *
	 * 用于分组来自不同导入选择器的结果的接口。
	 */
	interface Group {

		/**
		 * Process the {@link AnnotationMetadata} of the importing @{@link Configuration}
		 * class using the specified {@link DeferredImportSelector}.
		 */
		void process(AnnotationMetadata metadata, DeferredImportSelector selector);

		/**
		 * Return the {@link Entry entries} of which class(es) should be imported for this
		 * group.
		 */
		Iterable<Entry> selectImports();

		/**
		 * An entry that holds the {@link AnnotationMetadata} of the importing
		 * {@link Configuration} class and the class name to import.
		 */
		class Entry {

			private final AnnotationMetadata metadata;

			private final String importClassName;

			public Entry(AnnotationMetadata metadata, String importClassName) {
				this.metadata = metadata;
				this.importClassName = importClassName;
			}

			/**
			 * Return the {@link AnnotationMetadata} of the importing
			 * {@link Configuration} class.
			 */
			public AnnotationMetadata getMetadata() {
				return this.metadata;
			}

			/**
			 * Return the fully qualified name of the class to import.
			 */
			public String getImportClassName() {
				return this.importClassName;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) {
					return true;
				}
				if (o == null || getClass() != o.getClass()) {
					return false;
				}
				Entry entry = (Entry) o;
				return Objects.equals(this.metadata, entry.metadata) &&
						Objects.equals(this.importClassName, entry.importClassName);
			}

			@Override
			public int hashCode() {
				return Objects.hash(this.metadata, this.importClassName);
			}
		}
	}

}

/*******************************************************************************
 * Copyright 2017 InfinityRefactoring
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.infinityrefactoring.reflections;

import java.util.Map;

/**
 * Represents an instance factory.
 *
 * @see #DEFAULT_FACTORY
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public interface InstanceFactory {

	/**
	 * The default factory. This instance permits {@linkplain DelegatedInstanceFactory#put(Class, java.util.function.Function) puts a new factory for a given class}.
	 */
	public static final DelegatedInstanceFactory DEFAULT_FACTORY = DelegatedInstanceFactory.DEFAULT_FACTORY;

	/**
	 * Supplies a instance of the given class.
	 *
	 * @param c the desired class
	 * @return the instance
	 * @see #getInstance(Class, Map)
	 */
	public default <T> T getInstance(Class<T> c) {
		return getInstance(c, null);
	}

	/**
	 * Supplies a instance of the given class.
	 *
	 * @param c the desired class
	 * @param args the arguments used that will be used to creates the instance
	 * @return the instance
	 * @see #getInstance(Class)
	 */
	public <T> T getInstance(Class<T> c, Map<String, Object> args);

}

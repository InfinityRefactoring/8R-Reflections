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

import static com.infinityrefactoring.reflections.Reflections.newInstance;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This instance factory delegate the building process to a specific factory,
 * if exists a factory for the given class, otherwise uses the {@linkplain Reflections#newInstance(Class, Object...) default constructor}.
 *
 * @see #DEFAULT_FACTORY
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class DelegatedInstanceFactory implements InstanceFactory {

	/**
	 * A factory that build a byte of zero value.
	 */
	public static final Function<Map<String, Object>, Byte> BYTE_FACTORY = m -> Byte.valueOf((byte) 0);

	/**
	 * A factory that build a short of zero value.
	 */
	public static final Function<Map<String, Object>, Short> SHORT_FACTORY = m -> Short.valueOf((short) 0);

	/**
	 * A factory that build a integer of zero value.
	 */
	public static final Function<Map<String, Object>, Integer> INTEGER_FACTORY = m -> Integer.valueOf(0);

	/**
	 * A factory that build a long of zero value.
	 */
	public static final Function<Map<String, Object>, Long> LONG_FACTORY = m -> Long.valueOf(0L);

	/**
	 * A factory that build a float of zero value.
	 */
	public static final Function<Map<String, Object>, Float> FLOAT_FACTORY = m -> Float.valueOf(0F);

	/**
	 * A factory that build a double of zero value.
	 */
	public static final Function<Map<String, Object>, Double> DOUBLE_FACTORY = m -> Double.valueOf(0D);

	/**
	 * A factory that build a character of '\u0000' value.
	 */
	public static final Function<Map<String, Object>, Character> CHARACTER_FACTORY = m -> new Character('\u0000');

	/**
	 * A factory that build a boolean of false value.
	 */
	public static final Function<Map<String, Object>, Boolean> BOOLEAN_FACTORY = m -> Boolean.FALSE;

	/**
	 * The default factory. This instance by default has capacity to instantiates all primitive types and any classes with default constructor.
	 *
	 * @see #put(Class, Function)
	 * @see #remove(Class)
	 * @see #BYTE_FACTORY
	 * @see #SHORT_FACTORY
	 * @see #INTEGER_FACTORY
	 * @see #LONG_FACTORY
	 * @see #FLOAT_FACTORY
	 * @see #DOUBLE_FACTORY
	 * @see #CHARACTER_FACTORY
	 * @see #BOOLEAN_FACTORY
	 * @see Reflections#newInstance(Class, Object...)
	 */
	public static final DelegatedInstanceFactory DEFAULT_FACTORY = DelegatedInstanceFactory.empty(16)
			.put(Byte.class, BYTE_FACTORY)
			.put(byte.class, BYTE_FACTORY)
			.put(Short.class, SHORT_FACTORY)
			.put(short.class, SHORT_FACTORY)
			.put(Integer.class, INTEGER_FACTORY)
			.put(int.class, INTEGER_FACTORY)
			.put(Long.class, LONG_FACTORY)
			.put(long.class, LONG_FACTORY)
			.put(Float.class, FLOAT_FACTORY)
			.put(float.class, FLOAT_FACTORY)
			.put(Double.class, DOUBLE_FACTORY)
			.put(double.class, DOUBLE_FACTORY)
			.put(Character.class, CHARACTER_FACTORY)
			.put(char.class, CHARACTER_FACTORY)
			.put(boolean.class, BOOLEAN_FACTORY)
			.put(boolean.class, BOOLEAN_FACTORY);

	private final Map<Class<?>, Function<Map<String, Object>, ?>> SPECIFIC_FACTORIES;

	/**
	 * Creates a new instance of DelegatedInstanceFactory with a new map.
	 *
	 * @param initialMapSize the initial map size
	 * @return the instance
	 * @see #of(Map)
	 */
	public static DelegatedInstanceFactory empty(int initialMapSize) {
		return of(new HashMap<>(initialMapSize));
	}

	/**
	 * Creates a new instance of DelegatedInstanceFactory with the given map.
	 *
	 * @param specificFactories a map with the specific factories
	 * @return the instance
	 * @throws NullPointerException if the map is null
	 * @see #empty(int)
	 */
	public static DelegatedInstanceFactory of(Map<Class<?>, Function<Map<String, Object>, ?>> specificFactories) {
		return new DelegatedInstanceFactory(specificFactories);
	}

	/**
	 * Constructs a new instance of DelegatedInstanceFactory.
	 *
	 * @param specificFactories a map with the specific factories
	 */
	private DelegatedInstanceFactory(Map<Class<?>, Function<Map<String, Object>, ?>> specificFactories) {
		SPECIFIC_FACTORIES = requireNonNull(specificFactories);
	}

	/**
	 * {@inheritDoc}
	 * This instance factory delegate the building process to a specific factory,
	 * if exists a factory for the given class, otherwise uses the {@linkplain Reflections#newInstance(Class, Object...) default constructor}
	 * 
	 * @see #put(Class, Function)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> c, Map<String, Object> args) {
		if (c == null) {
			throw new IllegalArgumentException("The given class is null.");
		}
		Function<Map<String, Object>, ?> factory = SPECIFIC_FACTORIES.get(c);
		return ((factory == null) ? newInstance(c) : ((T) factory.apply(args)));
	}

	/**
	 * Puts a specific factory for the given class.
	 * 
	 * @param key the class that specified factory can handle
	 * @param factory the factory that will produce instances for the given class
	 * @return this
	 */
	public <T> DelegatedInstanceFactory put(Class<T> key, Function<Map<String, Object>, T> factory) {
		SPECIFIC_FACTORIES.put(requireNonNull(key, "The class key cannot be null."), requireNonNull(factory, "The factory cannot be null."));
		return this;
	}

	/**
	 * Removes the factory that is associated with the given class.
	 * 
	 * @param c the desired class
	 * @return this
	 */
	public DelegatedInstanceFactory remove(Class<?> c) {
		SPECIFIC_FACTORIES.remove(c);
		return this;
	}

}

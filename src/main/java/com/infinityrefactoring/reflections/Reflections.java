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

import static com.infinityrefactoring.reflections.ClassWrapper.getClassWrapper;
import static com.infinityrefactoring.reflections.InstanceFactory.DEFAULT_FACTORY;
import static com.infinityrefactoring.reflections.PathExpression.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is an utility class for facilitates the access, of centralized way, to the principal functionalities of the "com.infinityrefactoring.reflections" package.
 *
 * @see ClassWrapper
 * @see PathExpression
 * @see Predicates
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class Reflections {

	/**
	 * Returns a map with the value of all given path expressions.
	 *
	 * @param pathExpressions a set with all desired path expressions
	 * @param rootObj the instance that will have the node value extracted
	 * @return the map
	 * @see #setAllExpressions(Object, Map)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#getExpressionValue(Object, Map, InstanceFactory)
	 */
	public static Map<String, Object> getAllExpressionValues(Object rootObj, Set<String> pathExpressions) {
		return getAllExpressionValues(rootObj, pathExpressions, null, null);
	}

	/**
	 * Returns a map with the value of all given path expressions.
	 *
	 * @param pathExpressions a set with all desired path expressions
	 * @param rootObj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @return the map
	 * @see #setAllExpressions(Object, Map, Map)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#getExpressionValue(Object, Map, InstanceFactory)
	 */
	public static Map<String, Object> getAllExpressionValues(Object rootObj, Set<String> pathExpressions, Map<String, Object> args) {
		return getAllExpressionValues(rootObj, pathExpressions, args, null);
	}

	/**
	 * Returns a map with the value of all given path expressions.
	 *
	 * @param pathExpressions a set with all desired path expressions
	 * @param rootObj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the map
	 * @see #setAllExpressions(Object, Map, InstanceFactory)
	 * @see #setAllExpressions(Object, Map, Map, InstanceFactory)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#getExpressionValue(Object, Map, InstanceFactory)
	 */
	public static Map<String, Object> getAllExpressionValues(Object rootObj, Set<String> pathExpressions, Map<String, Object> args, InstanceFactory instanceFactory) {
		Map<String, Object> values = new HashMap<>(pathExpressions.size());
		for (String pathExpression : pathExpressions) {
			Object value = compile(pathExpression).getExpressionValue(rootObj, args, instanceFactory);
			values.put(pathExpression, value);
		}
		return values;
	}

	/**
	 * Returns the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param obj the instance that will have the field value extracted
	 * @param fieldName the field name
	 * @return the field value
	 * @throws IllegalArgumentException if not found
	 * @see #getStaticFieldValue(Class, String)
	 * @see #setFieldValue(Object, String, Object)
	 * @see ClassWrapper#getFieldValue(Object, String)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static <R> R getFieldValue(Object obj, String fieldName) {
		return getClassWrapper(obj).getFieldValue(obj, fieldName);
	}

	/**
	 * Returns the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param c the class that the field is contained
	 * @param fieldName the field name
	 * @return the field value
	 * @throws IllegalArgumentException if not found
	 * @see #getFieldValue(Object, String)
	 * @see #setStaticFieldValue(Class, String, Object)
	 * @see ClassWrapper#setStaticFieldValue(String, Object)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static <R> R getStaticFieldValue(Class<?> c, String fieldName) {
		return getClassWrapper(c).getStaticFieldValue(fieldName);
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param obj the instance that will have the given method invoked
	 * @param methodName the method name that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 * @throws IllegalArgumentException if not found
	 * @see #invokeStaticMethod(Class, String, Object...)
	 * @see ClassWrapper#invokeMethod(Object, String, Object...)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static <R> R invokeMethod(Object obj, String methodName, Object... args) {
		return getClassWrapper(obj).invokeMethod(obj, methodName, args);
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param c the class that the method is contained
	 * @param methodName the method name that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 * @throws IllegalArgumentException if not found
	 * @see #invokeMethod(Object, String, Object...)
	 * @see ClassWrapper#invokeStaticMethod(String, Object...)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static <R> R invokeStaticMethod(Class<?> c, String methodName, Object... args) {
		return getClassWrapper(c).invokeStaticMethod(methodName, args);
	}

	/**
	 * Creates a new instance of the given class, using a constructor that matches with the specified arguments.
	 *
	 * @param c the desired class
	 * @param args the arguments used that will be used to creates the instance
	 * @return the instance
	 * @throws IllegalArgumentException if not found
	 * @see ClassWrapper#newInstance(Object...)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static <T> T newInstance(Class<T> c, Object... args) {
		return getClassWrapper(c).newInstance(args);
	}

	/**
	 * Sets all expression value for the given root object.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param values a map with all path expressions with the respective new value
	 * @return the root object
	 * @see #setAllExpressions(Object, Map, Map)
	 * @see #setAllExpressions(Object, Map, InstanceFactory)
	 * @see #setAllExpressions(Object, Map, Map, InstanceFactory)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public static <T> T setAllExpressions(T rootObj, Map<String, Object> values) {
		return setAllExpressions(rootObj, values, null, DEFAULT_FACTORY);
	}

	/**
	 * Sets all expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @param values a map with all path expressions with the respective new value
	 * @return the root object
	 * @see #setAllExpressions(Object, Map)
	 * @see #setAllExpressions(Object, Map, Map)
	 * @see #setAllExpressions(Object, Map, Map, InstanceFactory)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public static <T> T setAllExpressions(T rootObj, Map<String, Object> values, InstanceFactory instanceFactory) {
		return setAllExpressions(rootObj, values, null, instanceFactory);
	}

	/**
	 * Sets all expression value for the given root object.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param values a map with all path expressions with the respective new value
	 * @return the root object
	 * @see #setAllExpressions(Object, Map)
	 * @see #setAllExpressions(Object, Map, InstanceFactory)
	 * @see #setAllExpressions(Object, Map, Map, InstanceFactory)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public static <T> T setAllExpressions(T rootObj, Map<String, Object> values, Map<String, Object> args) {
		return setAllExpressions(rootObj, values, args, DEFAULT_FACTORY);
	}

	/**
	 * Sets all expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @param values a map with all path expressions with the respective new value
	 * @return the root object
	 * @see #setAllExpressions(Object, Map)
	 * @see #setAllExpressions(Object, Map, Map)
	 * @see #setAllExpressions(Object, Map, InstanceFactory)
	 * @see PathExpression#compile(String)
	 * @see PathExpression#setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public static <T> T setAllExpressions(T rootObj, Map<String, Object> values, Map<String, Object> args, InstanceFactory instanceFactory) {
		values.forEach((pathExpression, newValue) -> compile(pathExpression).setExpressionValue(rootObj, newValue, args, instanceFactory));
		return rootObj;
	}

	/**
	 * Sets the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param obj the instance that will have the field value setted.
	 * @param fieldName the field name
	 * @param newValue the new field value
	 * @throws IllegalArgumentException if not found
	 * @see #setStaticFieldValue(Class, String, Object)
	 * @see #getFieldValue(Object, String)
	 * @see ClassWrapper#setFieldValue(Object, String, Object)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static void setFieldValue(Object obj, String fieldName, Object newValue) {
		getClassWrapper(obj).setFieldValue(obj, fieldName, newValue);
	}

	/**
	 * Sets the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param c the class that the field is contained
	 * @param fieldName the field name
	 * @param newValue the new field value
	 * @throws IllegalArgumentException if not found
	 * @see #setFieldValue(Object, String, Object)
	 * @see #getStaticFieldValue(Class, String)
	 * @see ClassWrapper#setStaticFieldValue(String, Object)
	 * @see ClassWrapper#getClassWrapper(Object)
	 */
	public static void setStaticFieldValue(Class<?> c, String fieldName, Object newValue) {
		getClassWrapper(c).setStaticFieldValue(fieldName, newValue);
	}

	private Reflections() {

	}

}

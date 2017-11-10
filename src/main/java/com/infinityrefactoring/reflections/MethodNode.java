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

import static com.infinityrefactoring.reflections.ClassWrapper.wrap;
import static com.infinityrefactoring.reflections.Reflections.invokeMethod;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents an {@linkplain ExpressionNode#isMethod(String) method node} of a {@linkplain PathExpression path expression}.
 * A method node is compound by method name, optional argument keys and an setter method name, only if the method name starts with the "get".
 * The argument keys are string that will be used to retrieves the arguments of the argument map.
 * Examples of Method node:
 * <ul>
 * <li>foo()</li>
 * <li>bar(a, b)</li>
 * <li>getName()</li>
 * <li>toString()</li>
 * <li>execute(argumentKey1, argumentKey2)</li>
 * </ul>
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class MethodNode extends ExpressionNode {

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final String COMMAN_REGEX = quote(",");
	private static final String GET_REGEX = quote("get");
	private final String METHOD_NAME;
	private final String JOINED_ARGUMENT_KEYS;
	private final List<String> ARGUMENT_KEYS;
	private final String SETTER_METHOD_NAME;

	/**
	 * Constructs a new instance of MethodNode.
	 *
	 * @param expressionNode the desired expression node
	 * @throws IllegalArgumentException if the expression node is not an {@linkplain ExpressionNode#isMethod(String) method}
	 */
	MethodNode(String expressionNode) {
		super(expressionNode);
		if (!ExpressionNode.isMethod(expressionNode)) {
			throw new IllegalArgumentException("This expression node is not a method.");
		}
		int leftParenthesisIndex = expressionNode.lastIndexOf('(');
		METHOD_NAME = expressionNode.substring(0, leftParenthesisIndex);

		String argKeys = expressionNode.substring((leftParenthesisIndex + 1), expressionNode.lastIndexOf(')')).trim();

		if (argKeys.isEmpty()) {
			ARGUMENT_KEYS = emptyList();
		} else {
			ARGUMENT_KEYS = Stream.of(argKeys.split(COMMAN_REGEX))
					.map(String::trim)
					.collect(collectingAndThen(toList(), Collections::unmodifiableList));
		}
		JOINED_ARGUMENT_KEYS = ARGUMENT_KEYS.toString();
		SETTER_METHOD_NAME = (METHOD_NAME.startsWith("get") ? METHOD_NAME.replaceFirst(GET_REGEX, "set") : null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodNode) {
			MethodNode expression = ((MethodNode) obj);
			return (METHOD_NAME.equals(expression.METHOD_NAME)
					&& JOINED_ARGUMENT_KEYS.equals(expression.JOINED_ARGUMENT_KEYS)
					&& Objects.equals(SETTER_METHOD_NAME, expression.SETTER_METHOD_NAME));
		}
		return false;
	}

	@Override
	public List<String> getArgumentKeys() {
		return ARGUMENT_KEYS;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see #getMethod(Class)
	 * @see #getMethod(Class, Map)
	 */
	@Override
	public Member getMember(Class<?> c, Map<String, Object> args) {
		Object[] methodArgs = getMethodArgs(args);
		return wrap(c).getCompatibleMethodWithValues(METHOD_NAME, methodArgs);
	}

	/**
	 * Returns the method that this node represents on the given class.
	 *
	 * @param c the class that have this node
	 * @return the method
	 * @see #getMethod(Class)
	 */
	public Method getMethod(Class<?> c) {
		return getMethod(c, null);
	}

	/**
	 * Returns the method that this node represents on the given class.
	 *
	 * @param c the class that have this node
	 * @param args the arguments that will be used to access this node.
	 * @return the method
	 * @see #getMethod(Class)
	 */
	public Method getMethod(Class<?> c, Map<String, Object> args) {
		return (Method) getMember(c, args);
	}

	@Override
	public Object getStaticValue(Class<?> c) {
		if (ARGUMENT_KEYS.isEmpty()) {
			return getStaticValue(c, null);
		}
		throw new UnsupportedOperationException("This method require arguments.");
	}

	@Override
	public Object getStaticValue(Class<?> c, Map<String, Object> args) {
		Object[] methodArgs = getMethodArgs(args);
		return Reflections.invokeStaticMethod(c, METHOD_NAME, methodArgs);
	}

	@Override
	public Object getValue(Object obj) {
		if (ARGUMENT_KEYS.isEmpty()) {
			return getValue(obj, null);
		}
		throw new UnsupportedOperationException("This method require arguments.");
	}

	@Override
	public Object getValue(Object obj, Map<String, Object> args) {
		Object[] methodArgs = getMethodArgs(args);
		return invokeMethod(obj, METHOD_NAME, methodArgs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(METHOD_NAME, JOINED_ARGUMENT_KEYS, SETTER_METHOD_NAME);
	}

	@Override
	public void setStaticValue(Class<?> c, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (SETTER_METHOD_NAME == null) {
			throw new UnsupportedOperationException("Unknown setter method.");
		}
		wrap(c).invokeStaticMethod(SETTER_METHOD_NAME, newValue);
	}

	@Override
	public void setValue(Object obj, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (SETTER_METHOD_NAME == null) {
			throw new UnsupportedOperationException("Unknown setter method.");
		}
		wrap(obj).invokeMethod(obj, SETTER_METHOD_NAME, newValue);
	}

	@Override
	public String toString() {
		return "MethodNode [METHOD_NAME=" + METHOD_NAME + ", ARGUMENT_KEYS=" + JOINED_ARGUMENT_KEYS + ", SETTER_METHOD_NAME=" + SETTER_METHOD_NAME + "]";
	}

	/**
	 * Returns an array with the values that is associated to the argument keys of this node.
	 *
	 * @param args the map that contains the argument values.
	 * @return the array.
	 */
	private Object[] getMethodArgs(Map<String, Object> args) {
		if (ARGUMENT_KEYS.isEmpty()) {
			return EMPTY_OBJECT_ARRAY;
		} else if ((!ARGUMENT_KEYS.isEmpty()) && ((args == null) || args.isEmpty())) {
			throw new IllegalArgumentException("The arguments map is null or empty.");
		}
		int size = ARGUMENT_KEYS.size();
		Object[] values = new Object[size];
		for (int i = 0; i < size; i++) {
			String key = ARGUMENT_KEYS.get(i);
			if (args.containsKey(key)) {
				values[i] = args.get(key);
			} else {
				throw new IllegalArgumentException(format("Not found argument for the key [%s].", key));
			}
		}
		return values;
	}

}

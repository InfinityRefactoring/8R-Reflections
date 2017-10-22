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
import static java.util.regex.Pattern.quote;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final String COMMAN_REGEX = quote(",");
	private static final String GET_REGEX = quote("get");
	private final String METHOD_NAME;
	private final String[] METHOD_ARG_KEYS;
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
			METHOD_ARG_KEYS = EMPTY_STRING_ARRAY;
		} else {
			String[] separatedArgKeys = argKeys.split(COMMAN_REGEX);
			for (int i = 0; i < separatedArgKeys.length; i++) {
				separatedArgKeys[i] = separatedArgKeys[i].trim();
			}
			METHOD_ARG_KEYS = separatedArgKeys;
		}
		SETTER_METHOD_NAME = (METHOD_NAME.startsWith("get") ? METHOD_NAME.replaceFirst(GET_REGEX, "set") : null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodNode) {
			MethodNode expression = ((MethodNode) obj);
			return (METHOD_NAME.equals(expression.METHOD_NAME)
					&& Arrays.equals(METHOD_ARG_KEYS, expression.METHOD_ARG_KEYS)
					&& Objects.equals(SETTER_METHOD_NAME, expression.SETTER_METHOD_NAME));
		}
		return false;
	}

	@Override
	public Class<?> getStaticNodeClass(Class<?> c, Map<String, Object> args) {
		return wrap(c).getCompatibleMethodWithValues(METHOD_NAME, getMethodArgs(args)).getReturnType();
	}

	@Override
	public Object getStaticValue(Class<?> c) {
		if (METHOD_ARG_KEYS.length == 0) {
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
		if (METHOD_ARG_KEYS.length == 0) {
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
		return Objects.hash(METHOD_NAME, METHOD_ARG_KEYS, SETTER_METHOD_NAME);
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
		return "MethodNode [METHOD_NAME=" + METHOD_NAME + ", METHOD_ARG_KEYS=" + Arrays.toString(METHOD_ARG_KEYS) + ", SETTER_METHOD_NAME=" + SETTER_METHOD_NAME + "]";
	}

	/**
	 * Returns an array with the values that is associated to the argument keys of this node.
	 *
	 * @param args the map that contains the argument values.
	 * @return the array.
	 */
	private Object[] getMethodArgs(Map<String, Object> args) {
		if (METHOD_ARG_KEYS.length == 0) {
			return EMPTY_OBJECT_ARRAY;
		} else if ((METHOD_ARG_KEYS.length > 0) && ((args == null) || args.isEmpty())) {
			throw new IllegalArgumentException("The arguments map is null or empty.");
		}
		Object[] values = new Object[METHOD_ARG_KEYS.length];
		for (int i = 0; i < METHOD_ARG_KEYS.length; i++) {
			String key = METHOD_ARG_KEYS[i];
			if (args.containsKey(key)) {
				values[i] = args.get(key);
			} else {
				throw new IllegalArgumentException(format("Not found argument for the key [%s].", key));
			}
		}
		return values;
	}

}

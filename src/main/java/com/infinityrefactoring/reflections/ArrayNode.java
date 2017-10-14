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

import static java.lang.String.format;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an {@linkplain ExpressionNode#isArray(String) array node} of a {@linkplain PathExpression path expression}.
 * An array node is compound by an index and an optional internal node, that can be:
 * a {@linkplain ExpressionNode#isField(String) field}, a {@linkplain ExpressionNode#isMethod(String) method} or an {@linkplain ExpressionNode#isArray(String) array}.
 * Examples of Array node:
 * <ul>
 * <li>[0]</li>
 * <li>field[1]</li>
 * <li>[0][1][2][3]</li>
 * <li>methodName()[1]</li>
 * <li>methodName(a, b)[2]</li>
 * </ul>
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class ArrayNode implements ExpressionNode {

	private final ExpressionNode INTERNAL_NODE;
	private final int INDEX;

	/**
	 * Constructs a new instance of ArrayNode.
	 *
	 * @param expressionNode the desired expression node
	 * @throws IllegalArgumentException if the expression node is not an {@linkplain ExpressionNode#isArray(String) array}
	 */
	ArrayNode(String expressionNode) {
		if (!ExpressionNode.isArray(expressionNode)) {
			throw new IllegalArgumentException("This expression node is not an array.");
		}
		int leftSquareBrackets = expressionNode.lastIndexOf('[');
		INDEX = Integer.parseInt(expressionNode.substring((leftSquareBrackets + 1), expressionNode.lastIndexOf(']')).trim());
		INTERNAL_NODE = ExpressionNode.compile(expressionNode.substring(0, leftSquareBrackets));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArrayNode) {
			ArrayNode expression = ((ArrayNode) obj);
			return ((INDEX == expression.INDEX) && Objects.equals(INTERNAL_NODE, expression.INTERNAL_NODE));
		}
		return false;
	}

	@Override
	public String getName() {
		return ((INTERNAL_NODE == null) ? format("[%d]", INDEX) : INTERNAL_NODE.getName());
	}

	@Override
	public Class<?> getNodeClass(Object rootObj, Map<String, Object> args) {
		if (INTERNAL_NODE != null) {
			return INTERNAL_NODE.getNodeClass(rootObj, args).getComponentType();
		}
		return rootObj.getClass();
	}

	@Override
	public Object getValue(Object obj, Map<String, Object> args) {
		Object array = ((INTERNAL_NODE == null) ? obj : INTERNAL_NODE.getValue(obj, args));
		return Array.get(array, INDEX);
	}

	@Override
	public int hashCode() {
		return Objects.hash(INDEX, INTERNAL_NODE);
	}

	@Override
	public void setValue(Object obj, Object value, Map<String, Object> args, InstanceFactory instanceFactory) {
		Object array = obj;
		if (INTERNAL_NODE != null) {
			array = INTERNAL_NODE.getValue(obj, args);
			if (array == null) {
				Class<?> nodeClass = INTERNAL_NODE.getNodeClass(obj, args);
				array = instanceFactory.getInstance(nodeClass, args);
				INTERNAL_NODE.setValue(obj, array, args);
			}
		}
		Array.set(array, INDEX, value);
	}

	@Override
	public String toString() {
		return "ArrayNode [INTERNAL_NODE=" + INTERNAL_NODE + ", INDEX=" + INDEX + "]";
	}

}

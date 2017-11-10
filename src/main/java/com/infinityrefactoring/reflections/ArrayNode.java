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

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.Map;

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
public class ArrayNode extends ExpressionNode {

	private final int INDEX;
	private final ExpressionNode INTERNAL_NODE;

	/**
	 * Constructs a new instance of ArrayNode.
	 *
	 * @param expressionNode the desired expression node
	 * @throws IllegalArgumentException if the expression node is not an {@linkplain ExpressionNode#isArray(String) array}
	 */
	ArrayNode(String expressionNode) {
		super(expressionNode);
		if (!ExpressionNode.isArray(expressionNode)) {
			throw new IllegalArgumentException("This expression node is not an array.");
		}
		int leftSquareBrackets = expressionNode.lastIndexOf('[');
		INDEX = Integer.parseInt(expressionNode.substring((leftSquareBrackets + 1), expressionNode.lastIndexOf(']')).trim());
		String internalExpressionNode = expressionNode.substring(0, leftSquareBrackets);
		INTERNAL_NODE = (internalExpressionNode.isEmpty() ? null : ExpressionNode.compile(internalExpressionNode));
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof ArrayNode) && NAME.equals(((ArrayNode) obj).NAME));
	}

	/**
	 * Returns the index of this expression.
	 *
	 * @return the index
	 */
	public int getIndex() {
		return INDEX;
	}

	/**
	 * Returns the internal node or null.
	 *
	 * @return the internal node or null
	 */
	public ExpressionNode getInternalNode() {
		return INTERNAL_NODE;
	}

	@Override
	public Member getMember(Class<?> c, Map<String, Object> args) {
		return ((INTERNAL_NODE == null) ? null : INTERNAL_NODE.getMember(c, args));
	}

	@Override
	public Class<?> getStaticNodeClass(Class<?> c, Map<String, Object> args) {
		if (INTERNAL_NODE != null) {
			return INTERNAL_NODE.getStaticNodeClass(c, args).getComponentType();
		}
		return c.getComponentType();
	}

	@Override
	public Object getStaticValue(Class<?> c, Map<String, Object> args) {
		Object array = ((INTERNAL_NODE == null) ? null : INTERNAL_NODE.getStaticValue(c, args));
		return ((array == null) ? null : Array.get(array, INDEX));
	}

	@Override
	public Object getValue(Object obj, Map<String, Object> args) {
		Object array = ((INTERNAL_NODE == null) ? obj : INTERNAL_NODE.getValue(obj, args));
		return ((array == null) ? null : Array.get(array, INDEX));
	}

	@Override
	public int hashCode() {
		return NAME.hashCode();
	}

	@Override
	public void setStaticValue(Class<?> c, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (INTERNAL_NODE != null) {
			Object array = INTERNAL_NODE.getStaticValue(c, args);
			if (array == null) {
				Class<?> nodeClass = INTERNAL_NODE.getStaticNodeClass(c, args);
				array = instanceFactory.getInstance(nodeClass, args);
				INTERNAL_NODE.setStaticValue(c, array, args);
			}
			if (array != null) {
				Array.set(array, INDEX, newValue);
			}
		}
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
		if (array != null) {
			Array.set(array, INDEX, value);
		}
	}

	@Override
	public String toString() {
		return "ArrayNode [INTERNAL_NODE=" + INTERNAL_NODE + ", INDEX=" + INDEX + "]";
	}

}

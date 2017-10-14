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

import static com.infinityrefactoring.reflections.InstanceFactory.DEFAULT_FACTORY;

import java.util.Map;

/**
 * Represents a node of a {@linkplain PathExpression path expression}.
 *
 * @see PathExpression
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public interface ExpressionNode {

	/**
	 * Compiles the given expression node in an {@linkplain ExpressionNode} instance, appropriate for your type.
	 *
	 * @param expressionNode the desired expression node
	 * @return an compiled instance or null, if the expression node not is a {@linkplain #isField(String) field}, {@linkplain #isMethod(String) method} or {@linkplain #isArray(String) array}
	 * @throws IllegalArgumentException if the given expression node is null
	 */
	public static ExpressionNode compile(String expressionNode) {
		if (expressionNode == null) {
			throw new IllegalArgumentException("The expression node cannot be null.");
		} else if (isMethod(expressionNode)) {
			return new MethodNode(expressionNode);
		} else if (isArray(expressionNode)) {
			return new ArrayNode(expressionNode);
		} else if (isField(expressionNode)) {
			return new FieldNode(expressionNode);
		}
		return null;
	}

	/**
	 * Returns true if the expression node ends with the string "]".
	 *
	 * @param expressionNode the desired expression node
	 * @return true if is an array
	 */
	public static boolean isArray(String expressionNode) {
		return (expressionNode.endsWith("]"));
	}

	/**
	 * Returns true if the expression node is not empty and is not an {@linkplain #isMethod(String) method} or an {@linkplain #isArray(String) array}.
	 *
	 * @param expressionNode the desired expression node
	 * @return true if is an field
	 */
	public static boolean isField(String expressionNode) {
		return ((!expressionNode.isEmpty()) && (!(isMethod(expressionNode) || isArray(expressionNode))));
	}

	/**
	 * Returns true if the expression node ends with the string ")".
	 *
	 * @param expressionNode the desired expression node
	 * @return true if is an method
	 */
	public static boolean isMethod(String expressionNode) {
		return expressionNode.endsWith(")");
	}

	/**
	 * Returns the node name.
	 *
	 * @return the name
	 */
	public String getName();

	/**
	 * Returns the class that this node will return when applied for the given root object.
	 *
	 * @param rootObj the object that has this node
	 * @param args the arguments that will be used to access this node.
	 * @return the class
	 * @throws IllegalArgumentException if the root object is null
	 */
	public Class<?> getNodeClass(Object rootObj, Map<String, Object> args);

	/**
	 * Returns the node value.
	 *
	 * @param obj the instance that will have the node value extracted
	 * @return the node value
	 */
	public default Object getValue(Object obj) {
		return getValue(obj, null);
	}

	/**
	 * Returns the node value.
	 *
	 * @param obj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node.
	 * @return the node value
	 */
	public Object getValue(Object obj, Map<String, Object> args);

	/**
	 * Sets the field value.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param obj the instance that will have the node value setted.
	 * @param newValue the new node value
	 * @throws IllegalArgumentException if not found
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public default void setValue(Object obj, Object newValue) {
		setValue(obj, newValue, null);
	}

	/**
	 * Sets the field value.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param obj the instance that will have the node value setted.
	 * @param args the arguments that will be used to access this node.
	 * @param newValue the new node value
	 * @throws IllegalArgumentException if not found
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public default void setValue(Object obj, Object newValue, Map<String, Object> args) {
		setValue(obj, newValue, args, DEFAULT_FACTORY);
	}

	/**
	 * Sets the field value.
	 *
	 * @param obj the instance that will have the node value setted.
	 * @param newValue the new node value
	 * @param args the arguments that will be used to access this node.
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional).
	 * @throws IllegalArgumentException if not found
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setValue(Object obj, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory);

}

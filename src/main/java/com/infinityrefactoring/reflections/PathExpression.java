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
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a path expression, that can be used to access nested fields or methods of a given root object.
 * A path expression is compound by a list of {@linkplain ExpressionNode expression nodes} separated by a {@linkplain #DOT_REGEX dot}.
 * Path Expression examples:
 * <ul>
 * <li>foo</li>
 * <li>person.name</li>
 * <li>person.name.toLowerCase()</li>
 * <li>bar.getMatrix()[4][6].foo.toString()</li>
 * <li>person.getAddresses()[1].country.name</li>
 * </ul>
 *
 * @see #compile(String)
 * @see Reflections
 * @see ClassWrapper
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class PathExpression {

	/**
	 * The default regex pattern for the literal String ".", that is used to split the nodes of a given path expression.
	 */
	public static final String DOT_REGEX = quote(".");
	private static final Map<String, PathExpression> PATH_EXPRESSIONS = new HashMap<>();
	private final String PATH_EXPRESSION;
	private final List<ExpressionNode> NODES;
	private int LAST_INDEX;

	/**
	 * Compiles the given path expression in an {@linkplain PathExpression} instance.
	 * Note: If this path expression already was compiled then the cached instance that will be returned.
	 *
	 * @param pathExpression the desired path expression
	 * @return an compiled instance
	 * @throws IllegalArgumentException if the given expression node is null or empty
	 */
	public static PathExpression compile(String pathExpression) {
		if ((pathExpression == null) || (pathExpression = pathExpression.trim()).isEmpty()) {
			throw new IllegalArgumentException("The path expression cannot be null or empty.");
		}
		return PATH_EXPRESSIONS.computeIfAbsent(pathExpression, PathExpression::new);
	}

	/**
	 * Returns the expression node value or a instance supplied by the given {@linkplain InstanceFactory}, if necessary.
	 * Note: If the expression node value is null and is necessary an instance and the instance factory is not null, then this instance will be setted to the given node in the root object.
	 *
	 * @param obj the instance that will have the node value extracted
	 * @param node the desired node
	 * @param args the arguments that will be used to access this node.
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional).
	 * @param isNecessaryAnInstance use true if is necessary an instance.
	 * @return the next object.
	 */
	private static Object getNextObj(Object rootObj, ExpressionNode node, Map<String, Object> args, InstanceFactory instanceFactory, boolean isNecessaryAnInstance) {
		Object nextObj = node.getValue(rootObj, args);
		if ((nextObj == null) && (instanceFactory != null) && isNecessaryAnInstance) {
			Class<?> nodeClass = node.getNodeClass(rootObj, args);
			nextObj = instanceFactory.getInstance(nodeClass, args);
			node.setValue(rootObj, nextObj, args);
		}
		return nextObj;
	}

	/**
	 * Constructs a new instance of PathExpression.
	 * @param pathExpression the desired path expression
	 * @throws
	 */
	private PathExpression(String pathExpression) {
		PATH_EXPRESSION = pathExpression;
		String[] expressionNodes = PATH_EXPRESSION.split(DOT_REGEX);
		if (expressionNodes.length == 0) {
			throw new IllegalArgumentException("Invalid path expression.");
		}
		NODES = unmodifiableList(Stream.of(expressionNodes).map(ExpressionNode::compile).collect(toList()));
		LAST_INDEX = (NODES.size() - 1);
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof PathExpression) && PATH_EXPRESSION.equals(((PathExpression) obj).PATH_EXPRESSION));
	}

	/**
	 * Returns the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value extracted
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getExpressionValue(Object, Map)
	 * @see #getExpressionValue(Object, InstanceFactory)
	 * @see #getExpressionValue(Object, Map, InstanceFactory)
	 * @see #setExpressionValue(Object, Object)
	 */
	public <R> R getExpressionValue(Object rootObj) {
		return getExpressionValue(rootObj, null, null);
	}

	/**
	 * Returns the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value extracted
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getExpressionValue(Object)
	 * @see #getExpressionValue(Object, Map)
	 * @see #getExpressionValue(Object, Map, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public <R> R getExpressionValue(Object rootObj, InstanceFactory instanceFactory) {
		return getExpressionValue(rootObj, null, instanceFactory);
	}

	/**
	 * Returns the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getExpressionValue(Object)
	 * @see #getExpressionValue(Object, InstanceFactory)
	 * @see #getExpressionValue(Object, Map, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, Map)
	 */
	public <R> R getExpressionValue(Object rootObj, Map<String, Object> args) {
		return getExpressionValue(rootObj, args, null);
	}

	/**
	 * Returns the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getExpressionValue(Object)
	 * @see #getExpressionValue(Object, Map)
	 * @see #getExpressionValue(Object, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	@SuppressWarnings("unchecked")
	public <R> R getExpressionValue(Object rootObj, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (rootObj == null) {
			return null;
		}
		int index = 0;
		Object nextObj = null;
		for (ExpressionNode node : NODES) {
			nextObj = getNextObj(rootObj, node, args, instanceFactory, (index < LAST_INDEX));
			if (nextObj == null) {
				return null;
			}
			rootObj = nextObj;
			index++;
		}
		return (R) nextObj;
	}

	/**
	 * Returns the expression path that this instance represents.
	 *
	 * @return the expression path.
	 */
	public String getPathExpression() {
		return PATH_EXPRESSION;
	}

	@Override
	public int hashCode() {
		return PATH_EXPRESSION.hashCode();
	}

	/**
	 * Sets the expression value for the given root object.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param newValue the new expression value
	 * @see #setExpressionValue(Object, Object, Map)
	 * @see #setExpressionValue(Object, Object, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see #getExpressionValue(Object)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setExpressionValue(Object rootObj, Object newValue) {
		setExpressionValue(rootObj, newValue, null, DEFAULT_FACTORY);
	}

	/**
	 * Sets the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param newValue the new expression value
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @see #setExpressionValue(Object, Object)
	 * @see #setExpressionValue(Object, Object, Map)
	 * @see #setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see #getExpressionValue(Object, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setExpressionValue(Object rootObj, Object newValue, InstanceFactory instanceFactory) {
		setExpressionValue(rootObj, newValue, null, instanceFactory);
	}

	/**
	 * Sets the expression value for the given root object.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @see #setExpressionValue(Object, Object)
	 * @see #setExpressionValue(Object, Object, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see #getExpressionValue(Object, Map)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setExpressionValue(Object rootObj, Object newValue, Map<String, Object> args) {
		setExpressionValue(rootObj, newValue, args, DEFAULT_FACTORY);
	}

	/**
	 * Sets the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value setted
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @see #setExpressionValue(Object, Object)
	 * @see #setExpressionValue(Object, Object, Map)
	 * @see #setExpressionValue(Object, Object, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see #getExpressionValue(Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setExpressionValue(Object rootObj, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (rootObj != null) {
			int index = 0;
			Object nextObj = null;
			for (ExpressionNode node : NODES) {
				if (index == LAST_INDEX) {
					node.setValue(rootObj, newValue, args);
				} else {
					nextObj = getNextObj(rootObj, node, args, instanceFactory, true);
					if (nextObj == null) {
						break;
					}
					rootObj = nextObj;
					index++;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "PathExpression [PATH_EXPRESSION=" + PATH_EXPRESSION + "]";
	}

}

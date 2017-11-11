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

import static com.infinityrefactoring.reflections.ClassWrapper.getMemberType;
import static com.infinityrefactoring.reflections.InstanceFactory.DEFAULT_FACTORY;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterator;
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
public class PathExpression implements Iterable<ExpressionNode> {

	/**
	 * The default regex pattern for the literal String ".", that is used to split the nodes of a given path expression.
	 */
	public static final String DOT_REGEX = quote(".");

	/**
	 * The static path expression prefix.
	 * A static path must be in the following pattern: "class(the.full.root.class.name)some.non.static.path.expression".
	 *
	 * @see #toStaticExpression(Class, String)
	 * @see #compile(Class, String)
	 */
	public static final String STATIC_PATH_EXPRESSION_PREFIX = "class(";

	private static final Map<String, PathExpression> PATH_EXPRESSIONS = new HashMap<>(50);
	private final Class<?> ROOT_CLASS;
	private final String PATH_EXPRESSION;
	private final List<ExpressionNode> NODES;
	private final boolean NEED_ARGUMENTS;
	private final int LAST_INDEX;

	/**
	 * Compiles the given path expression in an {@linkplain #STATIC_PATH_EXPRESSION_PREFIX static path expression} instance.
	 * Note: If this path expression already was compiled then the cached instance that will be returned.
	 *
	 * @param c the root class
	 * @param pathExpression the desired path expression
	 * @return an compiled instance
	 * @throws IllegalArgumentException if the given expression node is null or empty
	 * @see #compile(String)
	 * @see #toStaticExpression(Class, String)
	 */
	public static PathExpression compile(Class<?> c, String pathExpression) {
		return compile((c == null) ? pathExpression : toStaticExpression(c, pathExpression));
	}

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
	 * Returns the given {@linkplain #STATIC_PATH_EXPRESSION_PREFIX static path expression pattern} without the root class.
	 *
	 * @param pathExpression the desired path expression
	 * @return the non static path expression
	 * @see #toStaticExpression(Class, String)
	 */
	public static String toNonStaticExpression(String pathExpression) {
		if (pathExpression.startsWith(STATIC_PATH_EXPRESSION_PREFIX)) {
			int index = (pathExpression.indexOf(')') + 1);
			if ((index <= STATIC_PATH_EXPRESSION_PREFIX.length()) || (index == pathExpression.length())) {
				throw new IllegalArgumentException("Invalid static path expression.");
			}
			pathExpression = pathExpression.substring(index);
		}
		return pathExpression;
	}

	/**
	 * Returns a String in the {@linkplain #STATIC_PATH_EXPRESSION_PREFIX static path expression pattern}.
	 * Note: If the given path expression already a static path expression then only the root class will be replaced.
	 *
	 * @param c the root class
	 * @param pathExpression the desired path expression
	 * @return the static path expression
	 * @see #toNonStaticExpression(String)
	 * @see #compile(Class, String)
	 */
	public static String toStaticExpression(Class<?> c, String pathExpression) {
		return format("%s%s)%s", STATIC_PATH_EXPRESSION_PREFIX, c.getName(), toNonStaticExpression(pathExpression));
	}

	/**
	 * Returns the expression node value or a instance supplied by the given {@linkplain InstanceFactory}, if necessary.
	 * Note: If the expression node value is null and is necessary an instance and the instance factory is not null, then this instance will be setted to the given node in the root object.
	 *
	 * @param c the class that will have the node value extracted
	 * @param node the desired node
	 * @param args the arguments that will be used to access this node
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @param isNecessaryAnInstance use true if is necessary an instance
	 * @return the next object
	 */
	private static Object getNextObj(Class<?> c, ExpressionNode node, Map<String, Object> args, InstanceFactory instanceFactory, boolean isNecessaryAnInstance) {
		Object nextObj = node.getStaticValue(c, args);
		if ((nextObj == null) && (instanceFactory != null) && isNecessaryAnInstance) {
			Class<?> nodeClass = node.getStaticNodeClass(c, args);
			nextObj = instanceFactory.getInstance(nodeClass, args);
			node.setStaticValue(c, nextObj, args);
		}
		return nextObj;
	}

	/**
	 * Returns the expression node value or a instance supplied by the given {@linkplain InstanceFactory}, if necessary.
	 * Note: If the expression node value is null and is necessary an instance and the instance factory is not null, then this instance will be setted to the given node in the root object.
	 *
	 * @param obj the instance that will have the node value extracted
	 * @param node the desired node
	 * @param args the arguments that will be used to access this node
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @param isNecessaryAnInstance use true if is necessary an instance
	 * @return the next object
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
	 *
	 * @param pathExpression the desired path expression
	 * @throws IllegalArgumentException if the path expression is invalid
	 */
	private PathExpression(String pathExpression) {
		PATH_EXPRESSION = pathExpression;
		String[] expressionNodes;
		if (PATH_EXPRESSION.startsWith(STATIC_PATH_EXPRESSION_PREFIX)) {
			int lastParameter = PATH_EXPRESSION.indexOf(')');
			if (lastParameter <= STATIC_PATH_EXPRESSION_PREFIX.length()) {
				throw new IllegalArgumentException("Invalid static path expression.");
			}
			try {
				ROOT_CLASS = Class.forName(PATH_EXPRESSION.substring(STATIC_PATH_EXPRESSION_PREFIX.length(), lastParameter));
			} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Invalid static path expression.", ex);
			}
			lastParameter++;
			if (PATH_EXPRESSION.length() <= lastParameter) {
				throw new IllegalArgumentException("Invalid static path expression.");
			}
			expressionNodes = PATH_EXPRESSION.substring(lastParameter).split(DOT_REGEX);
		} else {
			ROOT_CLASS = null;
			expressionNodes = PATH_EXPRESSION.split(DOT_REGEX);
		}

		if (expressionNodes.length == 0) {
			throw new IllegalArgumentException("Invalid path expression.");
		}
		NODES = unmodifiableList(Stream.of(expressionNodes).map(ExpressionNode::compile).collect(toList()));
		LAST_INDEX = (NODES.size() - 1);
		NEED_ARGUMENTS = NODES.stream().anyMatch(ExpressionNode::needArguments);

		if (isStaticExpression() && (!needArguments())) {
			try {
				getLastMember();
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Invalid static path expression.", ex);
			}
		}
	}

	/**
	 * @param pathExpression the
	 * @return the concatenated path expression
	 */

	/**
	 * Concatenates the specified path expression to the end of this path expression.
	 * Note: If the given path expression is a static path expression then only the {@linkplain #toNonStaticExpression() non static part} will be concatenated.
	 * Examples:
	 * <code><pre>
	 * "foo".concat("bar") returns "foo.bar"
	 * "foo".concat("class(com.Bar)name") returns "foo.name"
	 * </pre></code>
	 *
	 * @param pathExpression the path expression that is concatenated to the end of this instance.
	 * @return a path expression that represents the concatenation of this instance followed by the given path expression.
	 */
	public PathExpression concat(PathExpression pathExpression) {
		return compile(format("%s.%s", PATH_EXPRESSION, pathExpression.toNonStaticExpression().PATH_EXPRESSION));
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
	 * @see #setStaticExpressionValue(Class, Object)
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
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
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
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #needArguments()
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
	 * @see #getStaticExpressionValue(Class, Map, InstanceFactory)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public <R> R getExpressionValue(Object rootObj, Map<String, Object> args, InstanceFactory instanceFactory) {
		return getExpressionValue(rootObj, args, instanceFactory, false);
	}

	/**
	 * Returns the first node.
	 *
	 * @return the first node.
	 * @see #getLastNode()
	 */
	public ExpressionNode getFirstNode() {
		return NODES.get(0);
	}

	/**
	 * Returns the member that the last node represents on the {@linkplain #getRootClass() root class}.
	 *
	 * @return the member
	 * @see #getLastMember(Class, Map)
	 * @see #isStaticExpression()
	 */
	public Member getLastMember() {
		return getLastMember(getRootClass());
	}

	/**
	 * Returns the member that the last node represents on the given class.
	 *
	 * @param c the class that have this node
	 * @return the member
	 * @see #getLastMember(Class, Map)
	 */
	public Member getLastMember(Class<?> c) {
		return getLastMember(c, null);
	}

	/**
	 * Returns the member that the last node represents on the given class.
	 *
	 * @param c the class that have this node
	 * @param args the arguments that will be used to access this node.
	 * @return the member
	 * @see #getLastMember(Class)
	 * @see #needArguments()
	 */
	public Member getLastMember(Class<?> c, Map<String, Object> args) {
		return getMemberOf(LAST_INDEX, c, args);
	}

	/**
	 * Returns the member that the last node represents on the {@linkplain #getRootClass() root class}.
	 *
	 * @param args the arguments that will be used to access this node.
	 * @return the member
	 * @see #getLastMember(Class, Map)
	 * @see #isStaticExpression()
	 */
	public Member getLastMember(Map<String, Object> args) {
		return getLastMember(getRootClass(), args);
	}

	/**
	 * Returns the last node.
	 *
	 * @return the last node.
	 * @see #getFirstNode()
	 */
	public ExpressionNode getLastNode() {
		return NODES.get(NODES.size() - 1);
	}

	public Member getMemberOf(int index) {
		return getMemberOf(index, getRootClass(), null);
	}

	public Member getMemberOf(int index, Class<?> c) {
		return getMemberOf(index, c, null);
	}

	/**
	 * Returns the member that the given node index represents on the given class.
	 * @param index the node index (inclusive)
	 * @param c the class that have this node
	 * @param args the arguments that will be used to access this node.
	 * @return the member
	 * @see #getMemberOf(int)
	 * @see #getMemberOf(int, Map)
	 * @see #getMemberOf(int, Class)
	 * @see #getLastMember(Class, Map)
	 * @see #needArguments()
	 * @throws IndexOutOfBoundsException if the index is invalid
	 * @throws
	 */
	public Member getMemberOf(int index, Class<?> c, Map<String, Object> args) {
		checkIfSupport(c, true);
		checkRange("Index", index, false);
		Member member = null;
		int i = 0;
		for (ExpressionNode node : NODES) {
			member = node.getMember(c, args);
			if (i == index) {
				return member;
			}
			if (i < LAST_INDEX) {
				c = getMemberType(member);
				while (c.isArray()) {
					c = c.getComponentType();
				}
			}
			i++;
		}
		return member;
	}

	public Member getMemberOf(int index, Map<String, Object> args) {
		return getMemberOf(index, getRootClass(), args);
	}

	/**
	 * Returns the expression nodes that is contained in this instance.
	 *
	 * @return the nodes.
	 */
	public List<ExpressionNode> getNodes() {
		return NODES;
	}

	/**
	 * Returns the nodes amount in this path expression.
	 *
	 * @return the amount.
	 */
	public int getNodesAmount() {
		return NODES.size();
	}

	/**
	 * Returns the expression path that this instance represents.
	 *
	 * @return the expression path.
	 */
	public String getPathExpression() {
		return PATH_EXPRESSION;
	}

	/**
	 * Returns the root class.
	 *
	 * @return the root class
	 * @see #isStaticExpression()
	 * @throws UnsupportedOperationException if this instance is not a {@linkplain #isStaticExpression() static path expression}
	 */
	public Class<?> getRootClass() {
		if (isStaticExpression()) {
			return ROOT_CLASS;
		}
		throw new UnsupportedOperationException("The path expression is not a static path expression.");
	}

	/**
	 * Returns the expression value for the {@linkplain #getRootClass() root class}.
	 *
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue(Map)
	 * @see #getStaticExpressionValue(InstanceFactory)
	 * @see #getStaticExpressionValue(Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Object)
	 * @see #isStaticExpression()
	 */
	public <R> R getStaticExpressionValue() {
		return getStaticExpressionValue(getRootClass());
	}

	/**
	 * Returns the expression value for the given class.
	 *
	 * @param c the class that will have the node value extracted
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue(Class, Map)
	 * @see #getStaticExpressionValue(Class, InstanceFactory)
	 * @see #getStaticExpressionValue(Class, Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object)
	 * @see #getExpressionValue(Object)
	 */
	public <R> R getStaticExpressionValue(Class<?> c) {
		return getStaticExpressionValue(c, null, null);
	}

	/**
	 * Returns the expression value for the given class.
	 *
	 * @param c the class that will have the node value extracted
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue(Class)
	 * @see #getStaticExpressionValue(Class, Map)
	 * @see #getStaticExpressionValue(Class, Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
	 * @see #getExpressionValue(Object, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public <R> R getStaticExpressionValue(Class<?> c, InstanceFactory instanceFactory) {
		return getStaticExpressionValue(c, null, instanceFactory);
	}

	/**
	 * Returns the expression value for the given class.
	 *
	 * @param c the class that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue(Class)
	 * @see #getStaticExpressionValue(Class, InstanceFactory)
	 * @see #getStaticExpressionValue(Class, Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #getExpressionValue(Object, Map)
	 * @see #needArguments()
	 */
	public <R> R getStaticExpressionValue(Class<?> c, Map<String, Object> args) {
		return getStaticExpressionValue(c, args, null);
	}

	/**
	 * Returns the expression value for the given class.
	 *
	 * @param c the class that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue(Class)
	 * @see #getStaticExpressionValue(Class, Map)
	 * @see #getStaticExpressionValue(Class, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #getExpressionValue(Object, Map, InstanceFactory)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public <R> R getStaticExpressionValue(Class<?> c, Map<String, Object> args, InstanceFactory instanceFactory) {
		return getExpressionValue(c, args, instanceFactory, true);
	}

	/**
	 * Returns the expression value for the {@linkplain #getRootClass() root class}.
	 *
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue()
	 * @see #getStaticExpressionValue(Map)
	 * @see #getStaticExpressionValue(Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Object, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 * @see #isStaticExpression()
	 */
	public <R> R getStaticExpressionValue(InstanceFactory instanceFactory) {
		return getStaticExpressionValue(getRootClass(), instanceFactory);
	}

	/**
	 * Returns the expression value for the {@linkplain #getRootClass() root class}.
	 *
	 * @param args the arguments that will be used to access this node (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue()
	 * @see #getStaticExpressionValue(InstanceFactory)
	 * @see #getStaticExpressionValue(Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Object, Map)
	 * @see #isStaticExpression()
	 * @see #needArguments()
	 */
	public <R> R getStaticExpressionValue(Map<String, Object> args) {
		return getStaticExpressionValue(getRootClass(), args);
	}

	/**
	 * Returns the expression value for the {@linkplain #getRootClass() root class}.
	 *
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 * @see #getStaticExpressionValue()
	 * @see #getStaticExpressionValue(Map)
	 * @see #getStaticExpressionValue(InstanceFactory)
	 * @see #setStaticExpressionValue(Object, Map, InstanceFactory)
	 * @see #isStaticExpression()
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public <R> R getStaticExpressionValue(Map<String, Object> args, InstanceFactory instanceFactory) {
		return getStaticExpressionValue(getRootClass(), args, instanceFactory);
	}

	@Override
	public int hashCode() {
		return PATH_EXPRESSION.hashCode();
	}

	/**
	 * Returns true if this instance is a {@linkplain #STATIC_PATH_EXPRESSION_PREFIX static path expression}.
	 *
	 * @return the true if this instance is a static path expression
	 * @see #getRootClass()
	 */
	public boolean isStaticExpression() {
		return (ROOT_CLASS != null);
	}

	@Override
	public Iterator<ExpressionNode> iterator() {
		return NODES.iterator();
	}

	/**
	 * Returns a list iterator over the nodes in this path (in proper sequence).
	 *
	 * @return a list iterator
	 * @see #listIterator(int)
	 */
	public ListIterator<ExpressionNode> listIterator() {
		return NODES.listIterator();
	}

	/**
	 * Returns a list iterator over the nodes in this path (in proper sequence).
	 *
	 * @param index index of the first element to be returned from the
	 *            list iterator (by a call to {@link ListIterator#next next})
	 * @return a list iterator
	 * @see #listIterator()
	 */
	public ListIterator<ExpressionNode> listIterator(int index) {
		return NODES.listIterator(index);
	}

	/**
	 * Returns another path expression instance with a nodes amount reduced.
	 * Per example: "foo.bar".moveBackward(1) returns "foo".
	 *
	 * @param nodesAmount the end nodes amount that will be discarded
	 * @return a another path expression
	 * @see #moveForward(int)
	 * @see #subPath(int, int)
	 */
	public PathExpression moveBackward(int nodesAmount) {
		return subPath(0, (NODES.size() - nodesAmount));
	}

	/**
	 * Returns another path expression instance with a nodes amount reduced.
	 * Per example: "foo.bar".moveForward(1) returns "bar".
	 *
	 * @param nodesAmount the begin nodes amount that will be discarded.
	 * @return a another path expression.
	 * @see #moveBackward(int)
	 * @see #subPath(int, int)
	 */
	public PathExpression moveForward(int nodesAmount) {
		return subPath(nodesAmount, NODES.size());
	}

	/**
	 * Returns true if some node require arguments to will be invoked.
	 *
	 * @return true if some node require arguments
	 */
	public boolean needArguments() {
		return NEED_ARGUMENTS;
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
	 * @see #setStaticExpressionValue(Class, Object)
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
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
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
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #needArguments()
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
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setExpressionValue(Object rootObj, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		setExpressionValue(rootObj, newValue, args, instanceFactory, false);
	}

	/**
	 * Sets the expression value for the given class.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param c the class that will have the node value setted
	 * @param newValue the new expression value
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(Class)
	 * @see #setExpressionValue(Object, Object)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Class<?> c, Object newValue) {
		setStaticExpressionValue(c, newValue, null, DEFAULT_FACTORY);
	}

	/**
	 * Sets the expression value for the given class.
	 *
	 * @param c the class that will have the node value setted
	 * @param newValue the new expression value
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @see #setStaticExpressionValue(Class, Object)
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(Class, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Class<?> c, Object newValue, InstanceFactory instanceFactory) {
		setStaticExpressionValue(c, newValue, null, instanceFactory);
	}

	/**
	 * Sets the expression value for the given class.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param c the class that will have the node value setted
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @see #setStaticExpressionValue(Class, Object)
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(Class, Map)
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #setExpressionValue(Object, Object, Map)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Class<?> c, Object newValue, Map<String, Object> args) {
		setStaticExpressionValue(c, newValue, args, DEFAULT_FACTORY);
	}

	/**
	 * Sets the expression value for the given class.
	 *
	 * @param c the class that will have the node value setted
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @see #setStaticExpressionValue(Class, Object)
	 * @see #setStaticExpressionValue(Class, Object, Map)
	 * @see #setStaticExpressionValue(Class, Object, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(Class, Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Class, Object, Map, InstanceFactory)
	 * @see #setExpressionValue(Object, Object, Map, InstanceFactory)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Class<?> c, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		setExpressionValue(c, newValue, args, instanceFactory, true);
	}

	/**
	 * Sets the expression value for the {@linkplain #getRootClass() root class}.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param newValue the new expression value
	 * @see #setStaticExpressionValue(Object, Map)
	 * @see #setStaticExpressionValue(Object, InstanceFactory)
	 * @see #setStaticExpressionValue(Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Object newValue) {
		setStaticExpressionValue(getRootClass(), newValue);
	}

	/**
	 * Sets the expression value for the {@linkplain #getRootClass() root class}.
	 *
	 * @param newValue the new expression value
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @see #setStaticExpressionValue(Object)
	 * @see #setStaticExpressionValue(Object, Map)
	 * @see #setStaticExpressionValue(Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(InstanceFactory)
	 * @see #setStaticExpressionValue(Object, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Object newValue, InstanceFactory instanceFactory) {
		setStaticExpressionValue(getRootClass(), newValue, instanceFactory);
	}

	/**
	 * Sets the expression value for the {@linkplain #getRootClass() root class}.
	 * Note: This method uses the {@linkplain InstanceFactory#DEFAULT_FACTORY default factory} to supplies instances if necessary
	 *
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @see #setStaticExpressionValue(Object)
	 * @see #setStaticExpressionValue(Object, InstanceFactory)
	 * @see #setStaticExpressionValue(Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(Map)
	 * @see #setStaticExpressionValue(Object, Map)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Object newValue, Map<String, Object> args) {
		setStaticExpressionValue(getRootClass(), newValue, args);
	}

	/**
	 * Sets the expression value for the {@linkplain #getRootClass() root class}.
	 *
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @see #setStaticExpressionValue(Object)
	 * @see #setStaticExpressionValue(Object, Map)
	 * @see #setStaticExpressionValue(Object, InstanceFactory)
	 * @see #setStaticExpressionValue(Object, Map, InstanceFactory)
	 * @see #getStaticExpressionValue(Map, InstanceFactory)
	 * @see #setStaticExpressionValue(Object, Map, InstanceFactory)
	 * @see #needArguments()
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public void setStaticExpressionValue(Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		setStaticExpressionValue(getRootClass(), newValue, args, instanceFactory);
	}

	@Override
	public Spliterator<ExpressionNode> spliterator() {
		return NODES.spliterator();
	}

	/**
	 * Returns a sequential Stream with the expression nodes of this instance.
	 *
	 * @return a stream
	 */
	public Stream<ExpressionNode> stream() {
		return NODES.stream();
	}

	/**
	 * Returns another path expression instance with a nodes amount reduced.
	 * Per example: "foo.bar.name".subPath(1, 2) returns "bar".
	 *
	 * @param beginIndex the index of the begin node (inclusive)
	 * @param endIndex the index of the end node (exclusive)
	 * @return a another path expression
	 * @throws IndexOutOfBoundsException if the index range is invalid
	 * @throws IllegalArgumentException if the endIndex is less or equal than the beginIndex
	 */
	public PathExpression subPath(int beginIndex, int endIndex) {
		if ((beginIndex == 0) && (endIndex == NODES.size())) {
			return this;
		}
		checkRange("BeginIndex", beginIndex, false);
		checkRange("EndIndex", endIndex, true);

		if (endIndex <= beginIndex) {
			throw new IllegalArgumentException("The endIndex must be greater than beginIndex.");
		} else if ((endIndex - beginIndex) == 1) {
			return compile(ROOT_CLASS, NODES.get(beginIndex).getName());
		}

		StringBuilder builder = new StringBuilder();
		for (int i = beginIndex; i < endIndex; i++) {
			if (builder.length() > 0) {
				builder.append('.');
			}
			builder.append(NODES.get(i).getName());
		}
		return compile(ROOT_CLASS, builder.toString());
	}

	/**
	 * Returns a path expression that represents this instance without the {@linkplain #getRootClass() root class}
	 *
	 * @return the non static path expression
	 * @see #toStaticExpression(Class)
	 */
	public PathExpression toNonStaticExpression() {
		if (isStaticExpression()) {
			return compile(toNonStaticExpression(PATH_EXPRESSION));
		}
		return this;
	}

	/**
	 * Returns a path expression in the {@linkplain #STATIC_PATH_EXPRESSION_PREFIX static path expression pattern}.
	 * Note: If this instance already a static path expression then only the root class will be replaced.
	 *
	 * @param c the root class
	 * @return the static path expression
	 * @see #toNonStaticExpression()
	 * @see #compile(Class, String)
	 */
	public PathExpression toStaticExpression(Class<?> c) {
		return compile(c, PATH_EXPRESSION);
	}

	/**
	 * Returns a string that represents this path expression.
	 *
	 * @return a string that represents this path expression
	 */
	@Override
	public String toString() {
		return PATH_EXPRESSION;
	}

	/**
	 * Checks if this instance support the given root obj. A static path expression only support a root obj equal to the {@linkplain #ROOT_CLASS root class}
	 *
	 * @param rootObj the desired root obj
	 * @param staticExpressionValue use true to indicates that this method is being called in a method that get/set a static value
	 * @throws UnsupportedOperationException if this instance is a static path expression and the given staticExpressionValue argument is false
	 * @throws IllegalArgumentException if the static path expression not support the given root obj
	 */
	private void checkIfSupport(Object rootObj, boolean staticExpressionValue) {
		if (isStaticExpression()) {
			if (!staticExpressionValue) {
				throw new UnsupportedOperationException("The static path expression not support this operation. Use a static method equivalent.");
			} else if (rootObj != ROOT_CLASS) {
				throw new IllegalArgumentException(format("The static path expression only support the %s class as root obj.", ROOT_CLASS.getName()));
			}
		}
	}

	/**
	 * Checks if the given index is less than zero or greater (or equal, if exclusive = false) than node amount + 1.
	 *
	 * @param indexName the index name that will be used in the exception message
	 * @param index the desired index
	 * @param exclusive use true to indicate that a given index equal than node amount + 1 is valid
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	private void checkRange(String indexName, int index, boolean exclusive) {
		if ((index < 0) || (exclusive ? (index > NODES.size()) : (index >= NODES.size()))) {
			throw new IndexOutOfBoundsException(format("%s: %d, Size: %d", indexName, index, NODES.size()));
		}
	}

	/**
	 * Returns the expression value for the given root object.
	 *
	 * @param rootObj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @param staticExpressionValue use true to indicate that the begin of this path is static.
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 */
	@SuppressWarnings("unchecked")
	private <R> R getExpressionValue(Object rootObj, Map<String, Object> args, InstanceFactory instanceFactory, boolean staticExpressionValue) {
		checkIfSupport(rootObj, staticExpressionValue);
		if (rootObj == null) {
			return null;
		}
		int index = 0;
		Object nextObj = null;
		for (ExpressionNode node : NODES) {
			if (staticExpressionValue && (index == 0)) {
				nextObj = getNextObj((Class<?>) rootObj, node, args, instanceFactory, (index < LAST_INDEX));
			} else {
				nextObj = getNextObj(rootObj, node, args, instanceFactory, (index < LAST_INDEX));
			}
			if (nextObj == null) {
				return null;
			}
			rootObj = nextObj;
			index++;
		}
		return (R) nextObj;
	}

	/**
	 * Sets the expression value for the given root object.
	 *
	 * @param c the class that will have the node value setted
	 * @param newValue the new expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @param staticExpressionValue use true to indicate that the begin of this path is static.
	 */
	private void setExpressionValue(Object rootObj, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory, boolean staticExpressionValue) {
		checkIfSupport(rootObj, staticExpressionValue);
		if (rootObj != null) {
			int index = 0;
			Object nextObj = null;
			for (ExpressionNode node : NODES) {
				if (index == LAST_INDEX) {
					if (staticExpressionValue && (index == 0)) {
						node.setStaticValue((Class<?>) rootObj, newValue, args);
					} else {
						node.setValue(rootObj, newValue, args);
					}
				} else {
					if (staticExpressionValue && (index == 0)) {
						nextObj = getNextObj((Class<?>) rootObj, node, args, instanceFactory, true);
					} else {
						nextObj = getNextObj(rootObj, node, args, instanceFactory, true);
					}
					if (nextObj == null) {
						break;
					}
					rootObj = nextObj;
					index++;
				}
			}
		}
	}

}

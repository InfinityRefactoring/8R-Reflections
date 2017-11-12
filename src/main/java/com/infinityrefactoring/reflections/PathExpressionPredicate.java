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

import static com.infinityrefactoring.reflections.PathExpression.compile;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.isEqual;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A predicate composed by one or two path expression and a predicate.
 * A PathExpressionPredicate gets the path expression values and tests this values with the internal predicate.
 * The defined pattern is a path expression named "when", a predicate named "is" and another path expression named "than".
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 * @see PathExpression
 * @see UncheckedPredicate
 */
public class PathExpressionPredicate {

	/**
	 * Returns a predicate that accept anything.
	 */
	public static final PathExpressionPredicate ACCEPT_ALL = new PathExpressionPredicate(true);

	/**
	 * Returns a predicate that not accept anything.
	 */
	public static final PathExpressionPredicate DENY_ALL = new PathExpressionPredicate(false);

	/**
	 * The default argument key that represents the root obj.
	 * Use the {@value} key in the path expression to enable the creation of a argument map with the root obj automatically.
	 *
	 * @see ExpressionNode#getArgumentKeys()
	 */
	public static final String ROOT_OBJ_KEY = "rootObj";

	private static final Predicate<String> ROOT_OBJ_KEY_PREDICATE = isEqual(ROOT_OBJ_KEY);
	private final PathExpression WHEN;
	private final UncheckedPredicate IS;
	private final PathExpression THAN;
	private final boolean PUT_ROOT_OBJ1;
	private final boolean PUT_ROOT_OBJ2;
	private final boolean DEFAULT_VALUE;

	/**
	 * Returns a predicate that gets the path expression values "when" and "than" and tests this values with the predicate "is".
	 *
	 * @param when the path expression that will be used to get the first value that will be tested
	 * @param is the predicate that will be used to test the values of the path expression "when" and "than"
	 * @param than the path expression that will be used to get the second value that will be tested
	 * @param defaultPredicate the default predicate that will be returned if no path expression was specified
	 * @return a predicate
	 * @throws NullPointerException if some of the gives "when", "is" or "than" arguments are null
	 * @throws IllegalArgumentException if some of the gives "when", "is" or "than" arguments is invalid
	 * @see #ACCEPT_ALL
	 * @see #DENY_ALL
	 */
	public static PathExpressionPredicate of(String when, UncheckedPredicate is, String than, PathExpressionPredicate defaultPredicate) {
		requireNonNull(when);
		requireNonNull(is);
		requireNonNull(than);
		if (when.isEmpty()) {
			if (is.isDefined()) {
				throw new IllegalArgumentException("The [is] predicate cannot be defined if the [when] path expression is not specified.");
			} else if (!than.isEmpty()) {
				throw new IllegalArgumentException(format("The [than] path expression cannot be specified if the [when] path expression is not specified."));
			}
		} else if (!is.isDefined()) {
			throw new IllegalArgumentException("The [is] predicate cannot be undefined if the [when] path expression is specified.");
		}
		if (is.requireTwoValues()) {
			if (than.isEmpty()) {
				throw new IllegalArgumentException(format("The [than] path expression must be specified because the [%s] predicate require two values.", is));
			}
		} else if (!than.isEmpty()) {
			throw new IllegalArgumentException(format("The [than] path expression cannot be specified because the [%s] predicate require only one value.", is));
		}
		return (when.isEmpty() ? defaultPredicate : new PathExpressionPredicate(when, is, than));
	}

	/**
	 * Returns the argument keys of the given path expression.
	 *
	 * @param p the desired path expression
	 * @return the argument keys
	 */
	private static Stream<String> getArgumentKeys(PathExpression p) {
		return (((p != null) && p.needArguments()) ? p.stream().flatMap(node -> node.getArgumentKeys().stream()) : Stream.empty());
	}

	/**
	 * Returns the expression value for the given root obj.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the given path expression {@linkplain PathExpression#isStaticExpression() is static} then the rootObj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param pathExpression the desired path expression
	 * @param rootObj the instance that will have the node value extracted
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return the expression value or null, if is not possible access the end of this expression or if this expression return void
	 */
	private static Object getExpressionValue(PathExpression pathExpression, Object rootObj, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (pathExpression.isStaticExpression()) {
			return pathExpression.getStaticExpressionValue(args, instanceFactory);
		} else if (rootObj instanceof Class) {
			pathExpression.getStaticExpressionValue((Class<?>) rootObj, args, instanceFactory);
		}
		return pathExpression.getExpressionValue(rootObj, args, instanceFactory);
	}

	/**
	 * Constructs a new instance of PathExpressionPredicate that always returns the given default value.
	 *
	 * @param defaultValue the desired default value
	 */
	private PathExpressionPredicate(boolean defaultValue) {
		WHEN = null;
		IS = null;
		THAN = null;
		DEFAULT_VALUE = defaultValue;
		PUT_ROOT_OBJ1 = false;
		PUT_ROOT_OBJ2 = false;
	}

	/**
	 * Constructs a new instance of PathExpressionPredicate.
	 *
	 * @param when the path expression that will be used to get the first value that will be tested
	 * @param is the predicate that will be used to test the values of the path expression "when" and "than"
	 * @param than the path expression that will be used to get the second value that will be tested
	 */
	private PathExpressionPredicate(String when, UncheckedPredicate is, String than) {
		WHEN = compile(when);
		IS = requireNonNull(is);
		THAN = (is.requireTwoValues() ? compile(than) : null);
		DEFAULT_VALUE = false;
		PUT_ROOT_OBJ1 = getArgumentKeys(WHEN).anyMatch(ROOT_OBJ_KEY_PREDICATE);
		PUT_ROOT_OBJ2 = getArgumentKeys(THAN).anyMatch(ROOT_OBJ_KEY_PREDICATE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathExpressionPredicate) {
			PathExpressionPredicate predicate = (PathExpressionPredicate) obj;
			return Objects.equals(WHEN, predicate.WHEN)
					&& Objects.equals(IS, predicate.IS)
					&& Objects.equals(THAN, predicate.THAN)
					&& Objects.equals(DEFAULT_VALUE, predicate.DEFAULT_VALUE);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(WHEN, IS, THAN, DEFAULT_VALUE);
	}

	/**
	 * Returns true if all argument keys of the "when" and "than" path expression is equal to the {@linkplain #ROOT_OBJ_KEY root obj key}.
	 *
	 * @return true if all argument keys of the "when" and "than" path expression is equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * @see #needArguments()
	 */
	public boolean hasOnlyRootObjKey() {
		return Stream.concat(getArgumentKeys(WHEN), getArgumentKeys(THAN)).allMatch(ROOT_OBJ_KEY_PREDICATE);
	}

	/**
	 * Returns true if the "when" or "than" path expression need arguments.
	 *
	 * @return true if need arguments
	 * @see #hasOnlyRootObjKey()
	 */
	public boolean needArguments() {
		return (((WHEN != null) && WHEN.needArguments()) || ((THAN != null) && THAN.needArguments()));
	}

	/**
	 * Gets the "when" and "than" expression values using the rootObj then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj the object that will be used to get the "when" and "than" expression values
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object, InstanceFactory)
	 * @see #test(Object, Map)
	 * @see #test(Object, Map, InstanceFactory)
	 */
	public boolean test(Object rootObj) {
		return test(rootObj, rootObj, null, null);
	}

	/**
	 * Gets the "when" and "than" expression values using the rootObj then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj the object that will be used to get the "when" and "than" expression values
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object)
	 * @see #test(Object, Map)
	 * @see #test(Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public boolean test(Object rootObj, InstanceFactory instanceFactory) {
		return test(rootObj, rootObj, null, instanceFactory);
	}

	/**
	 * Gets the "when" and "than" expression values using the rootObj then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * and the given argument map is null then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj the object that will be used to get the "when" and "than" expression values
	 * @param args the arguments that will be used to access this node (optional)
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object)
	 * @see #test(Object, InstanceFactory)
	 * @see #test(Object, Map, InstanceFactory)
	 */
	public boolean test(Object rootObj, Map<String, Object> args) {
		return test(rootObj, rootObj, args, null);
	}

	/**
	 * Gets the "when" and "than" expression values using the rootObj then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * and the given argument map is null then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj the object that will be used to get the "when" and "than" expression values
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object)
	 * @see #test(Object, InstanceFactory)
	 * @see #test(Object, Map)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public boolean test(Object rootObj, Map<String, Object> args, InstanceFactory instanceFactory) {
		return test(rootObj, rootObj, args, instanceFactory);
	}

	/**
	 * Gets the "when" expression value using the rootObj1 and gets the "than" expression value using the rootObj2 then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj1 the object that will be used to get the "when" expression value
	 * @param rootObj2 the object that will be used to get the "than" expression value
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object, Object, InstanceFactory)
	 * @see #test(Object, Object, Map)
	 * @see #test(Object, Object, Map, InstanceFactory)
	 */
	public boolean test(Object rootObj1, Object rootObj2) {
		return test(rootObj1, rootObj2, null, null);
	}

	/**
	 * Gets the "when" expression value using the rootObj1 and gets the "than" expression value using the rootObj2 then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj1 the object that will be used to get the "when" expression value
	 * @param rootObj2 the object that will be used to get the "than" expression value
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object, Object)
	 * @see #test(Object, Object, Map)
	 * @see #test(Object, Object, Map, InstanceFactory)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public boolean test(Object rootObj1, Object rootObj2, InstanceFactory instanceFactory) {
		return test(rootObj1, rootObj2, null, instanceFactory);
	}

	/**
	 * Gets the "when" expression value using the rootObj1 and gets the "than" expression value using the rootObj2 then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * and the given argument map is null then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj1 the object that will be used to get the "when" expression value
	 * @param rootObj2 the object that will be used to get the "than" expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object, Object)
	 * @see #test(Object, Object, InstanceFactory)
	 * @see #test(Object, Object, Map, InstanceFactory)
	 */
	public boolean test(Object rootObj1, Object rootObj2, Map<String, Object> args) {
		return test(rootObj1, rootObj2, args, null);
	}

	/**
	 * Gets the "when" expression value using the rootObj1 and gets the "than" expression value using the rootObj2 then tests this two values using the "is" predicate.
	 * <p>
	 * Note
	 * <ul>
	 * <li>If the defined "when" or "than" path expression has some node with a argument key equal to the {@linkplain #ROOT_OBJ_KEY root obj key}
	 * and the given argument map is null then the creation of a argument map with the root obj is enabled automatically.</li>
	 * <li>If the defined "when" or "than" path expression {@linkplain PathExpression#isStaticExpression() is static} the respective root obj will be ignored.</li>
	 * <li>If the given root object is a instance of {@linkplain Class} then will be used the {@linkplain PathExpression#getStaticExpressionValue(Class, Map, InstanceFactory)} method.</li>
	 * </ul>
	 * </p>
	 *
	 * @param rootObj1 the object that will be used to get the "when" expression value
	 * @param rootObj2 the object that will be used to get the "than" expression value
	 * @param args the arguments that will be used to access this node (optional)
	 * @param instanceFactory the factory the will be supplies instances if necessary (optional)
	 * @return true if the internal predicate returns true for the values obtained of the "when" and "than" path expression.
	 * @see #test(Object, Object)
	 * @see #test(Object, Object, InstanceFactory)
	 * @see #test(Object, Object, Map)
	 * @see InstanceFactory#DEFAULT_FACTORY
	 */
	public boolean test(Object rootObj1, Object rootObj2, Map<String, Object> args, InstanceFactory instanceFactory) {
		if (WHEN == null) {
			return DEFAULT_VALUE;
		}
		Map<String, Object> tempArgs = ((args == null) ? getArgs(args, rootObj1, rootObj2) : args);
		Object x = getExpressionValue(WHEN, rootObj1, tempArgs, instanceFactory);
		if (IS.requireTwoValues()) {
			tempArgs = ((args == null) ? getArgs(tempArgs, rootObj1, rootObj2) : args);
			Object y = getExpressionValue(THAN, rootObj2, tempArgs, instanceFactory);
			return IS.test(x, y);
		}
		return IS.test(x);
	}

	@Override
	public String toString() {
		if (WHEN == null) {
			return "PathExpressionPredicate [DEFAULT_VALUE=" + DEFAULT_VALUE + "]";
		}
		return "PathExpressionPredicate [WHEN=" + WHEN + ", IS=" + IS + ", THAN=" + THAN + "]";
	}

	/**
	 * Returns a map with the root obj, if necessary, otherwise return the given map.
	 *
	 * @param map the desired argument map.
	 * @param rootObj1 the object that will be used to get the "when" expression value
	 * @param rootObj2 the object that will be used to get the "than" expression value
	 * @return a map
	 */
	private Map<String, Object> getArgs(Map<String, Object> map, Object rootObj1, Object rootObj2) {
		if (PUT_ROOT_OBJ1 || PUT_ROOT_OBJ2) {
			if (map == null) {
				if (PUT_ROOT_OBJ1 && PUT_ROOT_OBJ2 && (rootObj1 != rootObj2)) {
					map = new HashMap<>(1);
					map.put(ROOT_OBJ_KEY, rootObj1);
					return map;
				}
				return singletonMap(ROOT_OBJ_KEY, rootObj1);
			} else if (map instanceof HashMap) {
				map.put(ROOT_OBJ_KEY, rootObj1);
			} else if (rootObj1 != rootObj2) {
				return singletonMap(ROOT_OBJ_KEY, rootObj2);
			}
		}
		return map;
	}

	/**
	 * Represents a unchecked predicate (boolean-valued function) of one or two arguments.
	 * Note: This predicate must be responsible for cast the arguments
	 *
	 * @author Thomás Sousa Silva (ThomasSousa96)
	 * @see PathExpressionPredicate
	 */
	public static interface UncheckedPredicate {

		/**
		 * Returns true if this instance has defined tests.
		 *
		 * @return true if this instance has defined tests
		 */
		public boolean isDefined();

		/**
		 * Returns true if this instance require two arguments for executes the test.
		 *
		 * @return true if this instance require two arguments for executes the test
		 */
		public boolean requireTwoValues();

		/**
		 * Evaluates this predicate on the given argument.
		 *
		 * @param x the desired argument
		 * @return true if the given argument matches the predicate
		 * @throws UnsupportedOperationException if this predicate require two values
		 */
		public default <T> boolean test(Object x) {
			throw new UnsupportedOperationException(format("The [%s] predicate require two values.", this));
		}

		/**
		 * Evaluates this predicate on the gives arguments.
		 *
		 * @param x the desired argument
		 * @param y the desired argument
		 * @return true if the gives arguments matches the predicate
		 * @throws UnsupportedOperationException if this predicate require only one value
		 */
		public default <T> boolean test(Object x, Object y) {
			throw new UnsupportedOperationException(format("The [%s] predicate require only one value.", this));
		}

	}

}

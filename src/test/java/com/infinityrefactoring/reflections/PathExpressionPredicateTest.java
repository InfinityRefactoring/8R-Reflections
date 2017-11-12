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

import static com.infinityrefactoring.reflections.PathExpression.toStaticExpression;
import static com.infinityrefactoring.reflections.PathExpressionPredicate.DENY_ALL;
import static com.infinityrefactoring.reflections.PathExpressionPredicate.ACCEPT_ALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Objects;

import org.junit.Test;

/**
 * Class used to test the {@linkplain PathExpressionPredicate} class.
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
@SuppressWarnings("javadoc")
public class PathExpressionPredicateTest {

	@SuppressWarnings("unused")
	private static String getLowercaseName(Person person) {
		if (person != null) {
			String name = person.getName();
			return ((name == null) ? null : name.toLowerCase());
		}
		return null;
	}

	@Test
	public void testEmpty() throws Exception {
		PathExpressionPredicate predicate1 = PathExpressionPredicate.of("", UncheckedPredicate.UNDEFINED, "", null);
		assertNull(predicate1);

		PathExpressionPredicate predicate2 = PathExpressionPredicate.of("", UncheckedPredicate.UNDEFINED, "", ACCEPT_ALL);
		assertSame(ACCEPT_ALL, predicate2);

		PathExpressionPredicate predicate3 = PathExpressionPredicate.of("", UncheckedPredicate.UNDEFINED, "", DENY_ALL);
		assertSame(DENY_ALL, predicate3);
	}

	@Test
	public void testInvalidPredicate() throws Exception {
		try {
			PathExpressionPredicate.of("a", UncheckedPredicate.UNDEFINED, "", null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("The [is] predicate cannot be undefined if the [when] path expression is specified.", ex.getMessage());
		}
		try {
			PathExpressionPredicate.of("", UncheckedPredicate.EQUAL, "a", null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("The [is] predicate cannot be defined if the [when] path expression is not specified.", ex.getMessage());
		}
		try {
			PathExpressionPredicate.of("", UncheckedPredicate.EQUAL, "", null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("The [is] predicate cannot be defined if the [when] path expression is not specified.", ex.getMessage());
		}
		try {
			PathExpressionPredicate.of("a", UncheckedPredicate.EQUAL, "", null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("The [than] path expression must be specified because the [EQUAL] predicate require two values.", ex.getMessage());
		}
		try {
			PathExpressionPredicate.of("a", UncheckedPredicate.NULL, "aa", null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("The [than] path expression cannot be specified because the [NULL] predicate require only one value.", ex.getMessage());
		}
	}

	@Test
	public void testPutRootObjAutomatically() throws Exception {
		String when = "name";
		UncheckedPredicate is = UncheckedPredicate.EQUAL;
		String than = toStaticExpression(PathExpressionPredicateTest.class, "getLowercaseName(rootObj)");

		PathExpressionPredicate predicate = PathExpressionPredicate.of(when, is, than, ACCEPT_ALL);
		assertNotSame(ACCEPT_ALL, predicate);

		Person person = new Person();
		assertTrue(predicate.test(null));

		assertTrue(predicate.test(person));

		person.setName("Thomás");
		assertFalse(predicate.test(person));
		person.setName("thomás");
		assertTrue(predicate.test(person));

		Person person2 = new Person();
		person2.setName("thomás");
		assertTrue(predicate.test(person, person2));
	}

	@Test
	public void testValidPredicate() throws Exception {
		PathExpressionPredicate predicate1 = PathExpressionPredicate.of("name", UncheckedPredicate.NULL, "", ACCEPT_ALL);
		assertNotSame(ACCEPT_ALL, predicate1);
		assertTrue(predicate1.test(null));

		Person person = new Person();
		assertTrue(predicate1.test(person));
		person.setName("Thomás");
		assertFalse(predicate1.test(person));

		PathExpressionPredicate predicate2 = PathExpressionPredicate.of("name", UncheckedPredicate.EQUAL, toStaticExpression(Person.class, "NAME"), ACCEPT_ALL);
		assertNotSame(ACCEPT_ALL, predicate2);
		assertFalse(predicate2.test(null));

		assertFalse(predicate2.test(person));

		person.setName(Person.NAME);
		assertTrue(predicate2.test(person));
	}

	public static enum UncheckedPredicate implements PathExpressionPredicate.UncheckedPredicate {

		NULL {

			@Override
			public boolean test(Object x) {
				return (x == null);
			}

		},
		EQUAL {

			@Override
			public boolean test(Object x, Object y) {
				return Objects.equals(x, y);
			}

		},
		UNDEFINED;

		@Override
		public boolean isDefined() {
			return (this != UNDEFINED);
		}

		@Override
		public boolean requireTwoValues() {
			return (this == EQUAL);
		}

	}

}

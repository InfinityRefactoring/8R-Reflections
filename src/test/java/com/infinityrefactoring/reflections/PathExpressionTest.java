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
import static com.infinityrefactoring.reflections.PathExpression.compile;
import static com.infinityrefactoring.reflections.PathExpression.toNonStaticExpression;
import static com.infinityrefactoring.reflections.PathExpression.toStaticExpression;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/**
 * Class used to test the {@linkplain PathExpression} class.
 *
 * @author Thomás Sousa Silva
 */
@SuppressWarnings("javadoc")
public class PathExpressionTest {

	@Test
	public void testCache() {
		String pathExpressionText = "addresses[0].state";
		assertSame(compile(pathExpressionText), compile(pathExpressionText));
		assertSame(compile(pathExpressionText).getFirstNode(), compile("addresses[0]").getFirstNode());
		assertSame(compile(pathExpressionText).toStaticExpression(Person.class), compile(Person.class, pathExpressionText));
		assertSame(compile(pathExpressionText), compile(pathExpressionText).toNonStaticExpression());
	}

	@Test
	public void testConcat() {
		PathExpression pathExpression1 = compile("addresses[0].state.foo");
		PathExpression pathExpression2 = pathExpression1.moveBackward(1);
		assertSame(pathExpression1, pathExpression2.concat(compile("foo")));

		PathExpression pathExpression3 = compile(Person.class, "addresses[0].state.isEmpty()");
		PathExpression pathExpression4 = pathExpression3.moveBackward(1);
		PathExpression pathExpression5 = pathExpression4.concat(compile(String.class, "isEmpty()"));
		assertSame(pathExpression3, pathExpression5);
	}

	@Test
	public void testGetExpressionValueInStaticPath() {
		PathExpression pathExpression = compile(Person.class, "ADDRESS.state");
		try {
			pathExpression.getExpressionValue(null);
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
		try {
			pathExpression.getExpressionValue(null, Collections.emptyMap());
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
		try {
			pathExpression.getExpressionValue(null, InstanceFactory.DEFAULT_FACTORY);
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
		try {
			pathExpression.getExpressionValue(null, null, null);
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
	}

	@Test
	public void testGetStaticExpressionValue() throws Exception {
		PathExpression pathExpression = compile("NAME.equals(x)");

		Map<String, Object> map = singletonMap("x", Person.NAME);
		Boolean equals = pathExpression.getStaticExpressionValue(Person.class, map);
		assertTrue(equals);

		PathExpression pathExpression2 = compile("NULL.equals(x)");
		Boolean equals2 = pathExpression2.getStaticExpressionValue(Person.class, map);
		assertNull(equals2);

		Boolean equals3 = pathExpression2.getStaticExpressionValue(Person.class, map, InstanceFactory.DEFAULT_FACTORY);
		assertFalse(equals3);

		PathExpression pathExpression3 = pathExpression2.moveBackward(1);
		pathExpression3.setStaticExpressionValue(Person.class, Person.NAME);
		Boolean equals4 = pathExpression2.getStaticExpressionValue(Person.class, map);
		assertTrue(equals4);
	}

	@Test
	public void testLastMember() throws Exception {
		Field expectedMember = wrap(Address.class).getField("country");
		PathExpression pathExpression = compile(Person.class, "addresses.country");
		Member lastMember = pathExpression.getLastMember();
		assertEquals(expectedMember, lastMember);
	}

	@Test
	public void testRawArrayNode() throws Exception {
		PathExpression pathExpression = compile("[0][2]");
		Integer[][] array = new Integer[][]{new Integer[]{1, 2, 3, 4, 5}};

		Integer value = pathExpression.getExpressionValue(array);
		assertSame(array[0][2], value);
		Integer newValue = 77;

		pathExpression.setExpressionValue(array, newValue);
		assertSame(newValue, array[0][2]);
		assertSame(newValue, pathExpression.getExpressionValue(array));
	}

	@Test
	public void testSetAllExpressions() {
		String state = "PE";
		Person person = Reflections.newInstance(Person.class);
		PathExpression pathExpression = compile("addresses[0].state");
		InstanceFactory.DEFAULT_FACTORY.put(Address[].class, m -> new Address[5]);
		pathExpression.setExpressionValue(person, state);
		String state2 = pathExpression.getExpressionValue(person);
		assertSame(state, state2);
	}

	@Test
	public void testSetExpressionValueInStaticPath() {
		PathExpression pathExpression = compile(Person.class, "ADDRESS.state");
		try {
			pathExpression.setExpressionValue(null, null);
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
		try {
			pathExpression.setExpressionValue(null, null, Collections.emptyMap());
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
		try {
			pathExpression.setExpressionValue(null, null, InstanceFactory.DEFAULT_FACTORY);
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
		try {
			pathExpression.setExpressionValue(null, null, null, null);
			fail();
		} catch (UnsupportedOperationException ex) {
			assertEquals("The static path expression not accept rootObj.", ex.getMessage());
		}
	}

	@Test
	public void testSetStaticExpressionValue() throws Exception {
		PathExpression pathExpression = compile("NAME");
		String name = pathExpression.getStaticExpressionValue(Person.class);
		assertSame(Person.NAME, name);

		String newName = "bar";
		pathExpression.setStaticExpressionValue(Person.class, newName, null, null);

		assertSame(newName, Person.NAME);
	}

	@Test
	public void testStaticPath() {
		String pathExpressionText = "addresses.state";
		String staticExpression1 = toStaticExpression(Person.class, pathExpressionText);
		assertEquals("class(com.infinityrefactoring.reflections.Person)addresses.state", staticExpression1);
		String staticExpression2 = toStaticExpression(Address.class, staticExpression1);
		assertEquals("class(com.infinityrefactoring.reflections.Address)addresses.state", staticExpression2);
		PathExpression pathExpression = compile(Person.class, pathExpressionText);

		assertTrue(pathExpression.isStaticExpression());
		assertFalse(compile(pathExpressionText).isStaticExpression());
		assertTrue(compile(pathExpressionText).toStaticExpression(Person.class).isStaticExpression());

		try {
			pathExpression.toStaticExpression(PathExpressionTest.class);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("Invalid static path expression.", ex.getMessage());
			Throwable cause = ex.getCause();
			assertNotNull(cause);
			assertEquals(IllegalArgumentException.class, cause.getClass());
			assertEquals("Field not found: com.infinityrefactoring.reflections.PathExpressionTest.addresses", cause.getMessage());
		}

		PathExpression pathExpression2 = compile(Person.class, "ADDRESS.state");

		assertSame(Person.ADDRESS.getState(), pathExpression2.getStaticExpressionValue());
		String newState = "PE";
		pathExpression2.setStaticExpressionValue(newState);
		assertSame(newState, Person.ADDRESS.getState());
		assertSame(newState, pathExpression2.getStaticExpressionValue());
	}

	@Test
	public void testSubPath() {
		PathExpression pathExpression = compile("addresses[0].state.isEmpty()");
		assertSame(pathExpression, pathExpression.subPath(0, pathExpression.getNodesAmount()));
		assertSame(pathExpression, pathExpression.moveBackward(0));
		assertSame(pathExpression, pathExpression.moveForward(0));
		PathExpression subPath = pathExpression.moveBackward(1);
		assertEquals("addresses[0].state", subPath.getPathExpression());
		assertEquals("addresses[0]", subPath.moveBackward(1).getPathExpression());
		assertEquals("state.isEmpty()", pathExpression.subPath(1, pathExpression.getNodesAmount()).getPathExpression());
	}

	@Test
	public void testToNonStaticPath() {
		String pathExpressionText = "addresses.state";
		assertEquals(pathExpressionText, toNonStaticExpression(pathExpressionText));
		assertEquals(pathExpressionText, toNonStaticExpression(toStaticExpression(Person.class, pathExpressionText)));

		PathExpression pathExpression = compile(Person.class, pathExpressionText);
		assertSame(compile(pathExpressionText), pathExpression.toNonStaticExpression());
	}

}

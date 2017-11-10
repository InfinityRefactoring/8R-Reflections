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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
		String pathExpression = "addresses[0].state";
		assertSame(PathExpression.compile(pathExpression), PathExpression.compile(pathExpression));

		assertSame(PathExpression.compile(pathExpression).getFirstNode(), PathExpression.compile("addresses[0]").getFirstNode());
	}

	@Test
	public void testGetStaticExpressionValue() throws Exception {
		PathExpression pathExpression = PathExpression.compile("NAME.equals(x)");

		Map<String, Object> map = Collections.singletonMap("x", Person.NAME);

		Boolean equals = pathExpression.getStaticExpressionValue(Person.class, map);
		assertTrue(equals);

		PathExpression pathExpression2 = PathExpression.compile("NULL.equals(x)");

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
	public void testSetAllExpressions() {
		String state = "PE";
		Person person = Reflections.newInstance(Person.class);
		PathExpression pathExpression = PathExpression.compile("addresses[0].state");
		InstanceFactory.DEFAULT_FACTORY.put(Address[].class, m -> new Address[5]);
		pathExpression.setExpressionValue(person, state);
		String state2 = pathExpression.getExpressionValue(person);
		assertSame(state, state2);
	}

	@Test
	public void testSetStaticExpressionValue() throws Exception {
		PathExpression pathExpression = PathExpression.compile("NAME");
		String name = pathExpression.getStaticExpressionValue(Person.class);
		assertSame(Person.NAME, name);

		String newName = "bar";
		pathExpression.setStaticExpressionValue(Person.class, newName, null, null);

		assertSame(newName, Person.NAME);
	}

	@Test
	public void testSubPath() {
		PathExpression pathExpression = PathExpression.compile("addresses[0].state.isEmpty()");

		assertSame(pathExpression, pathExpression.subPath(0, pathExpression.getNodesAmount()));
		assertSame(pathExpression, pathExpression.moveBackward(0));
		assertSame(pathExpression, pathExpression.moveForward(0));
		PathExpression subPath = pathExpression.moveBackward(1);
		assertEquals("addresses[0].state", subPath.getPathExpression());
		assertEquals("addresses[0]", subPath.moveBackward(1).getPathExpression());
		assertEquals("state.isEmpty()", pathExpression.subPath(1, pathExpression.getNodesAmount()).getPathExpression());
	}

	@Test
	public void testRawArrayNode() throws Exception {
		PathExpression pathExpression = PathExpression.compile("[0][2]");
		Integer[][] array = new Integer[][]{new Integer[]{1, 2, 3, 4, 5}};

		Integer value = pathExpression.getExpressionValue(array);

		assertSame(array[0][2], value);

		Integer newValue = 77;

		pathExpression.setExpressionValue(array, newValue);

		assertSame(newValue, array[0][2]);
		assertSame(newValue, pathExpression.getExpressionValue(array));
	}

}

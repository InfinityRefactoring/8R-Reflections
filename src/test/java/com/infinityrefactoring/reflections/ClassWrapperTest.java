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

import static com.infinityrefactoring.reflections.ClassWrapper.getGenericReturnType;
import static com.infinityrefactoring.reflections.ClassWrapper.getGenericType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Class used to test the {@linkplain ClassWrapper} class.
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
@SuppressWarnings("javadoc")
public class ClassWrapperTest {

	@Test
	public void testCache() {
		assertSame(ClassWrapper.wrap(Person.class), ClassWrapper.wrap(Person.class));
	}

	@Test
	public void testGetField() throws NoSuchFieldException, SecurityException {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		String fieldName = "NAME";
		Field field = classWrapper.getField(Predicates.withMemberName(fieldName));

		assertEquals(Person.class.getField(fieldName), field);
	}

	@Test
	public void testGetFields() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		Set<Field> fields = classWrapper.getFields(Predicates.acceptAll());

		List<Field> declaredFields = Arrays.asList(Person.class.getDeclaredFields());

		assertEquals(declaredFields.size(), fields.size());
		assertTrue(declaredFields.containsAll(fields));
	}

	@Test
	public void testGetFieldValue() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		Person person = classWrapper.newInstance();
		person.setName("foo");
		String name = classWrapper.getFieldValue(person, "name");
		assertSame(person.getName(), name);
	}

	@Test
	public void testGetGenericType() throws Exception {
		Field genericInterfaceField = ClassWrapper.wrap(Foo.class).getField("genericInterface");

		assertEquals(Integer.class, getGenericType(genericInterfaceField, GenericInterface.class, 0));

		ClassWrapper<Foo> classWrapper = ClassWrapper.wrap(Foo.class);
		Field subGenericInterfaceField = classWrapper.getField("subGenericInterface");
		assertEquals(Long.class, getGenericType(subGenericInterfaceField, GenericInterface.class, 0));

		assertEquals(Long.class, getGenericType(Bar.class, GenericInterface.class, 0));

		assertEquals(Boolean.class, getGenericType(Bar.class, GenericClass.class, 0));

		Method method = classWrapper.getCompatibleMethodWithTypes("getBar", SubGenericInterface.class);
		assertEquals(Long.class, getGenericReturnType(method, GenericInterface.class, 0));

		Parameter parameter = method.getParameters()[0];
		assertEquals(Long.class, getGenericType(parameter, GenericInterface.class, 0));
	}

	@Test
	public void testGetInvokeMethod() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		Person person = classWrapper.newInstance();
		String name = "foo";
		classWrapper.invokeMethod(person, "setName", name);
		assertSame(name, person.getName());
		String name2 = classWrapper.invokeMethod(person, "getName");
		assertSame(name, name2);
	}

	@Test
	public void testGetMethod() throws NoSuchMethodException, SecurityException {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		String methodName = "getName";
		Method method = classWrapper.getMethod(Predicates.withMemberName(methodName));

		assertEquals(Person.class.getMethod(methodName), method);
	}

	@Test
	public void testGetMethods() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		Set<Method> methods = classWrapper.getMethods(Predicates.acceptAll());

		Set<Method> expected = new HashSet<>();

		expected.addAll(Arrays.asList(Person.class.getDeclaredMethods()));
		expected.addAll(Arrays.asList(Person.class.getMethods()));
		expected.addAll(Arrays.asList(Object.class.getDeclaredMethods()));
		expected.addAll(Arrays.asList(Object.class.getMethods()));

		assertEquals(expected.size(), methods.size());
		assertTrue(expected.containsAll(methods));
	}

	@Test
	public void testGetStaticFieldValue() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		String foo = classWrapper.getStaticFieldValue("NAME");
		assertSame(Person.NAME, foo);
	}

	@Test
	public void testNewInstanceWithoutParameters() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);
		Person person = classWrapper.newInstance();

		assertNotNull(person);
		assertSame(person.getClass(), Person.class);
	}

	public void testNewInstanceWithParameters() {
		ClassWrapper<Person> classWrapper = ClassWrapper.wrap(Person.class);

		String name = "Thomás";
		Address[] addresses = new Address[5];

		Person person = classWrapper.newInstance(name, addresses);

		assertNotNull(person);
		assertSame(person.getClass(), Person.class);
		assertSame(name, person.getName());
		assertSame(addresses, person.getAddresses());
	}

	@SuppressWarnings("serial")
	public static class Bar extends GenericClass<Boolean> implements SubGenericInterface {

	}

	public static class Foo implements GenericInterface<String> {

		@SuppressWarnings("unused")
		private GenericInterface<Integer> genericInterface;

		@SuppressWarnings("unused")
		private SubGenericInterface subGenericInterface;

		public Bar getBar(SubGenericInterface g) {
			return null;
		}

	}

	public static abstract class GenericClass<T> {

	}

	public static interface GenericInterface<T> {

	}

	public static interface SubGenericInterface extends GenericInterface<Long>, Serializable {

	}

}

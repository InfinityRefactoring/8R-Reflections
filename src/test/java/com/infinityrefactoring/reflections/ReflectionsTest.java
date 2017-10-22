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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Class used to test the {@linkplain Reflections} class.
 *
 * @author Thomás Sousa Silva
 */
@SuppressWarnings("javadoc")
public class ReflectionsTest {

	@Test
	public void setAllExpressionsTest() {
		Person person = Reflections.newInstance(Person.class);

		String name = "Thomás Sousa Silva";
		Address[] addresses = new Address[5];
		String country = "Brazil";

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", name);
		map.put("addresses", addresses);
		map.put("addresses[0].country", country);

		Reflections.setAllExpressions(person, map);

		assertSame(name, person.getName());
		assertSame(addresses, person.getAddresses());
		assertSame(country, person.getAddresses()[0].getCountry());

		Map<String, Object> values = Reflections.getAllExpressionValues(person, map.keySet());

		assertEquals(map.size(), values.size());
		assertTrue(map.keySet().containsAll(values.keySet()));
		values.forEach((pathExpression, value) -> assertSame(map.get(pathExpression), value));
	}

}

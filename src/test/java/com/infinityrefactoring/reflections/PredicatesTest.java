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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.Test;

/**
 * Class used to test the {@linkplain Predicates} class.
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
@SuppressWarnings("javadoc")
public class PredicatesTest {

	@Test
	public void testWithAnnotationAnnotatedWith() throws Exception {
		Predicate<AnnotatedElement> predicateA = Predicates.withAnnotationAnnotatedWith(A.class);
		assertFalse(predicateA.test(Person.class));

		Predicate<AnnotatedElement> predicateB = Predicates.withAnnotationAnnotatedWith(B.class);
		assertTrue(predicateB.test(Person.class));
	}

	@Test
	public void testWithMethodSignature() {
		Predicate<Method> predicate1 = Predicates.withMethodSignature(void.class, "testWithMethodSignature");
		Method method1 = ClassWrapper.wrap(PredicatesTest.class).getCompatibleMethodWithTypes("testWithMethodSignature");
		assertTrue(predicate1.test(method1));

		Predicate<Method> predicate2 = Predicates.withMethodSignature(CharSequence.class, true, "getName");
		Method method2 = ClassWrapper.wrap(Person.class).getCompatibleMethodWithTypes("getName");
		assertTrue(predicate2.test(method2));
	}

}

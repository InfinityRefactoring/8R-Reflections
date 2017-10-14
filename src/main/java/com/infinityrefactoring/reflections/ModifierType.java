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

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.IntFunction;

/**
 * Represents a modifier type of the Java Language.
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 * @see Modifier
 */
public enum ModifierType implements IntFunction<Boolean> {

	/**
	 * @see Modifier#isPublic(int)
	 */
	PUBLIC(Modifier::isPublic),
	/**
	 * @see Modifier#isPrivate(int)
	 */
	PRIVATE(Modifier::isPrivate),
	/**
	 * @see Modifier#isProtected(int)
	 */
	PROTECTED(Modifier::isProtected),
	/**
	 * @see Modifier#isStatic(int)
	 */
	STATIC(Modifier::isStatic),
	/**
	 * @see Modifier#isFinal(int)
	 */
	FINAL(Modifier::isFinal),
	/**
	 * @see Modifier#isSynchronized(int)
	 */
	SYNCHRONIZED(Modifier::isSynchronized),
	/**
	 * @see Modifier#isVolatile(int)
	 */
	VOLATILE(Modifier::isVolatile),
	/**
	 * @see Modifier#isTransient(int)
	 */
	TRANSIENT(Modifier::isTransient),
	/**
	 * @see Modifier#isNative(int)
	 */
	NATIVE(Modifier::isNative),
	/**
	 * @see Modifier#isInterface(int)
	 */
	INTERFACE(Modifier::isInterface),
	/**
	 * @see Modifier#isAbstract(int)
	 */
	ABSTRACT(Modifier::isAbstract),

	/**
	 * @see Modifier#isStrict(int)
	 */
	STRICT(Modifier::isStrict);

	private final IntFunction<Boolean> FUNCTION;

	private ModifierType(IntFunction<Boolean> function) {
		FUNCTION = function;
	}

	/**
	 * Returns true if the given class has this modifier.
	 *
	 * @param c the desired class
	 * @return true if the given class has this modifier.
	 */
	public Boolean apply(Class<?> c) {
		return apply(c.getModifiers());
	}

	/**
	 * Returns true if the given code has this modifier.
	 *
	 * @param mod the desired code
	 * @return true if the given code has this modifier.
	 * @see Member#getModifiers()
	 * @see Class#getModifiers()
	 */
	@Override
	public Boolean apply(int mod) {
		return FUNCTION.apply(mod);
	}

	/**
	 * Returns true if the given member has this modifier.
	 *
	 * @param member the desired modifier
	 * @return true if the given member has this modifier.
	 */
	public Boolean apply(Member member) {
		return apply(member.getModifiers());
	}

}

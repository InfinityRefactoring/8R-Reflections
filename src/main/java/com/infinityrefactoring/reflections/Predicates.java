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

import static com.infinityrefactoring.reflections.ModifierType.FINAL;
import static com.infinityrefactoring.reflections.ModifierType.PRIVATE;
import static com.infinityrefactoring.reflections.ModifierType.PUBLIC;
import static com.infinityrefactoring.reflections.ModifierType.STATIC;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Utility class with static methods that returns a {@linkplain Predicate} for filter a {@linkplain Class}, {@linkplain Member}, {@linkplain Field} or {@linkplain Method}.
 *
 * @see ClassWrapper
 * @see Reflections
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class Predicates {

	/**
	 * Returns a predicate that accept anything.
	 *
	 * @return a predicate
	 */
	public static Predicate<Object> acceptAll() {
		return o -> true;
	}

	/**
	 * Returns a predicate that tests if the given class {@linkplain ModifierType#FINAL is final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isFinalClass() {
		return FINAL::apply;
	}

	/**
	 * Returns a predicate that tests if the given member {@linkplain ModifierType#FINAL is final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isFinalMember() {
		return FINAL::apply;
	}

	/**
	 * Returns a predicate that tests if the given method is a getter.
	 * A getter method {@linkplain String#startsWith(String) starts with} "get", {@linkplain #withReturn() return something} and {@linkplain #withoutParameters() have not parameters}.
	 *
	 * @return a predicate
	 * @see #isSetter()
	 * @see #isPublicGetter()
	 */
	public static Predicate<Method> isGetter() {
		return method -> (method.getName().startsWith("get") && withReturn().test(method) && withoutParameters().test(method));
	}

	/**
	 * Returns a predicate that tests if the given member {@linkplain ModifierType#FINAL is not final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isNotFinalMember() {
		return m -> !FINAL.apply(m);
	}

	/**
	 * Returns a predicate that tests if the given member is {@linkplain ModifierType#STATIC not static} and {@linkplain ModifierType#FINAL not final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isNotStaticAndNotFinalMember() {
		return m -> ((!STATIC.apply(m)) && (!FINAL.apply(m)));
	}

	/**
	 * Returns a predicate that tests if the given member {@linkplain ModifierType#STATIC is not static}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isNotStaticMember() {
		return m -> !STATIC.apply(m);
	}

	/**
	 * Returns a predicate that tests if the given class {@linkplain ModifierType#PRIVATE is private}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isPrivateClass() {
		return PRIVATE::apply;
	}

	/**
	 * Returns a predicate that tests if the given member {@linkplain ModifierType#PRIVATE is private}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isPrivateMember() {
		return PRIVATE::apply;
	}

	/**
	 * Returns a predicate that tests if the given class is {@linkplain ModifierType#PRIVATE private}, {@linkplain ModifierType#STATIC static} and {@linkplain ModifierType#FINAL final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isPrivateStaticFinalClass() {
		return c -> (PRIVATE.apply(c) && STATIC.apply(c) && FINAL.apply(c));
	}

	/**
	 * Returns a predicate that tests if the given member is {@linkplain ModifierType#PRIVATE private}, {@linkplain ModifierType#STATIC static} and {@linkplain ModifierType#FINAL final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isPrivateStaticFinalMember() {
		return member -> (PRIVATE.apply(member) && STATIC.apply(member) && FINAL.apply(member));
	}

	/**
	 * Returns a predicate that tests if the given class {@linkplain ModifierType#PUBLIC is public}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isPublicClass() {
		return PUBLIC::apply;
	}

	/**
	 * Returns a predicate that tests if the given method is a {@linkplain ModifierType#PUBLIC public} {@linkplain #isGetter() getter}.
	 *
	 * @return a predicate
	 * @see #isPublicSetter()
	 */
	public static Predicate<Method> isPublicGetter() {
		return method -> (PUBLIC.apply(method) && isGetter().test(method));
	}

	/**
	 * Returns a predicate that tests if the given method is a {@linkplain ModifierType#PUBLIC public} {@linkplain #isSetter() setter}.
	 *
	 * @return a predicate
	 * @see #isPublicGetter()
	 */
	public static Predicate<Method> isPublicSetter() {
		return method -> (PUBLIC.apply(method) && isSetter().test(method));
	}

	/**
	 * Returns a predicate that tests if the given class is {@linkplain ModifierType#PUBLIC public}, {@linkplain ModifierType#STATIC static} and {@linkplain ModifierType#FINAL final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isPublicStaticFinalClass() {
		return c -> (PUBLIC.apply(c) && STATIC.apply(c) && FINAL.apply(c));
	}

	/**
	 * Returns a predicate that tests if the given member is {@linkplain ModifierType#PUBLIC public}, {@linkplain ModifierType#STATIC static} and {@linkplain ModifierType#FINAL final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isPublicStaticFinalMember() {
		return member -> (PUBLIC.apply(member) && STATIC.apply(member) && FINAL.apply(member));
	}

	/**
	 * Returns a predicate that tests if the given method is a setter.
	 * A setter method {@linkplain String#startsWith(String) starts with} "set" and has at least one parameter.
	 *
	 * @return a predicate
	 * @see #isGetter()
	 * @see #isPublicSetter()
	 * @see Method#getParameterCount()
	 */
	public static Predicate<Method> isSetter() {
		return method -> (method.getName().startsWith("set") && (method.getParameterCount() > 0));
	}

	/**
	 * Returns a predicate that tests if the given class {@linkplain ModifierType#STATIC is static}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isStaticClass() {
		return STATIC::apply;
	}

	/**
	 * Returns a predicate that tests if the given class is {@linkplain ModifierType#STATIC static} and {@linkplain ModifierType#FINAL final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Class)
	 */
	public static Predicate<Class<?>> isStaticFinalClass() {
		return c -> (STATIC.apply(c) && FINAL.apply(c));
	}

	/**
	 * Returns a predicate that tests if the given member is {@linkplain ModifierType#STATIC static} and {@linkplain ModifierType#FINAL final}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isStaticFinalMember() {
		return member -> (STATIC.apply(member) && FINAL.apply(member));
	}

	/**
	 * Returns a predicate that tests if the given member {@linkplain ModifierType#STATIC is static}.
	 *
	 * @return a predicate
	 * @see ModifierType#apply(Member)
	 */
	public static <T extends Member> Predicate<T> isStaticMember() {
		return STATIC::apply;
	}

	/**
	 * Returns a predicate that tests if a class is a {@linkplain Class#isAssignableFrom(Class) sub type} of the given class.
	 *
	 * @param superClass the desired super class
	 * @return a predicate
	 * @throws NullPointerException if the given superClass is null
	 * @see #isSuperTypeOf(Class)
	 */
	public static Predicate<Class<?>> isSubTypeOf(Class<?> superClass) {
		requireNonNull(superClass);
		return c -> superClass.isAssignableFrom(c);
	}

	/**
	 * Returns a predicate that tests if a class is a {@linkplain Class#isAssignableFrom(Class) super type} of the given class.
	 *
	 * @param c the desired class
	 * @return a predicate
	 * @throws NullPointerException if the given class is null
	 * @see #isSubTypeOf(Class)
	 */
	public static Predicate<Class<?>> isSuperTypeOf(Class<?> c) {
		requireNonNull(c);
		return superType -> superType.isAssignableFrom(c);
	}

	/**
	 * Returns a predicate that tests if a method {@linkplain Method#getReturnType() returns} a type {@linkplain Class#equals(Object) equals} to the given class.
	 *
	 * @param c the desired class
	 * @return a predicate
	 * @throws NullPointerException if the given class is null
	 * @see #withReturn()
	 * @see #withoutReturn()
	 * @see #thatReturnsSubTypeOf(Class)
	 * @see #thatReturnsSuperTypeOf(Class)
	 */
	public static Predicate<Method> thatReturns(Class<?> c) {
		requireNonNull(c);
		return method -> method.getReturnType().equals(c);
	}

	/**
	 * Returns a predicate that tests if a method {@linkplain Method#getReturnType() returns} a type that is a {@linkplain Class#isAssignableFrom(Class) sub type} of the given class.
	 *
	 * @param superClass the desired super class
	 * @return a predicate
	 * @throws NullPointerException if the given superClass is null
	 * @see #withReturn()
	 * @see #withoutReturn()
	 * @see #thatReturns(Class)
	 * @see #thatReturnsSuperTypeOf(Class)
	 */
	public static Predicate<Method> thatReturnsSubTypeOf(Class<?> superClass) {
		requireNonNull(superClass);
		return method -> superClass.isAssignableFrom(method.getReturnType());
	}

	/**
	 * Returns a predicate that tests if a method {@linkplain Method#getReturnType() returns} a type that is a {@linkplain Class#isAssignableFrom(Class) super type} of the given class.
	 *
	 * @param c the desired class
	 * @return a predicate
	 * @throws NullPointerException if the given class is null
	 * @see #withReturn()
	 * @see #withoutReturn()
	 * @see #thatReturns(Class)
	 * @see #thatReturnsSubTypeOf(Class)
	 */
	public static Predicate<Method> thatReturnsSuperTypeOf(Class<?> c) {
		requireNonNull(c);
		return method -> method.getReturnType().isAssignableFrom(c);
	}

	/**
	 * Returns a predicate that tests if a annotated element {@linkplain AnnotatedElement#isAnnotationPresent(Class) has the given annotation class}.
	 *
	 * @param annotationClass the desired annotation class
	 * @return a predicate
	 * @throws NullPointerException if the given class is null
	 */
	public static <T extends AnnotatedElement> Predicate<T> withAnnotation(Class<? extends Annotation> annotationClass) {
		requireNonNull(annotationClass);
		return annotatedElement -> annotatedElement.isAnnotationPresent(annotationClass);
	}

	/**
	 * Returns a predicate that tests if a annotated element is annotated with the given annotation class.
	 *
	 * @param annotationClass the desired annotation class
	 * @return a predicate
	 * @throws NullPointerException if the given class is null
	 */
	public static <T extends AnnotatedElement> Predicate<T> withAnnotationAnnotatedWith(Class<? extends Annotation> annotationClass) {
		requireNonNull(annotationClass);
		return annotatedElement -> {
			Annotation[] annotations = annotatedElement.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().isAnnotationPresent(annotationClass)) {
					return true;
				}
			}
			return false;
		};
	}

	/**
	 * Returns a predicate that applies the {@linkplain Class#getModifiers() modifiers} of a given class to given predicate.
	 *
	 * @param predicate a predicate that tests the modifiers of a given class
	 * @return a predicate
	 * @see #withClassModifier(ModifierType)
	 * @see Modifier
	 * @throws NullPointerException if the given predicate is null
	 */
	public static Predicate<Class<?>> withClassModifier(IntFunction<Boolean> predicate) {
		requireNonNull(predicate);
		return c -> predicate.apply(c.getModifiers());
	}

	/**
	 * Returns a predicate that tests if a class has the given modifier.
	 *
	 * @param modifierType the desired modifier
	 * @return a predicate
	 * @see #withClassModifier(IntFunction)
	 * @throws NullPointerException if the given modifier type is null
	 */
	public static Predicate<Class<?>> withClassModifier(ModifierType modifierType) {
		requireNonNull(modifierType);
		return modifierType::apply;
	}

	/**
	 * Returns a predicate that tests if a class has the {@linkplain Class#getName() name} {@linkplain String#equals(Object) equals} to the given name.
	 *
	 * @param name the desired name
	 * @return a predicate
	 * @see #withClassPrefix(String)
	 * @see #withClassSuffix(String)
	 * @throws NullPointerException if the given name is null
	 */
	public static Predicate<Class<?>> withClassName(String name) {
		requireNonNull(name);
		return c -> c.getName().equals(name);
	}

	/**
	 * Returns a predicate that tests if a class has the {@linkplain Class#getName() name} {@linkplain String#startsWith(String) starts with} the given prefix.
	 *
	 * @param prefix the desired prefix
	 * @return a predicate
	 * @see #withClassSuffix(String)
	 * @see #withClassName(String)
	 * @throws NullPointerException if the given prefix is null
	 */
	public static Predicate<Class<?>> withClassPrefix(String prefix) {
		requireNonNull(prefix);
		return c -> c.getName().startsWith(prefix);
	}

	/**
	 * Returns a predicate that tests if a class has the {@linkplain Class#getName() name} {@linkplain String#endsWith(String) ends with} the given suffix.
	 *
	 * @param suffix the desired suffix
	 * @return a predicate
	 * @see #withClassPrefix(String)
	 * @see #withClassName(String)
	 * @throws NullPointerException if the given suffix is null
	 */
	public static Predicate<Class<?>> withClassSuffix(String suffix) {
		requireNonNull(suffix);
		return c -> c.getName().endsWith(suffix);
	}

	/**
	 * Returns a predicate that applies the {@linkplain Member#getModifiers() modifiers} of a given member to given predicate.
	 *
	 * @param predicate a predicate that tests the modifiers of a given member
	 * @return a predicate
	 * @see #withMemberModifier(ModifierType)
	 * @see Modifier
	 * @throws NullPointerException if the given predicate is null
	 */
	public static <T extends Member> Predicate<T> withMemberModifier(IntFunction<Boolean> predicate) {
		requireNonNull(predicate);
		return member -> predicate.apply(member.getModifiers());
	}

	/**
	 * Returns a predicate that tests if a member has the given modifier.
	 *
	 * @param modifierType the desired modifier
	 * @return a predicate
	 * @see #withMemberModifier(IntFunction)
	 * @throws NullPointerException if the given modifier type is null
	 */
	public static <T extends Member> Predicate<T> withMemberModifier(ModifierType modifierType) {
		return withMemberModifier((IntFunction<Boolean>) modifierType);
	}

	/**
	 * Returns a predicate that tests if a member has the {@linkplain Member#getName() name} {@linkplain String#equals(Object) equals} to the given name.
	 *
	 * @param name the desired name
	 * @return a predicate
	 * @see #withMemberPrefix(String)
	 * @see #withMemberSuffix(String)
	 * @throws NullPointerException if the given name is null
	 */
	public static <T extends Member> Predicate<T> withMemberName(String name) {
		requireNonNull(name);
		return member -> member.getName().equals(name);
	}

	/**
	 * Returns a predicate that tests if a member has the {@linkplain Member#getName() name} {@linkplain String#startsWith(String) starts with} the given prefix.
	 *
	 * @param prefix the desired prefix
	 * @return a predicate
	 * @see #withMemberSuffix(String)
	 * @see #withMemberName(String)
	 * @throws NullPointerException if the given prefix is null
	 */
	public static <T extends Member> Predicate<T> withMemberPrefix(String prefix) {
		requireNonNull(prefix);
		return member -> member.getName().startsWith(prefix);
	}

	/**
	 * Returns a predicate that tests if a member has the {@linkplain Member#getName() name} {@linkplain String#endsWith(String) ends with} the given suffix.
	 *
	 * @param suffix the desired suffix
	 * @return a predicate
	 * @see #withMemberPrefix(String)
	 * @see #withMemberName(String)
	 * @throws NullPointerException if the given suffix is null
	 */
	public static <T extends Member> Predicate<T> withMemberSuffix(String suffix) {
		requireNonNull(suffix);
		return member -> member.getName().endsWith(suffix);
	}

	/**
	 * Returns a predicate that tests if a executable ({@linkplain Method} or {@linkplain Constructor}) have not parameters.
	 *
	 * @return a predicate
	 * @see Executable#getParameterCount()
	 * @see #withParameterTypes(Class...)
	 */
	public static <T extends Executable> Predicate<T> withoutParameters() {
		return executable -> (executable.getParameterCount() == 0);
	}

	/**
	 * Returns a predicate that tests if a method returns void. In other words if the {@linkplain Method#getReturnType() return type} is equals to {@code void.class} or {@code Void.class}.
	 *
	 * @return a predicate
	 * @see #withReturn()
	 * @see #thatReturns(Class)
	 * @see Void
	 */
	public static Predicate<Method> withoutReturn() {
		return method -> (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class));
	}

	/**
	 * Returns a predicate that tests if a executable (method or constructor) have the {@linkplain Executable#getParameterTypes() parameter types} {@linkplain Arrays#equals(Object[], Object[]) equals}
	 * to the specified types.
	 *
	 * @param types the types that the given executable must have
	 * @return a predicate
	 * @throws NullPointerException if the given types is null
	 * @see #withoutParameters()
	 */
	public static <T extends Executable> Predicate<T> withParameterTypes(Class<?>... types) {
		requireNonNull(types);
		return executable -> Arrays.equals(executable.getParameterTypes(), types);
	}

	/**
	 * Returns a predicate that tests if a method returns something. In other words if the {@linkplain Method#getReturnType() return type} is not equals to {@code void.class} or {@code Void.class}.
	 *
	 * @return a predicate
	 * @see #withoutReturn()
	 * @see #thatReturns(Class)
	 * @see #thatReturnsSubTypeOf(Class)
	 * @see #thatReturnsSuperTypeOf(Class)
	 */
	public static Predicate<Method> withReturn() {
		return method -> !withoutReturn().test(method);
	}

	private Predicates() {

	}

}

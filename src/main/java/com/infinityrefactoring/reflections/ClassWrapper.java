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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wraps a java class for facilitate the handling of your members, using the Java Reflection API.
 *
 * @param <T> the wrapped class
 * @see Reflections
 * @see Predicates
 * @see PathExpression
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class ClassWrapper<T> {

	private static final Map<Class<?>, ClassWrapper<?>> CLASS_WRAPPERS = new HashMap<>();
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WAPPER_CLASS;
	static {
		Map<Class<?>, Class<?>> map = new HashMap<>(8);
		map.put(byte.class, Byte.class);
		map.put(short.class, Short.class);
		map.put(int.class, Integer.class);
		map.put(long.class, Long.class);
		map.put(float.class, Float.class);
		map.put(double.class, Double.class);
		map.put(char.class, Character.class);
		map.put(boolean.class, Boolean.class);
		PRIMITIVE_TO_WAPPER_CLASS = unmodifiableMap(map);
	}

	private final Class<T> CLASS;
	private final Set<ClassWrapper<? super T>> SUPER_CLASS_WRAPPERS;
	private final Set<Constructor<?>> CONSTRUCTORS;
	private final Map<String, Field> FIELDS;
	private final Map<String, Set<Method>> METHODS;

	/**
	 * Returns true if the given executable ({@linkplain Method} or {@linkplain Constructor}) accepts the given types.
	 *
	 * @param executable the desired executable
	 * @param types the desired types
	 * @return true if the given executable accepts the given types
	 * @see #acceptValues(Executable, Object...)
	 */
	public static boolean acceptTypes(Executable executable, Class<?>... types) {
		return accept(executable.getParameterTypes(), types);
	}

	/**
	 * Returns true if the given executable ({@linkplain Method} or {@linkplain Constructor}) accepts the given values.
	 *
	 * @param executable the desired executable
	 * @param values the desired values
	 * @return true if the given executable accepts the given values
	 * @see #acceptTypes(Executable, Class...)
	 */
	public static boolean acceptValues(Executable executable, Object... values) {
		return accept(executable.getParameterTypes(), values);
	}

	/**
	 * Returns a instance that wraps the given class.
	 * Note: If this class already was wrapped then the cached instance that will be returned.
	 *
	 * @param c the class that will be wrapped.
	 * @return the wrapper.
	 * @throws IllegalArgumentException if the class is null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ClassWrapper<T> getClassWrapper(Class<T> c) {
		if (c == null) {
			throw new IllegalArgumentException("The class cannot be null.");
		}
		return (ClassWrapper<T>) CLASS_WRAPPERS.computeIfAbsent(c, ClassWrapper::new);
	}

	/**
	 * Returns a instance that wraps the class of the given object.
	 * Note: If this class already was wrapped then the cached instance that will be returned.
	 *
	 * @param obj the object that will be used to get the class that will be wrapped
	 * @return the wrapper.
	 * @throws IllegalArgumentException if the object is null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ClassWrapper<T> getClassWrapper(T obj) {
		if (obj == null) {
			throw new IllegalArgumentException("The obj cannot be null.");
		}
		return (ClassWrapper<T>) getClassWrapper(obj.getClass());
	}

	/**
	 * Returns the field value.
	 *
	 * @param obj the instance that will have the field value extracted
	 * @param field the desired field
	 * @return the field value
	 * @see #getStaticFieldValue(Field)
	 * @see #setFieldValue(Object, Field, Object)
	 */
	@SuppressWarnings("unchecked")
	public static <R> R getFieldValue(Object obj, Field field) {
		try {
			return (R) field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Returns the field value.
	 *
	 * @param field the desired field
	 * @return the field value
	 * @see #getFieldValue(Object, Field)
	 * @see #setStaticFieldValue(Field, Object)
	 */
	public static <R> R getStaticFieldValue(Field field) {
		return getFieldValue(null, field);
	}

	/**
	 * Returns an array with the type of each object of the given array.
	 *
	 * @param args the desired values
	 * @return the array
	 */
	public static Class<?>[] getTypes(Object... args) {
		if (args.length == 0) {
			return EMPTY_CLASS_ARRAY;
		}
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			types[i] = (args[i] == null) ? null : args[i].getClass();
		}
		return types;
	}

	/**
	 * Returns the class that wraps the given primitive class.
	 *
	 * @param c the primitive class
	 * @return the class.
	 */
	public static Class<?> getWrapperClassOfPrimitiveClass(Class<?> c) {
		if (c == null) {
			return null;
		} else if (!c.isPrimitive()) {
			throw new IllegalArgumentException(format("The class %s is not a primitive type.", c));
		}
		return PRIMITIVE_TO_WAPPER_CLASS.get(c);
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 *
	 * @param obj the instance that will have the given method invoked
	 * @param method the method that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object obj, Method method, Object... args) {
		try {
			return (T) method.invoke(obj, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Invokes the given method with the specified parameters.
	 *
	 * @param method the method that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 */
	public static <R> R invokeStaticMethod(Method method, Object... args) {
		return invokeMethod(null, method, args);
	}

	/**
	 * Returns true if the given object is an instance of the given class.
	 * Note: This method returns true if the object {@linkplain Class#isInstance(Object) is an instance} of the class or if the class
	 * {@linkplain Class#isPrimitive() is primitive} and the value is an object of the associated to the primitive class or if the object is null and
	 * the class is not primitive.
	 *
	 * @param c the desired class
	 * @param obj the desired object
	 * @return true if the given object is an instance of the given class.
	 */
	public static boolean isInstance(Class<?> c, Object obj) {
		return (c.isInstance(obj) || (c.isPrimitive() && PRIMITIVE_TO_WAPPER_CLASS.get(c).isInstance(obj)) || ((obj == null) && (!c.isPrimitive())));
	}

	/**
	 * Creates a new instance of the given class, using the default constructor.
	 *
	 * @param c the desired class
	 * @return the instance
	 */
	public static <T> T newInstance(Class<T> c) {
		try {
			return c.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Creates a new instance of the given class, using the given constructor with the specified arguments.
	 *
	 * @param constructor the constructor that will be used to creates the instance
	 * @param args the arguments used that will be used to creates the instance
	 * @return the instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Constructor<?> constructor, Object... args) {
		try {
			return (T) constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Sets the field value.
	 *
	 * @param obj the instance that will have the field value setted.
	 * @param field the desired field
	 * @param newValue the new field value
	 * @see #setStaticFieldValue(Field, Object)
	 * @see #getFieldValue(Object, Field)
	 */
	public static void setFieldValue(Object obj, Field field, Object newValue) {
		try {
			field.set(obj, newValue);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Sets the field value.
	 *
	 * @param field the desired field
	 * @param newValue the new field value
	 * @see #setFieldValue(Object, Field, Object)
	 * @see #getStaticFieldValue(Field)
	 */
	public static void setStaticFieldValue(Field field, Object newValue) {
		setFieldValue(null, field, newValue);
	}

	/**
	 * Returns true if the given method accepts the given types or values.
	 *
	 * @param parameterTypes the desired parameter types
	 * @param typesOrValues the types (Class<?>[]) or values (Object[]) that desire check if matches with the given parameter types
	 * @return true if the given typesOrValues matches with the given parameter types
	 */
	private static boolean accept(Class<?>[] parameterTypes, Object typesOrValues) {
		if (typesOrValues instanceof Class<?>[]) {
			return Arrays.equals(parameterTypes, (Class<?>[]) typesOrValues);
		}
		Object[] args = (Object[]) typesOrValues;
		if (parameterTypes.length == args.length) {
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!isInstance(parameterTypes[i], args[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns an unmodifiable map (Map<FieldName, Field>) with the declared fields of the given class.
	 *
	 * @param c the desired class
	 * @return the map
	 */
	private static Map<String, Field> getFields(Class<?> c) {
		Field[] declaredFields = c.getDeclaredFields();
		Map<String, Field> map = new HashMap<>();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			map.put(field.getName(), field);
		}
		return unmodifiableMap(map);
	}

	/**
	 * Returns an unmodifiable map (Map<MethodName, Set<Method>>) with the declared methods of the given class.
	 *
	 * @param c the desired class
	 * @return the map
	 */
	private static Map<String, Set<Method>> getMethods(Class<?> c) {
		Map<String, Set<Method>> map = new HashMap<>();
		Method[] declaredMethods = c.getDeclaredMethods();
		for (Method method : declaredMethods) {
			method.setAccessible(true);
			map.computeIfAbsent(method.getName(), k -> new HashSet<>(5)).add(method);
		}
		map.entrySet().forEach(entry -> entry.setValue(unmodifiableSet(entry.getValue())));
		return unmodifiableMap(map);
	}

	/**
	 * Returns a set with the superclass wrappers of the given class.
	 * Note: This set includes the {@linkplain Class#getSuperclass()} and the {@linkplain Class#getInterfaces()}.
	 *
	 * @param c the desired class
	 * @return the set
	 */
	@SuppressWarnings("unchecked")
	private static <T> Set<ClassWrapper<? super T>> getSuperClassWrappers(Class<T> c) {
		if (c == null) {
			return emptySet();
		}
		Class<?> superclass = c.getSuperclass();
		Class<?>[] interfaces = c.getInterfaces();
		int total = ((superclass == null) ? 0 : 1);
		total += interfaces.length;

		if (total == 0) {
			return emptySet();
		}

		Set<ClassWrapper<? super T>> set = new HashSet<>(total);
		if (superclass != null) {
			set.add((ClassWrapper<? super T>) getClassWrapper(superclass));
		}
		for (Class<?> i : interfaces) {
			set.add((ClassWrapper<? super T>) getClassWrapper(i));
		}
		return set;
	}

	/**
	 * Returns a string representation of the given types.
	 *
	 * @param types the desired types
	 * @return the representation
	 */
	private static String getTypesAsString(Class<?>[] types) {
		StringBuilder builder = new StringBuilder("(");
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append((types[i] == null) ? "null" : types[i].getName());
		}
		return builder.append(")").toString();
	}

	/**
	 * Returns a string representation of the given types (Class<?>[]) or values (Object[]).
	 *
	 * @param types the desired types or values
	 * @return the representation
	 */
	private static String getTypesAsString(Object typesOrValues) {
		Class<?>[] types = ((typesOrValues instanceof Class<?>[]) ? (Class<?>[]) typesOrValues : getTypes((Object[]) typesOrValues));
		return getTypesAsString(types);
	}

	/**
	 * Checks is the predicates is not null.
	 *
	 * @param consumer the desired consumer
	 * @param predicate the desired predicate
	 */
	private static void validate(Consumer<?> consumer, Predicate<?> predicate) {
		if (predicate == null) {
			throw new IllegalArgumentException("The predicate is required.");
		} else if (consumer == null) {
			throw new IllegalArgumentException("The consumer is required.");
		}
	}

	/**
	 * Constructs a new instance of ClassWrapper.
	 *
	 * @param c the class that will be wrapped by this instance.
	 */
	private ClassWrapper(Class<T> c) {
		CLASS = Objects.requireNonNull(c);
		SUPER_CLASS_WRAPPERS = getSuperClassWrappers(CLASS);
		CONSTRUCTORS = unmodifiableSet(new HashSet<>(asList(CLASS.getConstructors())));
		FIELDS = getFields(CLASS);
		METHODS = getMethods(c);
	}

	/**
	 * Provides the constructors that matches with the predicate to the given consumer.
	 *
	 * @param consumer the constructors consumer
	 * @param predicate the predicate that will be used to filter the constructors
	 */
	public void acceptConstructors(Consumer<? super Constructor<?>> consumer, Predicate<? super Constructor<?>> predicate) {
		validate(consumer, predicate);
		for (Constructor<?> constructor : CONSTRUCTORS) {
			if (predicate.test(constructor)) {
				consumer.accept(constructor);
			}
		}
	}

	/**
	 * Provides the fields that matches with the predicate to the given consumer.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param consumer the fields consumer
	 * @param predicate the predicate that will be used to filter the fields
	 * @see #acceptFields(Consumer, Predicate)
	 */
	public void acceptFields(boolean searchInSuperclasses, Consumer<? super Field> consumer, Predicate<? super Field> predicate) {
		validate(consumer, predicate);
		Collection<Field> fields = FIELDS.values();
		for (Field field : fields) {
			if (predicate.test(field)) {
				consumer.accept(field);
			}
		}
		if (searchInSuperclasses) {
			for (ClassWrapper<? super T> wrapper : SUPER_CLASS_WRAPPERS) {
				wrapper.acceptFields(searchInSuperclasses, consumer, predicate);
			}
		}
	}

	/**
	 * Provides the fields that matches with the predicate to the given consumer.
	 * Note: this method will search in the superclasses.
	 *
	 * @param consumer the fields consumer
	 * @param predicate the predicate that will be used to filter the fields
	 * @see #acceptFields(boolean, Consumer, Predicate)
	 */
	public void acceptFields(Consumer<? super Field> consumer, Predicate<? super Field> predicate) {
		acceptFields(true, consumer, predicate);
	}

	/**
	 * Provides the methods that matches with the predicate to the given consumer.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param consumer the methods consumer
	 * @param predicate the predicate that will be used to filter the methods
	 * @see #acceptMethods(Consumer, Predicate)
	 */
	public void acceptMethods(boolean searchInSuperclasses, Consumer<? super Method> consumer, Predicate<? super Method> predicate) {
		validate(consumer, predicate);
		Collection<Set<Method>> methods = METHODS.values();
		for (Set<Method> set : methods) {
			for (Method method : set) {
				if (predicate.test(method)) {
					consumer.accept(method);
				}
			}
		}
		if (searchInSuperclasses) {
			for (ClassWrapper<? super T> wrapper : SUPER_CLASS_WRAPPERS) {
				wrapper.acceptMethods(searchInSuperclasses, consumer, predicate);
			}
		}
	}

	/**
	 * Provides the methods that matches with the predicate to the given consumer.
	 * Note: this method will search in the superclasses.
	 *
	 * @param consumer the methods consumer
	 * @param predicate the predicate that will be used to filter the methods
	 * @see #acceptMethods(boolean, Consumer, Predicate)
	 */
	public void acceptMethods(Consumer<? super Method> consumer, Predicate<? super Method> predicate) {
		acceptMethods(true, consumer, predicate);
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof ClassWrapper) && CLASS.equals(((ClassWrapper<?>) obj).CLASS));
	}

	/**
	 * Returns the public constructor that accepts the given types.
	 *
	 * @param parameterTypes the desired types
	 * @return the constructor
	 * @throws IllegalArgumentException if not found
	 * @see #getCompatibleConstructorWithValues(Object...)
	 */
	public Constructor<?> getCompatibleConstructorWithTypes(Class<?>... parameterTypes) {
		return getConstructor(parameterTypes);
	}

	/**
	 * Returns the public constructor that accepts the given values.
	 *
	 * @param args the desired values
	 * @return the constructor
	 * @throws IllegalArgumentException if not found
	 * @see #getCompatibleConstructorWithTypes(Class...)
	 */
	public Constructor<?> getCompatibleConstructorWithValues(Object... args) {
		return getConstructor(args);
	}

	/**
	 * Returns the method with the specified name that accepts the given types.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param methodName the method name
	 * @param parameterTypes the desired types
	 * @return the method
	 * @throws IllegalArgumentException if not found
	 * @see #getCompatibleMethodWithTypes(String, Class...)
	 * @see #tryGetCompatibleMethodWithTypes(boolean, String, Class...)
	 */
	public Method getCompatibleMethodWithTypes(boolean searchInSuperclasses, String methodName, Class<?>... parameterTypes) {
		Method method = tryGetCompatibleMethodWithTypes(searchInSuperclasses, methodName, parameterTypes);
		if (method == null) {
			throw new IllegalArgumentException(format("Method not found: %s.%s%s", CLASS.getName(), methodName, getTypesAsString(parameterTypes)));
		}
		return method;
	}

	/**
	 * Returns the method with the specified name that accepts the given types.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param methodName the method name
	 * @param parameterTypes the desired types
	 * @return the method
	 * @throws IllegalArgumentException if not found
	 * @see #getCompatibleMethodWithTypes(boolean, String, Class...)
	 * @see #tryGetCompatibleMethodWithTypes(String, Class...)
	 */
	public Method getCompatibleMethodWithTypes(String methodName, Class<?>... parameterTypes) {
		return getCompatibleMethodWithTypes(true, methodName, parameterTypes);
	}

	/**
	 * Returns the method with the specified name that accepts the given values.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param methodName the method name
	 * @param args the desired values
	 * @return the method
	 * @throws IllegalArgumentException if not found
	 * @see #getCompatibleMethodWithValues(String, Object...)
	 * @see #tryGetCompatibleMethodWithValues(boolean, String, Object...)
	 */
	public Method getCompatibleMethodWithValues(boolean searchInSuperclasses, String methodName, Object... args) {
		Method method = tryGetCompatibleMethodWithValues(searchInSuperclasses, methodName, args);
		if (method == null) {
			throw new IllegalArgumentException(format("Method not found: %s.%s%s", CLASS.getName(), methodName, getTypesAsString(args)));
		}
		return method;
	}

	/**
	 * Returns the method with the specified name that accepts the given values.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param methodName the method name
	 * @param args the desired values
	 * @return the method
	 * @throws IllegalArgumentException if not found
	 * @see #getCompatibleMethodWithValues(boolean, String, Object...)
	 */
	public Method getCompatibleMethodWithValues(String methodName, Object... args) {
		return getCompatibleMethodWithValues(true, methodName, args);
	}

	/**
	 * Returns the public constructor that matches with the given predicate.
	 *
	 * @param predicate the filter
	 * @return the constructor
	 * @throws IllegalArgumentException if not found
	 * @see #tryGetConstructor(Predicate)
	 * @see #getConstructors(Predicate)
	 * @see #acceptConstructors(Consumer, Predicate)
	 */
	public Constructor<?> getConstructor(Predicate<? super Constructor<?>> predicate) {
		Constructor<?> constructor = tryGetConstructor(predicate);
		if (constructor == null) {
			throw new IllegalArgumentException(format("None constructors of the class %s, matches with the given predicate.", CLASS.getName()));
		}
		return constructor;
	}

	/**
	 * Returns all public constructors that matches with the given predicate.
	 *
	 * @param predicate the filter
	 * @return the constructors
	 * @see #getConstructor(Predicate)
	 * @see #acceptConstructors(Consumer, Predicate)
	 */
	public Set<Constructor<?>> getConstructors(Predicate<? super Constructor<?>> predicate) {
		Set<Constructor<?>> set = new HashSet<>();
		acceptConstructors(set::add, predicate);
		return set;
	}

	/**
	 * Returns the field that matches with the given predicate.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param predicate the filter
	 * @return the field
	 * @throws IllegalArgumentException if not found
	 * @see #getField(Predicate)
	 * @see #tryGetField(boolean, Predicate)
	 */
	public Field getField(boolean searchInSuperclasses, Predicate<? super Field> predicate) {
		Field field = tryGetField(searchInSuperclasses, predicate);
		if (field == null) {
			throw new IllegalArgumentException(format("None field of the class %s, mathes with the given predicate.", CLASS.getName()));
		}
		return field;

	}

	/**
	 * Returns the field with the given name.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param name the field name
	 * @return the field
	 * @throws IllegalArgumentException if not found
	 * @see #getField(String)
	 * @see #tryGetField(boolean, String)
	 */
	public Field getField(boolean searchInSuperclasses, String name) {
		Field field = tryGetField(searchInSuperclasses, name);
		if (field == null) {
			throw new IllegalArgumentException(format("Field not found: %s.%s", CLASS.getName(), name));
		}
		return field;
	}

	/**
	 * Returns the field that matches with the given predicate.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param predicate the filter
	 * @return the field
	 * @throws IllegalArgumentException if not found
	 * @see #getField(boolean, Predicate)
	 * @see #tryGetField(Predicate)
	 */
	public Field getField(Predicate<? super Field> predicate) {
		return getField(true, predicate);
	}

	/**
	 * Returns the field with the given name.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param name the field name
	 * @return the field
	 * @throws IllegalArgumentException if not found
	 * @see #getField(boolean, String)
	 * @see #tryGetField(String)
	 */
	public Field getField(String name) {
		return getField(true, name);
	}

	/**
	 * Returns the fields that matches with the given predicate.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param predicate the filter
	 * @return the fields
	 * @see #getFields(Predicate)
	 * @see #acceptFields(Consumer, Predicate)
	 * @see #acceptFields(boolean, Consumer, Predicate)
	 */
	public Set<Field> getFields(boolean searchInSuperclasses, Predicate<? super Field> predicate) {
		Set<Field> set = new HashSet<>();
		acceptFields(searchInSuperclasses, set::add, predicate);
		return set;
	}

	/**
	 * Returns the fields that matches with the given predicate.
	 * Note: this method will search in the superclasses.
	 *
	 * @param predicate the filter
	 * @return the fields
	 * @see #getFields(boolean, Predicate)
	 * @see #acceptFields(Consumer, Predicate)
	 * @see #acceptFields(boolean, Consumer, Predicate)
	 */
	public Set<Field> getFields(Predicate<? super Field> predicate) {
		return getFields(true, predicate);
	}

	/**
	 * Returns the field value.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param obj the instance that will have the field value extracted
	 * @param fieldName the field name
	 * @return the field value
	 * @throws IllegalArgumentException if not found
	 * @see #getFieldValue(Object, String)
	 * @see #getStaticFieldValue(boolean, String)
	 * @see #setFieldValue(boolean, Object, String, Object)
	 */
	public <R> R getFieldValue(boolean searchInSuperclasses, Object obj, String fieldName) {
		Field field = getField(searchInSuperclasses, fieldName);
		return getFieldValue(obj, field);
	}

	/**
	 * Returns the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param obj the instance that will have the field value extracted
	 * @param fieldName the field name
	 * @return the field value
	 * @throws IllegalArgumentException if not found
	 * @see #getFieldValue(boolean, Object, String)
	 * @see #getStaticFieldValue(String)
	 * @see #setFieldValue(Object, String, Object)
	 */
	public <R> R getFieldValue(Object obj, String fieldName) {
		return getFieldValue(true, obj, fieldName);
	}

	/**
	 * Returns the method that matches with the given predicate.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param predicate the filter
	 * @return the method
	 * @throws IllegalArgumentException if not found
	 * @see #getMethod(Predicate)
	 * @see #tryGetMethod(boolean, Predicate)
	 */
	public Method getMethod(boolean searchInSuperclasses, Predicate<? super Method> predicate) {
		Method method = tryGetMethod(searchInSuperclasses, predicate);
		if (method == null) {
			throw new IllegalArgumentException(format("None method of the class %s, match with the given predicate.", CLASS.getName()));
		}
		return method;
	}

	/**
	 * Returns the method that matches with the given predicate.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param predicate the filter
	 * @return the method
	 * @throws IllegalArgumentException if not found
	 * @see #getMethod(boolean, Predicate)
	 * @see #tryGetMethod(Predicate)
	 */
	public Method getMethod(Predicate<? super Method> predicate) {
		return getMethod(true, predicate);
	}

	/**
	 * Returns the methods that matches with the given predicate.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param predicate the filter
	 * @return the methods
	 * @see #getMethods(Predicate)
	 * @see #acceptMethods(Consumer, Predicate)
	 * @see #acceptMethods(boolean, Consumer, Predicate)
	 */
	public Set<Method> getMethods(boolean searchInSuperclasses, Predicate<? super Method> predicate) {
		Set<Method> set = new HashSet<>();
		acceptMethods(searchInSuperclasses, set::add, predicate);
		return set;
	}

	/**
	 * Returns the methods that matches with the given predicate.
	 * Note: this method will search in the superclasses.
	 *
	 * @param predicate the filter
	 * @return the fields
	 * @see #getMethods(boolean, Predicate)
	 * @see #acceptMethods(Consumer, Predicate)
	 * @see #acceptMethods(boolean, Consumer, Predicate)
	 */
	public Set<Method> getMethods(Predicate<? super Method> predicate) {
		return getMethods(true, predicate);
	}

	/**
	 * Returns the field value.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param fieldName the field name
	 * @return the field value
	 * @throws IllegalArgumentException if not found
	 * @see #getFieldValue(boolean, Object, String)
	 * @see #setStaticFieldValue(boolean, String, Object)
	 */
	public <R> R getStaticFieldValue(boolean searchInSuperclasses, String fieldName) {
		return getFieldValue(searchInSuperclasses, null, fieldName);
	}

	/**
	 * Returns the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param fieldName the field name
	 * @return the field value
	 * @throws IllegalArgumentException if not found
	 * @see #getFieldValue(Object, String)
	 * @see #setStaticFieldValue(String, Object)
	 */
	public <R> R getStaticFieldValue(String fieldName) {
		return getStaticFieldValue(true, fieldName);
	}

	/**
	 * Returns an unmodifiable set with the superclass wrappers.
	 * Note: This set includes the {@linkplain Class#getSuperclass()} and the {@linkplain Class#getInterfaces()}.
	 *
	 * @return the set
	 */
	public Set<ClassWrapper<? super T>> getSuperClassWrappers() {
		return SUPER_CLASS_WRAPPERS;
	}

	/**
	 * Returns the wrapped class by this instance.
	 *
	 * @return the wrapped class
	 */
	public Class<T> getWrappedClass() {
		return CLASS;
	}

	@Override
	public int hashCode() {
		return CLASS.hashCode();
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param obj the instance that will have the given method invoked
	 * @param methodName the method name that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 * @throws IllegalArgumentException if not found
	 * @see #invokeMethod(Object, String, Object...)
	 * @see #invokeStaticMethod(boolean, String, Object...)
	 */
	public <R> R invokeMethod(boolean searchInSuperclasses, Object obj, String methodName, Object... args) {
		Method method = getCompatibleMethodWithValues(searchInSuperclasses, methodName, args);
		return invokeMethod(obj, method, args);
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param obj the instance that will have the given method invoked
	 * @param methodName the method name that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 * @throws IllegalArgumentException if not found
	 * @see #invokeMethod(boolean, Object, String, Object...)
	 * @see #invokeStaticMethod(String, Object...)
	 */
	public <R> R invokeMethod(Object obj, String methodName, Object... args) {
		return invokeMethod(true, obj, methodName, args);
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param methodName the method name that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 * @throws IllegalArgumentException if not found
	 * @see #invokeStaticMethod(String, Object...)
	 * @see #invokeMethod(boolean, Object, String, Object...)
	 */
	public <R> R invokeStaticMethod(boolean searchInSuperclasses, String methodName, Object... args) {
		return invokeMethod(searchInSuperclasses, null, methodName, args);
	}

	/**
	 * Invokes the given method, on the specified object with the specified parameters.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param methodName the method name that will be invoked
	 * @param args the arguments used for the method call
	 * @return the value returned by the method
	 * @throws IllegalArgumentException if not found
	 * @see #invokeStaticMethod(String, Object...)
	 * @see #invokeMethod(boolean, Object, String, Object...)
	 */
	public <R> R invokeStaticMethod(String methodName, Object... args) {
		return invokeStaticMethod(true, methodName, args);
	}

	/**
	 * Creates a new instance of the given class, using a constructor that matches with the specified arguments.
	 *
	 * @param args the arguments used that will be used to creates the instance
	 * @return the instance
	 * @throws IllegalArgumentException if not found
	 */
	public T newInstance(Object... args) {
		if (args.length == 0) {
			return newInstance(CLASS);
		}
		Constructor<?> constructor = getCompatibleConstructorWithValues(args);
		return newInstance(constructor, args);
	}

	/**
	 * Sets the field value.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param obj the instance that will have the field value setted.
	 * @param fieldName the field name
	 * @param newValue the new field value
	 * @throws IllegalArgumentException if not found
	 * @see #setStaticFieldValue(Field, Object)
	 * @see #getFieldValue(Object, Field)
	 */
	public void setFieldValue(boolean searchInSuperclasses, Object obj, String fieldName, Object newValue) {
		Field field = getField(searchInSuperclasses, fieldName);
		setFieldValue(obj, field, newValue);
	}

	/**
	 * Sets the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param obj the instance that will have the field value setted.
	 * @param fieldName the field name
	 * @param newValue the new field value
	 * @throws IllegalArgumentException if not found
	 * @see #setStaticFieldValue(Field, Object)
	 * @see #getFieldValue(Object, Field)
	 */
	public void setFieldValue(Object obj, String fieldName, Object newValue) {
		setFieldValue(true, obj, fieldName, newValue);
	}

	/**
	 * Sets the field value.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param fieldName the field name
	 * @param newValue the new field value
	 * @throws IllegalArgumentException if not found
	 * @see #setStaticFieldValue(Field, Object)
	 * @see #getFieldValue(Object, Field)
	 */
	public void setStaticFieldValue(boolean searchInSuperclasses, String fieldName, Object newValue) {
		setFieldValue(searchInSuperclasses, null, fieldName, newValue);
	}

	/**
	 * Sets the field value.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param fieldName the field name
	 * @param newValue the new field value
	 * @throws IllegalArgumentException if not found
	 * @see #setStaticFieldValue(Field, Object)
	 * @see #getFieldValue(Object, Field)
	 */
	public void setStaticFieldValue(String fieldName, Object newValue) {
		setStaticFieldValue(true, fieldName, newValue);
	}

	@Override
	public String toString() {
		return CLASS.toString();
	}

	/**
	 * Returns the method with the specified name that accepts the given types.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param methodName the method name
	 * @param parameterTypes the desired types
	 * @return the method or null
	 * @see #getCompatibleMethodWithTypes(boolean, String, Class...)
	 * @see #tryGetCompatibleMethodWithTypes(String, Class...)
	 */
	public Method tryGetCompatibleMethodWithTypes(boolean searchInSuperclasses, String methodName, Class<?>... parameterTypes) {
		return tryGetMethod(searchInSuperclasses, methodName, parameterTypes);
	}

	/**
	 * Returns the method with the specified name that accepts the given types.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param methodName the method name
	 * @param parameterTypes the desired types
	 * @return the method or null
	 * @see #getCompatibleMethodWithTypes(String, Class...)
	 * @see #tryGetCompatibleMethodWithTypes(boolean, String, Class...)
	 */
	public Method tryGetCompatibleMethodWithTypes(String methodName, Class<?>... parameterTypes) {
		return tryGetCompatibleMethodWithTypes(true, methodName, parameterTypes);
	}

	/**
	 * Returns the method with the specified name that accepts the given values.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param methodName the method name
	 * @param args the desired values
	 * @return the method or null
	 * @see #getCompatibleMethodWithValues(boolean, String, Object...)
	 * @see #tryGetCompatibleMethodWithValues(String, Object...)
	 */
	public Method tryGetCompatibleMethodWithValues(boolean searchInSuperclasses, String methodName, Object... args) {
		return tryGetMethod(searchInSuperclasses, methodName, args);
	}

	/**
	 * Returns the method with the specified name that accepts the given values.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param methodName the method name
	 * @param args the desired values
	 * @return the method or null
	 * @see #getCompatibleMethodWithValues(String, Object...)
	 * @see #tryGetCompatibleMethodWithValues(boolean, String, Object...)
	 */
	public Method tryGetCompatibleMethodWithValues(String methodName, Object... args) {
		return tryGetCompatibleMethodWithValues(true, methodName, args);
	}

	/**
	 * Returns the public constructor that matches with the given predicate.
	 *
	 * @param predicate the filter
	 * @return the constructor or null
	 * @see #getConstructor(Predicate)
	 * @see #getConstructors(Predicate)
	 * @see #acceptConstructors(Consumer, Predicate)
	 */
	public Constructor<?> tryGetConstructor(Predicate<? super Constructor<?>> predicate) {
		for (Constructor<?> constructor : CONSTRUCTORS) {
			if (predicate.test(constructor)) {
				return constructor;
			}
		}
		return null;
	}

	/**
	 * Returns the field that matches with the given predicate.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param predicate the filter
	 * @return the field or null
	 * @see #getField(boolean, Predicate)
	 * @see #tryGetField(Predicate)
	 */
	public Field tryGetField(boolean searchInSuperclasses, Predicate<? super Field> predicate) {
		Collection<Field> fields = FIELDS.values();
		for (Field field : fields) {
			if (predicate.test(field)) {
				return field;
			}
		}
		if (searchInSuperclasses) {
			for (ClassWrapper<? super T> wrapper : SUPER_CLASS_WRAPPERS) {
				Field field = wrapper.tryGetField(searchInSuperclasses, predicate);
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the field with the given name.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param name the field name
	 * @return the field or null
	 * @see #getField(boolean, String)
	 * @see #tryGetField(String)
	 */
	public Field tryGetField(boolean searchInSuperclasses, String name) {
		Field field = FIELDS.get(name);
		if (field != null) {
			return field;
		}
		if (searchInSuperclasses) {
			for (ClassWrapper<? super T> wrapper : SUPER_CLASS_WRAPPERS) {
				field = wrapper.tryGetField(searchInSuperclasses, name);
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the field that matches with the given predicate.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param predicate the filter
	 * @return the field or null
	 * @see #getField(Predicate)
	 * @see #tryGetField(boolean, Predicate)
	 */
	public Field tryGetField(Predicate<? super Field> predicate) {
		return tryGetField(true, predicate);
	}

	/**
	 * Returns the field with the given name.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param name the field name
	 * @return the field or null
	 * @see #getField(String)
	 * @see #tryGetField(boolean, String)
	 */
	public Field tryGetField(String name) {
		return tryGetField(true, name);
	}

	/**
	 * Returns the method that matches with the given predicate.
	 *
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param predicate the filter
	 * @return the method or null
	 * @throws IllegalArgumentException if not found
	 * @see #getMethod(boolean, Predicate)
	 * @see #tryGetMethod(Predicate)
	 */
	public Method tryGetMethod(boolean searchInSuperclasses, Predicate<? super Method> predicate) {
		Collection<Set<Method>> methods = METHODS.values();
		for (Set<Method> set : methods) {
			for (Method method : set) {
				if (predicate.test(method)) {
					return method;
				}
			}
		}
		if (searchInSuperclasses) {
			for (ClassWrapper<? super T> wrapper : SUPER_CLASS_WRAPPERS) {
				Method method = wrapper.tryGetMethod(searchInSuperclasses, predicate);
				if (method != null) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the method that matches with the given predicate.
	 * Note: this method will search in the superclasses, if necessary.
	 *
	 * @param predicate the filter
	 * @return the method or null
	 * @see #getMethod(Predicate)
	 * @see #tryGetMethod(boolean, Predicate)
	 */
	public Method tryGetMethod(Predicate<? super Method> predicate) {
		return tryGetMethod(true, predicate);
	}

	/**
	 * Returns the constructor that accepts the given types or values.
	 *
	 * @param typesOrValues the desired types (Class<?>[]) or values (Object[])
	 * @return the constructor
	 * @throws IllegalArgumentException if not found
	 */
	private Constructor<?> getConstructor(Object typesOrValues) {
		for (Constructor<?> constructor : CONSTRUCTORS) {
			if (accept(constructor.getParameterTypes(), typesOrValues)) {
				return constructor;
			}
		}
		throw new IllegalArgumentException(format("Constructor not found: %s%s", CLASS.getName(), getTypesAsString(typesOrValues)));
	}

	/**
	 * Returns the method with the specified name that accepts the given types or values.
	 *
	 * @param typesOrValues the desired types (Class<?>[]) or values (Object[])
	 * @param searchInSuperclasses use true to search in all superclass
	 * @param methodName the method name
	 * @return the method or null
	 */
	private Method tryGetMethod(boolean searchInSuperclasses, String methodName, Object typesOrValues) {
		Set<Method> methods = METHODS.get(methodName);
		if (methods != null) {
			for (Method method : methods) {
				if (accept(method.getParameterTypes(), typesOrValues)) {
					return method;
				}
			}
		}
		if (searchInSuperclasses) {
			for (ClassWrapper<? super T> wrapper : SUPER_CLASS_WRAPPERS) {
				Method method = wrapper.tryGetMethod(searchInSuperclasses, methodName, typesOrValues);
				if (method != null) {
					return method;
				}
			}
		}
		return null;
	}

}

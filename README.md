# 8R-Reflections

### Current version: 2.0.0

## What is it?

An utility library for facilitate the handling of Class, Field, Method and others elements of the Java Reflection API.

## Documentation

Read the [Javadoc here](https://infinityrefactoring.github.io/8R-Reflections/)

## Requirements for use

* Java 8, or newer

## Using Reflections

You can use our maven repository, that is easier, or you can download it and build the Jar file and later add to your project.

## Maven Project

Add this dependency in your pom.xml file:

```
<dependency>
	<groupId>com.infinityrefactoring</groupId>
	<artifactId>8R-reflections</artifactId>
	<version>2.0.0</version>
</dependency>
```
## Use example:

This example show how you access the fields and methods of the Person and Address classes.

```
public class Person {

	public static String NAME_PREFIX = "[USER]";
	private String name;
	private Address[] addresses;

	//Getters and setters

}

public class Address {

	private String state;
	private String country;

	//Getters and setters

}
```

Creating a new instence of Person with the default constructor:

```
Person person = Reflections.newInstance(Person.class);
```

Sets and Gets the "name" field value:

```
Reflections.setFieldValue(person, "name", "Thomás");
String name = Reflections.getFieldValue(person, "name");
System.out.println(name);
```

Invoking the getName() method on the given person instance:

```
String name = Reflections.invokeMethod(person, "getName");
System.out.println(name);
```

Using path expressions:

```
//Compiling the given path expression
PathExpression pathExpression = PathExpression.compile("addresses[0].state");

//Adding a address array factory to the default factory:
InstanceFactory.DEFAULT_FACTORY.put(Address[].class, m -> new Address[5]);

//Sets the "state" field value of the first address of addresses array  of the given person
pathExpression.setExpressionValue(person, "PE");
String state = pathExpression.getExpressionValue(person);
System.out.println(state);
```
Using static path expressions:

```
PathExpression pathExpression = PathExpression.compile("class(com.exanple.Person)NAME_PREFIX");
pathExpression.setStaticExpressionValue("[PERSON]");
String state = pathExpression.getStaticExpressionValue();
System.out.println("state = " + state);	
```


Using various path expressions of single time:

```
Map<String, Object> map = new LinkedHashMap<>();
map.put("name", "Thomás Sousa Silva");
map.put("addresses", new Address[5]);
map.put("addresses[0].country", "Brazil");

//Sets all expression value for the given person instance.
Reflections.setAllExpressions(person, map);

//Returns a map with the same keys defined in the above map and yours respective values.
Map<String, Object> values = Reflections.getAllExpressionValues(person, map.keySet());
System.out.println(values);
```

Unwrapping the fields and methods of the Person class:

```
//Gets the field with the given prefix.
ClassWrapper<Person> classWrapper = ClassWrapper.getClassWrapper(person);

//Throws IllegalArgumentException if not found
//Field field = classWrapper.getField(Predicates.withMemberPrefix("aaa"));

//Returns null if not found
Field field = classWrapper.tryGetField(Predicates.withMemberPrefix("aaa"));

//Returns all fields 
Set<Field> fields = classWrapper.getFields(Predicates.acceptAll());
System.out.println(fields);

//Return all methods
Set<Method> methods = classWrapper.getMethods(Predicates.acceptAll());
System.out.println(methods);
```

## Licensing

**8R-Reflections** is provided and distributed under the [Apache Software License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Refer to *LICENSE* for more information.
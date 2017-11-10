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
import static com.infinityrefactoring.reflections.Reflections.getFieldValue;
import static com.infinityrefactoring.reflections.Reflections.getStaticFieldValue;
import static com.infinityrefactoring.reflections.Reflections.setFieldValue;
import static com.infinityrefactoring.reflections.Reflections.setStaticFieldValue;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Map;

/**
 * Represents an {@linkplain ExpressionNode#isField(String) field node} of a {@linkplain PathExpression path expression}.
 * A field node is compound by a field name.
 * Examples of Field node:
 * <ul>
 * <li>a</li>
 * <li>foo</li>
 * <li>bar</li>
 * <li>fieldName</li>
 * <li>MAX_VALUE</li>
 * </ul>
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
public class FieldNode extends ExpressionNode {

	/**
	 * Constructs a new instance of FieldNode.
	 *
	 * @param expressionNode the desired expression node
	 * @throws IllegalArgumentException if the expression node is not an {@linkplain ExpressionNode#isField(String) field}
	 */
	FieldNode(String expressionNode) {
		super(expressionNode);
		if (!ExpressionNode.isField(expressionNode)) {
			throw new IllegalArgumentException("This expression node is not a field.");
		}
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof FieldNode) && (NAME.equals(((FieldNode) obj).NAME)));
	}

	/**
	 * Returns the field that this node represents on the given class.
	 *
	 * @param c the class that have this node
	 * @return the field
	 */
	public Field getField(Class<?> c) {
		return (Field) getMember(c, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see #getField(Class)
	 */
	@Override
	public Member getMember(Class<?> c, Map<String, Object> args) {
		return wrap(c).getField(NAME);
	}

	@Override
	public Object getStaticValue(Class<?> c, Map<String, Object> args) {
		return getStaticFieldValue(c, NAME);
	}

	@Override
	public Object getValue(Object obj, Map<String, Object> args) {
		return getFieldValue(obj, NAME);
	}

	@Override
	public int hashCode() {
		return NAME.hashCode();
	}

	@Override
	public void setStaticValue(Class<?> c, Object newValue, Map<String, Object> args, InstanceFactory instanceFactory) {
		setStaticFieldValue(c, NAME, newValue);
	}

	@Override
	public void setValue(Object obj, Object value, Map<String, Object> args, InstanceFactory instanceFactory) {
		setFieldValue(obj, NAME, value);
	}

	@Override
	public String toString() {
		return "FieldNode [FIELD_NAME=" + NAME + "]";
	}

}

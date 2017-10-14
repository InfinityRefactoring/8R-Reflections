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

import static com.infinityrefactoring.reflections.ClassWrapper.getClassWrapper;
import static com.infinityrefactoring.reflections.Reflections.getFieldValue;
import static com.infinityrefactoring.reflections.Reflections.setFieldValue;
import static java.util.Objects.requireNonNull;

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
public class FieldNode implements ExpressionNode {

	private final String FIELD_NAME;

	/**
	 * Constructs a new instance of FieldNode.
	 *
	 * @param expressionNode the desired expression node
	 * @throws IllegalArgumentException if the expression node is not an {@linkplain ExpressionNode#isField(String) field}
	 */
	FieldNode(String expressionNode) {
		if (!ExpressionNode.isField(expressionNode)) {
			throw new IllegalArgumentException("This expression node is not a field.");
		}
		FIELD_NAME = requireNonNull(expressionNode);
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof FieldNode) && (FIELD_NAME.equals(((FieldNode) obj).FIELD_NAME)));
	}

	@Override
	public String getName() {
		return FIELD_NAME;
	}

	@Override
	public Class<?> getNodeClass(Object rootObj, Map<String, Object> args) {
		return getClassWrapper(rootObj).getField(FIELD_NAME).getType();
	}

	@Override
	public Object getValue(Object obj, Map<String, Object> args) {
		return getFieldValue(obj, FIELD_NAME);
	}

	@Override
	public int hashCode() {
		return FIELD_NAME.hashCode();
	}

	@Override
	public void setValue(Object obj, Object value, Map<String, Object> args, InstanceFactory instanceFactory) {
		setFieldValue(obj, FIELD_NAME, value);
	}

	@Override
	public String toString() {
		return "FieldNode [FIELD_NAME=" + FIELD_NAME + "]";
	}

}

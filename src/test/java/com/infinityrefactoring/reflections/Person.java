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

import java.util.Collection;

/**
 * Class used as example for the tests.
 *
 * @author Thomás Sousa Silva (ThomasSousa96)
 */
@SuppressWarnings("javadoc")
@A
public class Person {

	public static String NAME = "foo";
	public static String NULL;
	public static Address ADDRESS = new Address();

	private String name;
	private Address[] addresses;
	private Collection<String> phones;

	public Person() {

	}

	public Person(String name, Address[] addresses) {
		this.name = name;
		this.addresses = addresses;
	}

	public Address[] getAddresses() {
		return addresses;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getPhones() {
		return phones;
	}

	public void setAddresses(Address[] addresses) {
		this.addresses = addresses;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhones(Collection<String> phones) {
		this.phones = phones;
	}

}

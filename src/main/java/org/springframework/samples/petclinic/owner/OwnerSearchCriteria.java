/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

/**
 * Form-backing object for the enhanced Find Owners page. Captures the selected search
 * criterion (Last Name, Telephone, or Pet Name) together with the value the user typed
 * into the search box.
 *
 * <p>
 * A dedicated form object is used (rather than reusing {@link Owner#getLastName()}) so
 * that validation errors and not-found messages for Telephone and Pet Name searches can
 * be associated with a single {@code searchTerm} field on this object, keeping the
 * {@link Owner} entity free of search concerns.
 * </p>
 */
public class OwnerSearchCriteria {

	/**
	 * Supported search criteria for the Find Owners page.
	 */
	public enum Criterion {

		LAST_NAME, TELEPHONE, PET_NAME

	}

	private Criterion criterion = Criterion.LAST_NAME;

	private String searchTerm;

	public Criterion getCriterion() {
		return this.criterion;
	}

	public void setCriterion(Criterion criterion) {
		this.criterion = (criterion == null ? Criterion.LAST_NAME : criterion);
	}

	public String getSearchTerm() {
		return this.searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	/**
	 * Return the search term with surrounding whitespace removed, or an empty string if
	 * no term was supplied. Never returns {@code null}.
	 */
	public String trimmedSearchTerm() {
		return this.searchTerm == null ? "" : this.searchTerm.strip();
	}

}

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Integration tests for {@link OwnerRepository#findDistinctByPetNameContainingIgnoreCase}
 * verifying that owners with multiple matching pets appear only once and that
 * {@link Page#getTotalElements()} reflects the distinct owner count.
 *
 * <p>
 * The seed data (see {@code db/h2/data.sql}) gives Jean Coleman (owner id 6) two pets:
 * "Samantha" and "Max". A pet-name query for the fragment "am" therefore matches both
 * pets that belong to the same owner, and must return that owner exactly once.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OwnerRepositoryPetNameSearchTests {

	@Autowired
	private OwnerRepository owners;

	@Test
	void ownerWithMultipleMatchingPetsAppearsExactlyOnce() {
		Page<Owner> page = owners.findDistinctByPetNameContainingIgnoreCase("am", Pageable.unpaged());

		// Jean Coleman owns both "Samantha" and "Max" (both match "am"
		// case-insensitively)
		// and must appear only once.
		long colemanOccurrences = page.getContent().stream().filter(o -> "Coleman".equals(o.getLastName())).count();

		assertThat(colemanOccurrences).as("Owner with multiple matching pets should appear exactly once").isEqualTo(1L);

		// totalElements should reflect distinct owners, not distinct owner-pet rows.
		assertThat(page.getTotalElements()).as("totalElements must count distinct owners")
			.isEqualTo(page.getContent().size());
	}

	@Test
	void petNameSearchIsCaseInsensitive() {
		Page<Owner> lower = owners.findDistinctByPetNameContainingIgnoreCase("max", Pageable.unpaged());
		Page<Owner> upper = owners.findDistinctByPetNameContainingIgnoreCase("MAX", Pageable.unpaged());
		Page<Owner> mixed = owners.findDistinctByPetNameContainingIgnoreCase("MaX", Pageable.unpaged());

		assertThat(lower.getTotalElements()).isEqualTo(upper.getTotalElements())
			.isEqualTo(mixed.getTotalElements())
			.isGreaterThan(0);
	}

	@Test
	void petNameSearchWithNoMatchReturnsEmptyPage() {
		Page<Owner> page = owners.findDistinctByPetNameContainingIgnoreCase("no-such-pet-name-xyz", Pageable.unpaged());
		assertThat(page.getContent()).isEmpty();
		assertThat(page.getTotalElements()).isZero();
	}

}

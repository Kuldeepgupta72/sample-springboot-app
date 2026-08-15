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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end integration tests for the Enhanced Owner Search feature (Jira SDLC-1: SDLC-2
 * last name / SDLC-3 telephone / SDLC-4 pet name / SDLC-5 pagination context
 * preservation).
 *
 * <p>
 * Uses {@link SpringBootTest} plus {@link MockMvc} to exercise the real HTTP endpoint,
 * the real {@code OwnerController}, the real {@code OwnerRepository} and Thymeleaf views
 * against the real H2 database seeded with production {@code db/h2/data.sql} plus
 * test-only supplementary fixtures loaded from
 * {@code src/test/resources/db/test-search-fixtures.sql}.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:/db/test-search-fixtures.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestPropertySource(
		properties = { "spring.docker.compose.enabled=false", "spring.docker.compose.lifecycle-management=none" })
@DisabledInNativeImage
@DisabledInAotMode
class EnhancedOwnerSearchIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	// ---------------------------------------------------------------------
	// SDLC-2: Last Name search
	// ---------------------------------------------------------------------

	/** SDLC-2 - Last Name is the default criterion. */
	@Test
	void lastNameIsDefaultCriterionWhenOnlySearchTermSupplied() throws Exception {
		// Only searchTerm supplied. Controller must default to LAST_NAME
		// and match Betty and Harold "Davis" from seed data.
		mockMvc.perform(get("/owners").param("searchTerm", "Da"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.LAST_NAME))
			.andExpect(model().attribute("searchTerm", "Da"))
			.andExpect(model().attribute("listOwners", ownersWithLastName("Davis", 2)));
	}

	/** SDLC-2 - Legacy {@code ?lastName=} request parameter still works. */
	@Test
	void legacyLastNameRequestParameterStillWorks() throws Exception {
		// Only owner "Franklin" matches - single result → redirect.
		mockMvc.perform(get("/owners").param("lastName", "Franklin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/owners/*"));
	}

	/**
	 * SDLC-2 - Last Name prefix search returns owners whose last name starts with the
	 * term.
	 */
	@Test
	void lastNamePrefixSearchReturnsMatchingOwners() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "LAST_NAME").param("searchTerm", "Da"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("listOwners", ownersWithLastName("Davis", 2)));
	}

	/** SDLC-2 - Last Name search with no matching owners renders the not-found error. */
	@Test
	void lastNameNoResultRendersNotFoundError() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "LAST_NAME").param("searchTerm", "NoSuchSurnameXYZ"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasFieldErrors("searchCriteria", "searchTerm"))
			.andExpect(model().attributeHasFieldErrorCode("searchCriteria", "searchTerm", "notFound"));
	}

	/**
	 * SDLC-2 - Multiple-result / pagination. Fixture seeds 6 owners with last name
	 * "Zztestpage"; page size is 5 so two pages are produced.
	 */
	@Test
	void lastNameMultipleResultsPaginate() throws Exception {
		mockMvc
			.perform(
					get("/owners").param("criterion", "LAST_NAME").param("searchTerm", "Zztestpage").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("currentPage", 1))
			.andExpect(model().attribute("totalPages", 2))
			.andExpect(model().attribute("totalItems", 6L));
	}

	// ---------------------------------------------------------------------
	// SDLC-3: Telephone search
	// ---------------------------------------------------------------------

	/** SDLC-3 - Telephone exact match returning a single owner redirects to details. */
	@Test
	void telephoneExactMatchSingleOwnerRedirects() throws Exception {
		// Seed owner George Franklin has telephone 6085551023 - unique.
		mockMvc.perform(get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "6085551023"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/owners/*"));
	}

	/** SDLC-3 - Surrounding whitespace in the telephone term is trimmed. */
	@Test
	void telephoneWhitespaceTrimming() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "   6085551023   "))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/owners/*"));
	}

	/**
	 * SDLC-3 - Telephone formatting is NOT normalized. A dashed value "608-555-1023" must
	 * not match the stored "6085551023".
	 */
	@Test
	void telephoneFormattingIsNotNormalized() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "608-555-1023"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasFieldErrors("searchCriteria", "searchTerm"))
			.andExpect(model().attributeHasFieldErrorCode("searchCriteria", "searchTerm", "notFound"));
	}

	/** SDLC-3 - Telephone search with no matching owner renders the not-found error. */
	@Test
	void telephoneNoResultRendersNotFoundError() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "0000000000"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasFieldErrors("searchCriteria", "searchTerm"))
			.andExpect(model().attributeHasFieldErrorCode("searchCriteria", "searchTerm", "notFound"));
	}

	/**
	 * SDLC-3 (multi-owner). Fixture seeds two owners sharing telephone 5551237777; the
	 * search must return the owners list (no redirect).
	 */
	@Test
	void telephoneMatchingMultipleOwnersRendersList() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "5551237777"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalItems", 2L))
			.andExpect(model().attribute("listOwners", hasSize(2)));
	}

	// ---------------------------------------------------------------------
	// SDLC-4: Pet Name search
	// ---------------------------------------------------------------------

	/** SDLC-4 - Pet Name contains search returns owners of matching pets. */
	@Test
	void petNameContainsSearchReturnsMatchingOwners() throws Exception {
		// Seed: pet "Lucky" belongs to Jeff Black AND Carlos Estaban.
		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "ucky"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalItems", 2L))
			.andExpect(model().attribute("listOwners", hasSize(2)));
	}

	/** SDLC-4 - Pet Name search is case-insensitive. */
	@Test
	void petNameCaseInsensitive() throws Exception {
		// Same 2 owners must be returned regardless of case.
		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "LUCKY"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalItems", 2L))
			.andExpect(model().attribute("listOwners", hasSize(2)));

		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "lucky"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalItems", 2L))
			.andExpect(model().attribute("listOwners", hasSize(2)));
	}

	/** SDLC-4 - Surrounding whitespace in Pet Name search is trimmed. */
	@Test
	void petNameWhitespaceTrimming() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "   ucky   "))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalItems", 2L))
			.andExpect(model().attribute("listOwners", hasSize(2)));
	}

	/**
	 * SDLC-4 - An owner with multiple pets matching the term appears exactly once. Seed
	 * owner Jean Coleman has pets "Samantha" and "Max"; both contain 'a'. Search "a" also
	 * matches Betty Davis (Basil) and Maria Escobito (Mulligan) - 3 distinct owners
	 * total.
	 */
	@Test
	void petNameUniqueOwnerWhenMultiplePetsMatch() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "a"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalItems", 3L))
			.andExpect(model().attribute("listOwners", hasSize(3)))
			.andExpect(model().attribute("listOwners", ownersWithLastNameOccurringExactlyOnce("Coleman")));
	}

	/** SDLC-4 - Pet Name search matching a single owner redirects to details. */
	@Test
	void petNameSingleResultRedirects() throws Exception {
		// Seed: only David Schroeder owns "Freddy".
		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "Freddy"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/owners/*"));
	}

	/** SDLC-4 - Pet Name search with no match renders the not-found error. */
	@Test
	void petNameNoResultRendersNotFoundError() throws Exception {
		mockMvc.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "NoSuchPetXYZ"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasFieldErrors("searchCriteria", "searchTerm"))
			.andExpect(model().attributeHasFieldErrorCode("searchCriteria", "searchTerm", "notFound"));
	}

	/**
	 * SDLC-4 - Multiple-result / pagination. Fixture seeds 6 owners each with a pet name
	 * containing "Zztestpet"; page size is 5 so two pages are produced.
	 */
	@Test
	void petNameMultipleResultsPaginate() throws Exception {
		mockMvc
			.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "Zztestpet").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("currentPage", 1))
			.andExpect(model().attribute("totalPages", 2))
			.andExpect(model().attribute("totalItems", 6L));
	}

	// ---------------------------------------------------------------------
	// SDLC-5: Pagination context preservation
	// ---------------------------------------------------------------------

	/**
	 * SDLC-5 - Last Name criterion + search term are preserved across pages.
	 * <ul>
	 * <li>Page 1 rendering must include a pagination link to page 2 that carries the
	 * criterion and searchTerm.</li>
	 * <li>Requesting page 2 with the same criterion and searchTerm must expose them again
	 * on the model.</li>
	 * </ul>
	 */
	@Test
	void paginationPreservesLastNameCriterionAndTerm() throws Exception {
		MvcResult page1 = mockMvc
			.perform(
					get("/owners").param("criterion", "LAST_NAME").param("searchTerm", "Zztestpage").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.LAST_NAME))
			.andExpect(model().attribute("searchTerm", "Zztestpage"))
			.andExpect(content().string(containsString("criterion=LAST_NAME")))
			.andExpect(content().string(containsString("searchTerm=Zztestpage")))
			.andReturn();
		// Sanity: the HTML must actually reference page 2 in a pagination anchor.
		String html = page1.getResponse().getContentAsString();
		org.assertj.core.api.Assertions.assertThat(html)
			.as("Pagination HTML for page 1 should link to page=2")
			.contains("page=2");

		mockMvc
			.perform(
					get("/owners").param("criterion", "LAST_NAME").param("searchTerm", "Zztestpage").param("page", "2"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("currentPage", 2))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.LAST_NAME))
			.andExpect(model().attribute("searchTerm", "Zztestpage"));
	}

	/** SDLC-5 - Telephone criterion + term are preserved across pages. */
	@Test
	void paginationPreservesTelephoneCriterionAndTerm() throws Exception {
		MvcResult page1 = mockMvc
			.perform(
					get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "9990001234").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.TELEPHONE))
			.andExpect(model().attribute("searchTerm", "9990001234"))
			.andExpect(content().string(containsString("criterion=TELEPHONE")))
			.andExpect(content().string(containsString("searchTerm=9990001234")))
			.andReturn();
		String html = page1.getResponse().getContentAsString();
		org.assertj.core.api.Assertions.assertThat(html)
			.as("Pagination HTML for page 1 should link to page=2")
			.contains("page=2");

		mockMvc
			.perform(
					get("/owners").param("criterion", "TELEPHONE").param("searchTerm", "9990001234").param("page", "2"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("currentPage", 2))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.TELEPHONE))
			.andExpect(model().attribute("searchTerm", "9990001234"));
	}

	/** SDLC-5 - Pet Name criterion + term are preserved across pages. */
	@Test
	void paginationPreservesPetNameCriterionAndTerm() throws Exception {
		MvcResult page1 = mockMvc
			.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "Zztestpet").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.PET_NAME))
			.andExpect(model().attribute("searchTerm", "Zztestpet"))
			.andExpect(content().string(containsString("criterion=PET_NAME")))
			.andExpect(content().string(containsString("searchTerm=Zztestpet")))
			.andReturn();
		String html = page1.getResponse().getContentAsString();
		org.assertj.core.api.Assertions.assertThat(html)
			.as("Pagination HTML for page 1 should link to page=2")
			.contains("page=2");

		mockMvc
			.perform(get("/owners").param("criterion", "PET_NAME").param("searchTerm", "Zztestpet").param("page", "2"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("currentPage", 2))
			.andExpect(model().attribute("criterion", OwnerSearchCriteria.Criterion.PET_NAME))
			.andExpect(model().attribute("searchTerm", "Zztestpet"));
	}

	// ---------------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------------

	private static org.hamcrest.Matcher<Collection<Owner>> ownersWithLastName(String lastName, int expectedCount) {
		return new org.hamcrest.TypeSafeDiagnosingMatcher<Collection<Owner>>() {
			@Override
			protected boolean matchesSafely(Collection<Owner> owners, org.hamcrest.Description mismatch) {
				long matching = owners.stream().filter(o -> lastName.equalsIgnoreCase(o.getLastName())).count();
				if (matching != expectedCount || owners.size() != expectedCount) {
					mismatch.appendText("got ")
						.appendValue(owners.size())
						.appendText(" owners of which ")
						.appendValue(matching)
						.appendText(" had last name ")
						.appendValue(lastName);
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText("exactly ")
					.appendValue(expectedCount)
					.appendText(" owners with last name ")
					.appendValue(lastName);
			}
		};
	}

	private static org.hamcrest.Matcher<Collection<Owner>> ownersWithLastNameOccurringExactlyOnce(String lastName) {
		return new org.hamcrest.TypeSafeDiagnosingMatcher<Collection<Owner>>() {
			@Override
			protected boolean matchesSafely(Collection<Owner> owners, org.hamcrest.Description mismatch) {
				long occurrences = owners.stream().filter(o -> lastName.equalsIgnoreCase(o.getLastName())).count();
				if (occurrences != 1L) {
					mismatch.appendText("owner ")
						.appendValue(lastName)
						.appendText(" occurred ")
						.appendValue(occurrences)
						.appendText(" times");
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText("owner ")
					.appendValue(lastName)
					.appendText(" occurs exactly once in the result list");
			}
		};
	}

	// Reference to prevent optimizer from stripping unused imports; also
	// available for future assertions that need to inspect list types.
	@SuppressWarnings("unused")
	private static <T> List<T> assertList(List<T> list) {
		return list;
	}

	@SuppressWarnings("unused")
	private static org.hamcrest.Matcher<Object> equalsExactly(Object o) {
		return is(o);
	}

}

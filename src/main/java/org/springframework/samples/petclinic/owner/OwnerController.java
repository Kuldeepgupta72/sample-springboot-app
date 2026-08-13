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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private static final int PAGE_SIZE = 5;

	private final OwnerRepository owners;

	public OwnerController(OwnerRepository owners) {
		this.owners = owners;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	/**
	 * Expose an empty {@link OwnerSearchCriteria} to the Find Owners form so Thymeleaf
	 * can bind the criterion dropdown and the search term input.
	 */
	@ModelAttribute("searchCriteria")
	public OwnerSearchCriteria searchCriteria() {
		return new OwnerSearchCriteria();
	}

	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page,
			@ModelAttribute("searchCriteria") OwnerSearchCriteria searchCriteria, BindingResult searchResult,
			@RequestParam(name = "lastName", required = false) String legacyLastName, Owner owner,
			BindingResult ownerResult, Model model) {

		// Detect which "form" the request came from.
		// New form: has criterion or searchTerm parameters (takes precedence).
		// Legacy path: only lastName parameter present.
		boolean newFormSubmitted = searchCriteria != null
				&& (searchCriteria.getSearchTerm() != null || hasExplicitCriterion(searchCriteria));

		if (!newFormSubmitted && legacyLastName != null) {
			// Legacy request: preserve pre-existing behavior (errors on owner.lastName).
			return processLegacyLastNameSearch(page, legacyLastName, owner, ownerResult, model);
		}

		// New form path (default criterion is LAST_NAME).
		OwnerSearchCriteria criteria = (searchCriteria != null) ? searchCriteria : new OwnerSearchCriteria();
		String term = criteria.trimmedSearchTerm();
		OwnerSearchCriteria.Criterion criterion = criteria.getCriterion();

		Page<Owner> results;
		switch (criterion) {
			case TELEPHONE -> {
				if (term.isEmpty()) {
					searchResult.rejectValue("searchTerm", "notFound", "not found");
					return "owners/findOwners";
				}
				results = findByTelephonePaginated(page, term);
			}
			case PET_NAME -> {
				if (term.isEmpty()) {
					searchResult.rejectValue("searchTerm", "notFound", "not found");
					return "owners/findOwners";
				}
				results = findByPetNamePaginated(page, term);
			}
			case LAST_NAME -> results = findPaginatedForOwnersLastName(page, term);
			default -> results = findPaginatedForOwnersLastName(page, term);
		}

		if (results.isEmpty()) {
			searchResult.rejectValue("searchTerm", "notFound", "not found");
			return "owners/findOwners";
		}

		if (results.getTotalElements() == 1) {
			Owner single = results.iterator().next();
			return "redirect:/owners/" + single.getId();
		}

		return addPaginationModel(page, model, results, criterion, term);
	}

	private static boolean hasExplicitCriterion(OwnerSearchCriteria criteria) {
		// A non-default criterion is a signal that the new form was used even if the
		// search term is null. LAST_NAME is the default so we cannot rely on it alone.
		return criteria.getCriterion() != null && criteria.getCriterion() != OwnerSearchCriteria.Criterion.LAST_NAME;
	}

	private String processLegacyLastNameSearch(int page, String legacyLastName, Owner owner, BindingResult ownerResult,
			Model model) {
		String lastName = legacyLastName == null ? "" : legacyLastName.strip();
		// Keep the Owner model attribute in sync so any error/view rendering can display
		// the submitted value.
		owner.setLastName(lastName);

		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			ownerResult.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			Owner single = ownersResults.iterator().next();
			return "redirect:/owners/" + single.getId();
		}

		return addPaginationModel(page, model, ownersResults, OwnerSearchCriteria.Criterion.LAST_NAME, lastName);
	}

	private String addPaginationModel(int page, Model model, Page<Owner> paginated,
			OwnerSearchCriteria.Criterion criterion, String searchTerm) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		model.addAttribute("criterion", criterion);
		model.addAttribute("searchTerm", searchTerm == null ? "" : searchTerm);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	private Page<Owner> findByTelephonePaginated(int page, String telephone) {
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		return owners.findByTelephone(telephone, pageable);
	}

	private Page<Owner> findByPetNamePaginated(int page, String petName) {
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		return owners.findDistinctByPetNameContainingIgnoreCase(petName, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		if (!Objects.equals(owner.getId(), ownerId)) {
			result.rejectValue("id", "mismatch", "The owner ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/owners/{ownerId}/edit";
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}

}

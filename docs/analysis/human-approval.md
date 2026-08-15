# Human-in-the-Loop Approval

## Analysis

The AI-SDLC BA Assistant analyzed the existing Spring PetClinic
application and identified five potential enhancements.

## AI Recommended MVP

Enhanced Owner Search (Telephone + Pet Name)

## Human Decision

APPROVED

## Approved Enhancement

Enhanced Owner Search

## Business Objective

Improve clinic staff efficiency by allowing owners to be located
using additional information available in the existing application,
such as telephone number and pet name.

## Approved MVP Scope

The Find Owners functionality will support:

1. Existing last-name prefix search
2. Telephone exact-match search
3. Pet-name case-insensitive partial/contains search
4. Clear display of matching owners
5. Preserve existing pagination behavior where applicable

## Deferred Scope

The following are explicitly excluded from the MVP:

- Fuzzy matching
- Phonetic search
- Address search
- City search
- Global visit-description search
- Complex Boolean search
- Advanced performance optimization

## Human Review Decision

APPROVED FOR REQUIREMENTS GENERATION

## Reviewer

Human-in-the-Loop / Product Owner


## Requirement Review — HITL #2

The AI-generated requirements were reviewed by the human
Product Owner / Business Analyst.

### Decisions

#### 1. Search Criteria Combination

Users can search using only one criterion at a time:

- Last Name
- Telephone
- Pet Name

Combining multiple search fields is out of scope for the MVP.

#### 2. Telephone Search

Telephone search will:

- Trim surrounding whitespace.
- Perform an exact match against the stored telephone value.
- Not perform advanced phone-number normalization.
- Not accept/transform formatting characters as part of the MVP.

#### 3. Pet Name Search

Pet Name search will:

- Trim surrounding whitespace.
- Perform case-insensitive partial/contains matching.
- Return an owner only once even if multiple pets match.

#### 4. Search Results

Existing behavior will be preserved:

- One result → redirect to owner details.
- No results → display not-found error.
- Multiple results → display owner list with pagination.

#### 5. Match Reason

The UI does not need to indicate why an owner matched
the search.

This is outside the MVP scope.

### Human Decision

APPROVED

### Status

Requirements approved for Jira creation.

## Jira Backlog Review — HITL #3

### Jira Project

SDLC

### Epic

SDLC-1 — Enhanced Owner Search

### Stories

- SDLC-2 — Maintain Last Name Prefix Search
- SDLC-3 — Search Owners by Telephone
- SDLC-4 — Search Owners by Pet Name
- SDLC-5 — Preserve Search Results and Pagination

### Human Review

The Jira backlog generated from the approved requirements was
reviewed by the Human Product Owner / Business Analyst.

### Validation

- Epic verified: APPROVED
- Four Stories verified: APPROVED
- Priorities verified: APPROVED
- Acceptance criteria verified: APPROVED
- MVP scope verified: APPROVED
- Out-of-scope functionality: NONE FOUND
- Duplicate issues: NONE FOUND

### Jira Warning

The JQL query `parent = SDLC-1` did not return the newly created
Stories. However, the Stories were individually verified and were
created with parent `SDLC-1`.

No duplicate issues were created.

### Human Decision

APPROVED

### Status

Jira backlog approved for planning.

## Planning Review — HITL #4

### Planning Artifact

Implementation Plan for SDLC-1 — Enhanced Owner Search

### Human Review Decision

APPROVED WITH CONDITIONS

### Approved Planning Decisions

#### 1. Search Form

A dedicated `OwnerSearchCriteria` form-backing object will be used.

It will contain:

- Selected search criterion
- Search term

This avoids reusing `Owner.lastName` for Telephone and Pet Name
validation and error handling.

#### 2. Telephone Search Validation

Telephone search will:

- Trim surrounding whitespace.
- Reject empty or whitespace-only input.
- Perform exact matching.
- Not enforce the Owner telephone 10-digit validation pattern.
- Not normalize telephone formatting.

#### 3. Default Search Criterion

The default search criterion will be:

Last Name

This preserves the existing user experience and minimizes regression
risk.

#### 4. Database

No database schema changes are planned for the MVP.

Existing Owner telephone data, Pet name data, and Owner-Pet
relationships will be reused.

#### 5. Template Verification

Before development begins, the actual Thymeleaf templates must be
verified:

- `src/main/resources/templates/owners/findOwners.html`
- `src/main/resources/templates/owners/ownersList.html`

The implementation must be based on their actual current structure.

#### 6. Pet Name Search

Pet Name search must return unique owners when multiple matching pets
belong to the same owner.

Integration testing must verify the distinct-owner query and
pagination behavior.

### Deferred

The following remain out of scope:

- Fuzzy search
- Phonetic search
- Address search
- City search
- Visit-description search
- Global search
- Boolean search
- Telephone normalization
- Advanced performance optimization
- Match-reason highlighting

### Human Decision

APPROVED WITH CONDITIONS

### Status

Implementation planning approved for Design phase.

## Design Review — HITL #5

### Design Artifact

Design Package for SDLC-1 — Enhanced Owner Search

### Human Review Decision

APPROVED WITH CONDITIONS

### Approved Design Decisions

#### 1. Target Architecture

The design will retain the existing Spring PetClinic MVC
architecture:

Browser → OwnerController → OwnerRepository → Database

No new service layer will be introduced for the MVP.

#### 2. Search Form

Introduce:

OwnerSearchCriteria

with:

- criterion
- searchTerm

Supported criteria:

- LAST_NAME
- TELEPHONE
- PET_NAME

#### 3. Default Criterion

The default search criterion is:

LAST_NAME

This preserves existing behavior.

#### 4. Backward Compatibility

Existing requests using the `lastName` request parameter must continue
to work.

When the legacy parameter is provided without the new search
parameters, the request will be treated as a Last Name search.

#### 5. Telephone Search

Telephone search will:

- Trim whitespace.
- Require a non-empty value.
- Perform exact matching.
- Not perform normalization.
- Not enforce the Owner creation/update 10-digit telephone pattern.

#### 6. Pet Name Search

Pet Name search will:

- Trim whitespace.
- Perform case-insensitive contains matching.
- Return unique owners.
- Preserve pagination.

#### 7. Database

No database schema changes are required or planned.

#### 8. Pagination

Pagination must preserve:

- criterion
- searchTerm
- page

#### 9. Error Handling

Existing Last Name not-found behavior will be preserved.

Telephone and Pet Name validation/not-found errors will be associated
with OwnerSearchCriteria.searchTerm.

### Conditions Before Development

1. Verify the actual Thymeleaf templates in the working development
   branch/environment.

2. Verify whether shared Thymeleaf fragments are used for the Find
   Owners and Owners List pages.

3. Verify the existing pagination markup before implementing changes.

4. Verify DISTINCT + pagination behavior through integration tests
   across supported databases.

### Deferred

The following remain out of scope:

- Fuzzy search
- Phonetic search
- Address search
- City search
- Visit-description search
- Global search
- Boolean search
- Telephone normalization
- Advanced performance optimization
- Match-reason highlighting

### Human Decision

APPROVED WITH CONDITIONS

### Status

Design approved for development after template verification.

## Development and Code Review — HITL #6

### Implementation

Enhanced Owner Search MVP was implemented using Claude Code through
CodeMie.

### Jira Scope Implemented

- SDLC-2 — Maintain Last Name Prefix Search
- SDLC-3 — Search Owners by Telephone
- SDLC-4 — Search Owners by Pet Name
- SDLC-5 — Preserve Search Results and Pagination

### Implementation Review

The implementation was reviewed against the approved requirements,
implementation plan, and design.

### Review Findings

- Last Name prefix search preserved.
- Telephone exact-match search implemented.
- Telephone whitespace trimming implemented.
- Pet Name case-insensitive contains search implemented.
- Duplicate owners prevented for multiple matching pets.
- Search criterion and search term preserved during pagination.
- Existing one-result behavior preserved.
- Existing no-result behavior preserved.
- Existing multiple-result behavior preserved.
- Dedicated OwnerSearchCriteria introduced.
- No database schema changes introduced.
- No unrelated functional scope identified.

### Automated Validation

Maven test suite:

- Tests executed: 87
- Failures: 0
- Errors: 0
- Skipped: 2
- Build: SUCCESS

### Integration Test Limitation

PostgreSQL/Testcontainers integration testing was not executed because
a valid Docker environment was unavailable.

This remains a validation item before final merge/deployment.

### Human Code Review Decision

APPROVED WITH CONDITION

### Condition

PostgreSQL/Testcontainers integration tests must be executed in an
environment with Docker before final merge.

### Status

Implementation approved for Pull Request / integration testing.

## Test Case Review — HITL #7

### Scope

Enhanced Owner Search MVP

### Jira Stories

- SDLC-2 — Maintain Last Name Prefix Search
- SDLC-3 — Search Owners by Telephone
- SDLC-4 — Search Owners by Pet Name
- SDLC-5 — Preserve Search Results and Pagination

### Human Review

The corrected Gherkin test suite was reviewed against the approved
requirements and human-approved MVP scope.

### Validation

- Last Name prefix search: APPROVED
- Last Name backward compatibility: APPROVED
- Telephone exact match: APPROVED
- Telephone whitespace trimming: APPROVED
- Telephone normalization exclusion: APPROVED
- Pet Name contains search: APPROVED
- Pet Name case-insensitive search: APPROVED
- Pet Name whitespace trimming: APPROVED
- Unique owner results: APPROVED
- One/no/multiple result behavior: APPROVED
- Pagination preservation: APPROVED
- Combined search criteria: EXCLUDED
- Out-of-scope functionality: EXCLUDED

### Conditions

1. Empty Telephone validation remains optional until the Jira
   acceptance criteria are verified.

2. Empty Pet Name validation remains optional until the Jira
   acceptance criteria are verified.

3. Placeholder test data must be resolved before automated execution.

4. All generated scenarios remain pending execution.

### Human Decision

APPROVED WITH CONDITIONS

### Status

Test cases approved for automated test implementation and execution.

## Test Execution Review — HITL #8

### Scope

Enhanced Owner Search MVP

### Automated Test Execution

Framework:

Spring Boot @SpringBootTest + MockMvc + JUnit 5

### Results

- Enhanced Owner Search tests: 20
- Passed: 20
- Failed: 0
- Blocked: 0
- Full regression tests: 107
- Full regression failures: 0
- Pre-existing skipped tests: 2

### Production Code

No production application code was modified during test automation.

### Limitations

- Browser-level Playwright/Selenium execution was not available.
- Tests use MockMvc rather than a real browser.
- PostgreSQL/Testcontainers execution remains pending because Docker
  is unavailable.
- H2 was used for the integration tests.

### Human Decision

APPROVED WITH CONDITIONS

### Conditions

1. PostgreSQL/Testcontainers validation remains pending.
2. Browser-level UI automation is not claimed as completed.
3. Test automation changes remain limited to test files.

### Status

Automated functional testing approved with documented limitations.

## Final Build Validation — HITL #9

### Validation

The final Maven verification was executed after implementation and
automated test validation.

### Command

mvn clean verify

### Result

SUCCESS

### Decision

APPROVED

### Status

Build validation passed. Ready for final PR approval and merge.

## Final SDLC Completion Review — HITL #10

### Enhancement

Enhanced Owner Search

### Jira Epic

SDLC-1 — Enhanced Owner Search

### Implemented Stories

- SDLC-2 — Maintain Last Name Prefix Search
- SDLC-3 — Search Owners by Telephone
- SDLC-4 — Search Owners by Pet Name
- SDLC-5 — Preserve Search Results and Pagination

### Implementation

The approved Enhanced Owner Search MVP was implemented in the
Spring PetClinic application.

The implementation supports:

- Existing last-name prefix search
- Telephone exact-match search
- Telephone surrounding whitespace trimming
- No advanced telephone-number normalization
- Pet-name case-insensitive contains search
- Pet-name surrounding whitespace trimming
- Unique owners when multiple matching pets are found
- Existing one-result owner-details behavior
- Existing no-result behavior
- Multiple-result owner list
- Pagination with search criterion and search term preserved

### Test Validation

Automated functional integration testing was completed using:

Spring Boot @SpringBootTest + MockMvc + JUnit 5

Results:

- Enhanced Owner Search tests: 20
- Passed: 20
- Failed: 0
- Blocked: 0
- Full regression tests: 107
- Failures: 0
- Pre-existing skipped tests: 2

### Build Validation

Final Maven verification:

mvn clean verify

Result:

BUILD SUCCESS

### Pull Request

Pull Request #1:

Enhanced Owner Search

Status:

MERGED

Merge commit:

5de7518

### Production Code Protection

Test automation did not modify the approved production implementation.

The test automation changes were limited to test-related files.

### Known Limitations

1. Browser-level Playwright/Selenium execution was not performed because
   browser automation tooling was not available in the environment.

2. Automated functional validation was performed using MockMvc.

3. PostgreSQL/Testcontainers integration testing was not executed because
   Docker was unavailable.

4. H2 was used for the automated integration test execution.

5. Empty Telephone and Empty Pet Name validation remain optional unless
   explicitly confirmed by the Jira acceptance criteria.

### Final Human Decision

APPROVED

### Final Status

The Enhanced Owner Search MVP has completed the approved AI-SDLC
workflow and has been merged into the main branch.

The implementation is considered complete for the approved MVP scope.

### Reviewer

Human-in-the-Loop / Product Owner / QA Review

### Completion Date

2026-08-15
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
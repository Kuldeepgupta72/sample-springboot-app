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
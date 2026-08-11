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
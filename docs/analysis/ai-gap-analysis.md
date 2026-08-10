## 1) Application Purpose

Spring PetClinic is a server-rendered web application for a veterinary clinic to maintain **owner records**, **pets owned by each owner**, and **visits for each pet**, and to display a **veterinarian directory** with specialties.

**Repository evidence**
- Landing page: `src/main/java/org/springframework/samples/petclinic/system/WelcomeController.java` (`GET /` → `welcome`)
- Owner CRUD + search: `src/main/java/.../owner/OwnerController.java`
- Pet add/edit under owner: `src/main/java/.../owner/PetController.java`
- Visit creation under pet: `src/main/java/.../owner/VisitController.java`
- Vet list + JSON endpoint: `src/main/java/.../vet/VetController.java`

---

## 2) Primary Users / Personas

1. **Receptionist / Front Desk**
    - Finds owners, adds/edits owner details, adds pets, records (“books”) visits.
    - Evidence: Owner/Pet/Visit controllers and corresponding templates.

2. **Veterinarian**
    - Views owner detail page to see pets and previous visit descriptions.
    - Evidence: Owner detail view includes pets + visit history: `templates/owners/ownerDetails.html`

3. **Clinic Manager / Admin (implicit, not implemented as a role)**
    - Would need reporting, configuration of pet types/specialties, access controls.
    - Evidence of absence: no authentication/roles in codebase; pet types/specialties exist only as reference data (DB scripts) with no admin UI.

---

## 3) Existing Functional Areas (What exists today)

### A. Owner Management
- Create owner (`/owners/new`)
- Edit owner (`/owners/{ownerId}/edit`)
- Find owners by last name prefix (paginated)
- View owner details

**Evidence**
- `src/main/java/.../owner/OwnerController.java`
- `src/main/java/.../owner/OwnerRepository.java` (`findByLastNameStartingWith(String, Pageable)`)
- Templates:
    - `src/main/resources/templates/owners/findOwners.html`
    - `src/main/resources/templates/owners/ownersList.html`
    - `src/main/resources/templates/owners/ownerDetails.html`
    - `src/main/resources/templates/owners/createOrUpdateOwnerForm.html`

### B. Pet Management (per owner)
- Add pet to owner (`/owners/{ownerId}/pets/new`)
- Edit pet (`/owners/{ownerId}/pets/{petId}/edit`)
- Pet type selection from reference data (`types` table)
- Duplicate pet name prevention per owner (case-insensitive via DB/index + controller validation)

**Evidence**
- `src/main/java/.../owner/PetController.java`
- `src/main/java/.../owner/PetTypeRepository.java` (`findPetTypes()`)
- `src/main/java/.../owner/PetValidator.java` (required fields)
- DB constraints:
    - H2: `src/main/resources/db/h2/schema.sql` (`unique_owner_pet_name UNIQUE (owner_id, name)`)
    - Postgres: `src/main/resources/db/postgres/schema.sql` (`unique_owner_pet_name ON pets (owner_id, LOWER(name))`)
- Template: `src/main/resources/templates/pets/createOrUpdatePetForm.html`

### C. Visit Recording (“Booking”)
- Create a visit for a pet (`/owners/{ownerId}/pets/{petId}/visits/new`)
- Visit has **date + description**
- Validation: visit date must be in the future (strictly after today)
- Displays previous visits in visit form

**Evidence**
- `src/main/java/.../owner/VisitController.java`
- `src/main/java/.../owner/Visit.java` (defaults date to tomorrow, requires `description`)
- Template: `src/main/resources/templates/pets/createOrUpdateVisitForm.html`

### D. Veterinarian Directory
- View paginated list of veterinarians and specialties (`/vets.html`)
- JSON endpoint `/vets` returning `Vets` wrapper object

**Evidence**
- `src/main/java/.../vet/VetController.java`
- `src/main/java/.../vet/VetRepository.java` (cached `findAll`)
- Template: `src/main/resources/templates/vets/vetList.html`

### E. Internationalization (i18n) Support
- Centralized message bundles and tests enforcing translation consistency and no hard-coded UI strings.

**Evidence**
- `src/main/resources/messages/messages*.properties`
- `src/test/java/.../system/I18nPropertiesSyncTest.java`

---

## 4) Main User Journeys (Current)

1. **Find owner**
    - User goes to “Find Owners” → enters last name → sees owners list (with paging) or gets redirected to the single match.
    - Evidence: `OwnerController.initFindForm()` + `OwnerController.processFindForm()`

2. **Register new owner**
    - Add Owner → submit owner form → redirect to owner details.
    - Evidence: `OwnerController.initCreationForm()` + `processCreationForm()`

3. **Update owner**
    - From owner details → Edit owner → submit.
    - Evidence: `OwnerController.initUpdateOwnerForm()` + `processUpdateOwnerForm()`

4. **Add pet**
    - From owner details → Add New Pet → submit pet form.
    - Evidence: `PetController.initCreationForm()` + `processCreationForm()`

5. **Edit pet**
    - From owner details → Edit Pet → submit pet form.
    - Evidence: `PetController.initUpdateForm()` + `processUpdateForm()`

6. **Add (book/record) visit**
    - From owner details → Add Visit for a pet → fill date + description → submit → returns to owner details.
    - Evidence: `VisitController.initNewVisitForm()` + `processNewVisitForm()`

7. **View vets**
    - Navigate to vets list page with pagination.
    - Evidence: `VetController.showVetList()`

---

## 5) Existing UI Functionality (Thymeleaf server-rendered)

### Navigation / Layout
Top-level navigation includes:
- Home
- Find owners
- Veterinarians
- Error

**Evidence**
- `src/main/resources/templates/fragments/layout.html` (navigation items)

### Key UI Screens (current)
- Owner search (`templates/owners/findOwners.html`)
- Owner list (`templates/owners/ownersList.html`) with pagination model values `currentPage`, `totalPages`, etc. (from `OwnerController.addPaginationModel()`)
- Owner details with pets and visits (`templates/owners/ownerDetails.html`)
- Owner create/update form (`templates/owners/createOrUpdateOwnerForm.html`)
- Pet create/update form (`templates/pets/createOrUpdatePetForm.html`)
- Visit create form + previous visits table (`templates/pets/createOrUpdateVisitForm.html`)
- Vets list (`templates/vets/vetList.html`) with pagination from `VetController`

Note: The repository search output for templates is partially abbreviated (headings and table skeletons). The presence of the templates and controller-to-view mappings is verifiable, but the full HTML content wasn’t fully displayed in the search excerpts.

---

## 6) Existing Backend Functionality

### Architecture pattern
- Spring MVC controllers + Thymeleaf views
- Spring Data JPA repositories for persistence
- Validation via Jakarta Bean Validation and custom validators
- SQL-based schema/data initialization (not Hibernate DDL auto)

**Evidence**
- `src/main/resources/application.properties`
    - `spring.jpa.hibernate.ddl-auto=none`
    - `spring.sql.init.schema-locations=classpath*:db/${database}/schema.sql`
    - `spring.sql.init.data-locations=classpath*:db/${database}/data.sql`

### Data access
- Owners: `OwnerRepository extends JpaRepository<Owner, Integer>` supports paging search and save/update.
- Pet types: `PetTypeRepository extends JpaRepository<PetType, Integer>` + JPQL query ordering.
- Vets: `VetRepository extends Repository<Vet, Integer>` with cached `findAll()` and paged `findAll(Pageable)`.

**Evidence**
- `src/main/java/.../owner/OwnerRepository.java`
- `src/main/java/.../owner/PetTypeRepository.java`
- `src/main/java/.../vet/VetRepository.java` (`@Cacheable("vets")`)

### Validation (existing)
- Owner: telephone must match `\d{10}`.
    - Evidence: `Owner.java` with `@Pattern(regexp="\\d{10}", message="{telephone.invalid}")`
- Pet: custom validation for name/type/birthDate required.
    - Evidence: `PetValidator.java`
- Visit:
    - `description` is `@NotBlank` in `Visit.java`
    - controller checks visit date must be after today: `VisitController.processNewVisitForm()`

---

## 7) Domain Entities and Relationships

### Entities
- `Owner` extends `Person` extends `BaseEntity`
- `Vet` extends `Person`
- `Pet` extends `NamedEntity` extends `BaseEntity`
- `PetType` extends `NamedEntity`
- `Visit` extends `BaseEntity`
- `Specialty` extends `NamedEntity`

**Evidence**
- `src/main/java/.../model/BaseEntity.java`, `Person.java`, `NamedEntity.java`
- `src/main/java/.../owner/Owner.java`, `Pet.java`, `PetType.java`, `Visit.java`
- `src/main/java/.../vet/Vet.java`, `Specialty.java`

### Relationships (as implemented)
- **Owner 1 → N Pets**: `@OneToMany(cascade=ALL, fetch=EAGER)` with `@JoinColumn(owner_id)` in `Owner.java`
- **Pet N → 1 PetType**: `@ManyToOne` with `@JoinColumn(type_id)` in `Pet.java`
- **Pet 1 → N Visits**: `@OneToMany(cascade=ALL, fetch=EAGER)` with `@JoinColumn(pet_id)` in `Pet.java`
- **Vet N ↔ N Specialty**: `@ManyToMany(fetch=EAGER)` via `vet_specialties` join table in `Vet.java`

Key behavior helpers:
- `Owner.addVisit(petId, visit)` adds a visit onto a selected pet (`Owner.java`)
- `Owner.getPet(...)` by id/name with ignoreNew option (`Owner.java`)
- `Pet.addVisit(visit)` (`Pet.java`)

---

## 8) Database Structure / Scripts

### Initialization approach
DB is initialized from SQL scripts depending on `database` property (default `h2`).

**Evidence**
- `src/main/resources/application.properties` with `database=h2` and `spring.sql.init.*` locations.

### Tables (H2 schema representative)
- `owners` (first_name, last_name, address, city, telephone)
- `pets` (name, birth_date, type_id, owner_id) + unique(owner_id, name)
- `visits` (pet_id, visit_date, description)
- `types` (pet type)
- `vets` (first_name, last_name)
- `specialties`
- `vet_specialties` join table

**Evidence**
- `src/main/resources/db/h2/schema.sql`
- Equivalent variants:
    - `src/main/resources/db/mysql/schema.sql`
    - `src/main/resources/db/postgres/schema.sql`

### Seed data
Seed owners/pets/visits/vets/specialties/pet types exist.

**Evidence**
- `src/main/resources/db/h2/data.sql`
- `src/main/resources/db/mysql/data.sql`
- `src/main/resources/db/postgres/data.sql`

---

## 9) Existing Tests (What is covered)

### MVC / Controller tests
- Extensive MockMvc tests for OwnerController behavior (create, validation errors, search behaviors incl whitespace trimming, update, view owner page).
  **Evidence**
- `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

### Repository / “service” tests (integration style)
Despite the name, `ClinicServiceTests` verifies repository behavior and entity persistence:
- find owners by last name
- insert/update owner
- pet types retrieval
- insert pet and generate id
- update pet
- find vets & specialties
- add visit
- verify visits by pet id
- enforce duplicate pet name constraint per owner
  **Evidence**
- `src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`

### Full application integration tests
- Boot app, call HTTP endpoints and verify OK
- VetRepository caching behavior invoked
  **Evidence**
- `src/test/java/org/springframework/samples/petclinic/PetClinicIntegrationTests.java`

### MySQL Testcontainers integration
- Runs against MySQL container (if Docker available)
  **Evidence**
- `src/test/java/org/springframework/samples/petclinic/MySqlIntegrationTests.java`

### i18n governance tests
- Ensures no hard-coded strings in HTML and translation keys are in sync
  **Evidence**
- `src/test/java/org/springframework/samples/petclinic/system/I18nPropertiesSyncTest.java`

---

## 10) Current Functional Limitations (Verified vs. repo)

1. **No authentication / authorization**
    - There are no login flows or role protections in the inspected controllers and build dependencies (no Spring Security shown in inspected build file excerpt earlier; not re-confirmed here but consistent with controller simplicity).
    - Evidence: controllers expose create/edit endpoints directly (`OwnerController`, `PetController`, `VisitController`) with no security annotations/config observed.

2. **Visits are date-only, not time-based, and not assigned to a vet**
    - Limits scheduling realism; cannot prevent double booking.
    - Evidence:
        - `Visit.java`: only `LocalDate date` and `description`
        - DB schema `visits.visit_date DATE` with no time fields (`db/**/schema.sql`)
        - Visit flow binds a Visit to a Pet only: `VisitController.loadPetWithVisit()` and `owner.addVisit(petId, visit)`

3. **No visit editing/cancellation**
    - Only “new visit” endpoint exists.
    - Evidence: `VisitController` only has `/visits/new` GET+POST mappings.

4. **Owner search limited to last name prefix**
    - No search by phone/city/address/pet name.
    - Evidence: `OwnerRepository.findByLastNameStartingWith(...)`; UI has “Last name” only (`templates/owners/findOwners.html`).

5. **No administrative UI for reference data**
    - Pet types and specialties exist but only as DB seed and selection lists.
    - Evidence:
        - `PetTypeRepository.findPetTypes()`
        - No controllers/templates found for managing `types` or `specialties` in inspected tree.

6. **Delete operations not supported**
    - No “delete owner/pet/visit” endpoints or repository usage.
    - Evidence: controllers show create/edit/view; no delete mappings in inspected controllers.

7. **EAGER fetching for pets and visits**
    - This is more technical, but functionally may degrade performance as data grows (owner always loads all pets; pet always loads all visits).
    - Evidence: `fetch = FetchType.EAGER` on `Owner.pets` and `Pet.visits`.

---

## 11) User Pain Points

1. **Front desk cannot manage real appointment slots**
    - A “visit” is basically a dated note; there’s no time slot, vet selection, or conflict detection.
    - Evidence: `Visit.java`, `VisitController`, `visits` table definition.

2. **Record lookup is slow/limited**
    - If the customer calls in, staff typically searches by phone; not possible here.
    - Evidence: only last name prefix search exists (`OwnerRepository`, `findOwners.html`).

3. **No cancel/reschedule workflow**
    - Mistakes require database edits or workaround by adding new visits.
    - Evidence: no edit/delete endpoints for visits (`VisitController`).

4. **Admin overhead for maintaining reference lists**
    - Adding a new pet type requires DB-level changes.
    - Evidence: types are seeded via `db/**/data.sql`; no UI management.

5. **Data governance risk**
    - Without access control, anyone can change owners/pets/visits.
    - Evidence: no security layer observed around modifying endpoints.

---

## 12) Business Gaps (What prevents “real clinic” use)

1. **Scheduling & capacity management gap**
    - No vet assignment, no time slots, no daily schedule view.
2. **Security & accountability gap**
    - No user accounts/roles; no ability to separate receptionist vs vet actions.
3. **Operational reporting gap**
    - No “upcoming visits”, “today’s appointments”, “visits per vet”, etc.
4. **Master data management gap**
    - No admin UI for pet types/specialties.
5. **Customer service efficiency gap**
    - Limited search makes quick service difficult and increases duplicate records risk.

---

# TOP 5 Realistic Enhancement Opportunities (Analysis)

## Enhancement 1: Time-based Appointment Scheduling (Visits with Time Slot) + Conflict Checking

- **Problem:** Current “visit booking” cannot represent real appointments or prevent double-booking.
- **Affected User:** Receptionist, Veterinarian, Manager
- **Current State:** Visit has only `LocalDate date` and `description`; visit created under a pet, no vet/time slot.
- **Repository Evidence:**
    - `src/main/java/.../owner/Visit.java` (fields: `date`, `description`)
    - `src/main/resources/db/*/schema.sql` (`visits.visit_date DATE`)
    - `src/main/java/.../owner/VisitController.java` adds visit to pet and validates date > today
- **Gap:** No time-of-day, no vet assignment, no schedule view, no conflicts prevention.
- **Proposed Capability:** Add appointment time (e.g., start time / start datetime) and basic conflict validation (at least per pet or per vet if vet selection is added). Provide a simple “daily schedule” list view.
- **Business Value:** Enables realistic clinic scheduling; reduces booking errors; improves clinic throughput and customer satisfaction.
- **User Benefit:** Faster, more reliable booking; clear view of upcoming appointments.
- **Priority:** HIGH
- **Estimated Complexity:** MEDIUM–HIGH (DB changes across H2/MySQL/Postgres; UI + controller validation)
- **Dependencies:** DB schema/data scripts; `Visit` model; visit form template; likely new repository/query support for conflict checks.
- **Risks:** Migration/compatibility with existing date-only data; edge cases (timezone if using datetime); additional UI complexity.
- **Recommended MVP Scope:**
    - Add time-of-day to visit (start time)
    - Update visit form to capture time
    - Basic conflict check (e.g., prevent duplicate same pet/time; optionally add vet selection if included)
    - Add “Upcoming appointments (today/tomorrow)” read-only page

---

## Enhancement 2: Enhanced Owner Search (Telephone/City/Pet Name)

- **Problem:** Owners can only be found by last name prefix; inefficient and error-prone.
- **Affected User:** Receptionist, Veterinarian
- **Current State:** Search is `findByLastNameStartingWith` only; UI asks for “Last name”.
- **Repository Evidence:**
    - `src/main/java/.../owner/OwnerRepository.java` (`findByLastNameStartingWith`)
    - `src/main/resources/templates/owners/findOwners.html` includes “Last name”
    - `OwnerController.processFindForm()` uses only `owner.getLastName()`
- **Gap:** Cannot search by telephone (common real workflow) or by pet name.
- **Proposed Capability:** Extend search form and results to support telephone and/or pet name (and optionally city).
- **Business Value:** Faster lookup reduces front-desk time and duplicate record creation.
- **User Benefit:** Locate the right customer record quickly even with common last names.
- **Priority:** HIGH (practical day-to-day impact)
- **Estimated Complexity:** MEDIUM (repository query changes + UI changes + controller branching)
- **Dependencies:** New repository methods / JPQL queries; update templates for search fields and results display.
- **Risks:** Result ambiguity if multiple filters; performance of pet-name join query (manageable for capstone).
- **Recommended MVP Scope:**
    - Add telephone search (exact match) and pet name (contains/starts-with) as optional fields
    - Keep last name search as-is; if multiple fields provided, apply precedence rules (analysis-level: define with PO)

---

## Enhancement 3: Visit Management Improvements (Edit/Cancel/Reschedule)

- **Problem:** Mistakes cannot be corrected; no cancel/reschedule flow.
- **Affected User:** Receptionist, Veterinarian
- **Current State:** Only “new visit” endpoint exists.
- **Repository Evidence:**
    - `src/main/java/.../owner/VisitController.java` only maps `/visits/new`
    - Owner details UI shows visits but no visit edit/cancel actions (template skeleton: `ownerDetails.html`)
- **Gap:** No lifecycle management of visits.
- **Proposed Capability:** Allow editing a visit (date/description) and/or canceling (soft-delete/flag) from pet/owner context.
- **Business Value:** Reduces data errors; supports real operational adjustments.
- **User Benefit:** Staff can fix scheduling/recording mistakes without workarounds.
- **Priority:** MEDIUM
- **Estimated Complexity:** MEDIUM (new endpoints + UI actions; possibly new persistence patterns)
- **Dependencies:** UI changes in owner details; controller endpoints; possibly new repository or modify persistence approach (visits currently persisted via owner save cascade).
- **Risks:** Data integrity if deleting visits; audit/history considerations.
- **Recommended MVP Scope:**
    - Add “Edit Visit” (date + description) only
    - Defer cancellation/delete until governance is agreed

---

## Enhancement 4: Admin UI for Reference Data (Pet Types, Specialties)

- **Problem:** Pet types and specialties are static seed data; no operational way to update.
- **Affected User:** Manager / Admin
- **Current State:** Types come from `types` table and are loaded into pet forms; specialties exist for vets.
- **Repository Evidence:**
    - `src/main/java/.../owner/PetTypeRepository.java` (`findPetTypes`)
    - `src/main/resources/db/*/data.sql` inserts into `types` and `specialties`
    - No admin controllers/templates found in inspected tree for managing these
- **Gap:** Requires DB changes to update lists; non-technical users can’t maintain data.
- **Proposed Capability:** Add basic admin pages to list/add/rename pet types and specialties.
- **Business Value:** Keeps master data accurate; enables clinic growth and consistency.
- **User Benefit:** Admin updates lists without DB access.
- **Priority:** MEDIUM
- **Estimated Complexity:** MEDIUM (CRUD UI + validation; keep simple)
- **Dependencies:** New controllers and templates; validation and “in use” checks.
- **Risks:** Deleting types/specialties could break existing records (prefer deactivate/rename only).
- **Recommended MVP Scope:**
    - Pet Type management: list + add + rename
    - Defer delete; optionally add “inactive” later

---

## Enhancement 5: Basic Role-Based Access Control (Staff Login)

- **Problem:** Anyone can modify data; not viable for real clinic operations.
- **Affected User:** Manager, Receptionist, Vet
- **Current State:** No security layer observed; modifying endpoints are open.
- **Repository Evidence:**
    - Modifying endpoints exist without security gates: `OwnerController`, `PetController`, `VisitController`
    - No security configuration classes were found in inspected sources; no login UI templates were referenced.
- **Gap:** No separation of duties or protection against unauthorized edits.
- **Proposed Capability:** Add login and restrict create/edit actions to staff roles.
- **Business Value:** Reduces operational/compliance risk; enables multi-user deployment.
- **User Benefit:** Clear permitted actions; prevents accidental/unauthorized changes.
- **Priority:** HIGH (for production realism), but may be heavy for capstone scope
- **Estimated Complexity:** HIGH (cross-cutting change impacting controllers, views, and testing approach)
- **Dependencies:** Security framework integration; user store; template updates to hide/show actions.
- **Risks:** Scope creep (password policies, user admin, logout flows, test refactors).
- **Recommended MVP Scope:**
    - Simple login + 1–2 roles (e.g., STAFF vs ADMIN)
    - Protect create/edit endpoints; keep read-only pages accessible to STAFF

---

## Enhancement Comparison Table

| Enhancement | Business Value | User Impact | Complexity | Risk | Priority |
|---|---:|---:|---:|---:|---|
| Time-based Appointment Scheduling + conflict checking | Very High | Very High | Med–High | Med | HIGH |
| Enhanced Owner Search (telephone/pet name) | High | High | Medium | Low–Med | HIGH |
| Visit Edit/Reschedule | Medium | Medium–High | Medium | Medium | MEDIUM |
| Admin UI for Pet Types/Specialties | Medium | Medium | Medium | Medium | MEDIUM |
| Role-Based Access Control (login/roles) | High | Medium–High | High | High | HIGH |

---

# Recommended ONE Enhancement for Capstone MVP (Proposal for Human Approval)

## Proposal: **Enhanced Owner Search (Telephone + Pet Name)**
(Recommended as the most feasible, high-value capstone MVP with contained scope.)

### 1) Why this enhancement should be selected
- It directly improves the most common front-desk workflow: quickly finding the right record.
- It is **incremental** (extends existing “Find Owners” journey) without requiring major schema changes.

### 2) Business value
- Reduces time per customer interaction.
- Reduces risk of creating duplicate owners when staff can’t find an existing record.

### 3) User impact
- Receptionists can search the way customers actually identify themselves (phone number) or by pet name.

### 4) Feasibility within the existing application
- The application already has:
    - a search page (`/owners/find`) and list page (`owners/ownersList`)
    - a repository-based query pattern for search (`OwnerRepository.findByLastNameStartingWith`)
    - controller logic for pagination and redirect-on-single-result (`OwnerController.processFindForm`)
- This enhancement primarily extends those existing points.

### 5) Existing repository components related to the enhancement
- `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
- `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
- `src/main/resources/templates/owners/findOwners.html`
- `src/main/resources/templates/owners/ownersList.html`
- Potentially `Owner`/`Pet` mappings for pet-name search joins:
    - `src/main/java/.../owner/Owner.java` (pets collection)
    - `src/main/java/.../owner/Pet.java` (name)

### 6) Recommended MVP scope
- Update “Find Owners” UI to allow:
    - Telephone (exact match)
    - Pet name (starts-with or contains)
    - Keep last name prefix search
- Update results page to clearly show matches and avoid confusion when multiple owners match.

### 7) What should be deferred
- Advanced filtering (address, city, combined boolean logic with many fields)
- Fuzzy matching / normalization / phonetic search
- Global search across visits/description
- Performance tuning beyond basic indexing considerations (likely unnecessary at capstone scale)

**Human-in-the-loop note:** This MVP recommendation is a proposal. A Product Owner/BA should confirm whether the capstone goal prioritizes “operational realism” (in which case appointment scheduling may be preferred) versus “high-impact, contained change” (search enhancement).

If you want, I can provide an alternate MVP recommendation focused on *appointment scheduling* and clearly define a “minimal viable” version that avoids vet assignment and avoids large UI redesign—still analysis-only.
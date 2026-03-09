# README

Generated on: 2026-03-10
Repository root: D:\projects\major-proj

## Scan Scope
- This report scans repository-owned files and separates source code from external dependency caches.
- Total files physically present in repository tree: 44,198.
- Project-owned files selected for documentation-grade analysis: 167.
- Major runtime modules identified:
- `MainProject` (Spring Boot + Spring Security + Spring AI + PostgreSQL/pgvector).
- `frontend` (Next.js 15 client with unified dashboard workflow).
- `new-frontend` (Next.js 14 client with route-separated workflow and legacy root artifacts).

## Methodology
- Step 1: Enumerate file inventory across all top-level folders.
- Step 2: Filter dependency/build caches to isolate authored files.
- Step 3: Inspect backend configuration, controllers, services, entities, repositories, DTOs, and exceptions.
- Step 4: Inspect both frontend stacks (routes, components, API clients, and style systems).
- Step 5: Derive architecture layers, workflow boundaries, strengths, and limitations.
- Step 6: Produce per-file traceability ledger for documentation teams.

## Architecture Snapshot
- Architecture style: layered monolith backend + one or more Next.js frontends.
- Backend ingress: REST routes under `/api/*`.
- Security boundary: JWT access/refresh tokens stored as HttpOnly cookies; header fallback supported.
- Data boundary: PostgreSQL entities persisted via Spring Data JPA.
- Retrieval boundary: pgvector similarity search with metadata filter by `userId`.
- AI boundary: Spring AI `ChatClient` and custom HuggingFace embedding model.
- Memory boundary: chat history persisted in database-backed `JpaChatMemory`.
- Assessment boundary: model-generated MCQ JSON persisted as `TestSession -> TestQuestion -> QuestionOption`.
- Frontend boundary: typed API wrappers with automatic token refresh on `401`.

## End-to-End Workflow Summary
- Workflow 01: User authenticates via register/login endpoints.
- Workflow 02: Backend sets cookie tokens and returns profile envelope.
- Workflow 03: Frontend bootstraps session by calling `/api/auth/me`.
- Workflow 04: Protected routes enforce authenticated-only access.
- Workflow 05: User creates a conversation with selected difficulty.
- Workflow 06: User sends message to `/api/chat/{conversationId}`.
- Workflow 07: Backend verifies conversation ownership.
- Workflow 08: Backend performs vector search over user-scoped chunks.
- Workflow 09: Backend builds prompt with difficulty and optional RAG context.
- Workflow 10: Model response is stored through persistent chat memory.
- Workflow 11: Document upload triggers extraction, chunking, embedding, and vector insertion.
- Workflow 12: Test generation prompts model for strict JSON MCQs.
- Workflow 13: Test submission computes score and stores selected answers.
- Workflow 14: Frontend renders results and explanations from persisted data.

## Advantages
- Advantage 01: Consistent `ApiResponse<T>` envelope simplifies frontend error/data handling.
- Advantage 02: Clear layering from controller to repository supports maintainability.
- Advantage 03: Retrieval-augmented chat is integrated into normal conversation flow.
- Advantage 04: User-level metadata filters improve context relevance and data isolation.
- Advantage 05: JWT refresh flow improves long-session UX.
- Advantage 06: Typed frontend contracts reduce payload mismatch defects.
- Advantage 07: Document + test modules create a full learning loop (learn -> test -> review).
- Advantage 08: Both frontends demonstrate reusable patterns for route guards and API clients.

## Limitations
- Limitation 01: Dual frontend stacks can drift and increase maintenance cost.
- Limitation 02: Duplicate legacy root files exist in `new-frontend`.
- Limitation 03: Test coverage is minimal (`contextLoads` only in backend tests).
- Limitation 04: Some components/services are large and carry multiple concerns.
- Limitation 05: Runtime logs and PID artifacts are present in source tree.
- Limitation 06: Security/env hardening remains necessary for production deployment.

## Recommended Documentation Pack
- Chapter A: Product and capability overview.
- Chapter B: Architecture and deployment topology.
- Chapter C: API reference with envelope examples.
- Chapter D: Authentication and session lifecycle.
- Chapter E: Conversation and RAG workflow sequence.
- Chapter F: Document ingestion and retrieval internals.
- Chapter G: Assessment generation and scoring lifecycle.
- Chapter H: Data model dictionary and table relationships.
- Chapter I: Frontend route map and component ownership.
- Chapter J: Known limitations and technical debt register.

## Per-File Traceability Ledger
- Every authored file is indexed below with role, strengths, and limitations.

### File 0001: `.idea\.gitignore`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .gitignore
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 1

### File 0002: `.idea\artifacts\M2_jar.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 185
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 2

### File 0003: `.idea\compiler.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 23
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 3

### File 0004: `.idea\encodings.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 6
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 4

### File 0005: `.idea\jarRepositories.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 20
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 5

### File 0006: `.idea\M2.iml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .iml
- Approximate text lines: 9
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 6

### File 0007: `.idea\misc.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 14
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 7

### File 0008: `.idea\modules.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 9
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 8

### File 0009: `.idea\vcs.xml`
- Module: .idea (IDE metadata)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 6
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 9

### File 0010: `1.pdf`
- Module: Repository root artifact
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .pdf
- Approximate text lines: 11380
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 10

### File 0011: `files.zip`
- Module: Repository root artifact
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .zip
- Approximate text lines: 269
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 11

### File 0012: `frontend\.env.example`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .example
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 12

### File 0013: `frontend\.gitignore`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .gitignore
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 13

### File 0014: `frontend\app\dashboard\page.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Route / View (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 9
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 14

### File 0015: `frontend\app\globals.css`
- Module: frontend (Next.js 15 client)
- Category: Frontend Route / View (frontend)
- Architectural layer: Presentation layer
- File extension: .css
- Approximate text lines: 36
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 15

### File 0016: `frontend\app\layout.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Route / View (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 16
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 16

### File 0017: `frontend\app\login\page.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Route / View (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 101
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 17

### File 0018: `frontend\app\page.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Route / View (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 114
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 18

### File 0019: `frontend\app\register\page.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Route / View (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 132
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 19

### File 0020: `frontend\components\auth-provider.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Component (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 99
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Reusable components improve maintainability and visual consistency.
- Limitation/risk note: Large UI files increase cognitive load for onboarding and review.
- Documentation action note: Document props and composition intent.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 20

### File 0021: `frontend\components\dashboard-client.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Component (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 911
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Reusable components improve maintainability and visual consistency.
- Limitation/risk note: Large UI files increase cognitive load for onboarding and review.
- Documentation action note: Document props and composition intent.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 21

### File 0022: `frontend\components\dashboard-client-old.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Component (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 870
- Status classification: Legacy or duplicate candidate
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Reusable components improve maintainability and visual consistency.
- Limitation/risk note: Potentially duplicated implementation path; can create maintenance drift.
- Documentation action note: Document props and composition intent.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 22

### File 0023: `frontend\components\protected-page.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend Component (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 30
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Reusable components improve maintainability and visual consistency.
- Limitation/risk note: Large UI files increase cognitive load for onboarding and review.
- Documentation action note: Document props and composition intent.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 23

### File 0024: `frontend\components\ui\badge.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 31
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 24

### File 0025: `frontend\components\ui\button.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 51
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 25

### File 0026: `frontend\components\ui\card.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 71
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 26

### File 0027: `frontend\components\ui\input.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 21
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 27

### File 0028: `frontend\components\ui\label.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 20
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 28

### File 0029: `frontend\components\ui\scroll-area.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 42
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 29

### File 0030: `frontend\components\ui\select.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 145
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 30

### File 0031: `frontend\components\ui\tabs.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 47
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 31

### File 0032: `frontend\components\ui\textarea.tsx`
- Module: frontend (Next.js 15 client)
- Category: Frontend UI Primitive (frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 20
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 32

### File 0033: `frontend\lib\api.ts`
- Module: frontend (Next.js 15 client)
- Category: Frontend Utility Library (frontend)
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 180
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 33

### File 0034: `frontend\lib\types.ts`
- Module: frontend (Next.js 15 client)
- Category: Frontend Utility Library (frontend)
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 103
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 34

### File 0035: `frontend\lib\utils.ts`
- Module: frontend (Next.js 15 client)
- Category: Frontend Utility Library (frontend)
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 5
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 35

### File 0036: `frontend\next.config.ts`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 5
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 36

### File 0037: `frontend\next-env.d.ts`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 4
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 37

### File 0038: `frontend\package.json`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .json
- Approximate text lines: 40
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 38

### File 0039: `frontend\package-lock.json`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .json
- Approximate text lines: 7185
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 39

### File 0040: `frontend\postcss.config.mjs`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .mjs
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 40

### File 0041: `frontend\README.md`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .md
- Approximate text lines: 17
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 41

### File 0042: `frontend\tailwind.config.ts`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 55
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 42

### File 0043: `frontend\tsconfig.json`
- Module: frontend (Next.js 15 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .json
- Approximate text lines: 27
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 43

### File 0044: `generate_systematic_doc.ps1`
- Module: Repository root artifact
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ps1
- Approximate text lines: 273
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 44

### File 0045: `-H`
- Module: Repository root artifact
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: [noext]
- Approximate text lines: 0
- Status classification: Repository artifact
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 45

### File 0046: `MainProject\.gitattributes`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .gitattributes
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 46

### File 0047: `MainProject\.gitignore`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .gitignore
- Approximate text lines: 30
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 47

### File 0048: `MainProject\.mvn\wrapper\maven-wrapper.properties`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .properties
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 48

### File 0049: `MainProject\backend.err.log`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .log
- Approximate text lines: 0
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 49

### File 0050: `MainProject\backend.out.log`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .log
- Approximate text lines: 0
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 50

### File 0051: `MainProject\backend.pid`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .pid
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 51

### File 0052: `MainProject\docker-compose.yml`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .yml
- Approximate text lines: 22
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 52

### File 0053: `MainProject\Dockerfile`
- Module: MainProject (Spring Boot backend)
- Category: Backend Container Definition
- Architectural layer: Infrastructure layer
- File extension: [noext]
- Approximate text lines: 5
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 53

### File 0054: `MainProject\mvnw`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: [noext]
- Approximate text lines: 263
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 54

### File 0055: `MainProject\mvnw.cmd`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .cmd
- Approximate text lines: 174
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 55

### File 0056: `MainProject\pom.xml`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .xml
- Approximate text lines: 176
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 56

### File 0057: `MainProject\src\main\java\com\auth\JwtUtil.java`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 57

### File 0058: `MainProject\src\main\java\com\config\HuggingFaceEmbeddingModel.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Configuration / Security / AI Wiring
- Architectural layer: Infrastructure layer
- File extension: .java
- Approximate text lines: 56
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Centralized wiring separates infrastructure concerns from business logic.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 58

### File 0059: `MainProject\src\main\java\com\config\JpaChatMemory.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Configuration / Security / AI Wiring
- Architectural layer: Infrastructure layer
- File extension: .java
- Approximate text lines: 99
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Centralized wiring separates infrastructure concerns from business logic.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 59

### File 0060: `MainProject\src\main\java\com\config\JwtAuthenticationFilter.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Configuration / Security / AI Wiring
- Architectural layer: Infrastructure layer
- File extension: .java
- Approximate text lines: 74
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Centralized wiring separates infrastructure concerns from business logic.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 60

### File 0061: `MainProject\src\main\java\com\config\JwtConfig.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Configuration / Security / AI Wiring
- Architectural layer: Infrastructure layer
- File extension: .java
- Approximate text lines: 84
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Centralized wiring separates infrastructure concerns from business logic.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 61

### File 0062: `MainProject\src\main\java\com\config\SecurityConfig.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Configuration / Security / AI Wiring
- Architectural layer: Infrastructure layer
- File extension: .java
- Approximate text lines: 91
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Centralized wiring separates infrastructure concerns from business logic.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 62

### File 0063: `MainProject\src\main\java\com\config\SpringAIConfig.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Configuration / Security / AI Wiring
- Architectural layer: Infrastructure layer
- File extension: .java
- Approximate text lines: 16
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Centralized wiring separates infrastructure concerns from business logic.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 63

### File 0064: `MainProject\src\main\java\com\controller\AuthController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 76
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 64

### File 0065: `MainProject\src\main\java\com\controller\ChatController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 33
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 65

### File 0066: `MainProject\src\main\java\com\controller\ConversationController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 82
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 66

### File 0067: `MainProject\src\main\java\com\controller\DocumentController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 88
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 67

### File 0068: `MainProject\src\main\java\com\controller\OpenApiFallbackController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 57
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 68

### File 0069: `MainProject\src\main\java\com\controller\PerformanceController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 69

### File 0070: `MainProject\src\main\java\com\controller\TestController.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend API Controller
- Architectural layer: Presentation layer
- File extension: .java
- Approximate text lines: 54
- Status classification: Active code/config context
- Workflow touchpoint: HTTP ingress and request lifecycle entrypoint.
- Strength contribution: Clear API surface with structured envelope responses improves client consistency.
- Limitation/risk note: Controller evolution may require explicit versioning strategy over time.
- Documentation action note: Document endpoint contract, auth requirement, and representative error examples.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 70

### File 0071: `MainProject\src\main\java\com\dto\ApiResponse.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 31
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 71

### File 0072: `MainProject\src\main\java\com\dto\AuthResponse.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 20
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 72

### File 0073: `MainProject\src\main\java\com\dto\ChatRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 73

### File 0074: `MainProject\src\main\java\com\dto\ChatResponse.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 17
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 74

### File 0075: `MainProject\src\main\java\com\dto\ConversationRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 11
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 75

### File 0076: `MainProject\src\main\java\com\dto\ConversationResponse.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 18
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 76

### File 0077: `MainProject\src\main\java\com\dto\GenerateTestRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 15
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 77

### File 0078: `MainProject\src\main\java\com\dto\LoginRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 12
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 78

### File 0079: `MainProject\src\main\java\com\dto\MessageResponse.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 16
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 79

### File 0080: `MainProject\src\main\java\com\dto\RefreshTokenRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 80

### File 0081: `MainProject\src\main\java\com\dto\RegisterRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 18
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 81

### File 0082: `MainProject\src\main\java\com\dto\SubmitTestRequest.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 9
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 82

### File 0083: `MainProject\src\main\java\com\dto\UserProfileResponse.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend DTO / API Contract
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 17
- Status classification: Active code/config context
- Workflow touchpoint: Contract boundary between service layers and clients.
- Strength contribution: Typed payload contracts reduce integration ambiguity.
- Limitation/risk note: Contract changes need compatibility planning across frontend clients.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 83

### File 0084: `MainProject\src\main\java\com\exception\ApiException.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Error Handling
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 12
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Uniform error envelope handling improves frontend resilience.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 84

### File 0085: `MainProject\src\main\java\com\exception\BadRequestException.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Error Handling
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 7
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Uniform error envelope handling improves frontend resilience.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 85

### File 0086: `MainProject\src\main\java\com\exception\ConflictException.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Error Handling
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 7
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Uniform error envelope handling improves frontend resilience.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 86

### File 0087: `MainProject\src\main\java\com\exception\ForbiddenException.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Error Handling
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 7
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Uniform error envelope handling improves frontend resilience.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 87

### File 0088: `MainProject\src\main\java\com\exception\GlobalExceptionHandler.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Error Handling
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 100
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Uniform error envelope handling improves frontend resilience.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 88

### File 0089: `MainProject\src\main\java\com\exception\NotFoundException.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Error Handling
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 7
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Uniform error envelope handling improves frontend resilience.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 89

### File 0090: `MainProject\src\main\java\com\MainProjectApplication.java`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 17
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 90

### File 0091: `MainProject\src\main\java\com\model\Conversation.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 51
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 91

### File 0092: `MainProject\src\main\java\com\model\Document.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 61
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 92

### File 0093: `MainProject\src\main\java\com\model\Message.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 39
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 93

### File 0094: `MainProject\src\main\java\com\model\QuestionOption.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 26
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 94

### File 0095: `MainProject\src\main\java\com\model\TestQuestion.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 37
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 95

### File 0096: `MainProject\src\main\java\com\model\TestSession.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 68
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 96

### File 0097: `MainProject\src\main\java\com\model\User.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend JPA Entity
- Architectural layer: Domain contract layer
- File extension: .java
- Approximate text lines: 64
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Explicit data model supports traceable persistence and relationship clarity.
- Limitation/risk note: Lazy-loading and serialization edges require disciplined DTO usage.
- Documentation action note: Document table mapping, relationships, and enum semantics.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 97

### File 0098: `MainProject\src\main\java\com\repositry\ConversationRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 13
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 98

### File 0099: `MainProject\src\main\java\com\repositry\DocumentRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 12
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 99

### File 0100: `MainProject\src\main\java\com\repositry\MessageRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 12
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 100

### File 0101: `MainProject\src\main\java\com\repositry\TestQuestionRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 9
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 101

### File 0102: `MainProject\src\main\java\com\repositry\TestRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 13
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 102

### File 0103: `MainProject\src\main\java\com\repositry\TestSessionRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 13
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 103

### File 0104: `MainProject\src\main\java\com\repositry\UserRepository.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Repository Interface
- Architectural layer: Data access layer
- File extension: .java
- Approximate text lines: 11
- Status classification: Active code/config context
- Workflow touchpoint: Persistence lifecycle and database state transitions.
- Strength contribution: Declarative query methods reduce boilerplate in data access.
- Limitation/risk note: Repository method growth can become hard to govern without conventions.
- Documentation action note: Document query purpose and expected indexing strategy.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 104

### File 0105: `MainProject\src\main\java\com\service\AuthService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 140
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 105

### File 0106: `MainProject\src\main\java\com\service\ChatService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 102
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 106

### File 0107: `MainProject\src\main\java\com\service\ConversationService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 86
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 107

### File 0108: `MainProject\src\main\java\com\service\CustomUserDetailsService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 31
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 108

### File 0109: `MainProject\src\main\java\com\service\DocumentService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 286
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 109

### File 0110: `MainProject\src\main\java\com\service\HuggingFaceEmbeddingService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 71
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 110

### File 0111: `MainProject\src\main\java\com\service\TestService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 253
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 111

### File 0112: `MainProject\src\main\java\com\service\UserService.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Domain Service
- Architectural layer: Application layer
- File extension: .java
- Approximate text lines: 3
- Status classification: Active code/config context
- Workflow touchpoint: Business orchestration and cross-component workflow engine.
- Strength contribution: Core logic is centralized, making behavior easier to reason about.
- Limitation/risk note: Some services are large and multi-responsibility, increasing change risk.
- Documentation action note: Document orchestration steps, side effects, and transaction scope.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 112

### File 0113: `MainProject\src\main\resources\application.properties`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .properties
- Approximate text lines: 78
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 113

### File 0114: `MainProject\src\main\resources\META-INF\MANIFEST.MF`
- Module: MainProject (Spring Boot backend)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .MF
- Approximate text lines: 2
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 114

### File 0115: `MainProject\src\test\java\com\MajorProject\MainProject\MainProjectApplicationTests.java`
- Module: MainProject (Spring Boot backend)
- Category: Backend Test
- Architectural layer: Support layer
- File extension: .java
- Approximate text lines: 9
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Current test depth is limited, reducing regression detection confidence.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 115

### File 0116: `new-frontend\.env.local`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .local
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 116

### File 0117: `new-frontend\.gitignore`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .gitignore
- Approximate text lines: 16
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 117

### File 0118: `new-frontend\api.ts`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 117
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 118

### File 0119: `new-frontend\app\chat\[id]\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 282
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 119

### File 0120: `new-frontend\app\chat\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 120

### File 0121: `new-frontend\app\chat\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 226
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 121

### File 0122: `new-frontend\app\dashboard\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 122

### File 0123: `new-frontend\app\dashboard\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 400
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 123

### File 0124: `new-frontend\app\documents\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 124

### File 0125: `new-frontend\app\documents\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 248
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 125

### File 0126: `new-frontend\app\globals.css`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .css
- Approximate text lines: 490
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 126

### File 0127: `new-frontend\app\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 20
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 127

### File 0128: `new-frontend\app\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 10
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 128

### File 0129: `new-frontend\app\tests\[id]\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 305
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 129

### File 0130: `new-frontend\app\tests\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 8
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 130

### File 0131: `new-frontend\app\tests\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Route / View (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 307
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Route-based decomposition maps UI concerns to concrete user journeys.
- Limitation/risk note: Client-heavy pages can become hard to maintain as feature scope grows.
- Documentation action note: Document user goals, interactions, and backend calls made from page.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 131

### File 0132: `new-frontend\components\auth\AuthForm.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Auth Page Component (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 318
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 132

### File 0133: `new-frontend\components\dashboard\SectionCard.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Dashboard Component (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 62
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 133

### File 0134: `new-frontend\components\dashboard\StatTile.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Dashboard Component (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 21
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 134

### File 0135: `new-frontend\components\layout\ProtectedLayout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Layout Component (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 19
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 135

### File 0136: `new-frontend\components\layout\Sidebar.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Layout Component (new-frontend)
- Architectural layer: Presentation layer
- File extension: .tsx
- Approximate text lines: 172
- Status classification: Active code/config context
- Workflow touchpoint: User interaction lifecycle and front-end state management.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 136

### File 0137: `new-frontend\dev-temp.err.log`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .log
- Approximate text lines: 0
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 137

### File 0138: `new-frontend\dev-temp.out.log`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .log
- Approximate text lines: 0
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 138

### File 0139: `new-frontend\globals.css`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .css
- Approximate text lines: 444
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 139

### File 0140: `new-frontend\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 20
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 140

### File 0141: `new-frontend\lib\api.ts`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Utility Library (new-frontend)
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 153
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 141

### File 0142: `new-frontend\lib\server-auth.ts`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Utility Library (new-frontend)
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 23
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 142

### File 0143: `new-frontend\lib\types.ts`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Utility Library (new-frontend)
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 116
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 143

### File 0144: `new-frontend\mnt\user-data\outputs\nexus-learn\app\chat\[id]\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 282
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 144

### File 0145: `new-frontend\mnt\user-data\outputs\nexus-learn\app\chat\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 145

### File 0146: `new-frontend\mnt\user-data\outputs\nexus-learn\app\chat\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 226
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 146

### File 0147: `new-frontend\mnt\user-data\outputs\nexus-learn\app\dashboard\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 29
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 147

### File 0148: `new-frontend\mnt\user-data\outputs\nexus-learn\app\dashboard\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 318
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 148

### File 0149: `new-frontend\mnt\user-data\outputs\nexus-learn\app\documents\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 149

### File 0150: `new-frontend\mnt\user-data\outputs\nexus-learn\app\documents\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 248
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 150

### File 0151: `new-frontend\mnt\user-data\outputs\nexus-learn\app\tests\[id]\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 305
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 151

### File 0152: `new-frontend\mnt\user-data\outputs\nexus-learn\app\tests\layout.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 152

### File 0153: `new-frontend\mnt\user-data\outputs\nexus-learn\app\tests\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 307
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 153

### File 0154: `new-frontend\next.config.js`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .js
- Approximate text lines: 5
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 154

### File 0155: `new-frontend\next-env.d.ts`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 4
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 155

### File 0156: `new-frontend\package.json`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .json
- Approximate text lines: 27
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 156

### File 0157: `new-frontend\package-lock.json`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .json
- Approximate text lines: 6073
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 157

### File 0158: `new-frontend\page.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 377
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 158

### File 0159: `new-frontend\postcss.config.js`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .js
- Approximate text lines: 6
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 159

### File 0160: `new-frontend\README.md`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .md
- Approximate text lines: 61
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 160

### File 0161: `new-frontend\scripts\run-dev.cjs`
- Module: new-frontend (Next.js 14 client)
- Category: Frontend Dev Script (new-frontend)
- Architectural layer: Application layer
- File extension: .cjs
- Approximate text lines: 27
- Status classification: Active code/config context
- Workflow touchpoint: Environment bootstrap and runtime infrastructure lifecycle.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 161

### File 0162: `new-frontend\Sidebar.tsx`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .tsx
- Approximate text lines: 156
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 162

### File 0163: `new-frontend\tailwind.config.js`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .js
- Approximate text lines: 11
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 163

### File 0164: `new-frontend\tsconfig.json`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .json
- Approximate text lines: 29
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 164

### File 0165: `new-frontend\types.ts`
- Module: new-frontend (Next.js 14 client)
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .ts
- Approximate text lines: 116
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 165

### File 0166: `SYSTEMATIC_DOCUMENTATION_SCAN_2026-03-10.md`
- Module: Repository root artifact
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .md
- Approximate text lines: 2275
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 166

### File 0167: `upload_smoke.txt`
- Module: Repository root artifact
- Category: Project Support Artifact
- Architectural layer: Support layer
- File extension: .txt
- Approximate text lines: 1
- Status classification: Active code/config context
- Workflow touchpoint: Auxiliary or observational workflow support.
- Strength contribution: Adds contextual clarity or operational support to the repository.
- Limitation/risk note: Needs periodic curation to stay aligned with active architecture.
- Documentation action note: Document ownership, role, and change frequency.
- Traceability anchor: map this entry to architecture/API/data-model/runbook chapters.
- Ledger index reference: 167

## Appendix: Quantitative Summary
- Total ledger entries: 167
- Authored file counts by top-level directory:
- `MainProject`: 70
- `new-frontend`: 50
- `frontend`: 32
- `.idea`: 9
- `SYSTEMATIC_DOCUMENTATION_SCAN_2026-03-10.md`: 1
- `upload_smoke.txt`: 1
- `files.zip`: 1
- `1.pdf`: 1
- `-H`: 1
- `generate_systematic_doc.ps1`: 1
- Authored file counts by extension:
- `.java`: 57
- `.tsx`: 48
- `.ts`: 12
- `.xml`: 8
- `.json`: 6
- `.log`: 4
- `.gitignore`: 4
- `[noext]`: 3
- `.md`: 3
- `.js`: 3
- `.css`: 3
- `.properties`: 2
- `.txt`: 1
- `.yml`: 1
- `.cmd`: 1
- `.local`: 1
- `.MF`: 1
- `.cjs`: 1
- `.zip`: 1
- `.example`: 1
- `.iml`: 1
- `.pdf`: 1
- `.gitattributes`: 1
- `.pid`: 1
- `.mjs`: 1
- `.ps1`: 1

## Closing Notes
- This document is intentionally exhaustive and structured for downstream documentation splitting.
- Next step: break into dedicated architecture, API, data model, and operations docs.
- Cleanup candidate: consolidate duplicate frontend artifacts and remove stale runtime outputs.


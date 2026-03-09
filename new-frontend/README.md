# Nexus — AI Learning Platform Frontend

A clean, editorial-styled Next.js 14 frontend for the Spring AI learning platform.

## Design System

| Token | Value |
|-------|-------|
| Background | `#080810` deep charcoal |
| Surface | `#0F0F1C` elevated surface |
| Gold accent | `#D4A84B` primary action color |
| Cream text | `#F2EBD9` primary text |
| Display font | Cormorant Garamond (serif) |
| Mono font | JetBrains Mono |
| Body font | DM Sans |

## Setup

```bash
# Install dependencies
npm install

# Create .env.local
echo "NEXT_PUBLIC_API_URL=http://localhost:8080" > .env.local

# Run dev server
npm run dev
```

## Structure

```
app/
  page.tsx                  # Auth (login / register)
  layout.tsx                # Root layout
  globals.css               # Design tokens + global styles
  dashboard/
    layout.tsx              # Sidebar shell (server component, fetches user)
    page.tsx                # Overview with stats
  chat/
    page.tsx                # Conversations list
    [id]/page.tsx           # Individual chat session
  documents/
    page.tsx                # Upload & manage documents
  tests/
    page.tsx                # Tests list + generate modal
    [id]/page.tsx           # Take test / view results
components/
  layout/
    Sidebar.tsx             # App-wide navigation sidebar
lib/
  api.ts                    # Typed API client (envelope parsing, auto-refresh)
  types.ts                  # All TypeScript types from API spec
```

## Pages

| Route | Description |
|-------|-------------|
| `/` | Login / Register |
| `/dashboard` | Overview with stats, recent activity |
| `/chat` | All conversations |
| `/chat/[id]` | AI chat session |
| `/documents` | Upload & manage documents |
| `/tests` | Assessments list & generation |
| `/tests/[id]` | Test-taking UI with results |

## Key Features

- **Auto token refresh** — 401 responses trigger a silent refresh before retrying
- **Optimistic UI** — Chat messages appear immediately, rolled back on error
- **Drag & drop uploads** — Documents page supports drag-and-drop
- **Animated test navigation** — Q-by-Q navigation with color-coded answer states
- **Responsive** — Sidebar collapses on mobile

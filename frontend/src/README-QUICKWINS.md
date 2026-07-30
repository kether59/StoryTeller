# StoryTeller frontend — Quick wins

Drop-in replacements for the first round of improvements.

## What changed

| File | Change |
|------|--------|
| `src/api/api.js` | Relative `baseURL` (Vite proxy + Docker/nginx). Friendly error message on interceptor. |
| `src/main.jsx` | Removed unused `BrowserRouter` / `react-router-dom`. |
| `src/hooks/useEntityCrud.js` | **New** shared CRUD hook (fetch / save / delete / edit / clear). |
| `src/utils/notify.js` | **New** central `notifySuccess` / `notifyError` / `confirmAction`. |
| `src/components/CharacterPanel.jsx` | Uses `useEntityCrud` — ~half the previous size. |
| `src/components/LocationPanel.jsx` | Same. |
| `src/components/LorePanel.jsx` | Same. |
| `src/components/LlmConfigpanel.jsx` | Loads existing config from `GET /api/llm/config` on mount. |
| `src/components/ProjectSelector.jsx` | Proper loading / error handling. |

## How to apply

From the repo root:

```bash
# Backup current files (optional)
cp -r frontend/src frontend/src.bak

# Copy the new files
cp artifacts/frontend-quickwins/src/api/api.js                    frontend/src/api/api.js
cp artifacts/frontend-quickwins/src/main.jsx                      frontend/src/main.jsx
mkdir -p frontend/src/hooks frontend/src/utils
cp artifacts/frontend-quickwins/src/hooks/useEntityCrud.js        frontend/src/hooks/useEntityCrud.js
cp artifacts/frontend-quickwins/src/utils/notify.js               frontend/src/utils/notify.js
cp artifacts/frontend-quickwins/src/components/CharacterPanel.jsx frontend/src/components/CharacterPanel.jsx
cp artifacts/frontend-quickwins/src/components/LocationPanel.jsx  frontend/src/components/LocationPanel.jsx
cp artifacts/frontend-quickwins/src/components/LorePanel.jsx      frontend/src/components/LorePanel.jsx
cp artifacts/frontend-quickwins/src/components/LlmConfigpanel.jsx frontend/src/components/LlmConfigpanel.jsx
cp artifacts/frontend-quickwins/src/components/ProjectSelector.jsx frontend/src/components/ProjectSelector.jsx
```

Or from inside `frontend/`:

```bash
# After copying the folder next to frontend/
cp -r ../frontend-quickwins/src/* src/
```

## Optional cleanup

You can remove `react-router-dom` from `package.json` dependencies (it is no longer imported):

```bash
cd frontend
npm uninstall react-router-dom
```

## API base URL

- **Dev**: Vite proxy already maps `/api` → `http://localhost:8000`. Leave `VITE_API_URL` unset.
- **Docker / prod**: nginx (or your reverse proxy) should serve the SPA and proxy `/api` to the backend. No hardcoded `localhost:8000` in the browser.
- Override if needed: create `frontend/.env` with `VITE_API_URL=https://your-api.example.com`.

## Backend note for LLM config

`LlmConfigpanel` now calls `GET /api/llm/config` on mount. If that endpoint does not exist yet, it silently keeps defaults (no crash). Prefer implementing a matching GET that returns the same shape as the POST body.

## Next (not in this pack)

- Apply the same hook pattern to `TimelinePanel` (needs related entities).
- React Query for shared lists (characters/locations used by several panels).
- Split the large AI panels.
- Real toast UI instead of `alert`.

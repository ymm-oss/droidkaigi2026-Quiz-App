# DroidKaigi Quiz — Staff Console Design System

Desktop admin console for venue quiz operations. Japanese UI only. Compose Multiplatform / Material 3.

## Brand

- Product: **DroidKaigi Quiz**
- Surface: Staff console (PC venue ops)
- Tone: calm ops tool, Android-green accent, dark-first for long sessions
- Avoid: purple gradients, cream+serif, newspaper layouts, neon glow, emoji decoration

## Color (from QuizTokens — dark theme primary for staff)

| Token | Hex | Use |
|-------|-----|-----|
| primaryDark | `#6FE89F` | CTA, links, active, ranks |
| onPrimaryDark | `#003822` | Text on primary buttons |
| primaryContainerDark | `#1B5E3A` | Selected folder row, chips |
| onPrimaryContainerDark | `#B8F5D0` | Text on primary container |
| surfaceDark | `#101814` | App background |
| onSurfaceDark | `#E2E8E4` | Primary text |
| onSurfaceVariantDark | `#BFC9C2` | Secondary / hints |
| outlineDark | `#89938C` | Borders, field outlines |
| secondaryContainerDark | `#3A4B40` | Cards / elevated panels |
| gradientStartDark | `#0D1F14` | Background gradient start |
| gradientEndDark | `#101814` | Background gradient end |
| correct | `#2E7D32` | Correct-answer emphasis |
| incorrect | `#C62828` | Destructive (delete confirm) |

Light tokens exist for participant app; staff stays dark.

## Typography

- Sans-serif (Material 3 defaults / system JP-capable)
- Hierarchy: `titleLarge` app bar · `headlineMedium` page title · `titleMedium` sidebar · `bodyLarge` content · `labelLarge` meta · `bodySmall` hints
- No display serif; no Inter/Roboto as brand hero

## Shape & space

- Corner: 8 / 12 / 16 / 24 dp
- Spacing: 8 / 16 / 24 / 32 dp
- Cards: 12dp radius, subtle elevation, no heavy multi-shadow
- Primary buttons: full-width in content panes, mint fill, dark text
- Text buttons: mint text for secondary actions (Edit, Logout, Reload)

## Layout shell (Desktop ~1440×900)

1. **TopAppBar**: title `Droid26クイズ管理アプリ` · trailing `参加者アプリ` · site publish · `ログアウト`
2. **Folder sidebar ~260dp**: list folders · `+` add · bottom CTA `参加者向けに公開`
3. **NavigationRail**: `クイズ` · `ランキング`
4. **Main pane**: folder-scoped content

Only one folder is `公開中` at a time.

## Screens

### Auth
Centered card max ~480dp: badge Staff · hero title · email/password · primary Login.

### Quiz
Page title (folder/set name) · Reload · Add question · drag-reorder list of question cards (type · prompt · correct · explanation · Edit/Delete).

### Ranking
`本日のランキング` · ranked rows (rank · nickname · accuracy % · completed time `MM/dd HH:mm` or 不明). Live list; no manual refresh.

### Dialogs
- Question editor: type dropdown · Markdown prompt+preview · choices (radio/checkbox/reorder) · explanation · Save/Cancel
- Create folder: name · description · Create/Cancel
- Confirm publish / delete: destructive or primary confirm
- Participant preview: phone frame · viewport width ± · **プレビュー言語** 日本語 / English segmented control (staff chrome stays Japanese; only the in-frame participant UI localizes)

## Improvement goals (vs current)

- Clearer hierarchy between Publish vs Add Question (same CTA weight today)
- Denser question list for long catalogs
- Stronger active-folder / published state
- Editor as side panel or larger focused surface instead of cramped AlertDialog
- Ranking empty/loading/error as intentional empty states, not sparse void
- Keep Android-green dark palette; refine contrast for a11y

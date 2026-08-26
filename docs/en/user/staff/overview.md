# Staff app overview

Console for venue staff. Use the Desktop app (`:staffDesktopApp`) on a venue PC, or sign in from the same Firebase Hosting site at the unlisted URL `/staff`. Security is Firebase Authentication, not URL obscurity. The layout is desktop-width first.

## Features

| Feature | Description |
|---------|-------------|
| **Quiz management** | Add/edit questions, explanations, per-type settings. Participant preview (phone frame) |
| **Folder management** | Split sets by date/level; switch the active (published) folder |
| **Site publish** | Open/close participant intake (`sitePublished`; separate from folder publish) |
| **Ranking** | View today's ranking per folder |

## Screens

| Screen | Description |
|--------|-------------|
| [Sign-in](/en/user/staff/auth) | Staff authentication |
| [Console](/en/user/staff/console) | Edit, publish, rankings |

::: tip
Production uses Firebase Authentication and Firestore. Schema details: [Firestore](/en/developer/firestore).
:::

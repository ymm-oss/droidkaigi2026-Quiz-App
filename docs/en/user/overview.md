# Overview for users

DroidKaigi 2026 ships **two** quiz-related apps.

## App types

| App | Audience | Platforms | What you can do |
|-----|----------|-----------|-----------------|
| **Participant** | Attendees | Android / Desktop (Web in progress) | Nickname → quiz → score → today's ranking |
| **Staff** | Organizers | Desktop, and participant Web `/staff` | Edit questions/folders, publish, view rankings |

## Participant flow (summary)

1. **Home** — language + nickname, start quiz
2. **Quiz** — single / multi / reorder, then correct/incorrect feedback
3. **Result** — correct count and score
4. **Ranking** — today's Top N with your row highlighted

See [Screen flow](/en/user/participant/flow).

## Staff flow (summary)

1. **Sign-in** — staff account
2. **Console** — edit folders/questions, switch the published folder
3. **Ranking** — inspect today's ranking per folder

See [Staff overview](/en/user/staff/overview).

## Network

Production requires online access for quiz fetch and score submit. Offline-only completion is out of scope.

::: tip For developers
Build, tests, and data-layer switching: [Developer overview](/en/developer/overview).
:::

import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'

export const en: LocaleSpecificConfig<DefaultTheme.Config> = {
  description: 'Specification for the DroidKaigi 2026 venue quiz apps',
  themeConfig: {
    nav: [
      { text: 'Home', link: '/en/' },
      { text: 'For users', link: '/en/user/overview' },
      { text: 'For developers', link: '/en/developer/overview' },
    ],
    sidebar: {
      '/en/': [
        {
          text: 'For users',
          collapsed: false,
          items: [
            { text: 'Overview', link: '/en/user/overview' },
            {
              text: 'Participant app',
              collapsed: false,
              items: [
                { text: 'Screen flow', link: '/en/user/participant/flow' },
                { text: 'Home', link: '/en/user/participant/home' },
                { text: 'Quiz', link: '/en/user/participant/quiz' },
                { text: 'Result', link: '/en/user/participant/result' },
                { text: 'Ranking', link: '/en/user/participant/ranking' },
              ],
            },
            {
              text: 'Staff app',
              collapsed: false,
              items: [
                { text: 'Overview', link: '/en/user/staff/overview' },
                { text: 'Sign-in', link: '/en/user/staff/auth' },
                { text: 'Console', link: '/en/user/staff/console' },
              ],
            },
          ],
        },
        {
          text: 'For developers',
          collapsed: false,
          items: [
            { text: 'Overview', link: '/en/developer/overview' },
            { text: 'Architecture', link: '/en/developer/architecture' },
            { text: 'Scoring', link: '/en/developer/scoring' },
            { text: 'Runtime (fake / prod)', link: '/en/developer/runtime' },
            { text: 'Firestore', link: '/en/developer/firestore' },
            { text: 'Build & run', link: '/en/developer/build' },
            { text: 'Contributing', link: '/en/developer/contributing' },
          ],
        },
      ],
    },
  },
}

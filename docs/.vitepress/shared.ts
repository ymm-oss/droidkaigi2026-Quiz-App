import { defineConfig, type DefaultTheme } from 'vitepress'

/**
 * Shared VitePress options (base path, exclusions, theme tokens).
 * Locale-specific nav / sidebar live in `ja.ts` / `en.ts`.
 */
export const shared = defineConfig({
  title: 'DroidKaigi 2026 Quiz',
  description: '会場向けクイズアプリの仕様書',
  // GitHub Pages project site: https://ymm-oss.github.io/droidkaigi2026-Quiz-App/
  base: '/droidkaigi2026-Quiz-App/',
  cleanUrls: true,
  lastUpdated: true,
  ignoreDeadLinks: true,
  srcExclude: [
    '**/SPEC.md',
    '**/DEVELOPMENT.md',
    '**/CONTRIBUTING.md',
    '**/FIRESTORE.md',
    '**/VERIFY.md',
    '**/firestore-seed.json',
    '**/stitch/**',
    '**/screenshots/**',
    '**/public/**',
    '**/node_modules/**',
    '**/package.json',
    '**/package-lock.json',
    '**/README.md',
  ],
  head: [
    ['meta', { name: 'theme-color', content: '#22c55e' }],
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/droidkaigi2026-Quiz-App/favicon.svg' }],
  ],
  themeConfig: {
    logo: { text: 'Quiz Spec' },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/ymm-oss/droidkaigi2026-Quiz-App' },
    ],
    search: {
      provider: 'local',
    },
    outline: {
      level: [2, 3],
    },
  } satisfies DefaultTheme.Config,
})

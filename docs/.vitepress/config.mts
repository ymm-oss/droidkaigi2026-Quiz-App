import { defineConfig } from 'vitepress'
import { shared } from './shared'
import { ja } from './ja'
import { en } from './en'

/**
 * VitePress site for DroidKaigi 2026 Quiz.
 *
 * Source lives under `docs/` alongside existing Markdown (SPEC.md etc.).
 * Legacy files are excluded via `srcExclude` so they stay repo references
 * without becoming site pages.
 */
export default defineConfig({
  ...shared,
  locales: {
    root: {
      label: '日本語',
      lang: 'ja',
      ...ja,
    },
    en: {
      label: 'English',
      lang: 'en',
      ...en,
    },
  },
})

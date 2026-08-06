import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'

export const ja: LocaleSpecificConfig<DefaultTheme.Config> = {
  description: 'DroidKaigi 2026 会場向けクイズアプリの仕様書',
  themeConfig: {
    nav: [
      { text: 'ホーム', link: '/' },
      { text: '利用者向け', link: '/user/overview' },
      { text: '開発者向け', link: '/developer/overview' },
    ],
    sidebar: {
      '/': [
        {
          text: '利用者向け',
          collapsed: false,
          items: [
            { text: '概要', link: '/user/overview' },
            {
              text: '参加者アプリ',
              collapsed: false,
              items: [
                { text: '画面の流れ', link: '/user/participant/flow' },
                { text: 'ホーム', link: '/user/participant/home' },
                { text: 'クイズ', link: '/user/participant/quiz' },
                { text: '結果', link: '/user/participant/result' },
                { text: 'ランキング', link: '/user/participant/ranking' },
              ],
            },
            {
              text: '管理者アプリ',
              collapsed: false,
              items: [
                { text: '概要', link: '/user/staff/overview' },
                { text: 'ログイン', link: '/user/staff/auth' },
                { text: '運営コンソール', link: '/user/staff/console' },
              ],
            },
          ],
        },
        {
          text: '開発者向け',
          collapsed: false,
          items: [
            { text: '概要', link: '/developer/overview' },
            { text: 'アーキテクチャ', link: '/developer/architecture' },
            { text: '採点ロジック', link: '/developer/scoring' },
            { text: 'ランタイム（fake / prod）', link: '/developer/runtime' },
            { text: 'Firestore', link: '/developer/firestore' },
            { text: 'ビルド・実行', link: '/developer/build' },
            { text: 'コントリビュート', link: '/developer/contributing' },
          ],
        },
      ],
    },
    outlineTitle: 'このページの内容',
    lastUpdatedText: '最終更新',
    docFooter: {
      prev: '前へ',
      next: '次へ',
    },
    darkModeSwitchLabel: '外観',
    sidebarMenuLabel: 'メニュー',
    returnToTopLabel: 'ページ上部へ',
    langMenuLabel: '言語を切り替え',
  },
}

# アーキテクチャ

## モジュール構成

| モジュール | 役割 |
|-----------|------|
| `:androidApp` | Android エントリ（`MainActivity`） |
| `:desktopApp` | Desktop エントリ（`main`） |
| `:wasmApp` | Web（Wasm）エントリ。`/` は参加者、`/staff` はスタッフ |
| `:composeApp` | 共有 UI + Nav3（参加者ルート定義はここ） |
| `:staffComposeApp` / `:staffDesktopApp` | スタッフ運営コンソール（JVM Desktop。Web は `staffComposeApp` を wasm からホスト） |
| `:core:domain` | モデル・採点・ユースケース |
| `:core:data` | Repository・Metro バインディング・`fakeMain`/`prodMain` |
| `:core:ui` | `QuizTheme`・トークン・共通コンポーネント |
| `:feature:quiz` | Home / Quiz / Result |
| `:feature:ranking` | Ranking |
| `:feature:staff` | スタッフ UI（JVM + wasmJs） |

## 依存方向

```
feature → core:ui, core:domain
data → domain
composeApp → feature:quiz, feature:ranking
staffComposeApp → feature:staff, feature:quiz
wasmApp → composeApp, staffComposeApp
```

**逆依存は禁止**です。参加者の Nav ルートは `composeApp` のみ。スタッフ Web はパス `/staff` で `StaffApp` を起動し、Nav3 には混ぜない。`main()` / `MainActivity` を `composeApp` に追加しないでください。

## 画面の MVI

各画面は次の型を揃えます。

- `XxxUiState`
- `XxxIntent`
- `XxxEvent`
- `XxxViewModel`

ビジネスロジックは domain のユースケースへ寄せます。

## 共有依存グラフ

`AppDependencies.shared` のみを使い、Metro で **一度だけ** 初期化します。参加者は `initQuizAppGraph()`、スタッフ（Desktop / Wasm `/staff`）は `initStaffQuizAppGraph()`。グラフの二重生成は避けてください。

## テーマ

- `QuizTheme { }` でラップ
- 色は `QuizTokens` / `QuizColors`（機能モジュールで生の `Color(0x…)` を直書きしない）

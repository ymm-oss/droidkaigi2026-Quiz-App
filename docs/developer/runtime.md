# ランタイム（fake / prod）

データ層はビルド時に **どちらか一方** だけがコンパイルされます（`src/fakeMain` または `src/prodMain`）。

| バリアント | `quiz.runtime` | 問題 | ランキング | 用途 |
|------------|----------------|------|------------|------|
| **fake**（既定） | `fake` | 同梱 `quiz_set.json` | インメモリ | オフライン UI・採点検証 |
| **prod** | `prod` | Firestore | Firestore | 会場・結合 |

UI モジュールは共通です。Repository 実装だけが切り替わります。

## 切り替え

| プラットフォーム | 方法 |
|------------------|------|
| Android | Build Variant（`fakeDebug` / `prodDebug`） |
| Desktop / スタッフ | `gradle.properties` の `quiz.runtime`、または `-Pquiz.runtime=prod` |

切替後は **必ず再ビルド**してください。選ばれていない source set はコンパイルされません。

## 注意

- fake は本番の代替実装ではない（開発ハーネス）
- ネット失敗時に fake へサイレントフォールバックしない
- Wasm の prod は未対応（起動時エラー）
- Android で `assembleFakeDebug` と `assembleProdDebug` を **1 コマンドで並べると** KMP は fake にフォールバックする。片方ずつビルドする

詳細手順はリポジトリの `docs/DEVELOPMENT.md` を参照してください。

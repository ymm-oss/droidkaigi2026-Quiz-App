# ログイン

スタッフ用アカウントで管理者アプリに入ります。

## 画面イメージ

<img src="/screenshots/staff/01-auth.png" alt="スタッフ認証画面" width="520" />

開発用（デモ）ビルドではワンクリックのデモログインボタンも表示されます。

<img src="/screenshots/staff/staff-auth-fake-quick-login.png" alt="デモアカウントでログイン" width="520" />

本番ビルドではデモ用ワンクリックは出ません。

<img src="/screenshots/staff/staff-auth-prod-no-quick-login.png" alt="本番ログイン（デモボタンなし）" width="520" />

## 操作

1. メールアドレスとパスワードを入力してログインする  
   （または開発ビルドで「デモアカウントでログイン」を使う）
2. 成功すると運営コンソールへ遷移する
3. トップバーの「ログアウト」で認証画面に戻る

::: warning
開発用のデモ認証情報は本番では使えません。本番アカウントは運営から共有されたものを使ってください。
:::

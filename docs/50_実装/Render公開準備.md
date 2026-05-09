# Render公開準備

## 1. 目的

本書は、APIM for Spring を Render の Web Service として公開するための準備事項、設定値、疎通確認観点を整理する。

TSK-041では、Renderに登録・公開できる前提を整えることを目的とする。Render上での実サービス作成、URL確定、公開判断はユーザー(人)が行う。

---

## 2. 前提

- 対象リポジトリ: `yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring`
- 対象ブランチ: `main`
- Runtime: Docker
- Java: 17
- Build: Maven Wrapper
- 起動対象: Spring Boot executable jar
- 公開確認入口: `/`

Renderでは、Web Service は外部HTTPを受けるポートへbindする必要がある。APIMでは `server.port=${PORT:8080}` を設定し、Render実行時は `PORT`、ローカル実行時は `8080` を使用する。

---

## 3. 追加・利用する設定ファイル

| ファイル | 用途 |
|---|---|
| `Dockerfile` | RenderがDocker imageをbuildし、Spring Boot jarを起動するための設定 |
| `render.yaml` | Render BlueprintでWeb Serviceを作成するための設定 |
| `src/main/resources/application.properties` | Renderの `PORT` 環境変数にSpring Bootを追従させる設定 |

---

## 4. Render Blueprint設定

`render.yaml` では以下を前提とする。

- `type: web`
- `runtime: docker`
- `dockerfilePath: ./Dockerfile`
- `dockerContext: .`
- `healthCheckPath: /`
- `autoDeployTrigger: off`

初回公開時は、意図しない自動公開を避けるため、auto deployをoffにし、ユーザー(人)がRender Dashboard上で手動deployまたは設定変更を判断する。

---

## 5. Build / Start方式

Docker buildでは、Maven Wrapperを使ってjarを作成する。

```text
./mvnw -B -DskipTests package
```

Docker runtimeでは、生成されたjarを起動する。

```text
java -jar app.jar
```

RenderのDocker Web Serviceでは、Dockerfileの `ENTRYPOINT` が起動コマンドになる。

---

## 6. 疎通確認観点

Renderへdeploy後、以下を確認する。

1. Renderのdeployが成功していること。
2. Render logsにSpring Boot起動完了ログが出ていること。
3. `/` へアクセスして初回体験ルートの入力画面が表示されること。
4. サンプル業務パターンを選択または入力し、設計生成結果画面へ遷移できること。
5. Markdown設計書プレビューを開けること。
6. AI実装指示書プレビューを開けること。
7. 設計書とAI実装指示書をダウンロードできること。
8. `/help` を開けること。

---

## 7. 公開前注意事項

以下はTSK-041の対象外とする。

- DB永続化。
- 外部LLM API連携。
- 認証・認可の本格実装。
- 完全動作するMCPサーバー実装。
- 本番運用レベルの監視・アラート設計。

公開前にREADMEおよび画面文言を確認し、APIMが「設計生成支援ツール」であり、完全動作するMCPサーバーやSpring Bootアプリケーションを生成するものではないことを維持する。

---

## 8. 完了条件

- Render用のbuild/start設定がリポジトリ上に存在すること。
- Spring BootがRenderの `PORT` 環境変数に追従できること。
- READMEからRender公開手順を確認できること。
- 公開後の疎通確認観点が明文化されていること。
- 初回体験ルートとの整合が取れていること。

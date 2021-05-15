# Oiyokan Initializr

Oiyokan Initializr is a low code tool that quickly publish database tables as a REST API server.

* Generate Spring Boot web based REST API Server (OData v4) with minimal text input and mouse ops.
* Knowledge of Spring Boot, Maven, and JDBC settings is recommended.

## Getting Started with the Oiyokan

Getting Started with the Oiyokan can be viewed at the following URL  (written in Japanese).

- http://www.igapyon.jp/igapyon/diary/2021/ig210511.html


# Usage

## Run `oiyokan-initializr`

1-1. Download the release versoin `Source code (zip)` of `oiyokan-initializr` from [github.com](https://github.com/igapyon/oiyokan-initializr/releases).

1-2. Extract `Source code (zip)` to a directory that is convenient for you.

2-1. Build and Run `oiyokan-initializr` using maven

展開されたディレクトリにディレクトリを移動。
以下の mvn コマンドを実行。

```sh
mvn install spring-boot:run
```

## Generate an OData Server using `oiyokan-initializr`

3-1. 起動後の `oiyokan-initializr` に Web ブラウザで接続

```sh
http://localhost:8082/
```

3-2. Click `START CREATING REST API SERVER FOR RDB`

3-3. If basic authentication is shown, login with User: admin, Password: passwd123

3-4. Click `ADD DATABASE`

3-5. Fill database settings

3-6. Click `CONNECTION TEST`

3-7. Click `APPLY DATABASE SETTINGS`

4-1. Click `ADD ENTITY`

4-2. Select and check Entity(table) you want to publish

4-3. Click `APPLY ENTITY SELLECTION`

5-1. Click `GENERATE REST API SERVER`, and you will download an zip file named `oiyokan-demo.zip`.
  Note: この手順で生成される `oiyokan-demo.zip` にはデータベース接続情報の記載を含む `oiyokan-settings.json` ファイルが含まれます。必要ない場合は手順の後に必ず削除します。

5-2. `Oiyokan Initializr` を終了。

## Run Generated the OData Server

6-1. どこか都合の良いディレクトリで `oiyokan-demo.zip` を zip展開
  ここで展開したファイルの `oiyokan-settings.json` にはデータベースの接続情報が含まれますので、必要ない場合は手順の後で必ず削除してください。

6-2. zip 展開後のフォルダで以下コマンドにて `oiyokan-demo` を起動。

```sh
mvn install spring-boot:run
```

6-3. 起動後の `oiyokan-demo` に Web ブラウザで接続

```sh
http://localhost:8080/
```

ここで起動されているサーバが OData v4 Server (provider) の機能を提供します。

7-1. 試行が終わったら、`oiyokan-demo` を終了。

7-2. 試行が終わり次第、`oiyokan-demo.zip` および展開後のファイル `oiyokan-settings.json` を削除します。
  Note: この一連の手順で登場する `oiyokan-settings.json` ファイルにはデータベース接続情報の記載が含まれます。`oiyokan-demo.zip` も含めて必要ない場合は手順の後に必ず削除します。

## Supported target RDBMS

- h2 database (1.4)
- PostgreSQL (13)
- MySQL (8)
- SQL Server (2008)
- Oracle XE (18c)

## Supported OData Method

- GET
- POST
- PATCH
- DELETE

## Supported OData system query options

- $select
- $count
- $filter
- $orderby
- $top
- $skip

## Oiyokan 関連リポジトリ 

- [Oiyokan Library - github](https://github.com/igapyon/oiyokan)
- [Oiyokan Initializr - github](https://github.com/igapyon/oiyokan-initializr)
- [Oiyokan UnitTest - github](https://github.com/igapyon/oiyokan-unittest)
- [Oiyokan Demosite - github](https://github.com/igapyon/oiyokan-demosite)
- [Oiyokan Demosite-Test - github](https://github.com/igapyon/oiyokan-demosite-test)

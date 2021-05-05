# Oiyokan Initializr

Oiyokan is an generater to create new maven project of OData v4 server (provider).
Using Oiyokan Initializr, you can quickly publish your RDB as an OData v4 Server (provider).

# Usage

## Run `oiyokan-initializr`

1. Clone the `oiyokan-initializr` project from [github.com](https://github.com/igapyon/oiyokan-initializr)

```sh
git clone https://github.com/igapyon/oiyokan-initializr.git
```

2. Build and Run `oiyokan-initializr` using maven

clone されたディレクトリにディレクトリを移動。
以下の mvn コマンドを実行。

```sh
mvn install spring-boot:run
```

## Generate an OData Server using `oiyokan-initializr`

3. 起動後の `oiyokan-initializr` に Web ブラウザで接続

```sh
http://localhost:8080/
```

4. Click `Oiyokan Initializr`

5. Fill database settings

6. Click `Connection Test`

7. Click `Select Table`

8. Select and check tables you want to publish

9. Click `GENERATE`, and you will download an zip file named `oiyokan-demo.zip`.
  なお、ここで生成された `oiyokan-demo.zip` および展開後のファイル `oiyokan-settings.json` にはデータベースの接続情報が含まれますので、必要ない場合は手順の後に必ず削除してください。

10. `Oiyokan Initializr` を終了。

## Run Generated the OData Server

11. どこか都合の良いディレクトリで `oiyokan-demo.zip` を zip展開
  ここで展開したファイルの `oiyokan-settings.json` にはデータベースの接続情報が含まれますので、必要ない場合は手順の後で必ず削除してください。

12. zip 展開後のフォルダで以下コマンドにて `oiyokan-demo` を起動。

```sh
mvn install spring-boot:run
```

13. 起動後の `oiyokan-demo` に Web ブラウザで接続

```sh
http://localhost:8080/
```

ここで起動されているサーバが OData v4 Server (provider) の機能を提供します。

14. 試行が終わったら、`oiyokan-demo` を終了。

15. 試行が終わり次第、`oiyokan-demo.zip` および展開後のファイル `oiyokan-settings.json` を削除する。

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
- [Oiyokan Demosite - github](https://github.com/igapyon/oiyokan-demosite)
- [Oiyokan Demosite-Test - github](https://github.com/igapyon/oiyokan-demosite-test)

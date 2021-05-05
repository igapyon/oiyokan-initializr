# Oiyokan Initializr

Oiyokan is an generater to create new maven project of OData v4 server (provider).
Using Oiyokan Initializr, you can quickly publish your RDB as an OData v4 Server (provider).

# Usage

1. Clone `oiyokan-initializr` project from [github.com](https://github.com/igapyon/oiyokan-initializr)
 
```sh
git clone https://github.com/igapyon/oiyokan-initializr.git
```

2. Build and Run `oiyokan-initializr` using maven

clone されたディレクトリに移動して mvn コマンドで以下を起動.

```sh
mvn install spring-boot:run
```

http://localhost:8080/

Click `Oiyokan Initializr`


Fill database settings

`Connection Test`

Click `Select Table`

Select tables you want to publish

Click `GENERATE`, and you will download an zip file named `oiyokan-demo.zip`.

実行中の `Oiyokan Initializr` を終了。


どこか都合の良いディレクトリで `oiyokan-demo.zip` を展開。

展開後のフォルダで以下コマンドにて起動。

```sh
mvn install spring-boot:run
```

http://localhost:8080/


`oiyokan-demo` を終了。

試行が終わり次第、`oiyokan-demo.zip` および展開後のファイルを削除してください。






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

# Table-lock demo

Demonstration how to solve simultaneous-insertion problem.

## Case analyzing

Client can request operation that inserts one or more record(s).

Request parameter determines key of record.

In that case, when inserting two or more records simultaneously and there are duplication among
those keys of each record, An error will be occurred.

## Idea

Lock is basic concept DBMS handling concurrency.

When two or more clients try update same record(s), DBMS blocks later update to record until the
earlier transaction is committed.

But, in case of insertion, there is no record to lock.

The core of idea is using the record intended to lock specific table. For convenience, we call this
table *lock table* and the table we originally intended to insert record *target table*.

Before insert record into specific table, select some entity from the *lock table*. Then insert the
record into *target table*.

Below is the pseudo-code of the flow of this transaction:

```text
function insert_with_lock(Long id) {
    begin_transaction()
    
    select_from_lock_table("my_entity")
    insert_into_target_table(id)
    
    commit_transaction()
}
```

## Demonstration

This project demonstrates the idea.

First, reproduce simultaneous-insertion problem.

### Prerequisites

This project uses:

* Go 1.16.5
* Java 8

### How to reproduce problem

1. Clone project
    ```shell
    git clone https://github.com/Laterality/table-lock-demo
    ```
    
2. Run API server

    ```shell
    ./gradlew bootRun
    ```

3. Send request to insertion without lock.

    Testing client will send 10 requests simultaneously with same key as parameter.

    ```shell
    cd test-client
    go run main.go
    ```

4. Then, test clients will print logs looks like:

    ```text
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.801+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.788+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.788+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.809+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.788+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.823+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.788+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.790+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[500 ] {\"timestamp\":\"2021-11-28T13:56:09.797+00:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/myentity\"}"
    time="2021-11-28T22:56:09+09:00" level=info msg="[200 ] "
    time="2021-11-28T22:56:10+09:00" level=info msg="1 item(s) found"
    ```

    Also, API server logs looks like:

    ```text
    org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: "PRIMARY KEY ON PUBLIC.MY_ENTITY(ID) [1638107769]"; SQL statement:
    insert into my_entity (id) values (?) [23505-200]
    ```

### How to reproduce solution

Logs above notifies the simultaneous insertion caused problem

So, let's see how lock solves the problem.

1. Clone project, you don't need if you already have.

   ```shell
   git clone https://github.com/Laterality/table-lock-demo
   ```

2. Run API server, also you don't need if you already have.

   ```shell
   ./gradlew bootRun
   ```

3. Send request to insertion **with** lock.

   Testing client will send 10 requests simultaneously with same key as parameter with flags indicating to use table lock.

   ```shell
   cd test-client
   go run main.go --lock
   ```

4. Then, test clients will print logs looks like:

   ```text
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="[200 ] "
   time="2021-11-28T23:06:03+09:00" level=info msg="2 item(s) found"
   ```

   API server logs looks like:

   ```text
   2021-11-28 23:10:33.999  INFO 28176 --- [nio-8080-exec-3] kr.latera.tablelockdemo.MyEntityService  : Locked
   2021-11-28 23:10:34.058  INFO 28176 --- [nio-8080-exec-3] kr.latera.tablelockdemo.MyEntityService  : Committed with: 1638108633
   2021-11-28 23:10:34.059  INFO 28176 --- [nio-8080-exec-2] kr.latera.tablelockdemo.MyEntityService  : Locked
   2021-11-28 23:10:34.061  INFO 28176 --- [nio-8080-exec-2] kr.latera.tablelockdemo.MyEntityService  : Committed with: 1638108633
   2021-11-28 23:10:34.062  INFO 28176 --- [io-8080-exec-10] kr.latera.tablelockdemo.MyEntityService  : Locked
   2021-11-28 23:10:34.064  INFO 28176 --- [io-8080-exec-10] kr.latera.tablelockdemo.MyEntityService  : Committed with: 1638108633
   ...
   ```

   The logs shows that each thread locked record on *lock table* and insert their record into *target table* **sequentially**.

## Conclusion

There are various cases that client requests (nearly)simultaneously that insert record(s). Therefore, there are various solutions for those problems.

This project assumes that key of each record is determined by request parameter. So the duplication of keys is possible.

If the key of record is not determined by client(e.g., auto increment), there might not be need to block insertion.





# MySQL Learning Lab

An interactive playground for learning **MySQL performance, features, and best practices**.  
Each lab demonstrates a specific concept using **Java + Dockerized MySQL**.


## Quickstart

```bash
git clone <repo-url>
cd mysql-lab
docker compose up -d    # start MySQL in Docker
mvn compile
````

* MySQL runs inside Docker (`labdb` database, user: `labuser` / pass: `labpass`).
* Java programs connect to it using JDBC.


## 📚 Topics

- [Indexing](./indexing/README.md)
- [Isolation](./isolation_levels/README.md)

## Further Reading:

- [MySQL Data type cheat sheet](https://github.com/batcity/high-level-design-lab/blob/main/cheat-sheets/mysql-data-type-cheat-sheet.md)



### 🔜 Coming Soon

I'll be adding labs for:

* **Views**
* **Joins & query plans**
* **Transactions**
* **Deadlocks & locks**
* **Stored procedures**
* **Partitioning**
* **Replication basics**
* **MySQL Document store**

## 🏗️ Structure

```
mysql-lab/
├── docker-compose.yml   # MySQL in Docker
├── pom.xml              # Maven project setup
├── indexing/            # Index-related lab
└── ...
```

## What You’ll Learn

* How to measure and interpret **query performance**.
* When and why indexes help (or hurt).
* How to read MySQL `EXPLAIN` plans.
* Core concepts of MySQL internals step by step.

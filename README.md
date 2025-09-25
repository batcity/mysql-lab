
# MySQL Learning Lab

An interactive playground for learning **MySQL performance, features, and best practices**.  
Each lab demonstrates a specific concept using **Java + Dockerized MySQL**.

---

## 🚀 Quickstart

```bash
git clone <repo-url>
cd mysql-lab
docker-compose up -d    # start MySQL in Docker
mvn compile
````

* MySQL runs inside Docker (`labdb` database, user: `labuser` / pass: `labpass`).
* Java programs connect to it using JDBC and measure performance.

---

## 📚 Labs

### ✅ Indexing

* [Good Index Benchmark](./indexing/IndexBenchmark.java)
  *High-cardinality column (`name`) → index makes queries much faster.*
* [Bad Index Benchmark](./indexing/BadIndexBenchmark.java)
  *Low-cardinality column (`department`) → index actually slows queries down.*

Run:

```bash
# Good index demo
mvn exec:java -Dexec.mainClass="IndexBenchmark"

# Bad index demo
mvn exec:java -Dexec.mainClass="BadIndexBenchmark"
```

📖 [Indexing README](./indexing/README.md)

---

### 🔜 Coming Soon

I'll be adding labs for:

* **Joins & query plans**
* **Transactions & isolation levels**
* **Deadlocks & locks**
* **Stored procedures**
* **Partitioning**
* **Replication basics**

---

## 🏗️ Structure

```
mysql-lab/
├── docker-compose.yml   # MySQL in Docker
├── pom.xml              # Maven project setup
├── indexing/            # Index-related lab
│   ├── IndexBenchmark.java
│   ├── BadIndexBenchmark.java
│   └── README.md
└── ... more labs coming soon ...
```

---

## 🧠 What You’ll Learn

* How to measure and interpret **query performance**.
* When and why indexes help (or hurt).
* How to read MySQL `EXPLAIN` plans.
* Core concepts of MySQL internals step by step.



# Index Benchmark Lab

Measure the effect of adding an index in MySQL by timing a filtered query before and after indexing.

---

## Requirements
- Maven installed (required to run)
- Java 8+
- MySQL running locally with a `labdb` database  
  - User: `root` / Password: `root`

---

## What it does

1. Creates an `employees` table and truncates it.
2. Generates **1,000,000 rows** to a CSV and loads them via `LOAD DATA LOCAL INFILE`.
3. Runs:

   ```sql
   SELECT * FROM employees WHERE department='IT';
   ```

   without an index and records the time.
4. Creates an index on `department`.
5. Runs the same query with the index and records the time.
6. Prints both timings and cleans up (table + temp CSV).

---

## What you must understand

* **Indexes drastically improve query performance** for filtered lookups.
* **Trade-offs:**

  * Speed up reads.
  * Consume storage.
  * Can slow down inserts/updates.

---

## RUN:

```bash
mvn compile exec:java -Dexec.mainClass="IndexBenchmark"
```

## Here's a sample result showing difference in performance:

![Sample benchmark result showing performance difference](TODO)


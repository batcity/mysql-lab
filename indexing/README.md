# Index Benchmark Lab

This folder contains Java programs that demonstrate **good vs bad indexes** in MySQL.

---

## 🔎 Experiments

### 1. Good Index Benchmark (`IndexBenchmark.java`)
- Table: `employees_good`
- Query:  
  ```sql
  SELECT * FROM employees_good WHERE name='Employee500000';
  ```

* Index on `name` (unique per row)
* **Effect:** Queries become **hundreds of times faster**.

---

### 2. Bad Index Benchmark (`BadIndexBenchmark.java`)

* Table: `employees`
* Query:

  ```sql
  SELECT * FROM employees WHERE department='IT';
  ```
* Index on `department` (only 5 possible values)
* **Effect:** Index actually makes performance **worse**, because many rows share the same value.

---

## 🛠️ Run the Benchmarks

From the project root:

```bash
# Good index (high-cardinality column)
mvn exec:java -Dexec.mainClass="IndexBenchmark"

# Bad index (low-cardinality column)
mvn exec:java -Dexec.mainClass="BadIndexBenchmark"
```

---

## 📊 Sample Results

**Good Index Benchmark**

```
Time without index: 436 ms
Time with index:    1 ms
🎯 Speedup: ~436x faster with index.
```

**Bad Index Benchmark**

```
Time without index: 485 ms
Time with index:    1779 ms
⚠️ Index hurt performance: query got slower with index!
```

---

## 🧠 Takeaways

* Use indexes wisely:

  * ✅ Great for **high-cardinality columns** (unique or nearly unique values)
  * ⚠️ Harmful on **low-cardinality columns** (few distinct values)
* Always check with `EXPLAIN` to see how MySQL plans to use your index
* Learn to read `EXPLAIN` output:

  * `ALL` → full table scan
  * `ref` → non-unique index lookup (can match many rows)
  * `eq_ref` → unique index lookup (exactly one row)

---

## 📌 Notes

* All benchmarks generate **1,000,000 rows** and clean up after themselves.
* The `LOAD DATA LOCAL INFILE` command is used to quickly load CSV data into MySQL.

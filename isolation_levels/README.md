# MySQL Isolation Levels

MySQL **isolation levels** define how the database engine (for example, **InnoDB**) handles concurrent transactions — specifically, how it controls visibility and prevents data anomalies such as **dirty reads**, **non-repeatable reads**, and **phantom reads**.

There are four standard isolation levels in MySQL:

| Level | Description |
|--------|--------------|
| **READ UNCOMMITTED** | Transactions can see uncommitted changes (allows dirty reads). |
| **READ COMMITTED** | Each query sees only committed data at the time it runs. |
| **REPEATABLE READ** | Ensures the same rows can be read repeatedly with consistent results. *(Default in InnoDB)* |
| **SERIALIZABLE** | Highest isolation level; transactions are fully isolated and executed sequentially. |

> 💡 **Note:** The default isolation level for InnoDB is **REPEATABLE READ**.

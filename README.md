# Synopsis
Hacky tool for pretty-printing Jdbc ResultSet

# Example
git clone https://github.com/ch6574/jdbc-pretty-print.git

copy the class into your codebase

```
ResultSet rs = ...
System.out.println(JdbcPrettyPrint.prettyPrintResultSet(rs));
```

# License
GPL v3.
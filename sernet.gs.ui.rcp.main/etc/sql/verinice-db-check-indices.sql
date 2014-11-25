SELECT
    t.relname AS TABLE_NAME,
    i.relname AS index_name,
    a.attname AS column_name
FROM
    pg_class t,
    pg_class i,
    pg_index ix,
    pg_attribute a
WHERE
    t.oid = ix.indrelid
    AND i.oid = ix.indexrelid
    AND a.attrelid = t.oid
    AND a.attnum = ANY(ix.indkey)
    AND t.relkind = 'r'
    AND t.relname IN ('cnalink','cnatreeelement','propertylist','properties','permission','note')
    AND i.relname NOT LIKE '%pkey'
ORDER BY
    t.relname,
    i.relname;

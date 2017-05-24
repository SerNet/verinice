-- Select the number of object types in the database
SELECT
object_type,COUNT(UUID) as number
FROM cnatreeelement
GROUP BY object_type
ORDER by number DESC;

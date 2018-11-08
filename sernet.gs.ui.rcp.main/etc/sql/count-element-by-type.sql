-- Returns the number of elements per type ID from SNCA.

-- All Elements:
SELECT OBJECT_TYPE, COUNT(*) AS NUMBER
FROM CNATREEELEMENT
GROUP BY OBJECT_TYPE
ORDER BY NUMBER DESC;

-- ITBP elements without groups:
SELECT OBJECT_TYPE, COUNT(*) AS NUMBER
FROM CNATREEELEMENT
-- Only ITBP elements
WHERE OBJECT_TYPE LIKE 'bp_%'
-- No groups
AND OBJECT_TYPE NOT LIKE '%_group'
GROUP BY OBJECT_TYPE
ORDER BY NUMBER DESC;


-- ITBP OLD elements without groups:
SELECT OBJECT_TYPE, COUNT(*) AS NUMBER
FROM CNATREEELEMENT
-- No groups
WHERE OBJECT_TYPE NOT LIKE '%kategorie' 
-- No models
AND OBJECT_TYPE NOT LIKE '%model' 
-- No privacy and other unwanted elements
AND OBJECT_TYPE NOT IN ('personengruppen','datenverarbeitung','stellungnahme-dsb','verantwortliche-stelle','verarbeitungsangaben')
GROUP BY OBJECT_TYPE
ORDER BY NUMBER DESC;
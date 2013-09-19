-- verinice Indices zum Verbessern der Performance
CREATE INDEX dependant_id_idx ON cnalink (dependant_id);
CREATE INDEX dependency_id_idx ON cnalink (dependency_id);
CREATE INDEX entity_id_idx ON cnatreeelement (entity_id);
CREATE INDEX parent_idx ON cnatreeelement (parent);
CREATE INDEX typedlist_id_idx ON propertylist (typedlist_id);
CREATE INDEX properties_id_idx ON properties (properties_id);
CREATE INDEX cte_id_idx ON permission (cte_id);
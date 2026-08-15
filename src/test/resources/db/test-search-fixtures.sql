-- Test-only supplementary seed data for Enhanced Owner Search integration tests.
-- Uses distinctive markers ("Zztest" / "Zzdup") to avoid collision with the
-- production seed data in db/h2/data.sql. Loaded once per test class via
-- @Sql(executionPhase = BEFORE_TEST_CLASS).

-- 6 owners sharing last name 'Zztestpage' and telephone '9990001234'
-- Used for:
--   * Last Name pagination (matches "Zztestpage" prefix, 6 owners → 2 pages)
--   * Telephone pagination  (matches "9990001234" exact,  6 owners → 2 pages)
INSERT INTO owners VALUES (100, 'Zt1', 'Zztestpage', '1 Test St', 'TestCity', '9990001234');
INSERT INTO owners VALUES (101, 'Zt2', 'Zztestpage', '2 Test St', 'TestCity', '9990001234');
INSERT INTO owners VALUES (102, 'Zt3', 'Zztestpage', '3 Test St', 'TestCity', '9990001234');
INSERT INTO owners VALUES (103, 'Zt4', 'Zztestpage', '4 Test St', 'TestCity', '9990001234');
INSERT INTO owners VALUES (104, 'Zt5', 'Zztestpage', '5 Test St', 'TestCity', '9990001234');
INSERT INTO owners VALUES (105, 'Zt6', 'Zztestpage', '6 Test St', 'TestCity', '9990001234');

-- 2 additional owners sharing telephone '5551237777' (for "multiple owners same phone")
INSERT INTO owners VALUES (106, 'Zt7', 'Zzduptel', '7 Test St', 'TestCity', '5551237777');
INSERT INTO owners VALUES (107, 'Zt8', 'Zzduptel', '8 Test St', 'TestCity', '5551237777');

-- Pets for the 6 Zztestpage owners. Every pet name contains 'Zztestpet' so a
-- Pet Name contains-search for 'Zztestpet' returns exactly those 6 distinct owners.
INSERT INTO pets VALUES (100, 'Zztestpet1', '2020-01-01', 1, 100);
INSERT INTO pets VALUES (101, 'Zztestpet2', '2020-01-01', 1, 101);
INSERT INTO pets VALUES (102, 'Zztestpet3', '2020-01-01', 1, 102);
INSERT INTO pets VALUES (103, 'Zztestpet4', '2020-01-01', 1, 103);
INSERT INTO pets VALUES (104, 'Zztestpet5', '2020-01-01', 1, 104);
INSERT INTO pets VALUES (105, 'Zztestpet6', '2020-01-01', 1, 105);

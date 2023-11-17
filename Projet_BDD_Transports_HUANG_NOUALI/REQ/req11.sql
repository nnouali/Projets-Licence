/*
    Trouver tous les routes de navigation accesible (pas de route entre des pays en guerre) (recursive)
*/
DROP VIEW if exists not_en_guerre;
CREATE VIEW not_en_guerre AS
SELECT p1.nom AS port1, p2.nom AS port2
FROM port p1 ,port p2, relation r
WHERE p1.nationalite = r.nation1
AND p2.nationalite = r.nation2
AND r.typerelation <> 'en guerre';


WITH RECURSIVE route(depart, destination) AS
(
    SELECT e.port_parent, e.port_dest FROM etapes e
    UNION
    SELECT e.port_parent, r.destination
    FROM etapes e, route r
    WHERE e.port_dest = r.depart
    AND (e.port_parent, r.destination) IN (SELECT * FROM not_en_guerre)
)
SELECT * FROM route ORDER BY depart;

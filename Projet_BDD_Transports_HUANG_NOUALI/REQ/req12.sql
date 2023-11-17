/*
    trouver des voyages intercontinentaux (difini par une distance >= 1000, une categorie de navire = 5 et les 2 ports sont intercontinentaux)
*/

SELECT id_voyage, depart, destination
FROM voyage NATURAL JOIN navire
WHERE navire.categorie = 5
AND (depart, destination) IN(
    SELECT d.port1, d.port2
    FROM distance d, port p1, port p2, nation n1, nation n2
    WHERE kilometre >= 1000
    AND d.port1 = p1.nom AND d.port2 = p2.nom
    AND p1.nationalite = n1.nom
    AND p2.nationalite = n2.nom
    AND n1.continent <> n2.continent
)
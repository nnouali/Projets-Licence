/*
    Montrer les pays ayant plusieurs port enregistre (auto-jointure)
*/

SELECT distinct p1.nationalite, p1.nom
FROM port p1 JOIN port p2
ON p1.nationalite = p2.nationalite
AND p1.nom <> p2.nom
ORDER BY nationalite

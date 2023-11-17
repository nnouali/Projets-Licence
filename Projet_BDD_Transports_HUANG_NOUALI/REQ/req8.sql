/*
Trouver des navires qui ne sont pas encore utilise (jointure externe)
*/

SELECT n.id_navire, n.typenavire, n.nationalite
FROM navire n LEFT JOIN voyage v
ON n.id_navire = v.id_navire
WHERE v.id_voyage IS NULL
ORDER BY n.id_navire;

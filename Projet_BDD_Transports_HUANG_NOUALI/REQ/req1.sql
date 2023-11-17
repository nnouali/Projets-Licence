/* 
    Trouver des long voyages dont le navire de categorie >= 3 (3 tables)
*/

SELECT id_voyage, n.typenavire
FROM voyage v, navire n, distance d
WHERE v.id_navire = n.id_navire
AND v.depart = d.port1 AND v.destination = d.port2
AND n.categorie >= 3 AND d.kilometre >= 2000

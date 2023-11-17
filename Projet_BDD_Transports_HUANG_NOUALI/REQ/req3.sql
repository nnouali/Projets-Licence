/*
Trouvez une requête dont le résultat contient tous les voyages dont la destination est Hambourg.
*/

SELECT v.id_voyage FROM voyage v
WHERE EXISTS
(SELECT e.id_voyage FROM etapes e
WHERE v.id_voyage = e.id_voyage and e.port_dest = 'Hambourg');


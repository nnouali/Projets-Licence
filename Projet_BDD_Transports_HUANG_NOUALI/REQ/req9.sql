/*
    Trouver des voyages n'ayant qu'une etapes(un voyage direct) (avec 2 version : une utilise l'agregation et l'autre pas)
*/

SELECT v.id_voyage, v.depart, v.destination
FROM voyage v
WHERE v.id_voyage IN(
    SELECT e.id_voyage
    FROM etapes e
    GROUP BY e.id_voyage
    HAVING COUNT(distinct e.numero) = 1
);


SELECT v.id_voyage, v.depart, v.destination
FROM etapes e NATURAL JOIN voyage v
WHERE NOT EXISTS(
    SELECT e1.id_voyage
    FROM etapes e1
    WHERE e.id_voyage = e1.id_voyage
    AND e.numero <> e1.numero
);




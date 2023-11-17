/*
    le nb de passagers transportes sur chaque annee
*/

SELECT voy.y, SUM(nbpassager)
FROM voyage NATURAL JOIN (SELECT id_voyage, EXTRACT(YEAR FROM datedebut) AS y FROM voyage) AS voy 
GROUP BY voy.y
ORDER BY voy.y

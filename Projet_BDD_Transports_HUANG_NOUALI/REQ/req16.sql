/*
Trouver pour chaque nationalité, le navire qui peut contenir le plus de passagers
*/

Select nationalite, id_navire, nbPassageMax
FROM navire
WHERE(nationalite, nbPassageMax) IN
	(SELECT nationalite, MAX(nbPassageMax)
	FROM navire 
	GROUP BY nationalite);

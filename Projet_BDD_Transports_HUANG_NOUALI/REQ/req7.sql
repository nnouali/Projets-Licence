/*
Trouver la nationalite dont le navire peut contenir le plus de de volume en moyenne 
*/

Select nationalite, CAST(AVG(nbvolumeMax) AS numeric(5,2))
FROM navire 
GROUP BY nationalite
HAVING AVG(nbvolumeMax) >= (
	SELECT  MAX(moy)
	FROM (SELECT AVG(nbvolumeMax) as moy FROM navire GROUP BY nationalite) as tab);

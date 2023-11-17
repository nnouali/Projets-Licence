/*
 Trouvez le nombre de produits ayant une conservation sec
*/

SELECT nbprod FROM
(SELECT count (DISTINCT id_produit) as nbprod
FROM produit
WHERE conservationsec='TRUE') as P;



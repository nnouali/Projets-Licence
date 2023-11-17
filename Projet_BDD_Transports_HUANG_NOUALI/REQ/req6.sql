/*
    trouver le produit le plus achetee par les vendeurs (GROUP BY et HAVING)
*/

SELECT nom, COUNT(distinct id_voyage) AS nb_voyage
FROM cargaison NATURAL JOIN produit
GROUP BY nom
HAVING COUNT(distinct id_voyage) >= ALL(
    SELECT COUNT(distinct id_voyage)
    FROM cargaison 
    GROUP BY id_produit
    );


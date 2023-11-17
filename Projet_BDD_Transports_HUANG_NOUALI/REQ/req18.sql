/*
    les quantites par produit par annee
*/


SELECT produit.nom,EXTRACT(YEAR FROM datedebut) AS annee , SUM(quantite) AS quantite_total
FROM (SELECT * FROM cargaison WHERE numero_etape = 0) AS c_init
NATURAL JOIN voyage v NATURAL JOIN produit
GROUP BY produit.nom, EXTRACT(YEAR FROM datedebut)
ORDER BY produit.nom

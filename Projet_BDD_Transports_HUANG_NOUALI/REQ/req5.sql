/*
    trouver des voyages ayant vendu(epuise) au moins un des produit (sous-requete WHERE)
*/

SELECT distinct c.id_voyage
FROM cargaison c
WHERE (c.id_voyage, c.numero_etape) in (
    SELECT e.id_voyage, e.numero
    FROM etapes e NATURAL JOIN voyage v
    WHERE v.destination = e.port_dest
    )
AND c.quantite = 0
ORDER BY c.id_voyage
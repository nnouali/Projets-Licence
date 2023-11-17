/*
    indiquer les voyages moyens et longs(> 1000 km) qui ont transporte des produits perissables et supprimer ces cargaison.
*/
DROP MATERIALIZED VIEW if exists illegal;

CREATE materialized VIEW illegal AS
SELECT c.id_voyage, c.numero_etape, c.id_produit, produit.nom
FROM cargaison c 
NATURAL JOIN voyage v
NATURAL JOIN produit
WHERE (depart, destination) IN(
    SELECT d.port1, d.port2
    FROM distance d, port p1, port p2
    WHERE kilometre >= 1000
    AND d.port1 = p1.nom AND d.port2 = p2.nom)
AND produit.conservationsec = FALSE;
SELECT * FROM illegal;

DELETE FROM cargaison
WHERE (id_voyage, numero_etape, id_produit) IN
(
    SELECT id_voyage, numero_etape, id_produit FROM illegal
);

REFRESH materialized VIEW illegal; 
SELECT * FROM illegal;

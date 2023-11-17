/*
    trouver les voyages pendant les quelles les equipages ont achete des nouveaux produits, indiquer les noms de ces prosuits
*/
DROP VIEW if exists new_produit_voyage;
DROP VIEW if exists c_init;
DROP VIEW if exists c_fin;

CREATE VIEW c_init AS
SELECT c.*
FROM cargaison c NATURAL JOIN voyage v
WHERE numero_etape = 0;

CREATE VIEW c_fin AS
SELECT *
FROM cargaison
WHERE (id_voyage, numero_etape) IN (SELECT e.id_voyage, e.numero FROM etapes e NATURAL JOIN voyage v WHERE v.destination = e.port_dest);

CREATE VIEW new_produit_voyage AS
SELECT id_voyage
FROM c_init 
GROUP BY id_voyage, numero_etape
HAVING COUNT(distinct id_produit) <> (SELECT COUNT(distinct c_fin.id_produit) 
                            FROM c_fin 
                            GROUP BY id_voyage, numero_etape
                            HAVING c_fin.id_voyage = c_init.id_voyage);

SELECT * FROM new_produit_voyage;

SELECT distinct nom, c_fin.id_voyage
FROM produit 
JOIN c_fin ON produit.id_produit = c_fin.id_produit
RIGHT JOIN new_produit_voyage ON c_fin.id_voyage =  new_produit_voyage.id_voyage
WHERE produit.id_produit IN (
    SELECT c_fin.id_produit
    FROM c_fin RIGHT JOIN new_produit_voyage ON c_fin.id_voyage =  new_produit_voyage.id_voyage
    WHERE c_fin.id_produit NOT IN (
        SELECT c_init.id_produit
        FROM c_init WHERE c_init.id_voyage IN (
            SELECT new_produit_voyage.id_voyage FROM new_produit_voyage
        )
    )
)
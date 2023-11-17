/*
    trouvez l'argent gagnant de chaque marchandise pour le voyage id 2
*/
DROP VIEW if exists c_init;
DROP VIEW if exists c_fin;

CREATE VIEW c_init AS
SELECT c.*, p.id_caracteristique
FROM cargaison c NATURAL JOIN produit p
WHERE id_voyage = 2
AND numero_etape = 0;

CREATE VIEW c_fin AS
SELECT c.*, p.id_caracteristique
FROM cargaison c NATURAL JOIN produit p
WHERE id_voyage = 2
AND numero_etape = (SELECT e.numero FROM etapes e NATURAL JOIN voyage v WHERE e.id_voyage = 2 AND v.destination = e.port_dest);

SELECT c_init.id_marchandise, CAST(SUM((c_init.quantite - c_fin.quantite)*c_init.volume )*AVG(c.valeuraukilo) AS numeric(6,2) )AS argent
FROM c_init JOIN c_fin ON c_init.id_produit = c_fin.id_produit
JOIN caracteristique c ON c_init.id_caracteristique = c.id_caracteristique AND c_fin.id_caracteristique = c.id_caracteristique
GROUP BY c_init.id_marchandise


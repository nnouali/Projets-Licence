/*
    trouver le voyage qui gagne le plus d'argent. Le montre dans une liste de 'argent' (id_voyage, argent) et les tirer d'ordre decroissant
*/

DROP VIEW if exists c_init;
DROP VIEW if exists c_fin;

CREATE VIEW c_init AS
SELECT c.*, p.id_caracteristique
FROM cargaison c NATURAL JOIN voyage v NATURAL JOIN produit p
WHERE numero_etape = 0;

CREATE VIEW c_fin AS
SELECT c.*, p.id_caracteristique
FROM cargaison c NATURAL JOIN produit p
WHERE (id_voyage, numero_etape) IN (SELECT e.id_voyage, e.numero FROM etapes e NATURAL JOIN voyage v WHERE v.destination = e.port_dest);

SELECT argent_march.id_voyage, CAST(SUM(arg) AS numeric(6,2))as argent
FROM(
    SELECT id_voyage, id_marchandise, SUM((q_init - q_fin)*v_init)*AVG(c.valeuraukilo) AS arg
    FROM (
            SELECT c_init.id_voyage, c_init.numero_etape AS e_ini, c_fin.numero_etape AS e_fin ,c_init.id_marchandise, c_init.id_produit, c_init.volume AS v_init, c_init.quantite AS q_init,c_fin.volume AS v_fin, c_fin.quantite AS q_fin, c_init.id_caracteristique 
            FROM c_init LEFT JOIN c_fin ON (c_init.id_voyage, c_init.id_produit) = (c_fin.id_voyage, c_fin.id_produit)
        ) AS a NATURAL JOIN caracteristique c
    GROUP BY id_voyage, id_marchandise
    ) AS argent_march NATURAL JOIN voyage v
GROUP BY argent_march.id_voyage
ORDER BY argent DESC
;

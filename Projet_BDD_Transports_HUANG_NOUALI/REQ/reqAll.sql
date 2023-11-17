/* drop*/
DROP VIEW if exists new_produit_voyage;
DROP VIEW if exists c_init;
DROP VIEW if exists c_fin;

/* 
 REQUETE 1 : Trouver des long voyages dont le navire de categorie >= 3 (3 tables)
*/

SELECT id_voyage, n.typenavire
FROM voyage v, navire n, distance d
WHERE v.id_navire = n.id_navire
AND v.depart = d.port1 AND v.destination = d.port2
AND n.categorie >= 3 AND d.kilometre >= 2000;

/*
 REQUETE 2 : Montrer les pays ayant plusieurs port enregistre (auto-jointure)
*/

SELECT distinct p1.nationalite, p1.nom
FROM port p1 JOIN port p2
ON p1.nationalite = p2.nationalite
AND p1.nom <> p2.nom
ORDER BY nationalite;

/*
 REQUETE 3 : Trouvez une requête dont le résultat contient tous les voyages dont la destination est Hambourg.
*/

SELECT v.id_voyage FROM voyage v
WHERE EXISTS
(SELECT e.id_voyage FROM etapes e
WHERE v.id_voyage = e.id_voyage and e.port_dest = 'Hambourg');


/*
REQUETE 4 : Trouvez le nombre de produits ayant une conservation sec
*/
SELECT nbprod FROM
(SELECT count (DISTINCT id_produit) as nbprod
FROM produit
WHERE conservationsec='TRUE') as P;

/*
  REQUETE 5 :  trouver des voyages ayant vendu(epuise) au moins un des produit (sous-requete WHERE)
*/

SELECT distinct c.id_voyage
FROM cargaison c
WHERE (c.id_voyage, c.numero_etape) in (
    SELECT e.id_voyage, e.numero
    FROM etapes e NATURAL JOIN voyage v
    WHERE v.destination = e.port_dest
    )
AND c.quantite = 0
ORDER BY c.id_voyage;


/*
 REQUETE 6 :  trouver le produit le plus acheté par les vendeurs (GROUP BY et HAVING)
*/

SELECT nom, COUNT(distinct id_voyage) AS nb_voyage
FROM cargaison NATURAL JOIN produit
GROUP BY nom
HAVING COUNT(distinct id_voyage) >= ALL(
    SELECT COUNT(distinct id_voyage)
    FROM cargaison 
    GROUP BY id_produit
    );


/*
 REQUETE 7 : Trouver la nationalite dont le navire peut contenir le plus de de volume en moyenne 
*/

Select nationalite, CAST(AVG(nbvolumeMax) AS numeric(5,2))
FROM navire 
GROUP BY nationalite
HAVING AVG(nbvolumeMax) >= (
	SELECT  MAX(moy)
	FROM (SELECT AVG(nbvolumeMax) as moy FROM navire GROUP BY nationalite) as tab);

/*
REQUETE 8 : Trouver des navires qui ne sont pas encore utilise (jointure externe)
*/

SELECT n.id_navire, n.typenavire, n.nationalite
FROM navire n LEFT JOIN voyage v
ON n.id_navire = v.id_navire
WHERE v.id_voyage IS NULL
ORDER BY n.id_navire;

/*
 REQUETE 9 :  Trouver des voyages n'ayant qu'une etapes(un voyage direct) (avec 2 version : une utilise l'agregation et l'autre pas)
*/

SELECT v.id_voyage, v.depart, v.destination
FROM voyage v
WHERE v.id_voyage IN(
    SELECT e.id_voyage
    FROM etapes e
    GROUP BY e.id_voyage
    HAVING COUNT(distinct e.numero) = 1
);


SELECT v.id_voyage, v.depart, v.destination
FROM etapes e NATURAL JOIN voyage v
WHERE NOT EXISTS(
    SELECT e1.id_voyage
    FROM etapes e1
    WHERE e.id_voyage = e1.id_voyage
    AND e.numero <> e1.numero
);

/*
 REQUETE 10 :  Des voyages n'ont pas transporte des produits (soit volumecale = 0 soit volumecale < nbvolumeMax) 
 */
SELECT v.*
FROM voyage v NATURAL JOIN navire n
WHERE v.volumecale < n.nbvolumeMax;

SELECT v.*
FROM voyage v NATURAL JOIN navire n
WHERE v.volumecale = 0;
	
/*
  REQUETE 11 :  Trouver tous les routes de navigation accesible (pas de route entre des pays en guerre) (recursive)
*/
DROP VIEW if exists not_en_guerre;
CREATE VIEW not_en_guerre AS
SELECT p1.nom AS port1, p2.nom AS port2
FROM port p1 ,port p2, relation r
WHERE p1.nationalite = r.nation1
AND p2.nationalite = r.nation2
AND r.typerelation <> 'en guerre';


WITH RECURSIVE route(depart, destination) AS
(
    SELECT e.port_parent, e.port_dest FROM etapes e
    UNION
    SELECT e.port_parent, r.destination
    FROM etapes e, route r
    WHERE e.port_dest = r.depart
    AND (e.port_parent, r.destination) IN (SELECT * FROM not_en_guerre)
)
SELECT * FROM route ORDER BY depart;
	
/*
  REQUETE 12 : trouver des voyages intercontinentaux (difini par une distance >= 1000, une categorie de navire = 5 et les 2 ports sont intercontinentaux)
*/

SELECT id_voyage, depart, destination
FROM voyage NATURAL JOIN navire
WHERE navire.categorie = 5
AND (depart, destination) IN(
    SELECT d.port1, d.port2
    FROM distance d, port p1, port p2, nation n1, nation n2
    WHERE kilometre >= 1000
    AND d.port1 = p1.nom AND d.port2 = p2.nom
    AND p1.nationalite = n1.nom
    AND p2.nationalite = n2.nom
    AND n1.continent <> n2.continent
);
	
/*
  REQUETE 13 : trouvez l'argent gagnant de chaque marchandise pour le voyage id 2
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
GROUP BY c_init.id_marchandise;

	
/*
 REQUETE 14 : trouver le voyage qui gagne le plus d'argent. Le montre dans une liste de 'argent' (id_voyage, argent) et les tirer d'ordre decroissant
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
ORDER BY argent DESC;


/*
 REQUETE 15 : le nb de passagers transportes sur chaque annee
*/

SELECT voy.y, SUM(nbpassager)
FROM voyage NATURAL JOIN (SELECT id_voyage, EXTRACT(YEAR FROM datedebut) AS y FROM voyage) AS voy 
GROUP BY voy.y
ORDER BY voy.y;
/*
 REQUETE 16 : Trouver pour chaque nationalité, le navire qui peut contenir le plus de passagers
*/

Select nationalite, id_navire, nbPassageMax
FROM navire
WHERE(nationalite, nbPassageMax) IN
	(SELECT nationalite, MAX(nbPassageMax)
	FROM navire 
	GROUP BY nationalite);
	
	
/*
 REQUETE 17 : trouver les voyages pendant les quelles les equipages ont achete des nouveaux produits, indiquer les noms de ces prosuits
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
);
	
	
/*
 REQUETE 18 : les quantites par produit par annee
*/


SELECT produit.nom,EXTRACT(YEAR FROM datedebut) AS annee , SUM(quantite) AS quantite_total
FROM (SELECT * FROM cargaison WHERE numero_etape = 0) AS c_init
NATURAL JOIN voyage v NATURAL JOIN produit
GROUP BY produit.nom, EXTRACT(YEAR FROM datedebut)
ORDER BY produit.nom;
	
/*
 REQUETE 19 : trouvez le nom du port parent ayant reçu le plus de voyage.
*/

SELECT port_parent, COUNT(distinct id_voyage)
FROM etapes NATURAL JOIN voyage
GROUP BY port_parent
HAVING COUNT(distinct id_voyage) >= ALL(
    SELECT COUNT(distinct id_voyage)
    FROM etapes 
    GROUP BY port_parent );
	
	
/*
REQUETE 20 :  indiquer les voyages moyens et longs(> 1000 km) qui ont transporte des produits perissables et supprimer ces cargaison.
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	

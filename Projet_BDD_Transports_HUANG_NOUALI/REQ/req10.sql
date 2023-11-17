/*
    Des voyages n'ont pas transporte des produits (soit volumecale = 0 soit volumecale < nbvolumeMax)
*/

SELECT v.*
FROM voyage v NATURAL JOIN navire n
WHERE v.volumecale < n.nbvolumeMax;

SELECT v.*
FROM voyage v NATURAL JOIN navire n
WHERE v.volumecale = 0;
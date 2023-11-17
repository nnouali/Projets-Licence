/*
trouvez le nom du port parent ayant reçu le plus de voyage.

*/

SELECT port_parent, COUNT(distinct id_voyage)
FROM etapes NATURAL JOIN voyage
GROUP BY port_parent
HAVING COUNT(distinct id_voyage) >= ALL(
    SELECT COUNT(distinct id_voyage)
    FROM etapes 
    GROUP BY port_parent
    );

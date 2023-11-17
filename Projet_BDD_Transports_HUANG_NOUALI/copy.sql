
\copy nation FROM ./CSV/nation.csv WITH csv header;
\copy relation FROM ./CSV/relation.csv WITH csv header;
\copy port FROM ./CSV/port.csv WITH csv header;
\copy distance FROM ./CSV/distance.csv WITH csv header;
\copy navire FROM ./CSV/navire.csv WITH csv header;
\copy voyage FROM ./CSV/voyage.csv WITH csv header;
\copy etapes FROM ./CSV/etape.csv WITH csv header;
\copy caracteristique FROM ./CSV/caracteristique.csv WITH csv header;
\copy produit FROM ./CSV/produit.csv WITH csv header;
\copy cargaison FROM ./CSV/cargaison.csv WITH csv header;

DROP VIEW if exists new_produit_voyage;
DROP VIEW if exists c_init;
DROP VIEW if exists c_fin;
DROP MATERIALIZED VIEW if exists illegal;


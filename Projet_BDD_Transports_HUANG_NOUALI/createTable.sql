drop table if exists voyage cascade;
drop table if exists port cascade;
drop table if exists nation cascade;
drop table if exists cargaison cascade;
drop table if exists navire cascade;
drop table if exists etapes cascade;
drop table if exists produit cascade;
drop table if exists relation cascade;
drop table if exists caracteristique cascade;
drop table if exists distance cascade;

create table nation (	
	nom text primary key,
	continent text
);
create table relation (	
	nation1 text ,
	nation2 text ,
	typerelation text check (typerelation in ('allies commerciaux', 'allies', 'neutre', 'en guerre')),
	primary key(nation1,nation2,typerelation)
);
create table port (	
	nom text primary key,
	nationalite text, 
	categorie integer check(categorie <=5 and categorie >0 ) , 
	localisation text not null,
	foreign key (nationalite) references nation(nom)
);
create table distance (
	port1 text references port(nom),
	port2 text references port(nom),
	kilometre integer
);
create table navire (	
	id_navire serial primary key, 
	typenavire text not null check(typenavire in ('Flute', 'Galion','Gabare', 'Caraque')), 
	categorie integer check(categorie <=5 and categorie >0 ) , 
	nationalite text not null,
	nbPassageMax integer,
	nbvolumeMax integer,
	foreign key (nationalite) references nation(nom)
);
create table voyage (	
	id_voyage int unique, 
	id_navire int, 
	volumecale integer,
	nbpassager integer,
	datedebut timestamp,
	datefin timestamp,
	depart text references port(nom),
	destination text references port(nom),
	primary key (id_voyage, id_navire),
	foreign key (id_navire) references navire(id_navire),
	check (datedebut < datefin)
);
create table etapes (	
	numero integer, 
	id_voyage integer references voyage(id_voyage), 
	port_parent text not null references port(nom),
	port_dest text not null references port(nom),
	nvPassagers integer,
	primary key (id_voyage, numero)
);
create table caracteristique (
	id_caracteristique integer primary key,
	dureeconservation real, 
	valeuraukilo numeric
);
create table produit (
	id_produit integer primary key ,
	nom text not null,
	conservationsec bool,
	id_caracteristique integer references caracteristique(id_caracteristique)
);
create table cargaison (
	id_voyage integer references voyage(id_voyage),
	numero_etape integer,
	id_marchandise integer,
	id_produit integer references produit(id_produit),
	volume integer, 
	quantite integer,
	primary key (id_voyage, numero_etape, id_marchandise, id_produit)
);


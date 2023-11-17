1. (Identifiants)
  
 + NOUALI Noura, @nouali, 71806787
 + COHEN Chlomite, @cohenc, 71807577

2. (Fonctionnalités)
   
   Pour ce rendu, nous avons implémenté cinq fonctionnalités, réparties selon différents modules, permettant de lire un fichier contenant un programme Polish et le traduire en syntaxe abstraite.
   - En partant de la syntaxe abstraite ainsi obtenue, il sera possible de réafficher ce programme suivant des règles de syntaxe concrète à l'aide de l'option -reprint. 
   - Il sera également possible d'évaluer ce programme grâce à l'option -eval. 
   - De plus, l'option -simpl permettra de simplifier ce programme par une propagation des constantes et l'élimination des blocs "morts". 
   - L'option -vars quant à elle permettra de calculer des variables risquant d'être accédées avant d'êtres écrites.
   - Enfin, l'option -sign fera une analyse statique du signe possible des variables lors du déroulement du programme et determinera le risque de division par zéro.
   	
   Extensions éventuelles:
   1. Utilisation de la bibliothèque zarith lors de l'évaluation pour permettre des calculs en précision arbitraire.
   2. Une analyse statique supplémentaire utilisant cette fois des intervalles au lieu
de juste des signes.
   3. Des enrichissements divers du langage : boucles FOR (et une option permettant leur traduction vers des WHILEs), conditions booléennes plus complètes
(et leur traduction vers des suites de IFs), tableaux, fonctions, ...

   Concernant l'option -sign, les comparaisons n'étant pas prises en compte, le signe obtenu est donc une (super)approximation.
   

3. (Compilation et exécution)
   
   Se placer dans le répertoire du projet puis entrer dans le terminal selon les différents cas:
   
   - Compilation:
       - $ dune build 
       - $ make clear && make 
   
   - Execution (exemple): 
   $ ./run -nom_de_l'option exemples/fact.p (Le nom de l'option pouvant être -reprint, -eval, -simpl, - vars ou -sign)
   
   Nous avons utiliser la bibliothèque externe str.cma pour utiliser regexp https://ocaml.org/api/Str.html

4. (Découpage modulaire)
   
   Le découpage en différents modules permet d'avoir un code plus clair et d'identifier plus précisément les défaillances du code et faciliter ainsi sa correction.
   Voici donc ci-dessous la liste des différents modules avec leur rôle respectif ainsi que les fonctions (avec leur type) les composants.
   
# PRINTER 
   
   ▪️printer.ml : Affiche un programme Polish, soit ses différents composants.
   - la condition: print_cond (e1, c, e2)
   - l'opération: print_expr o
   - l'expression: print_expr e
   - l'instruction: print_inst (i:instr) (tab:int)
   - le block: print_program (p:program) (tab:int) : unit
   - polish: print_polish (p:program) : unit
   	
   	
# READER
   
   ▪️reader.ml : Parse un programme Polish permettant la lecture de celui-ci.
   - prise en compte des tabulations/indentations: 
        + read_indent (str:string) : int
        + instr_body (lines:(int * string) list) (indent:int) : ((int * string) list * (int * string) list)
   - l'opération: read_op (str:string) : op
   - les comparaisons: read_comp (strs:string list) : (comp * string list)
   - la condition (à l'aide de fonctions preésentes dans tools) : read_cond (strs:string list) : cond
   - l'expression: read_expr (strs:string list) (n_line:int) : (expr * string list)
   - les instructions au cas par cas: 
        + read_IF et read_WHILE de type (strs:string list) (lines:(int * string) list) (indent:int) : (instr * (int * string) list)
        + read_SET, read_PRINT, read_READ de type (strs:string list) : instr
   - les instructions de façon génerale: (lines:(int * string) list) : (((int * instr) option) * (int * string) list)
   - ouverture pour la lecture d'un programme Polish:
        + read_program (lines:(int * string) list) : program
        + read_polish (filename:string) : program
   		
   		
# ANALYSE STATIQUE 
   
   ▪️non_init_var.ml : Détermine si une/des variable(s) est/sont initialisée(s) et renvoie la liste des variables initialisées et non initialisées.
   - expression : not_init_expr (exp:expr) (init:string list) : string list
   - instruction : not_init_instr (inst:instr) (init:string list) (ninit:string list) : (string list * string list)
   - programme : not_init_program (prog:program) (init:string list) (ninit:string list) : (string list * string list)
   - polish : not_init_polish (prog:program) : (string list * string list)
   	
   ▪️var_sign.ml : Détermine le signe d'une variable selon les différents cas possibles. La determination du signe de chaque variable, puis celui de l'expression et ainsi de l'instruction permettra  d'obtenir le signe du programme Polish.
   - signe selon les différentes opérations de type (ls:(sign * sign) list) : sign list) : sign_add, sign_sub, sign_mul, sign_div, sign_mod)
   - signe de l'opérateur : sign_op (o:op) (s1:sign list) (s2:sign list) : sign list
   - signe de l'expression : sign_expr (exp:expr) (env:senv) : sign list
   - sign_comp (c:comp) (s1:sign list) (s2:sign list) : sign list
   - signe de l'instruction : sign_instr (inst:instr) (env:senv) : senv
   - signe du programme : sign_program (prog:program) (env:senv) : senv
   - signe de polish : sign_polish (prog:program) : senv
   	
   	
# EVALUATOR 
   
   ▪️evaluator.ml : Evalue un programme Polish.
   - une opération: eval_op (o:op) (e1:expr) (e2:expr) (env:tenv) : int
   - une condition: eval_cond ((e1,c,e2):cond) (env:tenv) : bool
   - une expression: eval_expr (e:expr) (env:tenv) : int
   - une instruction while: eval_while c b env
   - une instruction: eval_instr (i:instr) (pos:position) (env:tenv) : tenv
   - un programme: eval_prog (p:program) (env:tenv) : tenv
   - polish: eval_polish (p:program) : unit
   
   
# SIMPLIFICATION 
   
   ▪️const_prop.ml : Propage les constantes.
   - l'opération: prop_const_op (o:op) (e1:expr) (e2:expr) : expr
   - l'expression: prop_const_expr (e:expr) (env:tenv) : expr
   - l'instruction: prop_const_instr (inst:instr) (env:tenv) : (instr * tenv)
   - le programme: prop_const_program (prog:program) (env:tenv) : (program * tenv)
   - polish: prop_const_polish (prog:program) : program
   	
   ▪️kill_dead_code.ml : Tue/Supprime le code mort.
   - code mort pour l'expression selon différents cas: expr_eq, expr_lt et expr_gt de type (e1:expr) (e2:expr) : (bool option)
   - code mort pour les conditions : pre_eval_cond (c:comp) (e1:expr) (e2:expr) : (bool option)
   - code mort au niveau de l'instrcution: elim_code_mort_instr (inst:instr) : (program option)
   - le programme et polish: elim_code_mort_program et elim_code_mort_polish de type (prog:program) : program
   	
   	
# TOOLS 
   
   ▪️tools.ml: Contient la boîte à outils et différentes fonctions auxiliaires ajoutées pour simplifier le code.
   - cherche si un élément existe dans une liste: contains (e:'a) (l:'a list) : bool
   - supprime les redondances: delete_repeated (l:'a list) : ('a list)
   - sert en particulier pour le sign: cartezian_product (l1:'a list) (l2:'b list) : ('a * 'b) list
   - fusionne les éléments des listes: merge (l1:'a list) (l2:'a list) : 'a list
   - ajoute un élément s'il n'existe pas: add_if_not_exist (e:'a) (l:'a list) : 'a list
   - crée tab tabulation: print_tab (tab:int) : unit
   - fusionne la l2 dans la l1: merge_lists l1 l2 
   - sépare en fonction des espaces: split_line (line:string) : (string list)  
   - fait une sous liste: sub_list (l:'a list) ((s,e):int*int) : ('a list)
   - sépare chaque sous liste: split_strs (strs:string list) (pos:int) : (string list * string list)
   - sert pour la propagation des constantes: bin_op_fun (o:op)
   - supprime la valeur associée a une clé a dans la liste si elle existe dans l'autre liste
        + assoc_delete (l1:('a * 'b) list) (l2:('a * 'b) list) : ('a * 'b) list
        + assoc_clean (l1:('a * 'b) list) (l2:('a * 'b) list) : ('a * 'b) list
   - fait une copie de liste, sert pour copier un environnement: list_copy (l:'a list) : 'a list
   - affiche la liste des varibales initialisées et celles qui ne le sont pas: print_string_list (l:string list) : unit
   - affiche la liste de signes: print_list_sign (l:sign list) : unit
   - affiche la variable et le signe associé: let rec print_sign_env (env:senv) : unit

  
   
   
# TYPES 

   ▪️types.ml: Contient les types imposés initialement et d'autres que nous avons ajouté qui sont les suivants
   - environnement: type tenv = (name * int) list
   - environnement du sign: type senv = (name * sign list) list


# POLISH 

   ▪️polish.ml: Contient le main qui permet d'exécuter les différents modules.
  
	
5. (Organisation du travail)

   Tout d'abord, nous avons lu le sujet afin de comprendre précisément ce qu'il était attendu de nous. Puis, de manière générale nous avons réflechis séparemment sur différentes options avant de regrouper nos idées et de corriger les éventuelles erreurs.
   Concernant le dépot, suite à des problèmes techniques, tout a été déposé par une seule personne.

6. (Misc)

 Des tests ont été ajoutés afin de tester les options -vars, -sign et en particulier le bon fonctionnement de  -killdeadcode et -constprop.



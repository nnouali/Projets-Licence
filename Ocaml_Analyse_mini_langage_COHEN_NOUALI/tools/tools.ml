open Polish_types.Types

(******************)
(* Boite à Outils *)
(******************)

(*chercher si un element existe dans une liste*)
let rec contains (e:'a) (l:'a list) : bool =
  match l with
  | [] -> false
  | x::r -> (x = e) || (contains e r);;

  (*supprimer les redondances*)
let rec delete_repeated (l:'a list) : ('a list) =
  match l with
  | [] -> []
  | x::r -> let res = (delete_repeated r) in
              if (contains x res) then res else x::res;;

(*sert en pariculier pour le sign:
on considere [Neg; Pos] + [Zero; Pos]
on aura ainsi [(Neg,Zero);(Neg,Pos);(Pos,Zero);(Pos,Pos)]*)
let rec cartezian_product (l1:'a list) (l2:'b list) : ('a * 'b) list =
  let rec product e l =
    match l with
    | [] -> failwith ""
    | [x] -> [(e,x)]
    | x::r -> (e,x)::(product e r) in
  match l1 with
  | [] -> failwith ""
  | [x] -> product x l2
  | x::r -> (product x l2) @ (cartezian_product r l2);;

  (* Fusionner elements des listes*)
let rec merge (l1:'a list) (l2:'a list) : 'a list =
  match l1 with
  | []   -> l2
  | x::r -> if (contains x l2) then merge r l2 else x::(merge r l2);;

  (*ajoute un element si il n'existe pas*)
let rec add_if_not_exist (e:'a) (l:'a list) : 'a list =
  match l with
  | [] -> [e]
  | x::r -> if (e = x) then l else x::(add_if_not_exist e r);;

(* Créer tab tabulation *)
let rec print_tab (tab:int) : unit =
  match tab with
  | 0 -> ()
  | _ -> print_string "  "; print_tab (tab-1);;

(* Fusionner la l2 dans l1 *)
let rec merge_lists l1 l2 =
  match l2 with
  | [] -> l1
  | (n,v)::r -> merge_lists ((n,v)::(List.remove_assoc n l1)) r;;

 (* separer en fonction des espaces*)
let split_line (line:string) : (string list) = 
  let strs = String.split_on_char ' ' line in
    List.filter (fun x -> x <> "") strs;;
  (* ["1", ">=", "2"] *)

let rec sub_list (l:'a list) ((s,e):int*int) : ('a list) =
  if (s = e) then []
  else 
    match l with
    | [] -> []
    | x::r -> x::(sub_list r (s+1,e));;

let split_strs (strs:string list) (pos:int) : (string list * string list) =
  (sub_list strs (0,pos), sub_list strs (pos+1,List.length strs));;

let bin_op_fun (o:op) =
  match o with
  | Add -> (+)
  | Sub -> (-)
  | Mul -> ( * )
  | Div -> (/)
  | Mod -> (mod)

  (* supp la val associer  a une clé a dans la liste si elle existe dans l'autre liste*)
let rec assoc_delete (l1:('a * 'b) list) (l2:('a * 'b) list) : ('a * 'b) list =
  match l2 with
  | [] -> l1
  | (a,b)::r -> if (contains (a,b) l1) then assoc_delete l1 r
                else assoc_delete (List.remove_assoc a l1) r

let rec assoc_clean (l1:('a * 'b) list) (l2:('a * 'b) list) : ('a * 'b) list =
  match l1 with
  | [] -> []
  | (a,b)::r -> try (let _ = List.assoc a l2 in (a,b)::(assoc_clean r l2)) with _ -> assoc_clean r l2;;

  (*fait une copie de liste sert pour copier un env *)
let rec list_copy (l:'a list) : 'a list =
  match l with
  | [] -> []
  | x::r -> x::(list_copy r)

let rec print_string_list (l:string list) : unit =
  match l with
  | [] -> ()
  | x::r -> print_string (x ^ " "); print_string_list r;;

  (*affiche la liste de signe*)
let rec print_list_sign (l:sign list) : unit =
  match l with
  | [] -> ()
  | x::r -> print_string ((match x with
            | Neg -> "-"
            | Zero -> "0"
            | Pos -> "+"
            | Error -> "!") ^ " "); print_list_sign r;;
(*affiche la variable et le signe associé*)
let rec print_sign_env (env:senv) : unit = 
  match env with
  | [] -> ()
  | (n,s)::r -> print_string (n ^ " : "); print_list_sign s; print_newline (); print_sign_env r;;
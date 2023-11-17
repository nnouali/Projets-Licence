open Polish_types.Types
open Polish_tools.Tools
(*****************************)
(* Parser un programme : READ*)
(*****************************)

(* Parser un programme Polish *)

(*prise en compte des tabulations/ indentations*)
let read_indent (str:string) : int =
  let indent = ref 0 in
  while (String.get str (!indent) = ' ') do
    indent := !indent + 1;
  done; !indent

let instr_body (lines:(int * string) list) (indent:int) : ((int * string) list * (int * string) list) =
  let rec instr_body_rec (l:(int * string) list) (lr:(int * string) list): ((int * string) list * (int * string) list) =
    match lr with
    | []   -> l, []
    | (pos, x)::r ->  let i = read_indent x in
      if (i > indent) then instr_body_rec (l @ [(pos, x)]) r
      else (l, lr)
  in instr_body_rec [] lines

  (* Parser un programme Polish *)

let rec read_op (str:string) : op =
  match str with
  | "+" -> Add
  | "-" -> Sub
  | "*" -> Mul
  | "/" -> Div
  | "%" -> Mod
  | _   -> failwith ""

and read_comp (strs:string list) : (comp * string list) =
  match strs with
  | [] -> failwith ""
  | x::r -> ((match x with
      | "="  -> Eq | "<>" -> Ne
      | "<"  -> Lt | "<=" -> Le 
      | ">"  -> Gt | ">=" -> Ge
      | _    -> failwith ""), r)



and read_cond (strs:string list) : cond =
  let (e1, remaining_e1) = read_expr strs in
  let (c, remaining_c) = read_comp remaining_e1 in
  let (e2, remaining_e2) = read_expr remaining_c in
  if (remaining_e2 = []) then (e1, c, e2)
  else failwith ""

  (*regexp--> chaine de char entre 0 et 9*)
(*List.tl pour ne pas prendre le premier element de la liste*)
(* imaginons cet arbre, nous devons gerer les differents cas pour une bonne lecture:  

                                        *
                                      /   \
                                     +     3
                                    / \
                                   1   2

*)
and read_expr (strs:string list) : (expr * string list) =
  match strs with
  | []   -> failwith ""
  | [e]  -> if (Str.string_match (Str.regexp "\\-?\\+?[0-9]+") e 0) then (Num(int_of_string e), [])
    else (Var(e), [])
  | x::r -> (match x with
      | "+" | "-" | "*" | "/" | "%" -> let (e1, strs_remaining1) = read_expr r in
        let (e2, strs_remaining2)  = read_expr (strs_remaining1) in
        (Op(read_op x, e1, e2), strs_remaining2)
      | _ -> if (Str.string_match (Str.regexp "\\-?\\+?[0-9]+") x 0) then (Num(int_of_string x), r)
        else (Var(x), r));

(*dans le cas d'une erreur on affiche toujours la ligne où ça ne fonctionne pas*)


and read_IF (strs:string list) (lines:(int * string) list) (indent:int) : (instr * (int * string) list) =
  if (List.length strs >= 3) then 
    let (if_body, instr_remaining) = instr_body lines indent in
      match instr_remaining with
      | [] -> (If(read_cond strs, read_program if_body, []), [])
      | (_, x)::r -> let next_if = split_line x in
                          match List.hd next_if with
                          | "ELSE" -> let ind = read_indent x in
                                        if (indent = ind) then
                                          let (else_body, else_remaining) = instr_body r indent in
                                            (If(read_cond strs, read_program if_body, read_program else_body), else_remaining)
                                        else (If(read_cond strs, read_program if_body, []), instr_remaining)
                          | _ -> (If(read_cond strs, read_program if_body, []), instr_remaining)
  else failwith ""

and read_WHILE (strs:string list) (lines:(int * string) list) (indent:int) : (instr * (int * string) list) =
  if (List.length strs >= 3) then
    let (while_body, instr_remaining) = instr_body lines indent in
    (While(read_cond strs, read_program while_body), instr_remaining)
  else failwith ""

and read_SET (strs:string list) : instr =
  if (List.length strs >= 3 && List.nth strs 1 = ":=") then
    let (e, strs_remaining) = read_expr (List.tl (List.tl strs)) in
    if (strs_remaining = []) then Set(List.hd strs, e) else failwith ""
  else failwith ""

and read_READ (strs:string list) : instr =
  if (List.length strs = 1) then Read(List.nth strs 0)
  else failwith ""

and read_PRINT (strs:string list) : instr =
  if (List.length strs >= 1) then
    let (e, strs_remaining) = read_expr strs in
    if (strs_remaining = []) then Print(e)
    else failwith ""
  else failwith ""

(*lire corectement une instruction polish: cas par cas plus haut *)

and read_instr (lines:(int * string) list) : (((int * instr) option) * (int * string) list) =
  match lines with
  | [] -> (None, [])
  | (n_line, line)::r ->
    try (
      let indent = read_indent line in
      let strs = split_line line in
      match (List.hd strs) with
      | "COMMENT" -> (None, r)
      | "PRINT"   -> (Some(n_line, read_PRINT (List.tl strs)), r)
      | "READ"    -> (Some(n_line, read_READ (List.tl strs)), r)
      | "IF"      -> let (inst, remaining) = read_IF (List.tl strs) r indent in
                        (Some(n_line, inst), remaining)
      | "WHILE"   -> let (inst, remaining) = read_WHILE (List.tl strs) r indent in
                        (Some(n_line, inst), remaining)
      | _         -> (Some(n_line, read_SET strs), r)
    ) with _ -> failwith ("Incorrect syntax at line " ^ string_of_int n_line)

(*ouverture pour la lecture d'un programme polish*)

and read_program (lines:(int * string) list) : program =
  if (lines = []) then []
  else
    let (pos_inst, remaining_lines) = read_instr lines in
    match pos_inst with
    | None -> read_program remaining_lines
    | Some(pos, inst) -> (pos, inst)::(read_program remaining_lines)


and read_polish (filename:string) : program =
  let in_c = open_in filename in
  let lines = ref [] and n_line = ref 1 in
  try
    while true do
      lines := !lines @ [(!n_line, input_line in_c)];
      n_line := !n_line + 1;
    done; read_program !lines
  with End_of_file -> read_program !lines
open Polish_types.Types
open Polish_tools.Tools

(*****************************)
(* Variables non initialisés *)
(*****************************)

(* retourne list des var initialisé et non initialisés*)
let rec not_init_expr (exp:expr) (init:string list) : string list =
  match exp with
  | Num(_)       -> []
  | Var(n)       -> if (contains n init) then [] else [n]
  | Op(_, l, r) -> let ninit_l = (not_init_expr l init) and ninit_r = (not_init_expr r init) in merge ninit_l ninit_r

and not_init_instr (inst:instr) (init:string list) (ninit:string list) : (string list * string list) =
  match inst with 
  | Set(n, e) -> (add_if_not_exist n init, (not_init_expr e init) @ ninit)
  | Read(n) -> (add_if_not_exist n init, ninit)
  | Print(e) -> (init, (not_init_expr e init) @ ninit)
  | If((e1, _, e2), b1, b2) -> let e1_ninit = not_init_expr e1 init and e2_ninit = not_init_expr e2 init in
                                let (b1_init, b1_ninit) = not_init_program b1 init (ninit @ e1_ninit @ e2_ninit) in
                                  let (b2_init, b2_ninit) = not_init_program b2 b1_init b1_ninit in
                                    (b2_init, b2_ninit)
  | While((e1, _, e2), b) -> let e1_ninit = not_init_expr e1 init and e2_ninit = not_init_expr e2 init in
                                let (in_init, in_ninit) = not_init_program b init (ninit @ e1_ninit @ e2_ninit) in (in_init, in_ninit)

and not_init_program (prog:program) (init:string list) (ninit:string list) : (string list * string list) =
  match prog with
  | [] -> (init, ninit)
  | (_, inst)::r -> let (i_init, i_ninit) = not_init_instr inst init ninit in
                        not_init_program r i_init i_ninit

and not_init_polish (prog:program) : (string list * string list) =
  not_init_program prog [] [];;
open Polish_types.Types
open Polish_tools.Tools

(**********************)
(* Signe des variables*)
(**********************)

(* Attention ici nous faisons une suraproximation et nous ne veridion pas 
les signes lors de la comparaisons*)


(*differents cas*)

let rec sign_add (ls:(sign * sign) list) : sign list =
  match ls with
  | [] -> []
  | (s1, s2)::r -> let res = (match s1, s2 with
                              | Neg, Neg | Neg, Zero | Zero, Neg -> [Neg]
                              | Pos, Pos | Zero, Pos | Pos, Zero  -> [Pos]
                              | Neg, Pos | Pos, Neg -> [Neg; Zero; Pos]
                              | Zero, Zero -> [Zero]
                              | Error, _ | _, Error -> [Error]) @ (sign_add r) in delete_repeated res

let rec sign_sub (ls:(sign * sign) list) : sign list =
  match ls with
  | [] -> []
  | (s1, s2)::r -> let res = (match s1, s2 with
                              | Neg, Neg| Zero, Pos | Neg,Zero | Neg, Pos -> [Neg]
                              | Pos, Zero | Zero, Neg -> [Pos]
                              | Pos, Neg -> [Neg; Zero; Pos]
                              | Zero, Zero -> [Zero]
                              | Pos, Pos ->[Zero; Pos]
                              | Error, _ | _, Error -> [Error])
                              @ (sign_sub r) in delete_repeated res

let rec sign_mul (ls:(sign * sign) list) : sign list =
  match ls with
  | [] -> []
  | (s1, s2)::r -> let res = (match s1, s2 with
                              | Neg, Pos | Pos, Neg -> [Neg]
                              | Neg, Neg | Pos, Pos -> [Pos]
                              | Zero, Zero |Pos, Zero | Neg,Zero | Zero, Pos| Zero, Neg -> [Zero]
                              | Error, _ | _, Error -> [Error]) @ (sign_mul r) in delete_repeated res

let rec sign_div (ls:(sign * sign) list) : sign list =
  match ls with
  | [] -> []
  | (s1, s2)::r -> let res = (match s1, s2 with
                              | Neg, Pos | Pos, Neg  -> [Neg]
                              | Neg, Neg| Pos, Pos-> [Pos]
                              | Zero, Neg | Zero, Pos-> [Zero]
                              | Error, _ | _, Error | Pos, Zero | Neg,Zero | Zero, Zero -> [Error])
                              @ (sign_div r) in delete_repeated res

let rec sign_mod (ls:(sign * sign) list) : sign list =
  match ls with
  | [] -> []
  | (s1, s2)::r -> let res = (match s1, s2 with
                              | Pos, Neg | Neg, Neg -> [Neg;Zero]
                              | Neg, Pos| Pos, Pos -> [Zero;Pos]
                              | Zero, Pos| Zero, Neg-> [Zero]
                              | Error, _ | _, Error | Zero, Zero |Pos, Zero | Neg,Zero -> [Error]) @ (sign_mod r) in delete_repeated res                   
            
                            
(* Faire un equivalent pour sub, mul, div, mod *)

let rec sign_op (o:op) (s1:sign list) (s2:sign list) : sign list =
  let sign_prod = cartezian_product s1 s2 in
    let f = (match o with
            | Add -> sign_add
            | Sub -> sign_sub 
            | Mul -> sign_mul
            | Div -> sign_div 
            | Mod -> sign_mod) in f sign_prod;

and sign_expr (exp:expr) (env:senv) : sign list =
  match exp with
  | Num(i) -> (match i with
              | 0 -> [Zero] 
              | _ -> if (i > 0) then [Pos] else [Neg]) 
  | Var(n) -> List.assoc n env
  | Op(o, e1, e2) -> sign_op o (sign_expr e1 env) (sign_expr e2 env)

(* and sign_comp (c:comp) (s1:sign list) (s2:sign list) : sign list = [] *)
  
and sign_instr (inst:instr) (env:senv) : senv =
  match inst with
  | Read(n) -> (n, [Neg; Zero; Pos])::(List.remove_assoc n env)
  | Print(e) -> let (*se*) _ = sign_expr e env in (* Verifier s'il existe un Error dans se *) env
  | Set(n, e) -> let se = sign_expr e env in (n,se)::(List.remove_assoc n env)
  | If ((_, _, _), b1, b2) -> let if_env = sign_program b1 env in
                                let else_env = sign_program b2 if_env in
                                  else_env
  | While ((_, _, _), b) -> sign_program b env 

and sign_program (prog:program) (env:senv) : senv =
  match prog with
  | [] -> env
  | (_, inst)::r -> let new_env = sign_instr inst env in sign_program r new_env

and sign_polish (prog:program) : senv =
  sign_program prog []
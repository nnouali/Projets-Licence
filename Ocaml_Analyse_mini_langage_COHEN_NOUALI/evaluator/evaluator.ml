open Polish_types.Types
open Polish_tools.Tools

(*******************************)
(* Evaluer un programme Polish *)
(*******************************)

(* Evaluation d'une operation polish *)
let rec eval_op (o:op) (e1:expr) (e2:expr) (env:tenv) : int =
  match o with
  | Add -> (eval_expr e1 env) + (eval_expr e2 env)
  | Sub -> (eval_expr e1 env) - (eval_expr e2 env)
  | Mul -> (eval_expr e1 env) * (eval_expr e2 env)
  | Div -> (eval_expr e1 env) / (eval_expr e2 env)
  | Mod -> (eval_expr e1 env) mod (eval_expr e2 env)
  (* Evaluation d'une condition polish *) 

and eval_cond ((e1,c,e2):cond) (env:tenv) : bool =
  match c with
  | Eq -> (eval_expr e1 env) = (eval_expr e2 env)
  | Ne -> (eval_expr e1 env) <> (eval_expr e2 env)
  | Lt -> (eval_expr e1 env) < (eval_expr e2 env)
  | Le -> (eval_expr e1 env) <= (eval_expr e2 env)
  | Gt -> (eval_expr e1 env) > (eval_expr e2 env)
  | Ge -> (eval_expr e1 env) >= (eval_expr e2 env)
(* Evaluation d'une expression polish *)
and eval_expr (e:expr) (env:tenv) : int =
  match e with
  | Num(i)        -> i
  | Var(n)        -> List.assoc n env
  | Op(o, e1, e2) -> eval_op o e1 e2 env

  (* Evaluation d'une instruction While polish *)

and eval_while c b env =
  if (eval_cond c env)
  then let inner_env = eval_prog b env in (eval_while c b (merge_lists env inner_env))
  else env
(* Evaluation d'une instruction polish *)

and eval_instr (i:instr) (pos:position) (env:tenv) : tenv =
  try (
    match i with
    | Set(n, e)     -> let r = (eval_expr e env) in (n, r)::(List.remove_assoc n env)
    | Read(n)       -> print_string (n ^ "? "); let r = read_int () in (n, r)::(List.remove_assoc n env)
    | Print(e)      -> print_int (eval_expr e env); print_newline (); env
    | If(c, b1, b2) ->
                      if (eval_cond c env)
                      then let inner_env = eval_prog b1 env in merge_lists env inner_env
                      else let inner_env = eval_prog b2 env in merge_lists env inner_env
    | While(c, b)   -> eval_while c b env
  ) with _ -> failwith ("Syntax error at line " ^ string_of_int pos)
   (*forme possible: [(pos,instr); (pos, instr);...;[(pos,instr)];...]*)

and eval_prog (p:program) (env:tenv) : tenv =
  match p with
    | []          -> env
    | (pos, i)::r -> let new_env = (eval_instr i pos env) in (eval_prog r new_env)
    (*pour avoir l'env avec la bonne forme*)

and eval_polish (p:program) : unit =
  let _ = eval_prog p [] in ();;
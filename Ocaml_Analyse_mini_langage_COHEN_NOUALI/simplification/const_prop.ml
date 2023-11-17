open Polish_types.Types
open Polish_tools.Tools

(*****************************)
(* Propagation des constantes*)
(*****************************)

let rec prop_const_op (o:op) (e1:expr) (e2:expr) : expr =
  let f = bin_op_fun o in
    match e1, e2 with
    | Num(i1), Num(i2) -> Num(f i1 i2)
    | _, _             -> Op(o, e1, e2)
      
and prop_const_expr (e:expr) (env:tenv) : expr =
  match e with
  | Num(_)        -> e
  | Var(n)        -> (try Num(List.assoc n env) with Not_found -> e)
  | Op(o, e1, e2) -> let simp_e1 = (prop_const_expr e1 env) in
                        let simp_e2 = (prop_const_expr e2 env) in
                          prop_const_op o simp_e1 simp_e2

and prop_const_instr (inst:instr) (env:tenv) : (instr * tenv) =
  match inst with
  | Read(n)   -> (inst, List.remove_assoc n env)
  | Print(e)  -> (Print(prop_const_expr e env), env)
  | Set(n, e) -> let simp_e = prop_const_expr e env in
                    (match simp_e with
                    | Num(i) -> (Set(n, simp_e), (n, i)::(List.remove_assoc n env))
                    | _      -> (Set(n, simp_e), List.remove_assoc n env))
  
  | If((e1, c, e2), b1, b2) ->
      let simp_e1 = prop_const_expr e1 env in
        let simp_e2 = prop_const_expr e2 env in
          let (prog_b1, env_b1) = prop_const_program b1 env in
            let (prog_b2, env_b2) = prop_const_program b2 env in
              let new_env = assoc_clean (assoc_delete (assoc_clean(assoc_delete env env_b1) env_b1) env_b2) env_b2 in
                (If((simp_e1, c, simp_e2), prog_b1, prog_b2), new_env)

  
  | While((e1, c, e2), b)   -> 
      let simp_e1 = prop_const_expr e1 env in
        let simp_e2 = prop_const_expr e2 env in
          let (prop_b, env_b) = prop_const_program b env (* [] *) in
            let new_env = assoc_delete env env_b in
              (While((simp_e1, c, simp_e2), prop_b), new_env)

and prop_const_program (prog:program) (env:tenv) : (program * tenv) =
  match prog with
    | [] -> ([], env)
    | (pos, inst)::r -> let (i, new_env) = prop_const_instr inst env in
                          let (p, new_env2) = (prop_const_program r new_env) in
                            ((pos, i)::p, new_env2)

and prop_const_polish (prog:program) : program =
  let (new_prog, _) = prop_const_program prog [] in new_prog;;
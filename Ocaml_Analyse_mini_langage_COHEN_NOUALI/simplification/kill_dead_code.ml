open Polish_types.Types

(*********************)
(* Tuer/supprimer le code mort *)
(*********************)

let rec expr_eq (e1:expr) (e2:expr) : (bool option) =
  match e1, e2 with
  | Num(i1), Num(i2) -> Some(i1 = i2)
  | Var(n1), Var(n2) -> if (n1 = n2) then Some(true) else None
  | Op(op1, l1, r1), Op(op2, l2, r2) -> (if (op1 = op2) then
                                          match expr_eq l1 l2, expr_eq r1 r2 with
                                          | Some(true), Some(true) -> Some(true)
                                          | _ -> (match expr_eq l1 r2, expr_eq r1 l2 with
                                                  | Some(b1), Some(b2) -> Some(b1 && b2)
                                                  | _ -> None)
                                        else None)
  | _ -> None

and expr_lt (e1:expr) (e2:expr) : (bool option) =
  match e1, e2 with
  | Num(i1), Num(i2) -> Some(i1 < i2)
  | Var(n1), Var(n2) -> if (n1 = n2) then Some(false) else None
  (*| Op(op1, l1, r1), Op(op2, l2, r2) -> None (* A developper *)*)
  | _ -> None

and expr_gt (e1:expr) (e2:expr) : (bool option) =
  match e1, e2 with
  | Num(i1), Num(i2) -> Some(i1 > i2)
  | Var(n1), Var(n2) -> if (n1 = n2) then Some(false) else None
  (*| Op(op1, l1, r1), Op(op2, l2, r2) -> None (* A developper *)*)
  | _ -> None

  (* elimination du code mort pour l'expression differents cas plus detaillés eb haut*)

and pre_eval_cond (c:comp) (e1:expr) (e2:expr) : (bool option) =
  match c with
  | Eq -> expr_eq e1 e2
  | Ne -> (match expr_eq e1 e2 with
            | Some(b) -> Some(not b)
            | None    -> None)
  | Lt -> expr_lt e1 e2
  | Le -> (match expr_lt e1 e2, expr_eq e1 e2 with
            | Some(b1), Some(b2) -> Some(b1 || b2)
            | Some(b), None | None, Some(b) -> Some(b)
            | None, None -> None)
  | Gt -> expr_gt e1 e2
  | Ge -> (match expr_gt e1 e2, expr_eq e1 e2 with
            | Some(b1), Some(b2) -> Some(b1 || b2)
            | Some(b), None | None, Some(b) -> Some(b)
            | None, None -> None)
            
(*suppression du code mort au niveau de l'instruction*)
and elim_code_mort_instr (inst:instr) : (program option) =
  match inst with
  | If((e1, c, e2), b1, b2) -> (match pre_eval_cond c e1 e2 with
                                | Some(true)  -> Some(b1)
                                | Some(false) -> Some(b2)
                                | None        -> None)
  | While((e1, c, e2), b)   -> (match pre_eval_cond c e1 e2 with
                                | Some(true)  -> Some(b)
                                | Some(false) -> Some([])
                                | None        -> None)
  | _                       -> None

and elim_code_mort_program (prog:program) : program =
  match prog with
  | [] -> []
  | (pos, inst)::r -> (match elim_code_mort_instr inst with
                        | Some(b) -> b @ (elim_code_mort_program r)
                        | None    -> (pos, inst)::(elim_code_mort_program r))
                        (**(match inst with
                                      | If(_,_,_) | While(_,_) -> elim_code_mort_program r
                                      | _ -> (pos, inst)::(elim_code_mort_program r))) *)

and elim_code_mort_polish (prog:program) : program =
  elim_code_mort_program prog;;
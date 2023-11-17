open Polish_types.Types
open Polish_tools.Tools

(********************************)
(* Afficher un programme Polish *)
(********************************)

(*Afficher la condition*)
let rec print_cond (e1, c, e2) = 
  match c with
  | Eq -> print_expr e1; print_string "= "; print_expr e2
  | Ne -> print_expr e1; print_string "<> "; print_expr e2
  | Lt -> print_expr e1; print_string "< "; print_expr e2
  | Le -> print_expr e1; print_string "<= "; print_expr e2
  | Gt -> print_expr e1; print_string "> "; print_expr e2
  | Ge -> print_expr e1; print_string ">= "; print_expr e2

  (*Afficher l'operation*)
and print_op o =
  match o with
  | Add -> print_string "+ "
  | Sub -> print_string "- "
  | Mul -> print_string "* "
  | Div -> print_string "/ "
  | Mod -> print_string "% "
(*Afficher l'expression*)
and print_expr e =
  match e with
  | Num(i)        -> print_int i; print_string " "
  | Var(n)        -> print_string n; print_string " "
  | Op(o, e1, e2) -> print_op o; print_expr e1; print_expr e2; print_string " "

  (*Afficher l'instruction*)
and print_inst (i:instr) (tab:int) =
  match i with
  | Set(n,e) -> print_tab tab; print_string n; print_string " := "; print_expr e; print_newline ();
  | Read(n) -> print_tab tab; print_string "READ "; print_string n; print_newline ();
  | Print(e) -> print_tab tab; print_string "PRINT "; print_expr e; print_newline ();
  | If(c, b1, b2) ->
      print_tab tab; print_string "IF "; print_cond c; print_newline ();
      print_program b1 (tab+1);
      if (b2 = []) then ()
      else
        begin
          print_tab tab; print_string "ELSE"; print_newline ();
          print_program b2 (tab+1);
        end
  | While(c, b) ->
    print_tab tab; print_string "WHILE "; print_cond c; print_newline (); print_program b (tab+1); 
  (*Afficher le block*)

and print_program (p:program) (tab:int) : unit =
  match p with
  | [] -> ()
  | (_, i)::r -> print_inst i tab; print_program r tab;

and print_polish (p:program) : unit =
  print_program p 0;;
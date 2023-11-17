(** Projet Polish -- Analyse statique d'un mini-langage impératif *)

(** Note : cet embryon de projet est pour l'instant en un seul fichier
    polish.ml. Il est recommandé d'architecturer ultérieurement votre
    projet en plusieurs fichiers source de tailles raisonnables *)

open Polish_types.Types
open Polish_evaluator.Evaluator
open Polish_tools.Tools
open Polish_printer.Printer
open Polish_reader.Reader
open Polish_simplification.Const_prop
open Polish_simplification.Kill_dead_code
open Polish_analyse_statique.Non_init_var
open Polish_analyse_statique.Var_sign

let usage () =
  print_string "Polish : analyse statique d'un mini-langage\n";
  print_string "usage: ./run -nom_de_l'option exemples/fact.p";;

let main () =
  match Sys.argv with
  | [|_;"-reprint";file|] -> print_polish (read_polish file)
  | [|_;"-eval";file|]    -> eval_polish (read_polish file)
  | [|_;"-constprop";file|]   -> print_polish (prop_const_polish (read_polish file))
  | [|_;"-killdeadcode";file|]   -> print_polish (elim_code_mort_polish (read_polish file))
  | [|_;"-simpl";file|]   -> print_polish (elim_code_mort_polish (prop_const_polish (read_polish file)))
  | [|_;"-vars";file|]    -> let (init, ninit) = not_init_polish (read_polish file) in
                                print_string "Initialized vars : "; print_string_list (delete_repeated init); print_newline (); print_string "Non-initialized vars : "; print_string_list (delete_repeated ninit)
  | [|_;"-sign";file|]    -> let env = sign_polish (read_polish file) in print_sign_env env;
  | _                     -> usage ();;

(* lancement de ce main *)
let () = main ();;
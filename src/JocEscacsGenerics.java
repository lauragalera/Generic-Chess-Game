/** @file JocEscacsGenerics.java
    @brief Un joc d'escacs genèrics amb mode text i mode gràfic
    @author David Pérez Sánchez i Laura Galera Alfaro
 */

import javafx.application.Application;

/** @class JocEscacsGenerics
    @brief Programa principal
 */
public class JocEscacsGenerics {
    /**
     @pre \p args és -t o -g.
     @post Executa un joc de d'escacs genèric; amb l'opció -t s'executa en mode text i amb l'opció -g en mode gràfic.
     */
    public static void main(String[] args) {
        boolean mostrarAjuda = false;

        try {
            if(args.length == 1) {
                if(args[0].equals("-t")) EscacsTXT.jugar();
                else if(args[0].equals("-g")) {
                    Application.launch(EscacsGrafic.class,args);
                }
                else mostrarAjuda = true;
            }
            else mostrarAjuda = true;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        if(mostrarAjuda)
            System.out.println("\nUtilització: \n" +
                    "\t\tMode gràfic\t->\tjava -jar JocEscacsGenerics -g\n" +
                    "\t\tMode text\t->\tjava -jar JocEscacsGenerics -t\n");
    }
}

/**
 * @file EscacsTXT.java
 * @brief Joc d'escacs en mode text.
 * @author David Pérez Sánchez
 */

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 * @class EscacsTXT
 * @brief Mòdul que actua com a una interfície d'usuari en mode text i permet jugar una partida d'escacs.
 */
public abstract class EscacsTXT{
    /**
     * @brief Jugar una partida d'escacs en mode text.
     * @pre ---
     * @post Es crea una partida d'escacs segons les dades entrades i es gestiona la partida de forma que pot jugar
     *       l'usuari i/o la CPU, gestionant totes les interaccions que això implica.
     */
    public static void jugar() {
        // Mostrar missatge d'entrada
        System.out.println("JOC D'ESCACS GENÈRIC EN MODE TEXT");

        // Variables
        boolean calAcabar = false;
        boolean taulesDemanades = false;

        // Demanar dades a l'usuari
        Partida partida = demanarDades();
        Pair<Boolean,Boolean> jugadors = demanarCPU();
        mostrarEstatTauler(partida);

        do {
            try {

                // Mostrar tauler i torn
                if(!taulesDemanades) System.out.println(partida.dibuixTauler());
                System.out.println(Partida.COLOR[partida.tornActual()] + ":\t");

                // Mirar si és torn de la CPU
                boolean tornCPU = (partida.tornActual()==0 && jugadors.first) ||
                        (partida.tornActual()==1 && jugadors.second);

                // Gestionar tirades
                Pair<Boolean,Boolean> res;
                if(tornCPU) res = gestionarCPU(partida,taulesDemanades);
                else res = gestionarJugador(partida,taulesDemanades);
                calAcabar = res.first;
                taulesDemanades = res.second;

                // Mostrar informacio sobre l'estat del tauler
                mostrarEstatTauler(partida);
            } catch (ExcepcioJugadaErronia e) {
                System.out.println(e.getMessage());
            }
        } while(!calAcabar);

        // Un cop s'ha acabat de jugar mostrar l'estat final del tauler i el resultat de la partida
        System.out.println("Estat final del tauler:");
        System.out.println(partida.dibuixTauler());
        System.out.println(partida.resultatPartida());

        // Guardar partida
        Scanner in = new Scanner(System.in);
        String path = "";
        boolean fitxerCorrecte;
        do {
            try {
                System.out.print("Entra el nom del fitxer on es guardarà la partida:  ");
                path = in.nextLine();
                Saver.guardarPartida(path, partida.dadesDesenvolupament());
                fitxerCorrecte = true;
            } catch (IOException e) {
                System.out.println("Error: no s'ha pogut obrir el fitxer");
                fitxerCorrecte = false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                fitxerCorrecte = false;
            }
        } while(!fitxerCorrecte);
    }

    /**
     * @brief Gestió d'una jugada efectuada per la CPU.
     * @pre \p partida != null
     * @post Si \p taulesDemanades era cert, es gestionen les taules de la CPU. Altrament la CPU efectua una jugada. Es
     *       retorna un Pair amb un booleà que indica si cal acabar la partida i un altre que indica si s'han demanat
     *       taules.
     * @param partida Partida en què s'està jugant.
     * @param taulesDemanades Indica si s'han demanat taules al torn anterior.
     * @return Un Pair amb un booleà que és cert si la partida ha finalitzat i cal gestionar-ne l'acabament i un segon
     *         booleà que és cert si s'han demanat taules.
     */
    private static Pair<Boolean,Boolean> gestionarCPU(Partida partida, boolean taulesDemanades){
        boolean calAcabar;

        // Gestionar taules
        if(taulesDemanades) {
            int contrincant = partida.tornActual() == 0 ? 1 : 0;
            System.out.print("El jugador de " + Partida.COLOR[contrincant] + " t'ha demanat taules. Acceptes? " +
                    "[acceptar/denegar]:  ");
            Partida.TiradaEspecial tirada = partida.respondreTaulesCPU(); //acceptar o denegar
            System.out.println(tirada.toString());
            calAcabar = partida.efectuarTiradaEspecial(tirada);
        }
        // Gestionar tirades
        else calAcabar = partida.efectuarJugadaCPU();

        return new Pair<>(calAcabar,false);
    }

    /**
     * @brief Gestió d'una jugada efectuada per un jugador.
     * @pre \p partida != null
     * @post Si \p taulesDemanades era cert, es gestiona la sol·licitud de taules preguntant al jugador si les accepta.
     *       Altrament es llegeix una tirada i s'efectua en cas que sigui possible.
     * @param partida Partida en què s'està jugant.
     * @param taulesDemanades Indica si s'han demanat taules al torn anterior.
     * @return Un Pair amb un booleà que és cert si la partida ha finalitzat i cal gestionar-ne l'acabament i un segon
     *         booleà que és cert si s'han demanat taules.
     * @throws ExcepcioJugadaErronia Si la jugada no té un format correcte o si la situació de la partida i/o el tauler
     *                               no permeten efectuar la jugada.
     */
    private static Pair<Boolean,Boolean> gestionarJugador(Partida partida, boolean taulesDemanades)
            throws ExcepcioJugadaErronia {
        boolean calAcabar = false;
        Scanner in = new Scanner(System.in);

        String input;
        // Gestionar taules
        if(taulesDemanades) {
            int contrincant = partida.tornActual() == 0 ? 1 : 0;
            System.out.print("El jugador de " + Partida.COLOR[contrincant] + " t'ha demanat taules. Acceptes? [acceptar/denegar]:  ");
            input = in.nextLine().toLowerCase();
            if(input.equals("acceptar"))
                calAcabar = partida.efectuarTiradaEspecial(Partida.TiradaEspecial.ACCEPTAR_TAULES);
            else if (input.equals("denegar")) {
                taulesDemanades = false;
                calAcabar = partida.efectuarTiradaEspecial(Partida.TiradaEspecial.DENEGAR_TAULES);
            }
            else System.out.println("Has d'acceptar o denegar les taules abans de continuar\n");
        }
        // Gestionar tirades
        else {
            input = in.nextLine().toLowerCase();
            if(comprovarTirada(input))
                calAcabar = aplicarTirada(partida, input);
            else {
                String[] s = input.trim().split(" ");
                switch (s[0]) {
                    case "?":
                        mostrarAjuda();
                        break;
                    case "taules":
                        taulesDemanades = true;
                        calAcabar = partida.efectuarTiradaEspecial(Partida.TiradaEspecial.DEMANAR_TAULES);
                        break;
                    case "rendir":
                        calAcabar = partida.efectuarTiradaEspecial(Partida.TiradaEspecial.RENDIR);
                        break;
                    case "ajornar":
                        calAcabar = partida.efectuarTiradaEspecial(Partida.TiradaEspecial.AJORNAR);
                        break;
                    case "desfer":
                        if(s.length == 2)
                            desferJugada(partida,Integer.parseInt(s[1]));
                        else throw new ExcepcioJugadaErronia("Format: desfer n\n");
                        calAcabar = false;
                        break;
                    case "refer":
                        if(s.length == 2)
                            referJugada(partida,Integer.parseInt(s[1]));
                        else throw new ExcepcioJugadaErronia("Format: refer n\n");
                        calAcabar = false;
                        break;
                    default:
                        throw new ExcepcioJugadaErronia("Format de la jugada incorrecte\n");
                }
            }
        }

        return new Pair<>(calAcabar,taulesDemanades);
    }

    /**
     * @brief Demanar les dades per iniciar la partida.
     * @pre ---
     * @post Es demana quin fitxer s'ha de carregar i es retorna una partida carregada amb les dades contingudes al
     *       fitxer indicat, en cas que es pugui obrir i que el format sigui correcte. En cas que sigui un fitxer de
     *       regles, la partida s'iniciarà però, si és un fitxer de desenvolupament, la partida es reprendrà
     *       desdel punt on es va ajornar.
     * @return Una partida inicialitzada si s'ha carregat un fitxer de regles o una partida en el punt d'ajornament si
     *         s'ha carregat un fitxer de desenvolupament.
     */
    private static Partida demanarDades() {
        // Demanar dades
        Scanner in = new Scanner(System.in);
        String input;
        do {
            System.out.print("Vols iniciar o carregar una partida? [i/c]:  ");
            input = in.nextLine().toLowerCase();
        } while (!input.equals("i") && !input.equals("c"));
        Partida partida = null;
        String path;
        boolean inputCorrecte;
        do {
            try {
                if (input.equals("i")) {
                    System.out.print("Entra la ruta del fitxer de regles:  ");
                    path = in.nextLine();
                    partida = Loader.carregarPartidaNova(path);
                } else {
                    System.out.print("Entra la ruta del fitxer de desenvolupament de la partida:  ");
                    path = in.nextLine();
                    partida = Loader.carregarPartidaComencada(path);
                }
                inputCorrecte = true;
            }
            catch (IOException e) {
                System.out.println("Error: no s'ha pogut obrir el fitxer " + e.getMessage());
                inputCorrecte = false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                inputCorrecte = false;
            }
        } while(!inputCorrecte);

        return partida;
    }

    /**
     * @brief Demanar quins jugadors són humans i quins són controlats per la CPU.
     * @pre ---
     * @post Es demana per pantalla que s'esculli si el jugador de blanques i/o de negres han d'estar controlats per la
     *       CPU. En cas d'entrar una \b s, el jugador indicat passarà a realitzar tirades i respondre a les taules de
     *       forma automàtica. En cas d'entrar \b n, el jugador s'interpreta com a humà i se li demanaran les tirades.
     *       Es retorna un Pair amb un booleà que indica si el jugador de blanques és CPU i un altre booleà que indica
     *       si el jugador de negres és CPU.
     * @return Un Pair amb dos booleans, el primer indica si el jugador de blanques és CPU, el segon si ho és el de
     *         negres.
     */
    private static Pair<Boolean, Boolean> demanarCPU(){
        Scanner in = new Scanner(System.in);
        String input;
        boolean inputCorrecte;
        do {
            System.out.print("Jugador de blanques CPU? [s/n]:  ");
            input = in.nextLine();
            inputCorrecte = input.equals("s") || input.equals("n");
        } while (! inputCorrecte);
        boolean jugCpuB = input.equals("s");
        do {
            System.out.print("Jugador de negres CPU? [s/n]:  ");
            input = in.nextLine();
            inputCorrecte = input.equals("s") || input.equals("n");
        } while (! inputCorrecte);
        boolean jugCpuN = input.equals("s");

        return new Pair<>(jugCpuB, jugCpuN);
    }

    /**
     * @brief Mostrar en quin estat es troba el tauler, en concret els reis de cada bàndol.
     * @pre \p partida != null
     * @post Es mostra per pantalla un missatge d'avís quan es produeix una situació d'escac, escac i mat o rei ofegat.
     * @param partida Partida en què s'està jugant.
     */
    private static void mostrarEstatTauler(Partida partida) {
        int contrincant = partida.tornActual() == 0 ? 1 : 0;
        switch (partida.estatReiContrincant(contrincant)){
            case ESCAC:
                System.out.println("ESCAC AL REI DE " + Partida.COLOR[partida.tornActual()] + '!');
                break;
            case ESCAC_MAT:
                System.out.println("ESCAC I MAT AL REI DE " + Partida.COLOR[partida.tornActual()] + '!');
                break;
            case REI_OFEGAT:
                System.out.println("EL REI DE " + Partida.COLOR[partida.tornActual()] + " ESTÀ OFEGAT!");
                break;
            default:
                break;
        }
    }

    /**
     * @brief Demanar per pantalla les dades d'una promoció.
     * @pre \p nomPromocionables no està buit, \p posPromo != null
     * @post Es demana per pantalla el nom de la peça a la qual es vol promocionar la peça que es troba a la poscició
     *       \p posPromo. Aquest nom ha d'existir a \p nomPromocionables. Es retorna aquest nom en majúscules.
     * @param nomPromocionables Nom de les peces a les que la peça situada a la posició \p posPromo pot promocionar.
     * @param posPromo Posició on es troba la peça que pot promocionar.
     * @param nomPecaVella Nom de la peça que pot promocionar.
     * @return El nom de la peça a la que es vol promocionar en majúscules.
     */
    private static String demanarPromocio(Set<String> nomPromocionables, Posicio posPromo, String nomPecaVella) {
        Scanner in = new Scanner(System.in);

        System.out.println("La peça \"" + nomPecaVella + "\" que es troba a la posició \"" +
                            posPromo + "\"" + " pot promocionar.\n" +
                            nomPromocionables + "\nIntrodueix el nom d'una peça (mateix nom per cancel·lar).");
        String input;
        boolean existeixPeca;
        do {
            input = in.nextLine().toUpperCase();
            existeixPeca = nomPromocionables.contains(input);
            if(!existeixPeca) System.out.println("Cal escollir una de les peces mostrades.");
        } while (!existeixPeca);

        return input;
    }

    /**
     * @brief Es comprova que la tirada \p input tingui el format d'una jugada ordinària o una jugada enroc.
     * @param input Tirada a la qual es vol evaluar el format.
     * @return Cert si \p input té el format d'una jugada ordinària o d'una jugada enroc, fals altrament.
     */
    private static boolean comprovarTirada (String input) {
        return input.matches(JugadaOrdinaria.regex) || input.matches(JugadaEnroc.regex);
    }

    /**
     * @brief Efectuar una tirada.
     * @pre \p p != null i \p input té el format d'una jugada ordinària o enroc.
     * @post S'efectua la tirada \p input a la partida \p p. Si \p input té format d'una jugada ordinària, es realitza
     *       una jugada ordinària, altrament, una jugada enroc. També es demana i s'efectua la promoció en cas
     *       d'haver-n'hi. Es retorna un booleà que és cert si cal acabar la partida.
     * @param p Partida en què s'efectuarà la tirada i la promoció (en cas d'haver-n'hi).
     * @param input Tirada que es vol efectuar.
     * @return Un booleà que és cert si la partida \p ha finalitzat i cal gestionar l'acabament de la partida, fals
     *         altrament.
     * @throws ExcepcioJugadaErronia Si la situació de la partida i/o el tauler no permeten efectuar la jugada.
     */
    private static boolean aplicarTirada (Partida p, String input) throws ExcepcioJugadaErronia {
        // Crear tipus de jugada pertinent
        Jugada jug;
        boolean esOrdinaria = input.matches(JugadaOrdinaria.regex);
        if(esOrdinaria) jug = new JugadaOrdinaria(input);
        else jug = new JugadaEnroc(input);

        // Efectuar la jugada
        boolean calAcabar = p.efectuarTiradaOrdinaria(jug);

        // Comprovar promocio
        Posicio posPromo = p.posicioPromocio();
        if(posPromo != null) {
            Set<String> nomPromocionables = p.nomPromocionables();
            if(!nomPromocionables.isEmpty()) {
                String nomPecaVella = p.pecaTauler(posPromo).nom();
                String nomPecaNova = demanarPromocio(nomPromocionables, posPromo, nomPecaVella);
                if (!nomPecaNova.equals(nomPecaVella))
                    calAcabar = p.efectuarPromocio(posPromo, nomPecaNova);
            }
        }

        return calAcabar;
    }

    /**
     * @brief Desfer un determinat nombre de jugades.
     * @pre \p p != null i 0 <= \p n
     * @post Es desfan jugades a la partida \p p fins que s'assoleixi el límit \p n o fins que no quedi cap jugada per
     *       desfer.
     * @param p Partida en què es volen desfer les jugades.
     * @param n Nombre de jugades que es vol desfer.
     */
    private static void desferJugada(Partida p, int n) {
        try {
            for (int i = 0; i < n; i++)
                p.desferJugada();
        }
        catch (CannotUndoException e) {
            System.out.println("No hi ha res per desfer");
        }
    }

    /**
     * @brief Refer un determinat nombre de jugades.
     * @pre p != null i 0 <= \p n
     * @post Es refan jugades a la partida \p p fins que s'assoleixi el límit \p n o fins que no quedi cap jugada per
     *       refer.
     * @param p Partida en què es vol refer una jugada.
     * @param n Nombre de jugades que es vol refer.
     */
    private static void referJugada(Partida p, int n) {
        try {
            for (int i = 0; i < n; i++)
                p.referJugada();
        }
        catch (CannotRedoException e) {
            System.out.println("No hi ha res per refer");
        }
    }

    /**
     * @brief Mostra per pantalla un missatge amb el format de les possibles tirades que s'admeten.
     */
    private static void mostrarAjuda() {
        System.out.println(
                "Comandes disponibles:\n" +
                "\t?\t\t\tMostra aquest missatge d'ajuda.\n" +
                "\ttaules\t\tDemana tauler a l'altre jugador.\n" +
                "\t\tacceptar\tAccepta acabar la partida en taules si l'altre jugador les ha demanat.\n" +
                "\t\tdenegar\t\tRebutja acabar la partida en taules si l'altre jugador les ha demanat.\n" +
                "\tajornar\t\tAcaba i guarda la partida per continuar-la més endavant.\n" +
                "\trendir\t\tAcaba la partida cedint la victòria al contrincant.\n" +
                "\tdesfer n\t\tDesfà les n jugades realitzades.\n" +
                "\trefer n\t\tTorna a fer les n darreres jugades desfetes.\n" +
                "\tFormat de les jugades:\n" +
                "\t\tP1 P2\t\tMoure la peça de la posició P1 a la P2.\n" +
                "\t\tP1 - P2\t\tRealitzar un enroc entre les peces que hi ha a P1 i P2.\n"
        );
    }
}
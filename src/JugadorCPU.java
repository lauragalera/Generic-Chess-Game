/** @file JugadorCPU.java
    @brief Un jugador AI.
    @author Laura Galera Alfaro
 */

import java.util.*;

/** @class  JugadorCPU
    @brief  Jugador controlat per la CPU.
    @details Realitza jugades ordinàries i enrocs, escull si vol promocionar i accepta o denega taules sol·licitades.
 */

public abstract class JugadorCPU{
    private final static int maxProf = 2; ///< Enter que indica la profunditat màxima a la que s'explora l'arbre de joc.

    /** @brief  Es retorna la jugada del jugadorCPU.
        @pre    \p t no pot ser buit.
        @param  t és el tauler d'escacs sobre el que s'està disputant la partida.
        @param  jugador és el color del jugadorCPU: 1 si és el negre i 0 si és el blanc.
        @post   Es retorna la jugada que desitja fer el jugadorCPU, tan pot ser ordinària com un enroc.
     */
    public static Jugada demanarJugada(TaulerEscacs t, int jugador){
        return millorJugada(t, jugador);
    }

    /** @brief  Indica si el JugadorCPU accepta o no les taules del contrincant.
        @pre    El jugador contrari ha sol·licitat taules. <br>
                El \c TaulerEscacs \p t no pot ser buit.
        @param  t és el tauler d'escacs sobre el que s'està disputant la partida.
        @param  jugador és el color del jugadorCPU: 1 si és el negre i 0 si és el blanc.
        @post   Es retorna el valor de l'enumeració \a TiradaEspecial, que hi ha a \c Partida, indicant si
                el jugadorCPU desitja acceptar les taules, \a ACCEPTAR_TAULES, o, pel contrari, prefereix denegar-les, \a DENEGAR_TAULES.
     */
    public static Partida.TiradaEspecial decidirTaules(TaulerEscacs t, int jugador){
        int valor = minimax(t, 0, false, Integer.MIN_VALUE, Integer.MAX_VALUE, jugador); //la última jugada ha estat del jugadorCPU per tant cal minimitzar.
        if(valor<0) return Partida.TiradaEspecial.ACCEPTAR_TAULES;
        else return Partida.TiradaEspecial.DENEGAR_TAULES;
    }

    /** @brief  Retorna el nom de la peça que s'usarà per a la promoció.
        @pre    El \c TaulerEscacs \p t no pot ser buit.
                Hi ha una peça del jugador CPU promocionable a \p pos.
        @param  t és el tauler d'escacs sobre el que s'està disputant la partida.
        @param  llPecesDisponibles és un set de peces que contè totes aquelles per
                a les que es pot promocionar, pintades del color pròpi de les peces del
                jugadorCPU.
        @param  pos és la posició del tauler on es troba la peça del jugadorCPU candidata a
                ser promocionada.
        @param  jugador és el color del jugadorCPU: 1 si és el negre i 0 si és el blanc.
        @post   Es retorna un \c String del nom de la peça que substituirà a la
                que promociona. Si el nom és el mateix que el de la peça que hi ha
                en un bon principi és perquè no es vol promocionar.
     */
    public static String promocionarPeca(TaulerEscacs t, Set<Peca> llPecesDisponibles, Posicio pos, int jugador){
        int maxim = Integer.MIN_VALUE;
        Peca promociona = t.solicitarPeca(pos);
        for(Peca p : llPecesDisponibles) { //per cada peça per a la que es pot promocionar
            TaulerEscacs nouTauler = new TaulerEscacs(t); //es fa una copia del tauler actual.
            nouTauler.efecuarPromocio(p, pos); //s'efectua la promoció
            int puntuacio = minimax(nouTauler, 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, jugador); //es simulen les jugades de l'adversari i s'obté el valor del tauler que millor minimitza les pèrdues.
            if (maxim < puntuacio) { //ens quedem amb la peça que ens dona un valor més gros
                maxim = puntuacio;
                promociona = p;
            }
        }
        return promociona.nom();
    }

    //*************************************************************************************************MÈTODES PRIVATS*****************************************************************************************

    /** @brief  Retorna la Jugada que millor minimitza la pèrdua màxima del jugadorCPU.
        @pre    El \c TaulerEscacs \p t no pot ser buit.
        @param  t és el tauler d'escacs sobre el que s'està disputant la partida.
        @param  jugador és el color del jugadorCPU: 1 si és el negre i 0 si és el blanc.
        @post   Es retorna la jugada, ja sigui enroc o ordinària, que aconsegueix maximitzar els guanys
                del jugadorCPU, considerant que el jugador contrari sempre escollirà el pitjor cas pel jugadorCPU.
     */
    private static Jugada millorJugada(TaulerEscacs t, int jugador) {
        int millorValor = Integer.MIN_VALUE;
        List<Jugada> jugades = new ArrayList<>();
        Jugada jugada = null;
        for (int i = 1; i <= t._fila; i++) {
            for (int j = 1; j <= t._columna; j++) {
                Posicio posPeca = new Posicio(j, t._fila + 1 - i);
                Peca peca = t.solicitarPeca(posPeca);
                if (peca != null && peca.color() == jugador) {
                    for (Posicio desti : t.solicitarDestinsPeca(posPeca)) {
                        JugadaOrdinaria jugadaOrd = new JugadaOrdinaria(posPeca, desti);
                        if (t.esPotFerJugadaOrdinaria(jugadaOrd, jugador)) {
                            TaulerEscacs nouTauler = new TaulerEscacs(t);
                            nouTauler.aplicarJugadaOrdinaria(jugadaOrd);
                            int valor = minimax(nouTauler, 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, jugador);
                            if (valor > millorValor) {
                                millorValor = valor;
                                jugades.clear();
                                jugades.add(jugadaOrd);
                            }else if(valor == millorValor){
                                jugades.add(jugadaOrd);
                            }
                        }
                    }
                    for (Posicio posEnroc : t.solicitarCompanyesEnroc(posPeca)) {
                        JugadaEnroc jugadaEnr = new JugadaEnroc(posPeca, posEnroc);
                        if (t.esPotFerJugadaEnroc(jugadaEnr, jugador)) {
                            TaulerEscacs nouTauler = new TaulerEscacs(t);
                            nouTauler.aplicarJugadaEnroc(jugadaEnr);
                            int valor = minimax(nouTauler, 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, jugador);
                            if (valor > millorValor) {
                                millorValor = valor;
                                jugades.clear();
                                jugades.add(jugadaEnr);
                            }else if(valor == millorValor){
                            jugades.add(jugadaEnr);
                            }
                        }
                    }
                }
            }
        }
        Random r = new Random();
        int numRandom = r.nextInt(jugades.size());
        jugada = jugades.get(numRandom);
        return jugada;
    }


    /** @brief  Retorna l'heurístic del tauler que minimitza la pèrdua màxima esperada per al jugadorCPU.
        @pre    \p profunditat >= 0 i \p profunditat <= 2.
                \p t != null.
        @param  t és el tauler que es prén com a referència per aplicar les diferents jugades del nivell.
        @param  profunditat és la profunditat a la que s'ha arribat en l'exploració de l'arbre de joc.
        @param  maximitzant és cert quan es maximitza el guany del jugadorCPU i fals quan es minimitza l'efecte del jugador contrincant.
        @param  alpha és el valor de l'heurístic del millor tauler fins el moment quan es maximitza.
        @param  beta és el valor de l'heurístic del millor tauler fins el moment quan es minimitza.
        @param  jugador és el color del jugadorCPU -1 si és el negre i 0 si és el blanc-.
        @post   S'ha aplicat l'algoritme minimax amb poda alpha-beta que retorna el valor del tauler que millor minimitza les pèrdues i maximitza els guanys pel jugadorCPU.
                A l'hora de crear l'arbre de joc no es tenen en compte les promocions, és a dir, no s'efectuen tot i haver-hi la possibilitat.
     */
    private static int minimax(TaulerEscacs t, int profunditat, Boolean maximitzant, int alpha, int beta, int jugador){
        if (profunditat == maxProf || t.fiJoc()) { //cas base: maxim profunditat o s'ha acabat el joc
            return evaluarTauler(t, jugador);
        }

        if(maximitzant){ //es vol maximitzar
            int millorValor = Integer.MIN_VALUE;
            for(int i=1; i<=t._fila; i++) {
                for (int j = 1; j <= t._columna; j++) { //es recorre el tauler
                    Posicio origen = new Posicio(j, t._fila + 1 - i);
                    Peca peca = t.solicitarPeca(origen); //obtenim la peca d'aquella posició
                    if (peca != null && peca.color() == jugador) { //si hi ha una peça i aquesta és la del jugadorCPU
                        //obtenim els destins possibles de la peça
                        for (Posicio desti : t.solicitarDestinsPeca(origen)) { //per cada desti possible...
                            JugadaOrdinaria jugadaOrd = new JugadaOrdinaria(origen, desti); //es crea la JugadaOrdinaria
                            TaulerEscacs nouTauler = new TaulerEscacs(t); //es fa una còpia del tauler
                            nouTauler.aplicarJugadaOrdinaria(jugadaOrd); //s'aplica la jugada sobre el nou tauler.
                            int score = minimax(nouTauler, profunditat + 1, false, alpha, beta, jugador); //crida recursiva
                            millorValor = Math.max(millorValor, score);
                            alpha = Math.max(alpha, score);
                            if (beta <= alpha) break;
                        }
                        for (Posicio posEnroc : t.solicitarCompanyesEnroc(origen)) { //per cada peça companya d'enroc...
                            JugadaEnroc jugadaEnr = new JugadaEnroc(origen, posEnroc); //es crea la JugadaEnroc
                            TaulerEscacs nouTauler = new TaulerEscacs(t); //es copia el tauler
                            nouTauler.aplicarJugadaEnroc(jugadaEnr); //s'aplica la jugada sobre el nou tauler
                            int score = minimax(nouTauler, profunditat + 1, false, alpha, beta, jugador); //crida recursiva
                            millorValor = Math.max(millorValor, score);
                            alpha = Math.max(alpha, score);
                            if (beta <= alpha) break;
                        }
                    }
                }
            }
            return millorValor;
        }else{ //es tracta de minimitzar
            int millorValor = Integer.MAX_VALUE;
            int jugadorCont = (jugador == 0) ? 1 : 0; //color del jugador contrari
            for(int i=1; i<=t._fila; i++) {
                for (int j = 1; j <= t._columna; j++) { //es recorre el tauler
                    Posicio origen = new Posicio(j, t._fila + 1 - i);
                    Peca peca = t.solicitarPeca(origen); //obtenim la peca d'aquella posició
                    if (peca != null && peca.color() == jugadorCont) { //si és una peça hi aquesta és la del jugador contrari
                        //obtenim els destins possibles de la peça
                        for (Posicio desti : t.solicitarDestinsPeca(origen)) { //per cada destí...
                            JugadaOrdinaria jugadaOrd = new JugadaOrdinaria(origen, desti); //es crea la JugadaOrdinaria
                            TaulerEscacs nouTauler = new TaulerEscacs(t); //es fa una copia del tauler
                            nouTauler.aplicarJugadaOrdinaria(jugadaOrd); //s'aplica la jugada sobre el tauler copiat
                            int score = minimax(nouTauler, profunditat + 1, true, alpha, beta, jugador); //crida recursiva
                            millorValor = Math.min(millorValor, score);
                            beta = Math.min(beta, score);
                            if (beta <= alpha) return beta;
                        }
                        for (Posicio posEnroc : t.solicitarCompanyesEnroc(origen)) { //per cada peça companya d'enroc...
                            JugadaEnroc jugadaEnr = new JugadaEnroc(origen, posEnroc); //es crea una JugadaEnroc
                            TaulerEscacs nouTauler = new TaulerEscacs(t); //es fa una copia del tauler
                            nouTauler.aplicarJugadaEnroc(jugadaEnr); //s'aplica la jugada sobre el nou tauler
                            int score = minimax(nouTauler, profunditat + 1, true, alpha, beta, jugador); //crida recursiva
                            millorValor = Math.min(millorValor, score);
                            beta = Math.min(beta, score);
                            if (beta <= alpha) break;
                        }
                    }
                }
            }
            return millorValor;
        }
    }

    /** @brief  Es retorna el valor heurístic que descriu com de bo és el tauler pel jugadorCPU
        @pre    \p t != null.
        @param  t és el tauler que és vol puntuar.
        @param  jugador és el color del jugadorCPU -1 si és el negre i 0 si és el blanc-.
        @post   Es retorna un enter que representa l'heurístic que determina com de favorable
                és la situació del \c Tauler \p t per al jugadorCPU.
     */
    private static int evaluarTauler(TaulerEscacs t, int jugador) {
        int sumaAccumulada = 0;

        for (int i = 1; i <= t._fila; i++) {
            for (int j = 1; j <= t._columna; j++) {
                Peca p = t.solicitarPeca(new Posicio(j, t._fila+1-i));
                if (p != null) {
                    if (p.color() == jugador)
                        sumaAccumulada += p.valor(); //s'incrementa el valor de les peces del jugadorCPU
                    else
                        sumaAccumulada -= p.valor(); //es decrementa el valor de les peces de l'adversari.
                }
            }
        }

        TaulerEscacs.resTauler res = t.estatActual(jugador);
        switch(res){ //com es troba el rei de l'adversari en aquest tauler?
            case ESCAC_MAT:
                sumaAccumulada += Integer.MAX_VALUE/2;
                break;
            case ESCAC:
                sumaAccumulada += Integer.MAX_VALUE/2-1000;
                break;
            default:
                break;
        }
        if(res!= TaulerEscacs.resTauler.ESCAC_MAT){
            int colorOponent = (jugador==0) ? 1 : 0;
            TaulerEscacs.resTauler resOponent = t.estatActual(colorOponent);
            switch (resOponent){ //com es troba el rei del jugadorCPU en aquest tauler?
                case ESCAC_MAT:
                    sumaAccumulada += Integer.MIN_VALUE/2;
                    break;
                case ESCAC:
                    sumaAccumulada += Integer.MIN_VALUE/2+1000;
                    break;
                default:
                    break;
            }
        }
        return sumaAccumulada;
    }
}
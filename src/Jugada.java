/**
 * @file Jugada.java
 * @brief Interfície d'una Jugada.
 * @author David Pérez Sánchez
 */

/**
 * @interface Jugada
 * @brief Interfície que defineix el comportament bàsic d'una jugada qualsevol.
 */
public interface Jugada {
    /**
     * @brief S'efectua la jugada i es comunica si s'ha capturat alguna peça.
     * @pre \p tauler != null, 0 <= \p torn <= 1
     * @post Es realitza la jugada i es retorna un booleà que és cert si alguna peça ha quedat capturada després
     *       d'haver-se realitzat la jugada.
     * @param tauler Tauler en el qual s'està disputant la partida.
     * @param torn Torn del jugador que realitza la jugada.
     * @return Cert si s'ha capturat alguna peça al fer la jugada, fals altrament.
     * @throws ExcepcioJugadaErronia Si les característiques d'aquesta Jugada no són compatibles amb l'estat actual
     *                               de \p tauler.
     */
    boolean efectuarJugada(TaulerEscacs tauler, int torn) throws ExcepcioJugadaErronia;
}

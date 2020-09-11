/**
 * @file JugadaOrdinaria.java
 * @brief Una jugada ordinària.
 * @author David Pérez Sánchez
 */

/**
 * @class JugadaOrdinaria
 * @brief Tipus de jugada que implica el moviment d'una peça a una altra posició.
 */
public class JugadaOrdinaria implements Jugada {
    private final Posicio _origen;    ///< Posició on es troba la peça que es vol moure.
    private final Posicio _desti;     ///< Posició a on es vol moure la peça.
    public final static String regex = "^[a-z]\\d+ +[a-z]\\d+$";
        ///< Expresió regular que verifica el format d'una jugada ordinària.

    /**
     * @brief Constructor amb paràmetres.
     * @pre ---
     * @post Es crea una JugadaOrdinaria amb posició orígen \p origen i posició destí \p desti.
     * @param origen La posició on es troba la peça dins el tauler.
     * @param desti La posició on es vol moure la peça dins el tauler.
     */
    public JugadaOrdinaria(Posicio origen, Posicio desti) {
        _origen = origen;
        _desti = desti;
    }

    /**
     * @brief Constructor amb paràmetres.
     * @pre \p jugada té el format correcte d'una jugada ordinària.
     * @post Es crea una JugadaOrdinaria amb la posició orígen i destí que s'indica a \p jugada.
     * @param jugada Text que compleix el format d'una jugada ordinària.
     */
    public JugadaOrdinaria(String jugada) {
        char columna = jugada.charAt(0);
        int fila = Integer.parseInt(jugada.substring(1,jugada.indexOf(" ")));
        _origen = new Posicio(columna,fila);

        int ultimEspai = jugada.lastIndexOf(" ");
        columna = jugada.charAt(ultimEspai+1);
        fila = Integer.parseInt(jugada.substring(ultimEspai+2));
        _desti = new Posicio(columna,fila);
    }

    /**
     * @brief L'orígen d'aquesta Jugada.
     * @return La posició orígen d'aquesta Jugada dins el tauler.
     */
    public Posicio origen() {
        return _origen;
    }
    /**
     * @brief El destí d'aquesta Jugada.
     * @return La posició destí d'aquesta Jugada dins el tauler.
     */
    public Posicio desti() {
        return _desti;
    }

    /**
     * @brief S'efectua la jugada i es comunica si s'ha capturat alguna peça.
     * @pre \p tauler != null, 0 <= \p torn <= 1
     * @post Es realitza la jugada ordinària i es retorna un booleà que és cert si alguna peça ha quedat capturada
     *       després d'haver-se realitzat la jugada.
     * @param tauler Tauler en el qual s'està disputant la partida.
     * @param torn Torn del jugador que realitza la jugada.
     * @return Cert si s'ha capturat alguna peça al fer la jugada, fals altrament.
     * @throws ExcepcioJugadaErronia Si les característiques d'aquesta Jugada no són compatibles amb l'estat actual
     *                               de \p tauler, és a dir, si la jugada no té lògica amb les mides del tauler ni amb
     *                               les peces involucrades o si la peça no disposa de cap moviment o, en cas contrari,
     *                               el moviment no és compitable amb la ubicació d'altres peces al tauler.
     */
    @Override
    public boolean efectuarJugada(TaulerEscacs tauler, int torn) throws ExcepcioJugadaErronia {
        if(!tauler.esPotFerJugadaOrdinaria(this,torn))
            throw new ExcepcioJugadaErronia("Error: no es pot fer la jugada ordinària");
        return tauler.aplicarJugadaOrdinaria(this);
    }

    @Override
    public String toString() {
        return _origen + "\n" + _desti;
    }
}

/** @file JugadaEnroc.java
    @brief Una jugada de tipus enroc.
    @author Laura Galera Alfaro
 */

/** @class JugadaEnroc
    @brief Tipus de jugada en què es realitza un moviment especial anomenat enroc, que permet moure alhora
            dues peces en la mateix tirada.
 */

public class JugadaEnroc implements Jugada{
    private final Posicio _posPecaPrimera;    ///< Posicio de la primera peça que el jugador ha entrat per a la jugada de tipus enroc.
    private final Posicio _posPecaSegona;     ///< Posicio de la segona peça que el jugador ha entrat per a la jugada de tipus enroc.
    private Posicio _posPecaFPrimera;   ///< Posicio final de la primera peça que el jugador ha entrat per a la jugada de tipus enroc.
    private Posicio _posPecaFSegona;    ///< Posicio final de la segona peça que el jugador ha entrat per a la jugada de tipus enroc.
    public final static String regex = "^[a-z]\\d+ +- +[a-z]\\d+$"; ///< Expressio regular per que defineix el partó de \c JugadaEnroc

    /** @brief  S'ha creat una jugada de tipus enroc.
        @pre    Cert.
        @param  posPrimera primera posició de l'enroc.
        @param  posSegona segona posició de l'enroc.
        @post   S'ha creat la jugada de tipus enroc amb les posicions d'entrada.
     */
    public JugadaEnroc(Posicio posPrimera, Posicio posSegona){
        _posPecaPrimera = posPrimera;
        _posPecaSegona = posSegona;
        _posPecaFPrimera = null;
        _posPecaFSegona = null;
    }

    /** @brief  S'ha creat una jugada de tipus enroc
        @pre    Cert
        @param  jugada és una cadena de caràcters que segueix el patró de \c JugadaEnroc
        @post   S'ha creat una jugada de tipus enroc amb les posicions que hi ha descrites
                a \p jugada.
     */
    public JugadaEnroc(String jugada){
        char columna = jugada.charAt(0);
        int fila = Integer.parseInt(jugada.substring(1,jugada.indexOf(" ")));
         _posPecaPrimera = new Posicio(columna,fila);

        int ultimEspai = jugada.lastIndexOf(" ");
        columna = jugada.charAt(ultimEspai+1);
        fila = Integer.parseInt(jugada.substring(ultimEspai+2));
         _posPecaSegona = new Posicio(columna,fila);
    }

    /**@brief Retorna la posició de la primera peça d'aquesta jugada enroc**/
    public Posicio posicioPrimera(){
        return _posPecaPrimera;
    }

    /**@brief Retorna la posició de la segona peça d'aquesta jugada enroc**/
    public Posicio posicioSegona(){
        return _posPecaSegona;
    }

    /** @brief      S'efectua la jugada \c JugadaEnroc al \c TaulerEscacs \p t i es comunica si s'ha
                    capturat alguna peça.
        @pre        \p tauler != null i \p torn és 1 o 0.
        @param      tauler és el tauler en el qual s'està disputant la partida.
        @param      torn torn és 0 si és una jugada del jugador de blanques o 1 si és del jugador de negres.
        @post       S'ha realitzat aquesta jugada enroc sobre el \c TaulerEscacs \t i s'ha retornat un booleà indicant si s'ha capturat alguna peça després d'efectuar
                    la jugada. Com que es tracta d'una jugada especial mai es captura.
        @throws     ExcepcioJugadaErronia si les posicions no existeixen al tauler o no existeix un enroc entre les peces involucrades
                    en aquesta jugada o les característiques de l'enroc no són compatibles amb la disposició d'altres peces al tauler.
     */
    @Override
    public boolean efectuarJugada(TaulerEscacs tauler, int torn) throws ExcepcioJugadaErronia{
        if(!tauler.esPotFerJugadaEnroc(this, torn))
            throw new ExcepcioJugadaErronia("Error: no es pot fer la jugada enroc");
        tauler.aplicarJugadaEnroc(this);
        return false;
    }

    /**@brief S'ha anotat la posició final de la primera peça de la jugada un cop fet l'enroc*/
    public void assignarPosFinalPrimera(Posicio pos){
        _posPecaFPrimera = pos;
    }

    /**@brief S'ha anotat la posició final a la segona peça de la jugada un cop fet l'enroc*/
    public void assignarPosFinalSegona(Posicio pos){
        _posPecaFSegona = pos;
    }

    /**@brief Retorna un \c String que representa aquesta jugada**/
    @Override
    public String toString() {
        return _posPecaPrimera+" - "+_posPecaSegona+ "\n" + _posPecaFPrimera+" - "+_posPecaFSegona;
    }
}

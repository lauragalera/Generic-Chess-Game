/**
 @file ExcepcioJugadaErronia.java
 @brief Error en una jugada.
 @author Laura Galera Alfaro
 */

/**
 @class ExcepcioJugadaErronia
 @brief Excepció usada per la incapacitat d'efectuar una jugada.
 */

public class ExcepcioJugadaErronia extends Exception {

    /**@brief Es crea una ExcepcioJugadaErronia.**/
    ExcepcioJugadaErronia() {
        super();
    }

    /**@brief Es crea una ExcepcioJugadaErronia que té com a missatge el \c String \p s.**/
    ExcepcioJugadaErronia(String s) {
        super(s);
    }
}

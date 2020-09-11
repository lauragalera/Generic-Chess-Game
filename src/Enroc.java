/** @file Enroc.java
    @brief Enroc d'una peça.
    @author Laura Galera i Alfaro
 */

/** @class Enroc.
    @brief Moviment especial en què dues peces es mouen alhora en una tirada.
    @details L'enroc pot requerir o no que les peces mai s'hagin mogut i/o que les caselles del mig siguin buides.
 */
public class Enroc {

    private final String _pecaA;      ///< Nom de la primera peça de l'enroc.
    private final String _pecaB;      ///< Nom de la segona peça de l'enroc.
    private final boolean _quiets;    ///< Indica si per realitzar l'enroc és necessari que les peces mai s'hagi mogut.
    private final boolean _buitMig;   ///< Indica si per realitzar l'enroc és necessari que no hi hagi peces entre mig.

    /** @brief  S'ha creat un enroc.
        @pre    \p pecaA i \p pecaB són noms de peces disponibles a la partida.
        @param  pecaA és el nom de la primera peça de l'enroc.
        @param  pecaB és el nom de la segona peça de l'enroc.
        @param  quiets és cert si l'enroc requereix que les peces mai s'hagin mogut.
        @param  buitMig és cert si l'enroc requereix que no hi hagi cap peça entre les caselles.
        @post   S'ha creat un enroc entre dues peces disponibles.
     */
    public Enroc(String pecaA, String pecaB, boolean quiets, boolean buitMig){
        _pecaA = pecaA;
        _pecaB = pecaB;
        _quiets = quiets;
        _buitMig = buitMig;
    }

    /**@brief Retorna el nom de la primera peça de l'enroc **/
    public String primeraPeca(){
        return _pecaA;
    }

    /**@brief Retorna cert si per fer l'enroc les peces mai s'han hagut de moure, fals altrament. **/
    public boolean quiets(){
        return _quiets;
    }

    /**@brief Retorna cert si per fer l'enroc no hi pot haver peces a les caselles entre la primera i la segona peça, fals altrament. **/
    public boolean buitAlMig(){
        return _buitMig;
    }

    /**@brief Retorna un \c String que representa aquest enroc**/
    @Override
    public String toString(){
        return "{"+_pecaA+","+_pecaB+","+_quiets+","+_buitMig+"}";
    }
}

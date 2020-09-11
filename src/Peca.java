/**
 * @file Peca.java
 * @brief Peca d'escacs.
 * @author David Pérez Sánchez
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @class Peca
 * @brief Peça d'escacs que es mou sobre un tauler efectuant moviments permesos.
 */
public class Peca implements Comparable<Peca>{
    private final String _nom;                          ///< Nom amb què s'identifica la peça.
    private final char _simbol;                         ///< Símbol amb què es representa.
    private final String _imatgeBlanca;                 ///< Ruta a la imatge del jugador de blanques.
    private final String _imatgeNegra;                  ///< Ruta a la imatge del jugador de negres.
    private final int _valor;                           ///< Valor de la peça.
    private final List<Moviment> _moviments;            ///< Llista de moviments que pot realitzar.
    private final List<Moviment> _movimentsInicials;
        ///< Llista de moviments que pot realitzar si no ha estat moguda de la seva posició inicial.
    private final boolean _potPromocionar;              ///< Si la peça pot promocionar en una altra peça.
    private final boolean _esInvulnerable;              ///< Si la peça és invulnerable, és a dir, no ser capturada.
    private int _color;                                 ///< Jugador al qual pertany la peça (negres o blanques).
    private boolean _esMoguda;                          ///< Si la peça s'ha mogut de la seva posició inical.
    private final HashMap<String,Enroc> _enrocs;
        ///< Llista d'enrocs que pot realitzar la peça i amb quina peça el pot realitzar.

    /**
     * @brief Constructor amb paràmetres.
     * @pre Cap paràmetre pot ser null, \p imatgeNegra és la ruta a una imatge vàlida, \p imatgeBlanca és la ruta a una
     *      imatge vàlida, 0 <= valor.
     * @post Es crea una peça del jugador de blanques i no moguda de la seva posició inicial amb els paràmetres
     *       d'entrada.
     * @param nom Nom de la peça.
     * @param simbol Símbol amb què es representa.
     * @param imatgeBlanca Imatge amb què es representa gràficament una peça del jugador de blanques.
     * @param imatgeNegra Imatge amb què es representa gràficament una peça del jugador de negres.
     * @param valor Valor de la peça.
     * @param mov Moviments que pot realitzar.
     * @param movIni Moviments que pot realitzar quan no s'ha mogut de la seva posició inicial.
     * @param promo Si pot promocionar.
     * @param invulnerable Si no pot ser capturada.
     * @param enrocs Enrocs que pot realitzar la peça i amb quina peça el pot fer.
     */
    public Peca (String nom, char simbol, String imatgeBlanca, String imatgeNegra, int valor, List<Moviment> mov,
                 List<Moviment> movIni, boolean promo, boolean invulnerable, HashMap<String,Enroc> enrocs) {
        _nom = nom;
        _simbol = simbol;
        _imatgeBlanca = imatgeBlanca;
        _imatgeNegra = imatgeNegra;
        _valor = valor;
        _moviments = mov;
        _movimentsInicials = movIni;
        _potPromocionar = promo;
        _esInvulnerable = invulnerable;
        _enrocs = enrocs;
        _color = 0;
        _esMoguda = false;
    }

    /**
     * @brief Constructor còpia.
     * @pre \p p != null
     * @post Es copien tots els atributs de \p p.
     * @param p Peca que es vol copiar.
     */
    public Peca(Peca p) {
        _nom = p._nom;
        _simbol = p._simbol;
        _imatgeBlanca = p._imatgeBlanca;
        _imatgeNegra = p._imatgeNegra;
        _valor = p._valor;
        _moviments = p._moviments;
        _movimentsInicials = p._movimentsInicials;
        _potPromocionar = p._potPromocionar;
        _esInvulnerable = p._esInvulnerable;
        _color = p._color;
        _esMoguda = p._esMoguda;
        _enrocs = p._enrocs;
    }

    /**
     * @brief Nom de la peça.
     * @return El nom de la peça.
     */
    public String nom() {
        return _nom;
    }

    /**
     * @brief Caràcter amb què es representa aquesta peça sobre el tauler.
     * @return El caràcter que representa aquesta peça.
     */
    public char simbol() {
        if(_color == 0) return Character.toUpperCase(_simbol);
        else return Character.toLowerCase(_simbol);
    }

    /**
     * @brief Jugador al què pertany la peça.
     * @return 0 si és del jugador de blanques, 1 si és del jugador de negres.
     */
    public int color() {
        return _color;
    }

    /**
     * @brief Valor d'aquesta peça.
     * @return El valor d'aquesta peça.
     */
    public int valor() {
        return _valor;
    }

    /**
     * @brief Ruta a la imatge que representa aquesta peça gràficament.
     * @return La ruta a la imatge.
     */
    public String pathImg() {
        return _color == 0 ? _imatgeBlanca : _imatgeNegra;
    }

    /**
     * @brief Indica si aquesta peça ha estat moguda, és a dir, si es troba a la seva posició inicial o no.
     * @return Cert si s'ha mogut (no està a la seva posició inicial), fals altrament.
     */
    public boolean esMoguda() {
        return _esMoguda;
    }

    /**
     * @brief Indica si aquesta peça és invulnerable.
     * @return Cert si la peça no pot ser capturada, fals altrament.
     */
    public boolean esInvulnerable() {
        return _esInvulnerable;
    }

    /**
     * @brief Indica si aquesta peça pot promocionar.
     * @return Cert si pot promocionar, fals altrament.
     */
    public boolean potPromocionar() {
        return _potPromocionar;
    }

    /**
     * @brief Indica si aquesta peça pot fer un enroc.
     * @return Cert si aquesta peça disposa de, com a mínim, un enroc. Fals altrament.
     */
    public boolean potEnrocar() {
        return ! _enrocs.isEmpty();
    }

    /**
     * @brief Moviments que pot realitzar la peça en la seva situació actual.
     * @pre ---
     * @post Retorna tots els moviments que la peça pot realitzar i, en cas de no haver-se mogut de la seva posició
     *       inicial, també els moviments inicials.
     * @return Una llista amb tots els moviments disponibles en las situació actual.
     */
    public List<Moviment> obtenirMoviments() { // FER DEEP COPY
        List<Moviment> aux = new ArrayList<>(_moviments);
        if(! _esMoguda) aux.addAll(_movimentsInicials);
        return aux;
    }

    /**
     * @brief Obtenir l'enroc que pot realitzar amb una peça determinada.
     * @pre \p p != null
     * @post Retorna l'enroc que permet realitzar un enroc enre aquesta peça i la peça \p p.
     * @param p Peça amb què es vol consultar si hi ha algun enroc.
     * @return Un enroc amb la peça \p p en cas que existeixi, null altrament.
     */
    public Enroc obtenirEnroc(Peca p) {
        return _enrocs.get(p._nom); // Retorna null si no hi és
    }

    /**
     * @brief Assignar a la peça que s'ha mogut de la seva posició inicial.
     * @pre ---
     * @post La peça passa a considerar-se moguda de la seva posició inicial.
     */
    public void actualitzarMoguda(){
        _esMoguda = true;
    }

    /**
     * @brief Assignar jugador propietari de la peça.
     * @pre 0 <= color <= 1
     * @post Aquesta peça passa a ser del jugador que indiqui \p color.
     * @param color Jugador al què es vol assignar la peça (0 per blanques, 1 per negres).
     * @exception IllegalArgumentException Si \p color no és 0 o 1.
     */
    void assignarColor(int color) {
        if(color != 0 && color != 1) throw new IllegalArgumentException();
        _color = color;
    }

    /**
     * @brief Compara segons el valor de les peces.
     */
    @Override
    public int compareTo(Peca o) {
        return Integer.compare(_valor, o._valor);
    }

    @Override
    public String toString() {
        return _nom;
    }
}
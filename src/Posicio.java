/**
 * @file Posicio.java
 * @brief Una posició d'un tauler.
 * @author David Pérez Sánchez
 */

import java.util.Objects;

/**
 * @class Posicio
 * @brief Una posició que correspon a una casella d'un tauler, amb files de 'a' a 'p' i amb columnes de 1 a 16.
 */

public class Posicio {
    private final int _fila;      ///< Fila representada amb un enter positiu
    private final int _columna;   ///< Columna representada amb un enter positiu

    /**
     * @brief Constructor amb paràmetres.
     * @pre \p 1 <= fila <= 16  i 'a' <= \p columna <= 'p'
     * @post Es crea una Posicio de la fila \p fila i a la columna \p columna
     * @param fila Fila del tauler.
     * @param columna Columna del tauler.
     * @exception IllegalArgumentException Si \p columna no és una lletra minúscula entre 'a' i 'p' o si \p fila no és
     *                                     un enter positiu entre 1 i 16 (extrems inclosos).
     */
    public Posicio(char columna, int fila) {
        boolean columnaCorrecte = Character.isLowerCase(columna) && columna <= 'p';
        boolean filaCorrecte = fila >= 1 && fila <= 16;
        if(!columnaCorrecte || !filaCorrecte) throw new IllegalArgumentException("Posició fora de rang");
        _columna = columna - 'a' + 1;
        _fila = fila;
    }

    /**
     * @brief Constructor amb paràmetres.
     * @pre \p 1 <= fila <= 16  i 1 <= \p columna <= 16
     * @post Es crea una Posicio de la fila \p fila i a la columna \p columna
     * @param fila Fila del tauler.
     * @param columna Columna del tauler.
     * @exception IllegalArgumentException Si \p columna no és un enter positiu entre 1 i 16 o si \p fila no és
     *                                     un enter positiu entre 1 i 16 (extrems inclosos).
     */
    public Posicio(int columna, int fila){
        boolean columnaCorrecte = columna >= 1 && columna <= 16;
        boolean filaCorrecte = fila >= 1 && fila <= 16;
        if(!columnaCorrecte || !filaCorrecte) throw new IllegalArgumentException("Posició fora de rang");
        _columna = columna;
        _fila = fila;
    }

    /**
     * @brief Constructor amb paràmetres
     * @pre \p str ha d'estar compost per: un primer caràcter que ha de ser una lletra minúscula entre 'a' i 'p' i
     *      una cadena de caràcters que ha de representar un enter positiu entre 1 i 16 (extrems inclosos).
     * @post Es crea una Posició amb la fila corresponent al primer caràcter de |p str i amb la columna corresponent a
     *      la cadena de caràcters que segueix.
     * @param str Posició indicada amb una cadena de caràcters.
     * @throws IllegalArgumentException Si la columna no és una lletra minúscula entre 'a' i 'p' o si la fila no és
     *                                  un enter positiu entre 1 i 16 (extrems inclosos) o si \p str és buit.
     */
    public Posicio(String str) {
        if(str.equals("")) throw new IllegalArgumentException("Posició buida");
        char columna = str.charAt(0);
        String fila = str.substring(1);
        boolean columnaCorrecte = Character.isLowerCase(columna) && columna <= 'p';
        boolean filaCorrecte = fila.matches("^\\d+$") && Integer.parseInt(fila) >= 1 && Integer.parseInt(fila) <= 16;
        if(!columnaCorrecte || !filaCorrecte) throw new IllegalArgumentException("Posició fora de rang");
        _columna = columna - 'a' + 1;
        _fila = Integer.parseInt(fila);
    }

    /**
     * @brief Constructor còpia.
     * @pre pos != null
     * @post Es crea una Posicio situada a la mateixa fila i columna que \p pos
     * @param pos Posició que es vol copiar.
     */
    public Posicio(Posicio pos) {
        _fila = pos._fila;
        _columna = pos._columna;
    }

    /**
     * @brief Fila d'un tauler.
     * @return La fila d'aquesta Posicio
     */
    public int fila() {
        return _fila;
    }

    /**
     * @brief Columna d'un tauler.
     * @return La columna d'aquesta Posicio com un enter
     */
    public int columna() {
        return _columna;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_columna, _fila);
    }

    @Override
    public boolean equals(Object o) {
        boolean r = false;
        if (o instanceof Posicio) {
            Posicio p = (Posicio)o;
            r = this._columna == p._columna && this._fila == p._fila;
        }
        return r;
    }

    @Override
    public String toString() {
        return (char)(_columna + 'a' - 1) + Integer.toString(_fila);
    }
}
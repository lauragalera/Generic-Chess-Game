/**
 @file Pair.java
 @brief Un parell genèric
 @author Laura Galera Alfaro
 */

/**
 @class Pair
 @brief Parell genèric
 */
public class Pair<S,T> {
    public S first;     ///< primer membre de la parella
    public T second;    ///< segon membre de la parella

    /**@brief Donats dos valors es crea una parella d'ambdos.**/
    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }
}
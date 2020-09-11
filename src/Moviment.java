/** @file Moviment.java
    @brief Moviment d'una peça.
    @author Laura Galera Alfaro
 */

import java.util.*;

/** @class Moviment
    @brief Vector en què la primera component és l'increment de la fila, la segona l'increment de la columna, la tercera la propietat de capturar i la última la propietat de saltar.
    @details Per la primera i segona component:<br>
            a i b representen valors enters diferents de 0.<br>
            n i m representen valors naturals diferents de 0.<br>
            -n o -m s'utilitzen per representar únicament números negatius.<br>
            Per la segona component:<br>
            -a i -b només són possibles pel moviment diagonal "\".<br>
            Per la tercera component:<br>
            \a 0 vol dir que la peça no pot capturar.<br>
            a destí, \a 1 significa que sí que pot i \a 2 vol dir que el moviment només és
            possible amb captura a destí.<br>
            Per la quarta component:<br>
            \a 0 vol dir que la peça no pot saltar usant
            aquest moviment. Si val \a 1 significa que pot saltar i si val \a 2, a més de saltar,
            captura les peces enemigues vulnerables.
 */

public class Moviment {

    private String _fila;    ///< Increment de la fila. Pot ser un enter determinat o un literal del conjunt {a,b,n,m}, incloent els negatius -n i -m.
    private String _columna; ///< Increment de la columna. Pot ser un enter determinat o un literal del conjunt {a,b,n,m}, incloent els negatius -n, -m i -a, -b (els dos últims reservats per moviments diagonals).
    private int _capturar;   ///< Valor numèric per indicar si el moviment permet a la peça capturar en destí.
    private int _saltar;     ///< Valor numèric per indicar si el moviment permet a la peça saltar o saltar capturant altres peces.


    /**@brief Enumeració usada per a classificar el moviment segons els valors de \a fila i \a columna.**/
    public enum subTipus {
        LITERAL_ENTER,  //< Almenys un valor, el de la fila o el de la columna, és un literal del conjunt {a,-a, b, -b}. G.e. [a,b], [a,-n], [7,-a].
        LITERAL_NATURAL,//< Almenys un valor, el de la fila o el de la columna, és un literal del conjunt {n,-n,m,-m}. Si els dos valors no són del conjunt anterior significa que un d'ells és un número enter. G.e. [n,-m], [m,8].
        NUMERIC         //< fila i columna són valors numèrics. G.e. [3,6], [0,5].
    }

    /**
        @brief  S'ha creat un moviment.
        @pre    Cert.
        @param  f és l'increment de la fila.
        @param  c és l'increment de la columna.
        @param  capt és el valor que si el moviment permet a la peça capturar en destí.
        @param  salt és el valor que indica si el moviment permet a la peça saltar o saltar capturant altres peces.
        @post   S'ha creat un moviment amb els paràmetres d'entrada.
        @throws IllegalArgumentException si els paràmetres d'entrada no es troben entre els valors permesos per un moviment.
     */
    public Moviment(String f, String c, int capt, int salt) throws IllegalArgumentException{
        boolean formatCorrecte = esValorPermesFila(f) && esValorPermesCol(f,c) && esValorCaptSalta(capt) && esValorCaptSalta(salt);
        if(formatCorrecte) {
            _fila = f;
            _columna = c;
            _capturar = capt;
            _saltar = salt;
            if(esCombinat() && _saltar!=1)
                formatCorrecte = false;
        }
        if(!formatCorrecte)
            throw new IllegalArgumentException("Error: el moviment" + this + " no és vàlid");
    }

    /**@brief Retorna l'increment de la fila del moviment*/
    public String fila(){
        return _fila;
    }

    /**@brief Retorna l'increment de la columna del moviment*/
    public String columna(){
        return _columna;
    }

    /**
     * @brief   Retorna un enter indicant si el moviment té capacitat de capturar a destí.
     * @pre     Cert.
     * @post    Retorna \a 0 si el moviment no permet capturar a destí. Si retorna \a 1 significa que permet
     *          capturar i si retorna \a 2 significa que només és possible usar aquest moviment
     *          quan es captura a destí.
     */
    public int movCaptura() {
        return _capturar;
    }

    /**
     * @brief   Retorna un enter indicant quin tipus de propietat té el moviment respecte el desplaçament.
     * @pre     Cert.
     * @post    Retorna \a 0 si el moviment no permet saltar altres peces. Si retorna \a 1 significa que permet saltar
     *          mentre s'avancen caselles i si retorna \a 2 significa que, a més de saltar, també captura.
     */
    public int movSalta() {
        return _saltar;
    }

    /**
     * @brief   Retorna un valor de l'enumeració \c subTipus indicant de quin subtipus és el moviment.
     * @pre     Cert
     * @post    Retorna un valor, pertanyent a l'enumeració \c subTipus, que indica si el moviment és \a LITERAL_ENTER,
     *          \a LITERAL_NATURAL o \a NUMERIC. Per tractar-se d'un moviment literal enter, el moviment ha de
     *          tenir com a increments de fila i/o columna algun dels literals a, b, -a, -b, que representen qualsevol enter
     *          excepte 0 (-a , -b només per diagonal). Si el moviment és literal natural significa que disposa d'algun
     *          increment del tipus n, -n, m, -m i, evidentment, el moviment no és literal enter (no té a, b, -a, -b).
     *          Finalment, pot tractar-se d'un subtipus numèric si tots els increments són numeros enters concrets, acceptant el 0.
     */
    public subTipus consultarSubtipus() {
        subTipus tipus;
        if (_fila.equals("a") || _fila.equals("b") || _columna.equals("a") || _columna.equals("b"))
            tipus = subTipus.LITERAL_ENTER;
        else if (_fila.equals("n") || _fila.equals("-n") || _fila.equals("m") || _fila.equals("-m") ||
                _columna.equals("n") || _columna.equals("-n") || _columna.equals("m") || _columna.equals("-m"))
            tipus = subTipus.LITERAL_NATURAL;
        else tipus = subTipus.NUMERIC;
        return tipus;
    }

    /**
     * @brief   Comprova si el moviment és rectilini.
     * @pre     Cert
     * @post    Retorna cert si el moviment és de tipus \a Rectilini, que són aquells que es realitzen
     *          al llarg d'una fila o d'una columna. Si no ho és retorna fals.
     */
    public boolean esRectilini() {
        String zero = "0";
        return (_fila.equals(zero) || _columna.equals(zero));
    }

    /**
     * @brief   Comprova si el moviment és diagonal.
     * @pre     Cert
     * @post    Retorna cert si el moviment és de tipus \a Diagonal, que són els que es realitzen
     *          a la diagonal "/" o a la diagonal "\".
     */
    public boolean esDiagonal() {
        char f = _fila.charAt(_fila.length() - 1);
        char c = _columna.charAt(_columna.length() - 1);
        return (f == c);
    }

    /**
     * @brief   Comprova si el moviment és combinat.
     * @pre     Cert
     * @post    Retorna cert si el moviment és de tipus \a Combinat, com el del cavall d'escacs convencional.
     */
    public boolean esCombinat() {
        return (!esDiagonal() && !esRectilini());
    }

    /**
     * @brief   Es retorna una llista de moviments \a LITERAL_NATURAL que sorgeixen de dividir l'actual.
     * @pre     Aquest moviment ha de ser de tipus \a LITERAL_ENTER.
     * @post    Retorna una llista de \c Moviment que conté els moviments de tipus LITERAL NATURAL fruit de
     *          dividir aquest moviment LITERAL ENTER. Per exemple, si el moviment era ["a",0], els dos moviments
     *          resultants són ["n",0] i ["-n",0].
     */
    public List<Moviment> dividirMov() {
        List<Moviment> divMov = new ArrayList<>();
        if (esDiagonal()) {
            if (_columna.equals("-a") || _columna.equals("-b")) {
                divMov.add(new Moviment("n", "-n", _capturar, _saltar));
                divMov.add(new Moviment("-n", "n", _capturar, _saltar));
            } else {
                divMov.add(new Moviment("n", "n", _capturar, _saltar));
                divMov.add(new Moviment("-n", "-n", _capturar, _saltar));
            }
        } else if (esRectilini()) {
            if (_fila.equals("a") || _fila.equals("b")) {
                divMov.add(new Moviment("n", "0", _capturar, _saltar));
                divMov.add(new Moviment("-n", "0", _capturar, _saltar));
            } else {
                divMov.add(new Moviment("0", "n", _capturar, _saltar));
                divMov.add(new Moviment("0", "-n", _capturar, _saltar));
            }
        } else { //si és un moviment combinat...
            if ((_columna.equals("a") && _fila.equals("b")) || (_columna.equals("b") && _fila.equals("a"))) { //es un combinat enter pur
                divMov.add(new Moviment("n", "m", _capturar, _saltar));
                divMov.add(new Moviment("-n", "m", _capturar, _saltar));
                divMov.add(new Moviment("-n", "-m", _capturar, _saltar));
                divMov.add(new Moviment("n", "-m", _capturar, _saltar));
            } else {
                if (_fila.equals("a") || _fila.equals("b")) { //la fila té part entera
                    String fila1, fila2;
                    if (_columna.equals("n") || _columna.equals("-n")) {
                        fila1 = "m";
                        fila2 = "-m";
                    } else {
                        fila1 = "n";
                        fila2 = "-n";
                    }
                    divMov.add(new Moviment(fila1, _columna, _capturar, _saltar));
                    divMov.add(new Moviment(fila2, _columna, _capturar, _saltar));
                } else { //la columna te part entera
                    String col1, col2;
                    if (_fila.equals("n") || _fila.equals("-n")) {
                        col1 = "m";
                        col2 = "-m";
                    } else {
                        col1 = "n";
                        col2 = "-n";
                    }
                    divMov.add(new Moviment(_fila, col1, _capturar, _saltar));
                    divMov.add(new Moviment(_fila, col2, _capturar, _saltar));
                }
            }
        }
        return divMov;
    }

    /**
     * @brief   S'inverteix el moviment.
     * @pre     Cert.
     * @post    Retorna un moviment amb les mateixes capacitats de capturar a desti i saltar que aquest
     *          però invertint el desplaçament de columnes i de files. Per exemple, si el moviment era ("n","n", 1, 1),
     *          diagonal positiva, el moviment retornat és ("-n","-n", 1, 1).
     */
    public Moviment invertir() {
        subTipus tipus = consultarSubtipus();
        String fila = _fila;
        String col = _columna;
        if (subTipus.NUMERIC == tipus) {
            if(!fila.equals("0"))
                fila = Integer.toString(-1 * Integer.parseInt(fila));
            if(!col.equals("0"))
            col = Integer.toString(-1 * Integer.parseInt(col));
        } else if (subTipus.LITERAL_NATURAL == tipus) {
            if (fila.length() == 2) {
                fila = String.valueOf(fila.charAt(fila.length() - 1));
            } else if(!fila.equals("0")) {
                fila = "-" + fila;
            }

            if (col.length() == 2) {
                col = String.valueOf(col.charAt(col.length() - 1));
            } else if(!col.equals("0")) {
                col = "-" + col;
            }
        }
        return new Moviment(fila, col, _capturar, _saltar);
    }

    /**
     * @brief   Comprova si el moviment d'entrada permet arriba com a mínim a una de les caselles que, aplicant \a aquest, també s'arribaria.
     * @pre     \p mov != null.
     * @param   mov moviment amb el que es vol comprovar.
     * @post    Retorna cert si el moviment \p mov i \a aquest permeten arribar, com a mínim, a una mateixa casella de destí, fals altrament.
     *          Per exemple, el moviment a,1 i el moviment n,n permeten arribar de a1 a b2.
     */
    public boolean comparteixenDesti(Moviment mov){
        boolean equivalents = false;
        subTipus tipusPrimer = consultarSubtipus();
        subTipus tipusSegon = mov.consultarSubtipus();
        if (!((esDiagonal() && mov.esCombinat() && tipusSegon == subTipus.NUMERIC) || (mov.esDiagonal() && esCombinat() && tipusPrimer == subTipus.NUMERIC))) { //si no és diagonal amb combinat numèric...
            if ((esDiagonal() && tipusPrimer == subTipus.LITERAL_ENTER) || (mov.esDiagonal() && tipusSegon == subTipus.LITERAL_ENTER)) { //si un dels dos és diagonal enter o tots dos...
                Moviment movDiagonal = this;
                Moviment altre = mov;
                if (mov.esDiagonal() && tipusSegon == subTipus.LITERAL_ENTER) { //guardem quin és el diagonal enter, si ho son tots dos no importa quin escollim
                    movDiagonal = mov;
                    altre = this;
                }

                if(altre.esCombinat() && altre.consultarSubtipus() == subTipus.LITERAL_ENTER) //si l'altre és combinat i enter segur que són equivalents
                    equivalents = true;
                else if(!altre.esRectilini()) { //si l'altre no es rectilini ni un combinat enter...
                    if (movDiagonal._columna.length() == 1) { //és [a,a] o [b,b]
                        equivalents = (altre._fila.length() == altre._columna.length()); //han de tenir mida igual, per exemple si altre és [n, 3]
                    } else { //és [a,-a] o [b,-b]
                        equivalents = (altre._fila.length() != altre._columna.length()); //han de tenir mida diferent, per exemple si altre és [-n, 3]
                    }
                }
            } else {
                equivalents = (inclou(_fila, mov._fila) && inclou(_columna, mov._columna));
            }
        }
        return equivalents;
    }

    /**@brief Retorna un \c String que representa aquest moviment**/
    @Override
    public String toString(){
        return "["+_fila+","+_columna+","+_capturar+","+_saltar+"]";
    }

    //****************************************************************************************************++MÈTODES PRIVATS***************************************************************************************************************************

    /**@brief Retorna cert si l'increment de la columna té un format correcte, fals altrament.*/
    private boolean esValorPermesCol(String fila, String columna){
        boolean correcte = fila.matches("^-?\\d+$");
        if(!correcte){
            Set<String> possiblesValors = new HashSet<>(Arrays.asList("a", "b", "n", "m", "-n", "-m"));
            char f = fila.charAt(fila.length() - 1);
            char c = columna.charAt(columna.length() - 1);
            if(f==c){
                possiblesValors.add("-a");
                possiblesValors.add("-b");
            }
            correcte = possiblesValors.contains(fila);
        }
        return correcte;
    }

    /**@brief Retorna cert si l'increment de la fila té un format correcte, fals altrament.*/
    private boolean esValorPermesFila(String fila){
        boolean correcte = fila.matches("^-?\\d+$");
        if(!correcte){
            Set<String> possiblesValors = new HashSet<>(Arrays.asList("a", "b", "n", "m", "-n", "-m"));
            correcte = possiblesValors.contains(fila);
        }
        return correcte;
    }

    /**@brief Retorna cert si el valor \p aux és 0, 1 o 2.*/
    private boolean esValorCaptSalta(int aux){
        return (aux==0 || aux==1 || aux==2);
    }

    /**
     * @brief   Comprova si es possible que dues components, la fila o la columna, de dos moviments puguin prendre un o més
     *          valors idèntics.
     * @pre     Si \p despl1 és l'increment de la fila d'un moviment, forçosament \p despl2 també ha de ser l'increment de la fila d'un
     *          segon moviment. El mateix passaria per la columna.
     * @param   despl1 és l'increment de la fila o l'increment de la columna d'un moviment.
     * @param   despl2 és l'increment de la fila o l'increment de la columna d'un segon moviment.
     * @post    Retorna cert si ambdues components, de dos moviments, poden prendre valors numèrics idèntics i, en altres paraules,
     *          coincidir com a increment de fila o de columna. En cas contrari es retorna fals.
     */
    private static boolean inclou(String despl1, String despl2) {
        boolean mateixValor = (esIgual(despl1, despl2));
        if (!mateixValor) { //si no son iguals...
            Pair<String, String> prioritat = ordreGeneric(despl1, despl2);
            if (!prioritat.first.equals("0") && !prioritat.second.equals("0")) { //qualsevol 0 és només compatible amb 0!
                if (prioritat.first.equals("a") || prioritat.first.equals("b")) { //si és a o b...
                    mateixValor = true;
                } else if (prioritat.first.equals("n") || prioritat.first.equals("m") || prioritat.first.equals("-n") || prioritat.first.equals("-m")) {
                    mateixValor = (prioritat.first.length() == prioritat.second.length());
                }
            }
        }
        return mateixValor;
    }

    /**
     * @brief   Retorna una parella de \c String, essent la primer la component més gènerica de les dues
     *          d'entrada.
     * @pre     Si \p despl1 és l'increment de la fila d'un moviment, forçosament \p despl2 també ha de ser l'increment de la fila d'un
     *          segon moviment. El mateix passaria per la columna.
     * @param   despl1 és l'increment de la fila o l'increment de la columna d'un moviment.
     * @param   despl2 és l'increment de la fila o l'increment de la columna d'un segon moviment.
     * @post    Retorna una parella de \c String que són justament els mateixos d'entrada, amb la diferència que estan ordenats seguint el críteri
     *          que el primer de la parella és igual o més genèric que el segon. Per exemple, \a a és més genèric que \a m i \a m és més genèric que un número. <br>
     *          Si tenen el mateix grau de generecitat l'ordre no és rellevant. <br>
     *          Compte, el primer no té perquè incloure el segon.
     */
    private static Pair<String, String> ordreGeneric(String despl1, String despl2) {
        String mesG = despl1;
        String menysG = despl2;
        if (!despl1.equals("a") && !despl1.equals("b")) {
            if (despl2.equals("a") || despl2.equals("b") || despl2.equals("n") || despl2.equals("m") || despl2.equals("-n") || despl2.equals("-m")) {
                mesG = despl2;
                menysG = despl1;
            }
        }
        return new Pair<>(mesG, menysG);
    }

    /**
     * @brief   Comprova si dues components de moviments diferents són iguals.
     * @pre     Si \p despl1 és l'increment de la fila d'un moviment, forçosament \p despl2 també ha de ser l'increment de la fila d'un
     *          segon moviment. El mateix passaria per la columna.
     * @param   despl1 és l'increment de la fila o l'increment de la columna d'un moviment.
     * @param   despl2 és l'increment de la fila o l'increment de la columna d'un segon moviment.
     * @post    Retorna cert si les components (totes dues representen l'increment de la fila o de la columna) de dos moviments són iguals, fals altrament.
     *          Iguals no significa que tinguin la mateixa aparença, que també podria ser, sinó que en termes del moviment aporten el mateix quan són literals (g.e. \a m i \a n).
     */
    private static boolean esIgual(String despl1, String despl2) {
        boolean iguals = (despl1.equals(despl2));
        if (!iguals) {
            String canvi;
            switch (despl1) {
                case "a":
                    canvi = "b";
                    break;
                case "b":
                    canvi = "a";
                    break;
                case "-a":
                    canvi = "-b";
                    break;
                case "n":
                    canvi = "m";
                    break;
                case "-n":
                    canvi = "-m";
                    break;
                case "m":
                    canvi = "n";
                    break;
                case "-m":
                    canvi = "-n";
                    break;
                default:
                    canvi = despl1;
            }
            iguals = (canvi.equals(despl2));
        }
        return iguals;
    }
}

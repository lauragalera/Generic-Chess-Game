/** @file TaulerEscacs.java
    @brief Un tauler d'escacs
    @author Laura Galera Alfaro
 */

import java.util.*;

/** @class TaulerEscacs
    @brief Espai on es disputa una partida d'escacs
    @details Files vàlides: 1.._fila; Columnes vàlides: 1.._columna;
 */

public class TaulerEscacs{

    public final int _fila;                                     ///< Total de files que té el tauler.
    public final int _columna;                                  ///< Total de columnes que té el tauler.
    private final Peca[][] _tauler;                             ///< Representació del tauler d'escacs.
    private Map<Posicio,Map<Posicio,Moviment>> _destinsPeces;   /**< \a Map que té com a clau la posició actual de cada peça (K1) i, com a descripció de cada clau, un segon \a Map amb entrades \a Posicio - \a Moviment. El segon \a Map té com a clau
                                                                 *   els destins (K2) que pot assolir la peça, donada la situació del tauler. El valor associat a cada clau és el moviment que permet a la peca  moure's de la posició
                                                                 *   actual (K1) a la posició de destí (K2).
                                                                 */
    private Map<Posicio,Map<Posicio,Posicio>>  _enrocsPeces;    /**< \a Map que té com a clau la posició actual de les peces enrocables (K1) i com a descripció de cada clau un segon \a Map amb entrades \a Posicio - \a Posicio. Aquest segon \a Map té
                                                                 *   com a clau les posicions (K2) de les peces amb les que pot enrocar i el valor associat a cada clau és la posició final en què acaba la peça que es troba a la posició
                                                                 *   K1 en realitzar l'enroc amb la peça que hi ha a la posició K2.
                                                                 */
    private Posicio _posHiHaPromo;                              ///< Guarda la posicio de la última peça que pot promocionar
    private Posicio _posReiNegre;                               ///< Guarda la posició del rei negre al tauler.
    private Posicio _posReiBlanc;                               ///< Guarda la posició del rei blanc al tauler.

    /**@brief Enumeració auxiliar usada per resumir l'estat dels reis sobre el tauler. **/
    public enum resTauler {
        NO_EFECTE,    //< No hi ha cap amenaça.
        ESCAC,        //< El rei es troba en escac.
        ESCAC_MAT,    //< El rei es troba en escac i mat.
        REI_OFEGAT,   //< El rei es troba en rei ofegat.
    }

    /**
     * @brief Crea el tauler.
     * @pre 4 <= \p fila <= 16 i 4 <= \p columna <= 16.
     * @param fila número de files del tauler.
     * @param columna número de columnes del tauler.
     * @post S'ha creat un tauler buit de dimensions \p fila x \p columna.
     * @throws IllegalArgumentException si les mides del tauler no són correctes.
     */
    public TaulerEscacs(int fila, int columna) throws IllegalArgumentException{
        _fila = fila;
        _columna = columna;
        if (_fila < 4 || _columna < 4 || _columna > 16 || _fila > 16)
            throw new IllegalArgumentException("Error en les mides del tauler");
        _tauler = new Peca[_fila + 1][_columna + 1];
        _enrocsPeces = new HashMap<>();
        _destinsPeces = new HashMap<>();
        _posReiNegre = null;
        _posReiBlanc = null;
        _posHiHaPromo = null;
    }

    /**
     * @brief Aquest tauler és una copia de \p t.
     */
    public TaulerEscacs(TaulerEscacs t){
        _fila = t._fila;
        _columna = t._columna;
        _tauler = new Peca[_fila + 1][_columna + 1];
        _destinsPeces = new HashMap<>(t._destinsPeces); //shallow copy
        _enrocsPeces = new HashMap<>(t._enrocsPeces); //shallow copy

        for (int i = 1; i <= _fila; i++) {
            for (int j = 1; j <= _columna; j++) {
                if (t._tauler[i][j] != null)
                    _tauler[i][j] = new Peca(t._tauler[i][j]);
            }
        }
        _posHiHaPromo = t._posHiHaPromo;
        _posReiBlanc = new Posicio(t._posReiBlanc);
        _posReiNegre = new Posicio(t._posReiNegre);
    }

    /**
     * @brief   Es col·loquen les peces de la partida al tauler.
     * @pre     \p conjuntPeces != null i ha de contenir dos reis, el blanc
     *          i el negre.
     * @param   conjuntPeces són les posicions del tauler
     *          acompanyada cadascuna de la peça que ocuparà aquella posició.
     * @post    El tauler conté les peces a la posició inicial
     *          per començar la partida d'escacs.
     * @exception IllegalArgumentException si alguna de les posicions no existeix
     *              al tauler.
     */
    public void posicionarPeces(Map<Posicio, Peca> conjuntPeces){
        for (Map.Entry<Posicio, Peca> pecaActual : conjuntPeces.entrySet()) {
            if(existeixPos(pecaActual.getKey())) {
                int fila = pecaActual.getKey().fila();
                int columna = pecaActual.getKey().columna();
                _tauler[_fila + 1 - fila][columna] = pecaActual.getValue();

                if (pecaActual.getValue().color() == 1 && pecaActual.getValue().nom().equals("REI")) {
                    _posReiNegre = pecaActual.getKey();
                } else if (pecaActual.getValue().color() == 0 && pecaActual.getValue().nom().equals("REI")) {
                    _posReiBlanc = pecaActual.getKey();
                }
            }else throw new IllegalArgumentException("Error: La posició " + pecaActual.getKey() + " no existeix al tauler");
        }
        calcularDestinsPeces();
        calcularEnrocsPeces();
    }

    /**
     * @brief Retorna cert si la \c Posicio \p pos existeix al tauler.
     **/
    public boolean existeixPos(Posicio pos) {
        return (0 < pos.fila() && pos.fila() <= _fila && 0 < pos.columna() && pos.columna() <= _columna);
    }

    /**
     * @brief Retorna la peça que hi ha a la \c Posicio \p pos. Si no n'hi ha cap es retorna \a null.
     **/
    public Peca solicitarPeca(Posicio pos){
        if(existeixPos(pos))
            return _tauler[_fila+1-pos.fila()][pos.columna()];
        else return null;
    }

    /**
     * @brief Retorna un \a Set amb els destins que pot assolir la peça que es troba a la \c Posicio \p pos.
     **/
    public Set<Posicio> solicitarDestinsPeca(Posicio pos){
        return _destinsPeces.get(pos).keySet();
    }

    /**
     * @brief Retorna un \a Set amb les posicions de les peces que permeten a la que es troba a la \c Posicio \p pos enrocar.
     **/
    public Set<Posicio> solicitarCompanyesEnroc(Posicio pos){
        Set<Posicio> enrocs = new HashSet<>();
        if(_enrocsPeces.containsKey(pos))
            enrocs = _enrocsPeces.get(pos).keySet();
        return enrocs;
    }

    /**
     * @brief Retorna cert si s'ha acabat el joc, fals altrament.
     **/
    public boolean fiJoc(){
        return (esEscacMat(0) || esEscacMat(1) || esReiOfegat(0) || esReiOfegat(1));
    }

    /**
     * @brief   Comprova si es pot realitzar la \c JugadaOrdinaria.
     * @pre     \p jugada != null. <br>
     *          La \p jugada és sol·licitada pel jugador del \p torn.<br>
     *          \p torn ha de ser 1 o 0.
     * @param   jugada és la jugada ordinària que es vol comprovar.
     * @param   torn és 0 si és el torn del jugador blanc o 1 si és el del jugador negre.
     * @post    Retorna cert si és possible realitzar la jugada, fals altrament.
     */
    public boolean esPotFerJugadaOrdinaria(JugadaOrdinaria jugada, int torn){
        Posicio origen = jugada.origen();
        Posicio desti = jugada.desti();
        boolean jugadaPossible = false;
        if (existeixPos(origen) && existeixPos(desti)){ //origen i desti existeixen
            if(_tauler[_fila+1-origen.fila()][origen.columna()]!=null) { //si hi ha una peça...
                boolean corresponTorn = (_tauler[_fila+1-origen.fila()][origen.columna()].color()==torn); //la peça correspon amb el torn
                if(corresponTorn && _destinsPeces.get(origen).containsKey(desti)) { //si correspon el torn i la peça pot anar a destí
                    jugadaPossible = true;
                }
            }
        }
        return jugadaPossible;
    }

    /**
     * @brief   Comprova si es pot realitzar la \c JugadaEnroc.
     * @pre     \p jugada != null. <br>
     *          La \p jugada és sol·licitada pel jugador del \p torn. <br>
     *          \p torn ha de ser 1 o 0.
     * @param   jugada és la jugada enroc que es vol comprovar.
     * @param   torn és 0 si és el torn del jugador blanc o 1 si és el del jugador negre.
     * @post    Retorna cert si és possible realitzar la jugada, fals altrament.
     */
    public boolean esPotFerJugadaEnroc(JugadaEnroc jugada, int torn){
        Posicio posicioPecaA = jugada.posicioPrimera();
        Posicio posicioPecaB = jugada.posicioSegona();
        boolean enrocPossible = false;
        if (existeixPos(posicioPecaA) && existeixPos(posicioPecaB)) { //les posicions de l'enroc existeixen al tauler
            boolean corresponTorn = (_tauler[_fila+1-posicioPecaA.fila()][posicioPecaA.columna()].color()==torn && _tauler[_fila+1-posicioPecaB.fila()][posicioPecaB.columna()].color()==torn);
            if (corresponTorn && _enrocsPeces.containsKey(posicioPecaA) && _enrocsPeces.get(posicioPecaA).containsKey(posicioPecaB)) { //existeix un enroc i aquest és possible
                enrocPossible = true;
            }
        }
        return enrocPossible;
    }

    /**
     * @brief   S'ha efectuat la \c JugadaOrdinaria.
     * @pre     \p jugada != null i ha estat vàlidada.
     * @param   jugada és la jugada ordinària que es vol realitzar.
     * @post    S'ha aplicat la \p jugada sobre el tauler i s'ha retornat cert en cas que s'hagi
     *          capturat alguna peça de l'enemic, fals altrament.
     */
    public boolean aplicarJugadaOrdinaria(JugadaOrdinaria jugada){
        Posicio origen = jugada.origen();
        Posicio desti = jugada.desti();
        Moviment mov = _destinsPeces.get(origen).get(desti); //s'obté el moviment que permet fer la jugada

        boolean capturats = aplicarMoviment(origen, desti, mov); //s'aplica la jugada

        actualitzarPromocio(origen, desti); //es comprova si amb la jugada alguna peça ha pogut promocionar

        calcularDestinsPeces(); //es calculen els nous destins
        calcularEnrocsPeces(); //es calculen els nous enrocs

        return capturats;
    }

    /**
     * @brief   S'ha realitzat la \c JugadaEnroc.
     * @pre     \p jugada != null i ha estat vàlidada.
     * @param   jugada és la jugada enroc que es vol realitzar.
     * @post    S'ha aplicat la jugada d'enroc sobre el tauler.
     */
    public void aplicarJugadaEnroc(JugadaEnroc jugada){
        Posicio posicioPecaA = jugada.posicioPrimera();
        Posicio posicioPecaB = jugada.posicioSegona();
        Posicio finalA = _enrocsPeces.get(posicioPecaA).get(posicioPecaB); //MOTOR
        Posicio finalB = _enrocsPeces.get(posicioPecaB).get(posicioPecaA); //MOTOR
        aplicarEnroc(posicioPecaA, finalA, posicioPecaB, finalB);
        jugada.assignarPosFinalPrimera(finalA);
        jugada.assignarPosFinalSegona(finalB);

        calcularDestinsPeces();
        calcularEnrocsPeces();

    }

    /**
     * @brief   Retorna la posició de la peça que promociona.
     * @pre     Cert.
     * @post    Retorna la posicio d'aquest tauler on hi ha una peça disponible
     *          per a promocionar com a conseqüència de la última jugada.
     *          Si no n'hi ha cap es retorna \a null.
     */
    public Posicio hiHaPromocio(){
        Posicio pos = _posHiHaPromo;
        _posHiHaPromo = null;
        return pos;
    }

    /**
     * @brief   S'ha promocionat la peça del tauler.
     * @pre     La peça \p novaPeca és una peça disponible, no és el rei i és del
     *          mateix color que la substituïda.<br>
     *          \p posAPro és una posició vàlida del tauler.<br>
     *          La peça que es troba a \p posAPro té la capacitat de promocionar.
     * @param   novaPeca és la peça per a la que es promociona.
     * @param   posAPro és la posició de la peça que promociona.
     * @post    La peça que inicialment hi havia a \p posAPro s'ha transformat en la peça \p novaPeca.
     */
    public void efecuarPromocio(Peca novaPeca, Posicio posAPro){

        _tauler[_fila + 1 - posAPro.fila()][posAPro.columna()] = novaPeca;

        calcularDestinsPeces();
        calcularEnrocsPeces();
    }


    /**
     * @brief   Retorna el valor de l'enumeració \c resTauler que descriu com es troba el rei
     *          contrari del \p jugador.
     * @pre     \a Aquest \a tauler no pot ser buit. <br>
     *          \p jugador ha de ser 1 o 0.
     * @param   jugador és qui ha fet la última jugada en aquest tauler: 0 si és el blanc o 1 si és el negre.
     * @post    Retorna el valor de l'enumeració \c resTauler que resumeix l'estat
     *          del rei rival de \p jugador. Els valors a retornar són \a NO_EFECTE -si no es troba en
     *          perill-, \a ESCAC, \a ESCAC_MAT o \a REI_OFEGAT.
     */
    public resTauler estatActual(int jugador){
        int reiAfectat = 0;
        if(jugador==0) reiAfectat = 1;
        resTauler efecte = resTauler.NO_EFECTE;
        if(esEscacMat(reiAfectat))
            efecte = resTauler.ESCAC_MAT;
        else if(esEscac(reiAfectat))
            efecte = resTauler.ESCAC;
        else if(esReiOfegat(reiAfectat))
            efecte = resTauler.REI_OFEGAT;
        return efecte;
    }

    /**@brief Retorna un \c String que representa aquest tauler**/
    @Override
    public String toString() {
        String s;
        String c = "abcdefghijklmnop";
        String f = "   ";
        for (int j = 0; j < _columna; j++)
            f += "+---";
        f += "+\n";
        s = f;
        for (int i = 0; i < _fila; i++) {
            if ((_fila - i) < 10)
                s += " ";
            s += (_fila - i) + " | ";
            for (int j = 0; j < _columna; j++) {
                Peca p = _tauler[i + 1][j + 1];
                if (p == null) s += " ";
                else switch (p.color()) {
                    case 0:
                        s += p.simbol();
                        break;
                    case 1:
                        s += Character.toLowerCase(p.simbol());
                        break;
                }
                s += " | ";
            }
            s += "\n" + f;
        }
        String l = "     ";
        for (int j = 0; j < _columna; j++)
            l += c.charAt(j) + "   ";
        l += "\n";
        s += l;
        s += "\n";
        return s;
    }

    //******************************************************************************************************FUNCIONS PRIVADES DEL TAULER************************************************************************************************

    /**
     * @brief   Es realitza el moviment que mou la peça d'\p origen a \p desti i es retorna cert si s'ha capturat
     *          alguna peça enemiga, fals altrament.
     * @pre     \p origen i \p desti són posicions vàlides al tauler.<br>
     *          \p origen conté una peça que segur pot accedir a \p desti amb el \c Moviment mov. <br>
     *          \p mov és un moviment o subtipus de moviment de la peça.
     * @param   origen és la posició on hi ha la peça que es vol moure.
     * @param   desti és la posició on es vol moure la peça.
     * @param   mov és el moviment de la peça que permet moure-la d'\p origen a \p desti.
     * @post    S'efectua el moviment que mou la peça d' \p origen fins a \p desti, reflectint les conseqüències del moviment sobre el tauler,
     *          i retorna cert si s'ha capturat alguna peça enemiga durant la seva realització o fals altrament.
     */
    private boolean aplicarMoviment(Posicio origen, Posicio desti, Moviment mov) {
        Peca peca = _tauler[_fila + 1 - origen.fila()][origen.columna()];
        if (peca.nom().equals("REI")) { //si es tracta del rei...
            if (peca.color() == 0) //s'actualitza la posició
                _posReiBlanc = desti;
            else _posReiNegre = desti;
        }

        List<Posicio> posCaptura = posicionsPecaCaptura(origen, desti, mov); //llista amb totes les posicions on la peça captura.

        for(Posicio pos : posCaptura){ //per cada posició on es captura
            _tauler[_fila + 1 - pos.fila()][pos.columna()] = null;
        }
        _tauler[_fila + 1 - origen.fila()][origen.columna()] = null; //la posició origen queda buida
        _tauler[_fila+1-desti.fila()][desti.columna()] = peca; //la nova posició de la peça és destí
        if (!peca.esMoguda()) peca.actualitzarMoguda(); //la peça ha estat moguda

        return (posCaptura.size()>0); //s'ha capturat alguna peça?
    }

    /**
     * @brief   S'actualitza la posició on hi ha una peça que pot promocionar.
     * @pre     \p origen i \p desti són posicions vàlides del tauler.
     *          \p desti ha de contenir una peça.
     * @param   origen és la posició inicial on es trobava la peça que s'ha mogut en la última jugada ordinària.
     * @param   desti és la posició final on es troba la peça després de la última jugada ordinària feta.
     * @post    Es comprova si amb la última jugada ordinària feta la peça afectada pot promocionar. Si és el cas,
     *          s'anota la posició, ara bé, en cas contrari, no es duu a terme cap acció.
     */
    private void actualitzarPromocio(Posicio origen, Posicio desti){
        Peca peca = _tauler[_fila+1-desti.fila()][desti.columna()];
        int colorPeca = peca.color();
        int fila = 1; //negres
        if(colorPeca == 0)
            fila = _fila; //blanques
        if(peca.potPromocionar() && desti.fila()==fila && desti.fila()!=origen.fila())
            _posHiHaPromo = desti;

    }

    /**
     * @brief   S'efectua l'enroc entre les peces que hi havia a \p posPecaA i \p posPecaB.
     * @pre     Totes les posicions d'entrada són vàlides al tauler.
     *          \p posPecaA i \p posPecaB han de contenen les peces que poden enrocar entre sí.
     *          \p posFinalA i \p posFinalB han de ser les posicions finals de cada peça respectivament.
     * @param   posPecaA és la posició on es troba la primera peça de l'enroc.
     * @param   posPecaB és la posició on es troba la segona peça de l'enroc.
     * @param   posFinalA és la posició final de la peça que inicialment hi ha a \p posPecaA després de l'enroc.
     * @param   posFinalB és la posició final de la peça que inicialment hi ha a \p posPecaB després de l'enroc.
     * @post    S'ha realitzat l'enroc entre les peces que es trobaven a \p posPecaA
     *          i \p posPecaB.
     */
    private void aplicarEnroc(Posicio posPecaA, Posicio posFinalA, Posicio posPecaB, Posicio posFinalB) {
        Peca pecaA = _tauler[_fila + 1 - posPecaA.fila()][posPecaA.columna()];
        Peca pecaB = _tauler[_fila + 1 - posPecaB.fila()][posPecaB.columna()];
        if (!pecaA.esMoguda()) //s'actualitzen les peces mogudes
            pecaA.actualitzarMoguda();
        if(!pecaB.esMoguda())
                pecaB.actualitzarMoguda();

        if (pecaA.nom().equals("REI")) { //s'actualitza la posició del rei
            if (pecaA.color() == 0)
                _posReiBlanc = posFinalA;
            else _posReiNegre = posFinalA;
        }

        if (pecaB.nom().equals("REI")) { //s'actualitza la posició del rei
            if (pecaB.color() == 0)
                _posReiBlanc = posFinalB;
            else _posReiNegre = posFinalB;
        }
        //les posicions inicials de l'enroc queden buides
        _tauler[_fila + 1 - posPecaA.fila()][posPecaA.columna()] = null;
        _tauler[_fila + 1 - posPecaB.fila()][posPecaB.columna()] = null;
        //les peces es col·loquen a les posicions finals de l'enroc
        _tauler[_fila + 1 - posFinalA.fila()][posFinalA.columna()] = pecaA;
        _tauler[_fila + 1 - posFinalB.fila()][posFinalB.columna()] = pecaB;
    }

    /**
     * @brief   Comprova si el rei es troba en estat de rei ofegat.
     * @pre     El tauler no és buit.
     * @param   colorRei és 0 si és vol comprovar pel rei blanc o 1 si es vol comprovar pel rei negre.
     * @post    Retorna cert si el rei està en estat de rei ofegat, és a dir, el jugador no té moviments legals i el
     *          rei no es troba en estat d'escac. En cas contrari es retorna fals.
     */
    private boolean esReiOfegat(int colorRei) {
        return !esEscac(colorRei) && !pecaMovimentLegal(colorRei);
    }

    /**
     * @brief   Comprova si hi ha alguna peça del jugador indicat que tingui un moviment legal.
     * @pre     El tauler no és buit.
     * @param   color és 0 si es busca un moviment legal pel jugador de blanques, 1 si es busca pel jugador de negres.
     * @post    Retorna cert si hi ha alguna peça -peces si és un enroc- del jugador \p color que tingui un moviment
     *          legal sobre el tauler actual.
     */
    private boolean pecaMovimentLegal(int color) {
        boolean pecaTeDesti = false;
        int i = 1;
        while (i <= _fila && !pecaTeDesti) {
            int j = 1;
            while (j <= _columna && !pecaTeDesti) { //cerca sobre el tauler
                Posicio posAct = new Posicio(j, _fila + 1 - i);
                Peca pecaAct = _tauler[i][j];
                if (pecaAct != null && pecaAct.color() == color){ //si hi ha una peça i aquesta és del color del jugador...
                    if(!_destinsPeces.get(posAct).isEmpty() || (_enrocsPeces.containsKey(posAct))) //existeixen destins o enrocs per aquesta peça?
                    pecaTeDesti = true;
                }
                j++;
            }
            i++;
        }
        return pecaTeDesti;
    }

    /**
     * @brief   Comprova si el rei indicat està en estat d'escac.
     * @pre     El tauler no és buit.
     * @param   colorRei és 0 si es comprova pel rei blanc o 1 si es comprova pel rei negre.
     * @post    Retorna cert si el rei de color \p colorRei es troba en situació d'escac, és a dir, sota amenaça
     *          immediata de ser capturat, fals altrament.
     */
    private boolean esEscac(int colorRei){
        boolean pecaCapturaRei = false;
        Posicio posRei = _posReiBlanc;
        if(colorRei!=0)
            posRei = _posReiNegre;
        int i = 1;
        while(i<=_fila && !pecaCapturaRei){
            int j = 1;
            while(j<=_columna && !pecaCapturaRei){ //cerca sobre el tauler
                Posicio posActual = new Posicio(j,_fila+1-i);
                Peca pecaActual = _tauler[i][j];
                if(pecaActual!=null && pecaActual.color()!=colorRei){ //alguna peça enemiga posa en perill al rei?
                    for (Moviment moviment : pecaActual.obtenirMoviments()) { //per cada moviment de la peça
                        Iterator<Map.Entry<Posicio, Moviment>> destinsPeca = posicionsDestiMov(posActual, moviment).entrySet().iterator(); //tots els possibles destins amb el seu moviment, si no n'hi ha es buit
                        while(destinsPeca.hasNext() && !pecaCapturaRei){ //cerca sobre els destins
                            Map.Entry<Posicio, Moviment> valor = destinsPeca.next();
                            List<Posicio> posCaptura = posicionsPecaCaptura(posActual, valor.getKey(), valor.getValue()); //posicions on captura la peça per aquest destí
                            pecaCapturaRei = posCaptura.contains(posRei);
                        }
                    }
                }
                j++;
            }
            i++;
        }
        return pecaCapturaRei;
    }

    /**
     * @brief   Comprova si el rei indicat està en estat d'escac i mat.
     * @pre     El tauler no és buit.
     * @param   colorRei és 0 si és vol comprovar pel rei blanc o 1 si es vol comprovar pel rei negre.
     * @post    Retorna cert si el rei de color \p colorRei es troba en situació d'escac i mat, és a dir, amenaçat per les peces contràries
     *          i sense cap moviment que el tregui d'escac, fals altrament.
     */
    private boolean esEscacMat(int colorRei){
        boolean reiEnPerill = false;
        if(esEscac(colorRei)){ //el rei es troba en escac, te alguna manera de salvar-se?
            boolean potFugir = false;
            int i=1;
            while(i<=_fila && !potFugir){
                int j=1;
                while(j<=_columna && !potFugir){ //es fa una cerca sobre el tauler
                    Posicio posAct = new Posicio(j,_fila+1-i);
                    Peca pecaAct = _tauler[i][j];
                    if(pecaAct!=null && pecaAct.color()==colorRei){ //es una peça companya del rei
                        Iterator<Map.Entry<Posicio,Moviment>> destins =  _destinsPeces.get(posAct).entrySet().iterator(); //iterador sobre els destins de la peça
                        while(destins.hasNext() && !potFugir){ //busquem un desti que salvi al rei
                            Map.Entry<Posicio, Moviment> desti = destins.next();
                            potFugir = !destiDeixaReiEnEscac(posAct, desti.getKey(), desti.getValue()); //aplicant aquest moviment d'origen a desti el rei segueix en escac?
                        }
                        if(!potFugir && _enrocsPeces.containsKey(posAct)){ //cap destí de la peça ha pogut salvar al rei però... i un enroc?
                            Iterator<Map.Entry<Posicio,Posicio>> pecaFaEnroc = _enrocsPeces.get(posAct).entrySet().iterator(); //iterador sobre els enrocs d'aquella peca
                            while(pecaFaEnroc.hasNext() && !potFugir){ //busquem l'enroc que salva al rei
                                Map.Entry<Posicio,Posicio> enroc = pecaFaEnroc.next();
                                Posicio destiEnroc = enroc.getKey(); //desti després de fer l'enroc
                                Posicio parellaEnroc = enroc.getKey(); //posicio de la peça amb la que fa enroc
                                Posicio destiSegonaPeca = _enrocsPeces.get(parellaEnroc).get(posAct); //desti de la peça companya després de fer l'enroc
                                potFugir = !enrocDeixaReiEnEscac(posAct, destiEnroc, parellaEnroc, destiSegonaPeca); //aplicant l'enroc el rei segueix en escac?
                            }
                        }
                    }
                    j++;
                }
                i++;
            }
            reiEnPerill = !potFugir;
        }
        return reiEnPerill;
    }


    /**
     * @brief   Genera el \a Map de destins de les peces.
     * @pre     El tauler no és buit.
     * @post    S'ha generat el \a Map de destins on cada posició que conté una peça representa una clau i aquesta té associada com a valor
     *          un \a Map de les posicions de destí amb el moviment que permet a la peça arribar a aquell destí.
     */
    private void calcularDestinsPeces() {
        _destinsPeces = new HashMap<>();
        for (int i = 1; i <= _fila; i++) {
            for (int j = 1; j <= _columna; j++) { //es recorre el tauler
                Posicio posAct = new Posicio(j, _fila + 1 - i);
                Peca pecaAct = _tauler[i][j];
                if (pecaAct != null) { //si hi ha una peça...
                    for (Moviment moviment : pecaAct.obtenirMoviments()) { //per cada moviment de la peça
                        Map<Posicio, Moviment> destinsPeca = posicionsDestiMov(posAct, moviment); //tots els possibles destins amb el seu moviment, si no n'hi ha es buit
                        destinsPeca.entrySet().removeIf(k -> jugadaMataReiContrari(posAct, k.getKey(), k.getValue())); //una peça mai pot matar a un rei
                        destinsPeca.entrySet().removeIf(k -> destiDeixaReiEnEscac(posAct, k.getKey(), k.getValue())); //eliminem els destins que deixen al propi rei en escac
                        if (_destinsPeces.putIfAbsent(posAct, destinsPeca) != null) //afegim el mapa a la peça
                            _destinsPeces.get(posAct).putAll(destinsPeca);
                    }
                }
            }
        }
    }

    /**
     * @brief   Genera el \a Map d'enrocs de les peces que poden enrocar.
     * @pre     El tauler no és buit.
     * @post    S'ha generat el \a Map d'enrocs on cada posició que conté una peça amb un enroc possible és una clau i aquesta té associada com a valor
     *          un \a Map de posicions. El primer valor representa la posició de la peça amb la que fa l'enroc i com a valor el seu destí després d'efectuar
     *          l'enroc.
     */
    private void calcularEnrocsPeces() {
        _enrocsPeces = new HashMap<>();
        for (int i = 1; i <= _fila; i++) {
            for (int j = 1; j <= _columna; j++) { //recorrem el tauler
                Posicio posPrimera = new Posicio(j, _fila + 1 - i);
                Peca pecaPrimera = _tauler[i][j];
                if (pecaPrimera != null && pecaPrimera.potEnrocar()) { //si la peça és candidata a tenir un enroc
                    for (int k = 1; k <= _columna; k++) { //recorrem la fila de la peça
                        Posicio posSegona = new Posicio(k, _fila + 1 - i);
                        Peca pecaSegona = _tauler[i][k];
                        if (pecaSegona != null && pecaPrimera.color() == pecaSegona.color()) { //si trobem una peça candidata a ser companya d'enroc
                            Enroc enroc = pecaPrimera.obtenirEnroc(pecaSegona);
                            if(enroc!=null) { //existeix un enroc entre les dues peces
                                Posicio primeraEnroc = posPrimera;
                                Posicio segonaEnroc = posSegona;
                                boolean canvi = false;
                                if (!pecaPrimera.nom().equals(enroc.primeraPeca())) { //l'enroc és al reves
                                    canvi = true;
                                    primeraEnroc = posSegona;
                                    segonaEnroc = posPrimera;
                                }
                                if (esPossibleEnroc(primeraEnroc, segonaEnroc, enroc)) { //es donen les condicions per a fer l'enroc
                                    Pair<Posicio, Posicio> posicionsFinals = posicionsFinalEnroc(primeraEnroc, segonaEnroc); //posicions finals de l'enroc
                                    Posicio pecaPosFinal = posicionsFinals.first; //posicio final de pecaPrimera si enroca amb pecaSegona
                                    if (canvi) //si s'han canviat les posicions perquè pecaPrimera és la segona de l'enroc
                                        pecaPosFinal = posicionsFinals.second; //la posicio final de pecaPrimera és la segona
                                    Map<Posicio, Posicio> parellaDesti = new HashMap<>();
                                    parellaDesti.put(posSegona, pecaPosFinal); //afegim la companya d'enroc i la posició final
                                    boolean enrocDeixaReiEscac = enrocDeixaReiEnEscac(primeraEnroc, posicionsFinals.first, segonaEnroc, posicionsFinals.second);
                                    if (!enrocDeixaReiEscac) { //si aquest enroc no deixa al rei en escac...
                                        if (_enrocsPeces.putIfAbsent(posPrimera, parellaDesti) != null) //s'afegeix l'enroc
                                            _enrocsPeces.get(posPrimera).put(posSegona, pecaPosFinal);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * @brief   Comprova si, efectuant l'enroc, el rei del mateix bàndol queda en estat d'escac.
     * @pre     Totes les posicions d'entrada existeixen al tauler.<br>
     *          \p posA i \p posB contenen les peces d'un enroc vàlid.
     *          \p posFiA i \p posFiB han de ser les posicions finals de cada peça al fer l'enroc.
     * @param   posA és la posició on es troba la primera peça de l'enroc.
     * @param   posB és la posició on es troba la segona peça de l'enroc.
     * @param   posFiA és la posició final de la peça que inicialment hi ha a \p posA després de l'enroc.
     * @param   posFiB és la posició final de la peça que inicialment hi ha a \p posB després de l'enroc.
     * @post    Retorna cert si efectuant l'enroc entre les peces que hi ha a \p posA i \p posB el seu rei
     *          queda en estat d'escac, fals altrament.
     */
    private boolean enrocDeixaReiEnEscac(Posicio posA, Posicio posFiA, Posicio posB, Posicio posFiB){
        int jugadorActual = _tauler[_fila+1-posA.fila()][posA.columna()].color();
        boolean hiHaEscac;
        TaulerEscacs nouTauler = new TaulerEscacs(this); //copiem el tauler actual
        nouTauler.aplicarEnroc(posA, posFiA, posB, posFiB); //fem la jugada enroc
        hiHaEscac = (nouTauler.esEscac(jugadorActual)); //comprovem si es troba en escac
        return hiHaEscac;
    }

    /**
     * @brief   Comprova si, efectuant el moviment que mou la peça d'origen a destí, el rei del mateix bàndol queda en estat d'escac.
     * @pre     \p origen i \p desti existeixen al tauler.<br>
     *          \p origen conté una peça que podria accedir a \p desti amb \p mov. <br>
     *          \p mov ha de ser un moviment o subtipus de moviment de la peça.
     * @param   origen és la posició on hi ha la peça que es vol moure.
     * @param   desti és la posició on es vol moure la peça.
     * @param   mov és el moviment que permet a la peça moure's d'\p origen a \p desti.
     * @post    Retorna cert si efectuant el moviment que mou la peça d'\p origen a \p desti el seu rei
     *          queda en estat d'escac, fals altrament.
     */
    private boolean destiDeixaReiEnEscac(Posicio origen, Posicio desti, Moviment mov) {
        int jugadorActual = _tauler[_fila + 1 - origen.fila()][origen.columna()].color();
        TaulerEscacs nouTauler = new TaulerEscacs(this); //copiem el tauler actual
        boolean reiEnEscac;
        nouTauler.aplicarMoviment(origen, desti, mov); //fem la jugada
        reiEnEscac = nouTauler.esEscac(jugadorActual); //comprovem si el rei ha quedat en escac
        return (reiEnEscac);
    }

    /**
     * @brief   Comprova si el rei contrari seria capturat realitzant el moviment que porta la peça d'\p origen a \p desti.
     * @pre     \p origen i \p desti existeixen al tauler.
     *          \p origen conté una peça que podria accedir a \p desti amb \p mov. <br>
     *          \p mov ha de ser un moviment o subtipus de moviment de la peça.
     * @param   origen és la posició on hi ha la peça que es vol moure.
     * @param   desti és la posició on es vol moure la peça.
     * @param   mov és el moviment de la peça que permet moure-la d'\p origen a \p desti.
     * @post    Retorna cert si, simulant el moviment que mou la peça d'\p origen a \p desti, el rei contrari
     *          seria capturat -situació impossible en els escacs-, fals altrament.
     */
    private boolean jugadaMataReiContrari(Posicio origen, Posicio desti, Moviment mov){
        int jugador = _tauler[_fila+1-origen.fila()][origen.columna()].color();
        boolean capturaRei = false;
        ListIterator<Posicio> posicionsAtrapa = posicionsPecaCaptura(origen, desti, mov).listIterator(); //totes les posicions on es captura
        while(posicionsAtrapa.hasNext() && !capturaRei){ //alguna posició on captura és la del rei?
            Posicio perill = posicionsAtrapa.next();
            Peca pecaPerill = _tauler[_fila+1-perill.fila()][perill.columna()];
            if(pecaPerill!=null && pecaPerill.nom().equals("REI") && pecaPerill.color()!=jugador)
                capturaRei = true; //captura al rei contrari
        }
        return capturaRei;
    }

    /**
     * @brief   Retorna les posicions on es capturen peces quan s'efectua la jugada que mou la peça d'\p origen a \p destí.
     * @pre     \p origen i \p desti existeixen al tauler.<br>
     *          \p origen conté una peça que podria accedir a \p desti amb \p mov. <br>
     *          \p mov ha de ser un moviment o subtipus de moviment de la peça.
     * @param   origen és la posició on hi ha la peça que es vol moure.
     * @param   desti és la posició on es vol moure la peça.
     * @param   mov és el moviment que permet a la peça moure's d'\p origen a \p desti.
     * @post    Retorna una llista amb les posicions del tauler on hi ha les peces que seran capturades una vegada s'efectui la jugada
     *          que mou la peça d'\p origen a \p destí usant el moviment \p mov. Si no es captura cap peça la llista és buida.
     */
    private List<Posicio> posicionsPecaCaptura(Posicio origen, Posicio desti, Moviment mov){
        List<Posicio> llPosicions = new ArrayList<>();
        Peca pecaOrigen = _tauler[_fila+1-origen.fila()][origen.columna()];

        if (mov.movSalta() == 2) { //el moviment indica que la peça captura saltant
            Posicio posAct = seguentPosicio(origen,desti);
            while (!posAct.equals(desti)) {
                Peca pecaTrobada = _tauler[_fila + 1 - posAct.fila()][posAct.columna()];
                if (pecaTrobada != null && pecaTrobada.color() != pecaOrigen.color() && !pecaTrobada.esInvulnerable()){ //aquesta peça serà capturada
                    llPosicions.add(posAct);
                }
                posAct = seguentPosicio(posAct,desti);
            }
        }

        Peca pecaDesti = _tauler[_fila + 1 - desti.fila()][desti.columna()];
        if (pecaDesti != null) { //hi ha una peça a destí, aquesta també serà capturada
            llPosicions.add(desti);
        }
        return llPosicions;
    }

    /**
     * @brief   Retorna un \a Map de les posicions de destí de la peça acompanyades del moviment que ho permet.
     * @pre     \p origen és una posició vàlida del tauler i conté una peça.
     *          \p moviment ha de ser un moviment de la peça que hi ha a \p origen.
     * @param   origen és la posició on es troba la peça.
     * @param   moviment és un dels moviments de la peça.
     * @post    Retorna un \a Map que té com a clau els destins que pot assolir la peça que es troba a \p origen
     *          efectuant el \c Moviment \p moviment. Com a valor de cada destí aparèix el moviment que ho permet, essent
     *          aquest el mateix \p moviment o un subtipus, considerant que si és un \c Moviment \a LITERAL_ENTER apareixerà el
     *          seu \c Moviment \a LITERAL_NATURAL, o si la peça és de color negre el moviment estarà invertit.
     */
    private Map<Posicio, Moviment> posicionsDestiMov(Posicio origen, Moviment moviment) {
        List<Moviment> llMoviments = new ArrayList<>();
        if (moviment.consultarSubtipus() == Moviment.subTipus.LITERAL_ENTER) //si el moviment és literal enter...
            llMoviments.addAll(moviment.dividirMov()); //dividim el moviment en dos moviments naturals
        else
            llMoviments.add(moviment); //s'afegeix el moviment tal i com és.
        Map<Posicio, Moviment> posicions = new HashMap<>();
        for (Moviment mov : llMoviments) { //per cada moviment...
            if(_tauler[_fila + 1 - origen.fila()][origen.columna()].color() == 1) //si la peça és negra
                mov = mov.invertir(); //el moviment s'inverteix
            if (mov.consultarSubtipus() == Moviment.subTipus.NUMERIC) { //Si es un moviment numèric només hi ha una posicio de desti
                int sumaCol = Integer.parseInt(mov.columna());
                int sumaFil = Integer.parseInt(mov.fila());
                if (filaColDinsLimit(origen.columna() + sumaCol, origen.fila() + sumaFil)) { //si és una posicio vàlida...
                    Posicio novaPos = new Posicio(origen.columna() + sumaCol, origen.fila() + sumaFil); //es crea la posicio
                    if (existeixPos(novaPos) && esPossibleAnarDesti(origen, novaPos, mov)) //si existeix dins del tauler...
                        posicions.put(novaPos, mov);
                }
            } else { //Si es tracta d'un moviment LITERAL NATURAL
                if (mov.esCombinat()) { //Si es un moviment combinat com [n,m], [n, -4]...
                    posicions.putAll(trobarDestiCombinat(origen, mov));
                }
                else { //si és rectilini o diagonal...
                    boolean calSeguir = true;
                    Posicio posActual = seguentPosicio(origen, mov);
                    while (posActual != null && existeixPos(posActual) && calSeguir) {
                        if (esPossibleAnarDesti(origen, posActual, mov)) //si es donen les condicions per arribar a desti...
                            posicions.put(posActual, mov); //afegeixo la posició...
                        if (mov.movSalta() == 0 && _tauler[_fila + 1 - posActual.fila()][posActual.columna()] != null) //si el moviment no permet saltar peces i hi ha una peca...
                            calSeguir = false; //no cal que continuem
                        posActual = seguentPosicio(posActual, mov);
                    }
                }
            }
        }
        return posicions;
    }

    /**
     * @brief Retorna un \a Map amb totes les possibles posicions de destí acompanyades del moviment combinat que ho permet.
     * @pre   \p origen és una posició vàlida del tauler i conté una peça. <br>
     *        \p mov ha de ser combinat, de subtipus literal natural i de la peça que hi ha a \p origen.
     * @param origen és la posició on es troba la peça.
     * @param mov és un moviment de la peça que hi ha a \p origen.
     * @post  Donat el \c Moviment combinat i \a LITERAL_NATURAL d'una peça que es troba a \p origen, retorna
     *        un \a Map que té com a clau totes les posicions de destí a les que podria accedir la peça si se li apliqués el
     *        moviment \p mov, considerant les característiques del moviment i la situació del tauler. Com a valor de cada clau
     *        aparèix el moviment que permet anar d'\origen a destí, és a dir, \p mov.
     */
    private Map<Posicio, Moviment> trobarDestiCombinat(Posicio origen, Moviment mov) {
        int incrF = 1; //increment que es suma la fila
        int incrC = 1; //increment que es suma a la columna
        int maxIncrF = _fila - 1; //increment màxim de fila, considerant el pitjor cas.
        int maxIncrC = _columna - 1; //increment màxim de columna, considerant el pitjor cas.
        if (!mov.fila().equals("n") && !mov.fila().equals("m")) { //si és -m, -n o un número...
            incrF = -1; //l'increment és negatiu
            if (!mov.fila().equals("-n") && !mov.fila().equals("-m")) { //si es tracta d'un número...
                incrF = Integer.parseInt(mov.fila()); //l'increment és el número
                maxIncrF = Math.abs(incrF); //el màxim de files que s'avancen és l'increment en valor absolut
            }
        }
        //Fem el mateix per a les columnes...
        if (!mov.columna().equals("n") && !mov.columna().equals("m")) {
            incrC = -1;
            if (!mov.columna().equals("-n") && !mov.columna().equals("-m")) {
                incrC = Integer.parseInt(mov.columna());
                maxIncrC = Math.abs(incrC);
            }
        }
        int antIncF = incrF; //Guardem a una variable l'increment inicial de la fila
        int antIncCol = incrC; //Guardem a una variable l'increment inicial de la columna
        boolean fiColm = false; //Variable per saber si no queden més columnes
        boolean fiFiles = false; //Variable per saber si no queden més files
        Map<Posicio, Moviment> destins = new HashMap<>();
        if (!(origen.fila() + incrF > _fila || origen.fila() + incrF < 1 || origen.columna() + incrC > _columna || origen.columna() + incrC < 1)) { //si sumant l'increment a la fila o la columna la posició ja no existeix vol dir que no hi ha cap destí
            while (Math.abs(incrC) <= maxIncrC && !fiColm) { //Mentre no haguem arribat al maxim de columnes avançades i encara quedin columnes...
                while (Math.abs(incrF) <= maxIncrF && !fiFiles) { //Mentre no haguem arribat al maxim de files avançades i encara quedin files...
                        Posicio posAct = new Posicio(origen.columna() + incrC, origen.fila() + incrF); //nova posició
                        if (!(existeixPos(posAct))) //si no existeix la posició...
                            fiFiles = true; //no podem seguir
                        else if (esPossibleAnarDesti(origen, posAct, mov)) { //La nova posició existeix però podem anar-hi segons mov.movCaptura() i el tauler?
                            destins.put(posAct, mov); //s'afegeix el destí amb el moviment.
                        }
                        incrF = incrF + antIncF; //avancem a la següent fila
                        if(!filaColDinsLimit(origen.columna()+incrC, origen.fila()+incrF)) fiFiles = true; //la següent fila no es troba dins els limits de posició
                }
                incrF = antIncF; //tornem a començar pel mateix increment de la fila
                fiFiles = false;
                incrC = incrC + antIncCol; //avancem a la següent columna
                if (origen.columna() + incrC > _columna || origen.columna() + incrC < 1) //si voliem accedir a una columna que no existeix...
                    fiColm = true; //no podem seguir
            }
        }
        return destins;
    }

    /**
     * @brief   Comprova si és possible anar d'\p origen a \p desti usant el moviment indicat.
     * @pre     \p origen i \p desti són posicions vàlides del tauler. <br>
     *          \p origen conté una peça i \p desti és un possible destí per a ella. <br>
     *          \p mov ha de ser un moviment de la peça o un subtipus d'algun.
     * @param   origen és la posició on es troba la peça.
     * @param   desti és la posició on es vol moure la peça.
     * @param   mov és el moviment de la peça que aconsegueix dur-la d'origen a destí.
     * @post    Retorna cert si és possible anar d'\p origen a \p desti efectuant el \c Moviment \p mov,
     *          tenint en compte les caracterísitiques del moviment (si captura a destí o salta peces) i
     *          la situació del tauler, en funció del trajecte per anar d'\p origen a \p desti i la disposició de les altres
     *          peces sobre el tauler. Si no és viable retorna fals.
     */
    private boolean esPossibleAnarDesti(Posicio origen, Posicio desti, Moviment mov) {
        boolean arribaDesti = false;
        Peca pecaOrigen = _tauler[_fila + 1 - origen.fila()][origen.columna()];
        Peca pecaDesti = _tauler[_fila + 1 - desti.fila()][desti.columna()];
        int movCapturaDesti = mov.movCaptura(); //capacitat de capturar a destí.
        boolean movNoCaptura = (movCapturaDesti == 0 && pecaDesti != null); //cert si moviment no permet capturar a destí però hi ha una peça
        boolean noPotCapturar = (pecaDesti != null && (movCapturaDesti == 1 || movCapturaDesti == 2) && pecaDesti.color() == pecaOrigen.color()); // cert si pot capturar a destí pero la peça és del mateix jugador
        boolean capturarOblig = (movCapturaDesti == 2 && pecaDesti == null); //cert si la peça obligatòriament ha de capturar a destí però no hi ha peça
        boolean capturarInvulnerable = (pecaDesti != null && pecaDesti.esInvulnerable() && (movCapturaDesti == 2 || movCapturaDesti == 1)); //cert si la peça l'hauria de capturar a destí però l'enemic és invulnerable
        //si no es compleix cap dels casos comentats...
        if(!(movNoCaptura || noPotCapturar || capturarOblig || capturarInvulnerable)){
            arribaDesti = true;
            //si el moviment no permet saltar peces enemigues...
            if (mov.movSalta() == 0) {
                boolean existeixPeca = false;
                Posicio posActual = seguentPosicio(origen, desti);
                while (!existeixPeca && !posActual.equals(desti)) { //mentre no s'arriba al desti i no es troba cap peça en el trajecte
                    if (_tauler[_fila + 1 - posActual.fila()][posActual.columna()] != null)
                        existeixPeca = true;
                    posActual = seguentPosicio(posActual, desti);
                }
                arribaDesti = !existeixPeca;
            }
        }
        return arribaDesti;
    }

    /**
     * @brief   Comprova si l'enroc és possible entre les peces que es troben a les dues posicions
     * @pre     \p posPecaA i \p posPecaB són posicions vàlides del tauler. <br>
     *          \p posPecaA i \p posPecaB contenen dues peces i aquestes són les de \p enroc.
     * @param   posPecaA és la posició on hi ha la primera peça de l'enroc.
     * @param   posPecaB és la posició on hi ha la segona peça de l'enroc.
     * @param   enroc és l'enroc entre les peces de \p posPecaA i \p posPecaB
     * @post    Es retorna cert si l'enroc entre les peces que hi ha a les posicions \p posPecaA i \p posPecaB
     *          és possible a causa de les característiques de l'enroc i l'estat del tauler, fals altrament.
     */
    private boolean esPossibleEnroc(Posicio posPecaA, Posicio posPecaB, Enroc enroc) {
        boolean esValid = false;
        if (Math.abs(posPecaB.columna() - posPecaA.columna()) > 1) { //com a mínim hi ha d'haver dos espais
            Peca pecaA = _tauler[_fila + 1 - posPecaA.fila()][posPecaA.columna()];
            Peca pecaB = _tauler[_fila + 1 - posPecaB.fila()][posPecaB.columna()];
            if ((enroc.quiets() && !pecaA.esMoguda() && !pecaB.esMoguda()) || !enroc.quiets()) { //es compleix la condició de quiets, si cal
                Pair<Posicio, Posicio> novesPos = posicionsFinalEnroc(posPecaA, posPecaB); //posicions finals dels enrocs
                Peca existeixPecaA = _tauler[_fila + 1 - novesPos.first.fila()][novesPos.first.columna()];
                Peca existeixPecaB = _tauler[_fila + 1 - novesPos.second.fila()][novesPos.second.columna()];
                if (existeixPecaA == null && existeixPecaB == null) {//si no hi ha cap peça als llocs on aniran les peces de l'enroc...
                    esValid = true;
                    if (enroc.buitAlMig()) { //es dona la condició de buit al mig
                        boolean hiHaPeces = false;
                        Posicio posActual = seguentPosicio(posPecaA, posPecaB);
                        while (!hiHaPeces && !posActual.equals(posPecaB)) { //cerca per trobar una peça entre les posicions de l'enroc
                            if (_tauler[_fila + 1 - posActual.fila()][posActual.columna()] != null)
                                hiHaPeces = true;
                            posActual = seguentPosicio(posActual, posPecaB);
                        }
                        esValid = !hiHaPeces;
                    }
                }
            }
        }
        return esValid;
    }

    /**
     * @brief   Retorna la posició següent a \p posActual per intentar arribar a \p posFinal.
     * @pre     \p posActual i \p posFinal han d'estar a la mateixa diagonal, fila o columna.
     * @param   posActual és la posició actual i a partir de la qual s'intenta arribar a \p posFinal.
     * @param   posFinal és la posició a la que es vol arribar.
     * @post    Es retorna una nova posició que representa la posició a la que hauria de moure's
     *          una peça que es troba a \p posActual si vulgués arribar a \p posFinal, considerant
     *          que només es pot moure d'una casella a una altra consecutiva.
     */
    private static Posicio seguentPosicio(Posicio posActual, Posicio posFinal) {
        int difFila = posFinal.fila() - posActual.fila();
        int difCol = posFinal.columna() - posActual.columna();
        if (difFila != 0)
            difFila = difFila / Math.abs(difFila);
        if (difCol != 0)
            difCol = difCol / Math.abs(difCol);

        return new Posicio(posActual.columna() + difCol, posActual.fila() + difFila);
    }

    /** @brief Retorna cert si \p c i \p f són valors dins dels límits d'un tauler*/
    private static boolean filaColDinsLimit(int c, int f) {
        return (c > 0 && c <= 16 && f > 0 && f <= 16);
    }

    /**
     * @brief   Retorna la posició següent a \p posActual desplacant-se segons el moviment \p mov.
     * @pre     \p mov no pot ser de subtipus \a LITERAL_ENTER i tampoc combinat.
     * @param   posActual és la posició actual i sobre la que s'aplica l'orientació del Moviment \p mov.
     * @param   mov és el moviment que s'aplica sobre \p posActual per conèixer quina ha de ser la següent posició.
     * @post    Retorna la següent posició a la que es mouria una peça que actualment es troba a \p posActual, movent-se
     *          d'una casella a una altra de consecutiva, si se li apliqués el moviment \p mov, és a dir, prenent
     *          com a refèriencia l'orientació del moviment.
     */
    private static Posicio seguentPosicio(Posicio posActual, Moviment mov) {
        int col = 1;
        int fila = 1;

        if (mov.columna().equals("-n") || mov.columna().equals("-m"))
            col = -1;
        else if (mov.columna().equals("0"))
            col = 0;
        else if (!mov.columna().equals("n") && !mov.columna().equals("m"))
            col = Integer.parseInt(mov.columna()) / Math.abs(Integer.parseInt(mov.columna()));

        if (mov.fila().equals("-n") || mov.fila().equals("-m"))
            fila = -1;
        else if (mov.fila().equals("0"))
            fila = 0;
        else if (!mov.fila().equals("n") && !mov.fila().equals("m"))
            fila = Integer.parseInt(mov.fila()) / Math.abs(Integer.parseInt(mov.fila()));

        if (filaColDinsLimit(posActual.columna() + col, posActual.fila() + fila))
            return new Posicio(posActual.columna() + col, posActual.fila() + fila);
        return null;
    }

    /**
     * @brief   Retorna les posicions finals de les dues peces que
     *          participen en un enroc.
     * @pre     \p posA i \p posB són posicions vàlides del tauler i pertanyen a la mateix fila.
     * @param   posA és la posició on es troba la primera peça de l'enroc.
     * @param   posB és la posició on es troba la segona peça de l'enroc.
     * @post    Es retorna una parella de valors \c Posicio on el primer element
     *          és la \c Posicio final de la peça que es troba a \p posA i el segon
     *          element és la \c Posicio final de la peça que es troba a \p posB
     *          després de l'enroc.
     */
    private static Pair<Posicio, Posicio> posicionsFinalEnroc(Posicio posA, Posicio posB) {
        int dif = posB.columna() - posA.columna();
        int fila = posA.fila();
        int incr;
        if (Math.abs(dif) % 2 != 0) { //si es imparell...
            incr = dif / 2 + dif / Math.abs(dif); //no hi ha un mig concret, depen de si la peçaA està a la dreta o l'esquerra.
        } else {
            incr = dif / 2; //hi ha un mig concret
        }
        Posicio novaPosA = new Posicio(posA.columna() + incr, fila);
        if (dif > 0) { //si la peçaB està a la dreta
            incr = -1; //va a l'esquerra de la peçaA
        } else { //si la peçaB està a l'esquerra
            incr = 1; //va a la dreta de la peçaA
        }
        Posicio novaPosB = new Posicio(novaPosA.columna() + incr, fila);
        return new Pair<>(novaPosA, novaPosB);
    }
}
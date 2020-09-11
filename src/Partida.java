/**
 * @file Partida.java
 * @brief Una partida d'escacs.
 * @author David Pérez Sánchez
 */

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.*;

/**
 * @class Partida
 * @brief Una partida d'escacs, encarregada de gestionar la dinàmica de joc i el tauler.
 */
public class Partida {
    public final static String [] COLOR = {"BLANQUES","NEGRES"};   ///< Colors dels jugadors.

    /**
     * @brief Enumeració dels tipus de tirades especials que es poden efectuar en una partida.
     */
    public enum TiradaEspecial {
        DEMANAR_TAULES, ACCEPTAR_TAULES, DENEGAR_TAULES, AJORNAR, RENDIR;

        @Override
        public String toString() {
            switch (this) {
                case DEMANAR_TAULES: return "taules";
                case ACCEPTAR_TAULES: return "acceptar";
                case DENEGAR_TAULES: return "denegar";
                case AJORNAR: return "ajornar";
                case RENDIR: return "rendir";
                default: return "";
            }
        }
    }

    /**
     * @brief Enumeració dels possibles resultats d'una tirada.
     */
    private enum ResultatTirada {
        ESCAC, ESCAC_MAT, REI_OFEGAT, TAULES_INACCIO, TAULES_ESCAC_CONTINU, NO_EFECTE, TAULES_ACCEPTADES,
        TAULES_DENEGADES, RENDICIO, AJORNAMENT, TAULES_DEMANADES;

        @Override
        public String toString() {
            switch (this) {
                case ESCAC: return "ESCAC";
                case ESCAC_MAT: return "ESCAC I MAT";
                case REI_OFEGAT: return "TAULES PER REI OFEGAT";
                case TAULES_INACCIO: return "TAULES PER INACCIO";
                case TAULES_ESCAC_CONTINU: return "TAULES PER ESCAC CONTINU";
                case TAULES_ACCEPTADES: return "TAULES ACCEPTADES";
                case TAULES_DEMANADES: return "TAULES SOL·LICITADES";
                case AJORNAMENT: return "AJORNAMENT";
                case RENDICIO: return "RENDICIÓ";
                default: return "";
            }
        }
    }

    /**
     * @class Dades
     * @brief Conté els atributs de Partida que poden ser modificats a cada torn i, per tant, és equiparable a l'estat
     *        actual de la Partida.
     */
    private static class Dades {
        public final TaulerEscacs _tauler; ///< Tauler d'escacs sobre el que es duu a terme el joc.
        public int _tornActual;            ///< Torn actual.
        public int _tornsEscacsB;          ///< Torns consecutius fins al moment on s'ha produit escac per blanques.
        public int _tornsEscacsN;          ///< Torns consecutius fins al moment on s'ha produit escac per negres.
        public int _tornsInaccio;          ///< Torns consecutius fins al moment sense capturar cap peça.

        /**
         * @brief Constructor amb paràmetres
         * @pre \p t != null, 0 <= \p tornActual <= 1, \p tornsEscacsB > 0, \p tornsEscacsN > 0, \p tornsInaccio > 0
         * @post Es crea un objecte Dades amb els paràmetres indicats.
         * @param t Tauler d'escacs.
         * @param tornActual A quin jugador li pertany el torn actual.
         * @param tornsEscacsB Quantitat de torns consecutius en què s'ha produit escac per el jugador de blanques.
         * @param tornsEscacsN Quantitat de torns consecutius en què s'ha produit escac per el jugador de negres.
         * @param tornsInaccio Quantitat de torns consecutius en què no s'ha capturat cap peça.
         */
        public Dades(TaulerEscacs t, int tornActual, int tornsEscacsB, int tornsEscacsN, int tornsInaccio){
            _tauler = t;
            _tornActual = tornActual;
            _tornsEscacsB = tornsEscacsB;
            _tornsEscacsN = tornsEscacsN;
            _tornsInaccio = tornsInaccio;
        }

        /**
         * @brief Constructor còpia
         * @pre \p d != null
         * @post Es crea un objecte que és una còpia de \p d
         * @param d Objecte que es vol copiar.
         */
        public Dades(Dades d) {
            _tauler = new TaulerEscacs(d._tauler);
            _tornActual = d._tornActual;
            _tornsEscacsB = d._tornsEscacsB;
            _tornsEscacsN = d._tornsEscacsN;
            _tornsInaccio = d._tornsInaccio;
        }
    }
    private Dades _dades;   ///< Dades susceptibles de ser modificades al realitzar una tirada.

    private final int _limitEscacs; ///< Nombre d'escacs consecutius permesos.
    private final int _limitInaccio; ///< Nombre de torns seguits sense capturar cap peça permesos.
    private final Map<String,Peca> _pecesDisponibles; ///< Totes les peces amb què es pot jugar en la partida actual.
    private final String _fitxerRegles; ///< Ruta del fitxer de regles.
    private final LinkedHashMap<Posicio,Peca> _posicionsInicials; ///< Posicions de les peces a l'inici de la partida.
    private String _resultatPartida; ///< Resultat final de la partida, és a dir, per quina raó ha finalitzat.

    private final Stack<Dades> _historialDadesTirar;
        ///< Historial dels estats de la partida abans de cada tirada efectuada.
    private final Stack<Dades> _historialDadesDesfer;
        ///< Historial dels estats de la partida de les tirades desfetes.
    private final Stack<LinkedHashMap<String,String>> _historialJugadesTirar;
        ///< Historial de les jugades realitzades.
    private final Stack<LinkedHashMap<String,String>> _historialJugadesDesfer;
        ///< Historial de les jugades desfetes.

    /**
     * @brief Constructor amb paràmetres.
     * @pre \p limitEscacs > 1 i \p limitInaccio > 1 i 0 <= \p torn <= 1.
     * @post Es crea una Partida amb el tauler buit.
     * @param t Tauler d'escacs en què es jugarà.
     * @param pecesDisponibles Totes les peces amb què es pot jugar en aquesta partida.
     * @param limitEscacs Nombre d'escacs consecutius sobre un mateix jugador que s'han de dur a terme perquè es
     *                    consideri que la partida ha de finalitzar en taules.
     * @param limitInaccio Nombre de torns seguits (parell de tirades consecutives) que han de passar sense que es
     *                     capturi cap peça perquè es consideri que la partida ha de finalitzar en taules.
     * @param fitxerRegles Fitxer que conté les regles del joc.
     * @param torn Torn amb què començarà la partida.
     * @throws IllegalArgumentException Si els límits no són més grans que 1.
     */
    Partida(TaulerEscacs t, Map<String,Peca> pecesDisponibles, int limitEscacs, int limitInaccio, String fitxerRegles,
            int torn) {
        if(limitEscacs <= 1 || limitInaccio <= 1)
            throw new IllegalArgumentException("Error: els límits han de ser nombres enters més grans que 1");
        _pecesDisponibles = pecesDisponibles;
        _limitEscacs = limitEscacs;
        _limitInaccio = limitInaccio;
        _fitxerRegles = fitxerRegles;
        _posicionsInicials = new LinkedHashMap<>();
        _resultatPartida = "";
        _historialDadesTirar = new Stack<>();
        _historialDadesDesfer = new Stack<>();
        _historialJugadesTirar = new Stack<>();
        _historialJugadesDesfer = new Stack<>();
        _resultatPartida = "";
        _dades = new Dades(t, torn, 0, 0, 0);
    }

    /**
     * @brief Posiciona al tauler totes les peces a les posicions que els pertoca.
     * @pre \p ordrePeces != null i les posicions inicials no causen l'acabament de la partida.
     * @post El tauler té les peces posicionades pel jugador blanc segons indica \p ordrePeces i el jugador negre
     *       també (però de forma simètrica al altre costat del tauler).
     * @param ordrePeces Ordre en el que s'han de posicionar les peces a tauler segons la perspectiva del jugador blanc.
     */
    public void inicialitzarPeces(Collection<String> ordrePeces) {
        Iterator<String> it = ordrePeces.iterator();
        Map<Posicio,Peca> res = new HashMap<>();
        int mida = _dades._tauler._fila * _dades._tauler._columna;
        int c = 1, f = 1;
        while(it.hasNext() && (c*f) <= mida) {
            String nom = it.next();
            if(!nom.equals("")) {
                // Blanques
                Peca peca = new Peca(_pecesDisponibles.get(nom));
                int columna = c;
                int fila = f;
                Posicio pos = new Posicio(columna,fila);
                peca.assignarColor(0);
                res.put(pos,peca);

                // Negres
                peca = new Peca(_pecesDisponibles.get(nom));
                columna =  c;
                fila = _dades._tauler._fila - f + 1;
                pos = new Posicio(columna,fila);
                peca.assignarColor(1);
                res.put(pos,peca);
            }
            // Seguent casella
            c++;
            if(c == _dades._tauler._columna+1) {
                f++;
                c = 1;
            }
        }

        // Copiar peces disponibles
        copiarPosIni(res);

        _dades._tauler.posicionarPeces(res);
    }

    /**
     * @brief Posiciona al tauler totes les peces a les posicions que els pertoca.
     * @pre \p posicioPeces != null i les posicions inicials no causen l'acabament de la partida.
     * @post El tauler té les peces situades segons indica \p posicioPeces.
     * @param posicioPeces Posicions del tauler amb la peça que s'hi ha de posicionar.
     */
    public void repartirPeces(Map<Posicio,Peca> posicioPeces) {
        copiarPosIni(posicioPeces);
        _dades._tauler.posicionarPeces(posicioPeces);
    }

    /**
     * @brief Efectuar una jugada ordinària.
     * @pre \p jug ha de ser una jugada correcta.
     * @post S'ha realitzat la jugada \p jug en cas que es pogués efectuar, moguent i capturant les peces necessàries.
     *       A més, s'ha canviat el torn.
     * @param jug Jugada que es vol realitzar.
     * @return Cert si cal acabar la partida, fals altrament.
     * @throws ExcepcioJugadaErronia Si les característiques d'aquesta Jugada no són compatibles amb l'estat actual
     *                               de \p tauler.
     */
    public boolean efectuarTiradaOrdinaria(Jugada jug) throws ExcepcioJugadaErronia {
        // Guardar estat del tauler abans d'efecutar la tirada
        Dades dades = new Dades(new Dades(_dades));

        // Efectuar jugada
        boolean pecaCapturada = jug.efectuarJugada(_dades._tauler, tornActual());
        if (! pecaCapturada) _dades._tornsInaccio++;
        else _dades._tornsInaccio = 0;

        // Evaluar estat tauler
        Pair<ResultatTirada,Boolean> resultatTirada = evaluarResultatTirada();
        String aux = resultatTirada.first.toString();

        // Empilar estat de la partida
        _historialDadesTirar.push(dades);
        _historialDadesDesfer.clear();

        // Empilar Tirada
        String origen = jug.toString().split("\\n")[0];
        String desti = jug.toString().split("\\n")[1];
        _historialJugadesTirar.add(generarTirada(tornActual(),origen,desti,aux));
        _historialJugadesDesfer.clear();

        // Següent torn
        if(resultatTirada.second)
            assignarResultatPartida(resultatTirada.first);
        seguentTorn();

        return resultatTirada.second;
    }

    /**
     * @brief Efectua una tirada especial, és a dir, que no implica el desplaçament de peces al tauler.
     * @pre \p tirada != null
     * @post S'ha efectuat la jugada especial i s'ha canviat el torn.
     * @param tirada Tirada que es vol efectuar.
     * @return Cert si cal acabar la partida, fals altrament.
     */
    public boolean efectuarTiradaEspecial(TiradaEspecial tirada) {
        ResultatTirada resJug = ResultatTirada.NO_EFECTE;
        switch (tirada) {
            case RENDIR: resJug = ResultatTirada.RENDICIO; break;
            case AJORNAR: resJug = ResultatTirada.AJORNAMENT; break;
            case ACCEPTAR_TAULES: resJug = ResultatTirada.TAULES_ACCEPTADES; break;
            case DENEGAR_TAULES: resJug = ResultatTirada.TAULES_DENEGADES; break;
            case DEMANAR_TAULES: resJug = ResultatTirada.TAULES_DEMANADES; break;
        }

        boolean acabarPartida = resJug == ResultatTirada.RENDICIO ||
                                resJug == ResultatTirada.AJORNAMENT ||
                                resJug == ResultatTirada.TAULES_ACCEPTADES;

        String resultatTirada;

        if(resJug != ResultatTirada.TAULES_DENEGADES) {
            // Empilar estat de la partida
            _historialDadesTirar.push(new Dades(_dades));
            _historialDadesDesfer.clear();

            // Guardar resultat de la jugada
            resultatTirada = resJug.toString();

            // Empilar Tirada
            String origen = "";
            String desti = "";
            _historialJugadesTirar.add(generarTirada(tornActual(),origen,desti,resultatTirada));
            _historialJugadesDesfer.clear();
        }
        else {
            _historialDadesTirar.pop();
            _historialJugadesTirar.pop();
        }

        // Següent torn
        if(acabarPartida)
            assignarResultatPartida(resJug);
        seguentTorn();

        return acabarPartida;
    }

    /**
     * @brief Efectuar una jugada del jugador CPU.
     * @pre ---
     * @post S'ha efectuat una jugada de forma automàtica, gestionant la promoció en cas de produir-se la situació i es
     *       retorna un booleà que indica si cal acabar la partida.
     * @return Cert si la partida ha finalitzat i cal acabar la partida, fals altrament.
     */
    public boolean efectuarJugadaCPU(){
        // Copiar TaulerEscacs
        TaulerEscacs aux = new TaulerEscacs(_dades._tauler);

        // Generar jugada del jugadorCPU
        Jugada jug = JugadorCPU.demanarJugada(aux, tornActual());

        // Efectuar la tirada
        boolean calAcabar = false;
        try {
            calAcabar = efectuarTiradaOrdinaria(jug);
        } catch (ExcepcioJugadaErronia excepcioJugadaErronia) {
            excepcioJugadaErronia.printStackTrace();
        }

        // Copiar TaulerEscacs
        aux = new TaulerEscacs(_dades._tauler);

        // Comprovar promocio
        Posicio posPromo = posicioPromocio();
        if(posPromo != null) {
            String pecaVella = pecaTauler(posPromo).nom();
            int jugTornAnterior = _historialDadesTirar.peek()._tornActual;
            String pecaNova = JugadorCPU.promocionarPeca(aux,pecesPromocionables(jugTornAnterior),posPromo,
                    jugTornAnterior);
            if(!pecaVella.equals(pecaNova))
                calAcabar = efectuarPromocio(posPromo,pecaNova);
        }

        return calAcabar;
    }

    /**
     * @brief Respondre una sol·licitud al jugador CPU.
     * @pre ---
     * @post Es decideix si acceptar o denegar les taules sol·licitades i es retorna una TiradaEspecial amb la resposta.
     * @return Una TiradaEspecial que, en cas d'haver-se acceptat les taules serà \b TiradaEspecial.ACCEPTAR_TAULES.
     *         En cas contrari serà TiradaEspecial.DENEGAR_TAULES.
     */
    public TiradaEspecial respondreTaulesCPU(){
        return JugadorCPU.decidirTaules(new TaulerEscacs(_dades._tauler), tornActual());
    }

    /**
     * @brief Posició de la peça que pot ser promocionada.
     * @return La posició de la peça del jugador que té el torn actual que pot promocionar. Null si no hi ha cap
     *         promoció disponible.
     */
    public Posicio posicioPromocio() {
        return _dades._tauler.hiHaPromocio();
    }

    /**
     * @brief El nom de les peces per les que es pot promocionar.
     * @pre ---
     * @post Es retorna el nom de les peces per les que una peça podrà promocionar.
     * @return un Set amb el nom de les peces.
     */
    public Set<String> nomPromocionables() {
        Set<String> aux = new HashSet<>(_pecesDisponibles.keySet());
        aux.remove("REI");
        return aux;
    }

    /**
     * @brief Promocionar una peça.
     * @pre \p posPromo != null i \p nomPecaNova no és el nom de la peça que es vol promocionar, és a dir, la que es
     *      troba a \p posPromo. S'ha realitzat una jugada abans de realitzar una promoció.
     * @post Es promociona la peça que es troba a \p posPromo per la peça que té com a nom \p nomPecaNova i es retorna
     *       un booleà que indica si cal acabar la partida.
     * @param posPromo Posició on es troba la peça que es vol promocionar.
     * @param nomPecaNova Nom de la peça per la qual es vol promocionar.
     * @return Cert si la partida ha finalitzat i cal gestionar-ne l'acabament, fals altrament.
     */
    public boolean efectuarPromocio(Posicio posPromo, String nomPecaNova) {
        // Obtenir nom de la peca vella
        String nomPecaVella = _dades._tauler.solicitarPeca(posPromo).nom();

        // Agafar el jugador de la tirada anterior
        int jugAnterior;
        if(_historialJugadesTirar.peek().get("torn").equals("BLANQUES")) // CANVIAR A ENUMERACIO
            jugAnterior = 0;
        else jugAnterior = 1;

        // Crear peca amb el color corresponent
        Peca pecaPromo = pecaDisponible(nomPecaNova.toUpperCase());
        pecaPromo.assignarColor(jugAnterior);

        // Efectuar la promocio
        _dades._tauler.efecuarPromocio(pecaPromo,posPromo);

        // Recuperar els tornsEscacs
        _dades._tornsEscacsB = _historialDadesTirar.peek()._tornsEscacsB;
        _dades._tornsEscacsN = _historialDadesTirar.peek()._tornsEscacsN;

        // Tornar a evaluar l'estat del tauler
        Pair<ResultatTirada,Boolean> res = evaluarResultatTirada();

        // Modificar el resultat de la tirada anterior
        String promo = "PROMOCIÓ: " + nomPecaVella + "-" + nomPecaNova;
        if(res.first == ResultatTirada.NO_EFECTE)
            _historialJugadesTirar.peek().replace("resultat",promo);
        else
            _historialJugadesTirar.peek().replace("resultat", res.first.toString() + ", " + promo);

        assignarResultatPartida(res.first);

        return res.second;
    }

    /**
     * @brief Refer la darrera jugada desfeta.
     * @pre S'havia desfet alguna jugada.
     * @post S'ha refet la darrera jugada desfeta.
     * @throws CannotRedoException Si no hi ha hagut cap jugada desfeta abans.
     */
    public void referJugada() {
        if(_historialDadesDesfer.isEmpty()) throw new CannotRedoException();
        _historialDadesTirar.push(_dades);
        _dades = _historialDadesDesfer.pop();
        _historialJugadesTirar.push(_historialJugadesDesfer.pop());
    }

    /**
     * @brief Desfer la darrera jugada.
     * @pre S'havia realitzat alguna jugada.
     * @post S'ha desfet la darrera jugada efectuada.
     * @throws CannotUndoException Si no hi ha hagut cap jugada realitzada abans.
     */
    public void desferJugada() {
        if(_historialDadesTirar.isEmpty()) throw new CannotUndoException();
        _historialDadesDesfer.push(_dades);
        _dades = _historialDadesTirar.pop();
        _historialJugadesDesfer.push(_historialJugadesTirar.pop());
    }

    /**
     * @brief Indica de qui és el torn actual.
     * @return El nombre corresponent al jugador que li toca el torn (0 per blanques i 1 per negres)
     */
    public int tornActual() {
        return _dades._tornActual;
    }

    /**
     * @brief Retorna una de les peces disponibles amb el color del jugador que té el torn.
     * @pre \p nomPeca Ha de ser el nom d'una peça disponible.
     * @post S'ha retornat una còpia de la peça de les disponibles que tingui per nom \p nomPeca. També s'ha assignat al
     *       jugador que té el torn actual.
     * @param nomPeca Nom de la peça.
     * @return Una còpia de la peça indicada.
     * @throws NoSuchElementException Si \p nomPeca no és el nom d'una de les peces disponibles.
     */
    public Peca pecaDisponible(String nomPeca) {
        if(! _pecesDisponibles.containsKey(nomPeca))
            throw new NoSuchElementException();
        Peca res = new Peca(_pecesDisponibles.get(nomPeca));
        res.assignarColor(tornActual());
        return res;
    }

    /**
     * @brief Resultat final de la partida.
     * @return Una cadena de caràcters amb el resultat final de la partida (buida si no s'ha acabat).
     */
    public String resultatPartida() {
        return _resultatPartida;
    }

    /**
     * @brief Indica quin és l'estat del del rei.
     * @return Un enum que indica l'estat.
     */
    public TaulerEscacs.resTauler estatReiContrincant(int jugador) {
        return _dades._tauler.estatActual(jugador);
    }

    /**
     * @brief Files del tauler.
     * @return El nombre de files que té el tauler.
     */
    public int filesTauler() {
        return _dades._tauler._fila;
    }

    /**
     * @brief Columnes del tauler
     * @return El nombre de columnes que té el tauler.
     */
    public int columnesTauler() {
        return _dades._tauler._columna;
    }

    /**
     * @brief Dibuix del tauler en text.
     * @return Cadena de caràcters que dibuixa el tauler actual en mode text.
     */
    public String dibuixTauler() {
        return _dades._tauler.toString();
    }

    /**
     * @brief Peca colocada sobre el tauler.
     * @pre \p pos != null
     * @post Es retorna la peça que es troba a la posició \p pos del tauler.
     * @param pos Posició del tauler on es vol obtenir la peça.
     * @return La peça situada a la posició indicada.
     */
    public Peca pecaTauler(Posicio pos) {
        return _dades._tauler.solicitarPeca(pos);
    }

    /**
     * @brief Destins possibles d'una peça del tauler.
     * @pre \p pos != null
     * @post Es retorna un conjunt de posicions a les que la peça situada a la posició \p pos del tauler pot anar.
     * @param pos Posicio del tauler on es vol consultar els destins.
     * @return Un conjunt de posicions a les quals la peça pot anar.
     */
    public Set<Posicio> destinsPeca(Posicio pos) {
        return _dades._tauler.solicitarDestinsPeca(pos);
    }

    /**
     * @brief Enrocs possibles d'una peça del tauler.
     * @pre \p pos != null
     * @post Es retorna un conjunt de posicions on es troben les peces amb què la peça situada a la posició \p pos del
     *       tauler pot realitzar un enroc.
     * @param pos Posicio del tauler on es vol consultar els enrocs.
     * @return Un conjunt de posicions a les quals la peça enrocar.
     */
    public Set<Posicio> enrocsPeca(Posicio pos) {
        return _dades._tauler.solicitarCompanyesEnroc(pos);
    }

    /**
     * @brief Genera i retorna una estructura que conté les dades del desenvolupament de la partida.
     * @pre ---
     * @post S'ha retornat un mapa amb la ruta del fitxer de regles, la posició inicial de les peces
     *       (blanques i negres), el proper torn, l'historial de tirades i el resultat final de la partida.
     * @return Un mapa amb ordre d'inserció amb les dades del desenvolupament de la partida.
     */
    public LinkedHashMap<String,Object> dadesDesenvolupament() {
        // PosIni
        List<Object> posIniBlanques = new ArrayList<>();
        List<Object> posIniNegres = new ArrayList<>();
        for (Map.Entry<Posicio, Peca> entry : _posicionsInicials.entrySet()) {
            LinkedHashMap<String,String> aux = new LinkedHashMap<>();
            aux.put("pos", entry.getKey().toString());
            aux.put("tipus", entry.getValue().nom());
            aux.put("moguda", Boolean.toString(entry.getValue().esMoguda()));

            if(entry.getValue().color() == 0) // Blanca
                posIniBlanques.add(aux);
            else // Negra
                posIniNegres.add(aux);
        }

        // Mapa principal
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("fitxerRegles", _fitxerRegles);
        map.put("posIniBlanques", posIniBlanques);
        map.put("posIniNegres", posIniNegres);
        map.put("proper_torn", _historialJugadesTirar.firstElement().get("torn"));
        map.put("tirades", _historialJugadesTirar);
        map.put("resultat_final", _resultatPartida);
        return map;
    }

    /**
     * @brief Peces a les que es pot promocionar.
     * @pre 0 <= \p jugador <= 1
     * @post Es retorna un conjunt de peces a les quals es pot promocionar, assignades al jugador \p jugador.
     * @param jugador Jugador pel què es sol·liciten les peces.
     * @return Un conjunt de peces a les quals es pot promocionar que pertanyen al jugador \p jugador.
     */
    private Set<Peca> pecesPromocionables(int jugador) {
        Set<Peca> list = new HashSet<>();
        for (Map.Entry<String, Peca> entry : _pecesDisponibles.entrySet()) {
            if(!entry.getKey().equals("REI")) {
                Peca p = new Peca(entry.getValue());
                p.assignarColor(jugador);
                list.add(p);
            }
        }
        return list;
    }

    /**
     * @brief Consultar el resultat de la tirada anterior.
     * @pre ---
     * @post S'evalua el resultat de la tirada anterior i es retorna un Pair amb el resultat de la tirada i un booleà,
     *       que indica si cal acabar la partida.
     * @return Un ResultatTirada que indica com ha afectat la darrera tirada a l'estat de la partida i un booleà que
     *         és cert si s'ha finalitzat la partida i cal gestionar-ne l'acabament.
     */
    private Pair<ResultatTirada,Boolean> evaluarResultatTirada() {
        // Variables auxiliars
        ResultatTirada resultat;
        boolean acabarPartida = false;

        // Comprovar taules d'inaccio
        if(_limitInaccio > _dades._tornsInaccio) {
            switch (estatReiContrincant(tornActual())) {
                case ESCAC:
                    if(tornActual() == 0) _dades._tornsEscacsB++;
                    else _dades._tornsEscacsN++;
                    resultat = ResultatTirada.ESCAC;
                    break;
                case ESCAC_MAT:
                    acabarPartida = true;
                    resultat = ResultatTirada.ESCAC_MAT;
                    break;
                case REI_OFEGAT:
                    acabarPartida = true;
                    resultat = ResultatTirada.REI_OFEGAT;
                    break;
                default:
                    if(tornActual() == 0) _dades._tornsEscacsB = 0;
                    else _dades._tornsEscacsN = 0;
                    resultat = ResultatTirada.NO_EFECTE;
            }

            // Comprovar taules escac blanques
            if(_limitEscacs <= _dades._tornsEscacsB || _limitEscacs <= _dades._tornsEscacsN) {
                acabarPartida = true;
                resultat = ResultatTirada.TAULES_ESCAC_CONTINU;
            }
        }
        else {
            acabarPartida = true;
            resultat = ResultatTirada.TAULES_INACCIO;
        }

        return new Pair<>(resultat, acabarPartida);
    }

    /**
     * @brief Genera una estructura amb les dades d'una tirada.
     * @pre 0 <= \p torn <= 1, \p origen i \p desti són posicions vàlides i \p resultat és un dels resultats de la
     *      tirada possibles.
     * @post Es retorna un mapa amb les dades d'una tirada.
     * @param torn Jugador que ha realitzat la jugada.
     * @param origen Posicio d'origen de la jugada.
     * @param desti Posicio de destí de la jugada.
     * @param resultat Com ha afectat la jugada al tauler i a la partida.
     * @return Un mapa amb ordre d'inserció que conté les dades de la tirada seguint l'ordre dels paràmetre, és a dir,
     *         les claus són (per ordre): torn, origen, desti i resultat.
     */
    private LinkedHashMap<String,String> generarTirada(int torn, String origen, String desti, String resultat) {
        LinkedHashMap<String,String> res = new LinkedHashMap<>();
        res.put("torn",COLOR[torn]);
        res.put("origen",origen);
        res.put("desti",desti);
        res.put("resultat", resultat);
        return res;
    }

    /**
     * @brief Copiar un mapa de posicions i peces a l'atribut de la classe.
     * @pre \p map != null
     * @post S'ha copiat a l'atribut de la classe tot el contingut de \p map.
     * @param map Mapa amb posicions i peces.
     */
    private void copiarPosIni(Map<Posicio,Peca> map) {
        // Copiar peces disponibles
        for (Map.Entry<Posicio, Peca> entry : map.entrySet()) {
            _posicionsInicials.put(new Posicio(entry.getKey()), new Peca(entry.getValue()));
        }
    }

    /**
     * @brief Assigna l'atribut que indica el resultat final de la partida la causa de finalització de la partida.
     * @pre \p res != null
     * @post Si \p res no era NO_EFECTE, s'assigna el resultat de la partida a l'atribut de la classe. Altrament
     *       s'assigna una cadena de caràcters buida.
     * @param res Resultat de la darrera tirada.
     */
    private void assignarResultatPartida(ResultatTirada res) {
        switch (res) {
            case ESCAC_MAT:
                _resultatPartida = COLOR[tornActual()] + " GUANYEN";
                break;
            case REI_OFEGAT:
                _resultatPartida = "TAULES PER REI OFEGAT";
                break;
            case TAULES_INACCIO:
                _resultatPartida = "TAULES PER INACCIÓ";
                break;
            case TAULES_ESCAC_CONTINU:
                _resultatPartida = "TAULES PER ESCAC CONTINU";
                break;
            case RENDICIO:
                int contrincant = tornActual() == 0 ? 1 : 0;
                _resultatPartida = COLOR[contrincant] + " GUANYEN";
                break;
            case AJORNAMENT:
                _resultatPartida = "PARTIDA AJORNADA";
                break;
            case TAULES_ACCEPTADES:
                _resultatPartida = "TAULES";
                break;
            default:
                _resultatPartida = "";
        }
    }

    /**
     * @brief Es passa al següent torn.
     */
    private void seguentTorn() {
        _dades._tornActual = _dades._tornActual == 0 ? 1 : 0;
    }
}

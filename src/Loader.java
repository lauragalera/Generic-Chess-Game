/**
 * @file Loader.java
 * @brief Mòdul que permet carregar una partida.
 * @author David Pérez Sánchez
 */
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")

/**
 * @class Loader
 * @brief Mòdul que permet carregar una partida mitjançant un fitxer de dades JSON.
 */
public abstract class Loader {
    private static final JSONparser parser = new JSONparser();

    /**
     * @brief Iniciar una partida a partir d'un fitxer de regles.
     * @pre \p path és la ruta a un fitxer JSON vàlid i conté les regles d'una partida amb un format vàlid.
     * @post Es retorna una Partida iniciada amb el fitxer de regles \p path. La partida estarà a punt per
     *       començar.
     * @param path Ruta a un fitxer de regles JSON.
     * @return Una partida iniciada amb el fitxer de regles.
     * @throws IOException Si el fitxer no és un JSON o no es pot obrir.
     * @throws IllegalArgumentException Si el format del fitxer no és correcte, les dades contingudes no són coherents o
     *                                  els tipus de dades no són correctes.
     */
    public static Partida carregarPartidaNova(String path) throws IOException {
        Partida p;
        try {
            p = carregarRegles(path, true, 0);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Error: els tipus de dades no són correctes");
        }
        return p;
    }

    /**
     * @brief Carregar una partida a partir d'un fitxer de desenvolupament de partida.
     * @pre \p path és la ruta a un fitxer JSON vàlid i conté el desenvolupament d'una partida amb un format vàlid.
     * @post Es retorna una Partida carregada amb el fitxer de desenvolupament \p path. La partida estarà
     *       just en el punt en què es va ajornar o començarà amb les posicions inicials i el torn indicats.
     * @param path Ruta a un fitxer de dades JSON.
     * @return Una partida carregada amb el fitxer de desenvolupament.
     * @throws IOException Si el fitxer no és un JSON o no es pot obrir.
     * @throws IllegalStateException Si la partida ja està finalitzada.
     * @throws IllegalArgumentException Si el format del fitxer no és correcte, les dades contingudes no són coherents
     *                                  o els tipus de dades no són correctes.
     */
    public static Partida carregarPartidaComencada(String path) throws IOException {
        try {
            // Per comprovar format
            String[] pattern = {"fitxerRegles", "posIniBlanques", "posIniNegres", "proper_torn", "tirades",
                    "resultat_final"};

            // Llegir fitxer
            Map<String,Object> map = parser.parse(path);

            // Comprovar format
            validarFormat(map.keySet().toArray(), pattern);

            // Agafar valors
            Object[] values = map.values().toArray();

            // Comprovar que no s'hagi acabat la partida
            String resultatFinal = (String) values[5];
            if(!resultatFinal.equals("PARTIDA AJORNADA") && !resultatFinal.equals(""))
                throw new IllegalStateException("Error: la partida ja està acabada");

            // Carregar regles
            String fitxerRegles = (String) values[0];
            String properTorn = (String) values[3];
            int torn = properTorn.equals("BLANQUES") ? 0 : 1;
            Partida partida = carregarRegles(fitxerRegles, false, torn);

            // Llegir dades restants
            List<Map<String,Object>> posIniBlanques = (List<Map<String,Object>>) values[1];
            List<Map<String,Object>> posIniNegres = (List<Map<String,Object>>) values[2];
            List<Map<String,Object>> tirades = (List<Map<String,Object>>) values[4];

            // Carregar les peces a la posició corresponent
            Map<Posicio,Peca> posIni = new HashMap<>();
            llegirPos(posIni, posIniBlanques, partida, 0);
            llegirPos(posIni, posIniNegres, partida, 1);
            partida.repartirPeces(posIni);

            aplicarTirades(tirades, partida, properTorn);

            return partida;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Error: els tipus de dades no són correctes");
        }
    }

    /**
     * @brief Iniciar una partida a partir d'un fitxer de regles.
     * @pre \p path és la ruta a un fitxer JSON vàlid i conté les regles d'una partida amb un format vàlid i
     *      0 <= properTorn <= 1
     * @post Es retorna una Partida iniciada amb el fitxer de regles \p path i es retorna. La partida estarà a punt per
     *       començar amb el jugador que indiqui \p properTorn. Si \p partidaNova és cert, es posicionaran les peces a
     *       la seva posició inicial, altrament no es posicionarà cap peça.
     * @param path Ruta a un fitxer de regles JSON.
     * @param partidaNova Indica si la partida és nova o es reprèn el joc.
     * @param properTorn Indica a quin jugador li toca tirar a continuació.
     * @return Una partida iniciada amb el fitxer de regles.
     * @throws IOException Si el fitxer no és un JSON o no es pot obrir.
     */
    private static Partida carregarRegles(String path, boolean partidaNova, int properTorn) throws IOException {
        // Per comprovar format
        String[] pattern = {"nFiles", "nCols", "peces", "posInicial", "limitEscacsSeguits", "limitTornsInaccio", "enrocs"};

        // Llegir fitxer
        Map<String,Object> map = parser.parse(path);

        // Comprovar format
        validarFormat(map.keySet().toArray(), pattern);

        // Agafar valors
        Object[] values = map.values().toArray();

        // Llegir dades
        int nFiles = (int) values[0];
        int nCols = (int) values[1];
        int limitEscacsSeguits = (int) values[4];
        int limitTornsInaccio = (int) values[5];

        // Llegir peces i enrocs
        List<Map<String,Object>> peces = (List<Map<String,Object>>) values[2];
        List<Map<String,Object>> enrocs = (List<Map<String,Object>>) values[6];

        // Generar enrocsDisponibles i pecesDisponibles
        HashMap<String, HashMap<String, Enroc>> enrocsDisponibles = llegirEnrocs(enrocs); // Peca, <AltraPeca,enroc>
        HashMap<String, Peca> pecesDisponibles = llegirPeces(peces, enrocsDisponibles);

        // Comprovar que cada bàndol té un sol REI, que no hi hagi més peces que caselles i que les peces de
        // ordrePeces existeixen
        List<String> ordrePeces = (List<String>) values[3];
        int i = 0;
        int nPeces = 0;
        for (String s : ordrePeces) {
            if (s.equals("REI")) i++;
            else if(!s.equals("")) nPeces++;

            if(!s.equals("") && !pecesDisponibles.containsKey(s))
                throw new IllegalArgumentException("Error: a les posicions inicials només poden haver-hi " +
                        "peces definides");
        }
        nPeces *= 2;
        if(partidaNova && nCols*nFiles <= nPeces)
            throw new IllegalArgumentException("Error: No hi poden haver més peces que caselles");
        if(i != 1) throw new IllegalArgumentException("Error: cada bàndol ha de tenir un \"REI\"");

        // Crear tauler i partida
        TaulerEscacs t = new TaulerEscacs(nFiles, nCols);
        Partida p = new Partida(t, pecesDisponibles, limitEscacsSeguits, limitTornsInaccio, path, properTorn);
        if (partidaNova) p.inicialitzarPeces(ordrePeces);
        return p;
    }

    /**
     * @brief Llegir els enrocs disponibles.
     * @pre \p enrocs != null. Les dades tenen el format correcte.
     * @post Es llegeixen i comproven els enrocs disponibles del fitxer. Es crea un mapa amb els enrocs de cada peça
     *       juntament amb quina peça el realitzen i l'objecte Enroc.
     * @param enrocs Llista que conté els mapes que defineixen els enrocs disponibles.
     * @return Un mapa que és els enrocs de cada peça juntament amb quina peça el realitzen i l'objecte Enroc.
     */
    private static HashMap<String, HashMap<String, Enroc>> llegirEnrocs(List<Map<String,Object>> enrocs) {
        // Per comprovar format
        String[] pattern = {"peçaA", "peçaB", "quiets", "buitAlMig"};

        HashMap<String, HashMap<String, Enroc>> enrocsDisponibles = new HashMap<>(); // Peca, <AltraPeca,enroc>
        for (Map<String, Object> aux : enrocs) {
            // Comprovar format
            validarFormat(aux.keySet().toArray(), pattern);

            // Agafar valors
            Object[] values = aux.values().toArray();

            // Llegir dades
            String pecaA = values[0].toString();
            String pecaB = values[1].toString();
            boolean quiets = Boolean.parseBoolean(values[2].toString());
            boolean buitAlMig = Boolean.parseBoolean(values[3].toString());
            Enroc e = new Enroc(pecaA, pecaB, quiets, buitAlMig);

            // enrocsDisponibles per la primera peca
            HashMap<String, Enroc> temp = new HashMap<>();
            temp.put(pecaB, e);
            if (enrocsDisponibles.putIfAbsent(pecaA, temp) != null)
                enrocsDisponibles.get(pecaA).put(pecaB, e);

            // enrocsDisponibles per la segona peca
            temp = new HashMap<>();
            temp.put(pecaA, e);
            if (enrocsDisponibles.putIfAbsent(pecaB, temp) != null)
                enrocsDisponibles.get(pecaB).put(pecaA, e);
        }
        return enrocsDisponibles;
    }

    /**
     * @brief Llegir les peces.
     * @pre \p peces != null i \p enrocsDisponibles != null. A \p peces, no hi pot haver peces amb el mateix nom i el
     *      valor de totes elles ha de ser un enter positiu. El rei ha de ser únic, vulnerable i ha de tenir el valor
     *      més alt. Les dades tenen el format correcte.
     * @post Es llegeixen i comproven les peces. Es crea un mapa de nomPeca,Peca i es retorna. Les peces contingudes al
     *       mapa tenen s'han carregat amb totes les dades llegides, inclosos els enrocs definits a
     *       \p enrocsDisponibles.
     * @param peces Llista que conté els mapes que defineixen les peces.
     * @param enrocsDisponibles Enrocs disponibles per cada peça juntament amb quina peça el realitzen i l'enroc en sí.
     * @return Un mapa amb les peces creades.
     * @throws IllegalArgumentException Si hi ha peces amb el mateix nom, si el valor de les peces no són enters
     *                                  positius, si el rei és invulnerable, si no hi ha un rei, si el rei no té el
     *                                  valor més alt, si les dades no tenen un format correcte.
     */
    private static HashMap<String, Peca> llegirPeces(List<Map<String,Object>> peces,
                                                     HashMap<String,HashMap<String,Enroc>> enrocsDisponibles) {
        // Per comprovar format
        String[] pattern = {"nom", "simbol", "imatgeBlanca", "imatgeNegra", "valor", "moviments",
                "movimentsInicials", "promocio", "invulnerabilitat"};

        HashMap<String, Peca> pecesDisponibles = new HashMap<String, Peca>();
        Iterator<Map<String,Object>> itPeces = peces.iterator();
        int i = 0;
        boolean reiExisteix = false;
        Pair<Integer,List<String>> pecesValorMaxim = new Pair<>(0,new ArrayList<>());
        pecesValorMaxim.first = 0;
        while (itPeces.hasNext() && i < 25) { // Màxim 25 tipus
            Map<String,Object> aux = itPeces.next();

            // Comprovar format
            validarFormat(aux.keySet().toArray(), pattern);

            // Agafar valors
            Object[] values = aux.values().toArray();

            // Comprovar peces repetides
            String nom = values[0].toString();
            if(pecesDisponibles.containsKey(nom)) throw new IllegalArgumentException("Error: no hi poden haver " +
                                                                                     "peces amb el mateix nom");
            // Comprovar valor enter i positiu
            int valor = Integer.parseInt(values[4].toString());
            if(valor < 0) throw new IllegalArgumentException("Error: el valor d'una peça ha de ser enter i positiu");

            // Agafar peces amb el valor màxim
            if(valor > pecesValorMaxim.first) {
                pecesValorMaxim.second.clear();
                pecesValorMaxim.second.add(nom);
                pecesValorMaxim.first = valor;
            }
            else if(valor == pecesValorMaxim.first) {
                pecesValorMaxim.second.add(nom);
            }

            // Llegir moviments, eliminar equivalents i comprovar que tingui algun moviment
            List<Moviment> moviments = llegirMov((List<List<Object>>) values[5]);
            if(moviments.isEmpty())
                throw new IllegalArgumentException("Error: Totes les peces han de tenir un moviment");
            List<Moviment> movimentsInicials = llegirMov((List<List<Object>>) values[6]);
            for (Moviment mov: moviments)
                movimentsInicials.removeIf(mov::comparteixenDesti);

            // Llegir dades restants
            char simbol = values[1].toString().charAt(0);
            String imatgeBlanca = values[2].toString();
            String imatgeNegra = values[3].toString();
            boolean promocio = Boolean.parseBoolean(values[7].toString());
            boolean invulnerabilitat = Boolean.parseBoolean(values[8].toString());

            // Mirar si hi ha un REI, que no sigui invulnerable i que no pugui promocionar
            if(nom.equals("REI")) {
                reiExisteix = true;
                if(invulnerabilitat) throw new IllegalArgumentException("Error: El \"REI\" no pot ser invulnerable");
                if(promocio) throw new IllegalArgumentException("Error: El \"REI\" no ha de poder promocionar");
            }

            // Crear Peca
            Peca p;
            HashMap<String,Enroc> temp;
            if(enrocsDisponibles.containsKey(nom)) temp = enrocsDisponibles.get(nom);
            else temp = new HashMap<>();
            p = new Peca(nom, simbol,imatgeBlanca, imatgeNegra, valor, moviments, movimentsInicials, promocio,
                         invulnerabilitat, temp);

            // Afegir al resultat
            pecesDisponibles.put(nom, p);

            i++;
        }

        // Comprovar que el rei existeix i que té un valor superior a les altres peces
        if(!reiExisteix) throw new IllegalArgumentException("Error: hi ha d'haver obligatòriament un peça \"REI\" definida");
        boolean reiValorMax = pecesValorMaxim.second.contains("REI") && pecesValorMaxim.second.size() == 1;
        if(!reiValorMax) throw new IllegalArgumentException("Error: la peça \"REI\" ha de tenir el valor més alt");

        return pecesDisponibles;
    }

    /**
     * @brief Llegir les posicions inicials de les peces.
     * @pre \p res != null, \p posIni != null, \p partida != null i 0 <= \p color <= 1
     * @post S'afegeixen a \p res les peces amb la seva posició inicial. Les peces són del jugador \p color. A \p posIni
     *       hi ha una peça per posició, hi ha peces descrites al fitxer de regles i les dades tenen el format correcte.
     * @param res Mapa on s'afegiran les peces amb la seva posició al tauler.
     * @param posIni Llista que conté els mapes que defineixen les posicions inicials de les peces.
     * @param partida Partida on s'estan carregant les dades.
     * @param color Color del jugador al qual corresponen les peces de \p posIni.
     * @throws IllegalArgumentException Si hi ha peces no descrites al fitxer de regles, si hi ha més d'una peça a una
     *                                  posició, si les dades no tenen el format correcte.
     */
    private static void llegirPos(Map<Posicio,Peca> res, List<Map<String,Object>> posIni, Partida partida, int color) {
        // Per comprovar format
        String[] pattern = {"pos", "tipus", "moguda"};

        boolean hiHaRei = false;

        for (Map<String, Object> aux : posIni) {
            // Comprovar format
            validarFormat(aux.keySet().toArray(), pattern);

            // Agafar valors
            Object[] values = aux.values().toArray();

            // Llegir dades
            String pos = values[0].toString();
            String tipus = values[1].toString();
            boolean moguda = Boolean.parseBoolean(values[2].toString());

            // Crear peca
            Posicio posicio = new Posicio(pos);
            Peca peca;
            try {
                peca = new Peca(partida.pecaDisponible(tipus));
            } catch (NoSuchElementException noSuchElementException) {
                throw new IllegalArgumentException("Error: a les posicions inicials només hi poden haver peces " +
                        "descrites al fitxer de regles");
            }
            peca.assignarColor(color);
            if (moguda) peca.actualitzarMoguda();

            // Comprovar que cada bandol te un rei
            hiHaRei = hiHaRei || peca.nom().equals("REI");

            // Afegir al resultat
            if (res.putIfAbsent(posicio, peca) != null)
                throw new IllegalArgumentException("Error: No hi pot haver més d'una peça a la mateixa casella");
        }

        if (! hiHaRei) throw new IllegalArgumentException("Error: cada bàndol ha de tenir un \"REI\"");
    }

    /**
     * @brief Llegir els moviments d'una peça.
     * @pre \p mov != null
     * @post Es retorna una llista de moviments. Si hi ha moviments solapats només s'accepta un. El format de les dades
     *       ha de ser correcte.
     * @param mov Llista que conté llistes que són la descripció de moviments d'una peça.
     * @return Una llista de moviments.
     * @throws IllegalArgumentException Si el format de les dades no és correcte.
     */
    private static List<Moviment> llegirMov(List<List<Object>> mov) {
        // Per comprovar format
        int pattern = 4;

        List<Moviment> moviments = new ArrayList<>();
        for (List<Object> strings : mov) {
            // Comprovar format
            if (strings.size() != pattern) throw new IllegalArgumentException("Error: Format incorrecte");

            // Llegir dades
            String f = strings.get(0).toString();
            String c = strings.get(1).toString();
            int capt = Integer.parseInt(strings.get(2).toString());
            int salt = Integer.parseInt(strings.get(3).toString());
            Moviment x = new Moviment(f, c, capt, salt);

            // Comprovar moviments equivalents
            boolean esUnic = true;
            Iterator<Moviment> itEq = moviments.iterator();
            while (itEq.hasNext() && esUnic)
                esUnic = !x.comparteixenDesti(itEq.next());

            if (esUnic) moviments.add(x);
            else System.out.println("Error: el moviment " + x + " està solapat, s'igonra");
        }
        return moviments;
    }

    /**
     * @brief Aplicar les tirades a la partida.
     * @pre \p tirades != null, \p partida != null i \p properTorn val el mateix que el torn de la primera tirada. Les
     *      dades han de tenir un format correcte.
     * @post S'apliquen a \p partida totes les tirades contingudes a \p tirades en cas que sigui viable. Un cop
     *       aplicades les tirades, \p partida estarà en el punt en què es va ajornar.
     * @param tirades Llista que conté la definició de les tirades a realitzar.
     * @param partida Partida on s'aplicaran les tirades.
     * @param properTorn Torn del jugador que comença a tirar.
     * @throws IllegalArgumentException Si les dades no tenen un format correcte, si les tirades no són coherents, si
     *                                  el proper torn no és coherent.
     */
    private static void aplicarTirades(List<Map<String,Object>> tirades, Partida partida, String properTorn) {
        // Per comprovar format
        String[] pattern = {"torn", "origen", "desti", "resultat"};
        boolean esPrimeraTirada = true;

        for (Map<String, Object> aux : tirades) {
            // Comprovar format
            validarFormat(aux.keySet().toArray(), pattern);

            // Agafar valors
            Object[] values = aux.values().toArray();

            // Comprovar torn
            String torn = values[0].toString();
            if(!tirades.isEmpty() && esPrimeraTirada && !properTorn.equals(torn))
                throw new IllegalArgumentException("Error: Els torns no són coherents");
            esPrimeraTirada = false;

            // Llegir dades restants
            String origen = values[1].toString();
            String desti = values[2].toString();
            String resultat = values[3].toString();

            // Aplicar tirades
            try {
                boolean jugadaNormal = !origen.isEmpty() && !desti.isEmpty();
                if (jugadaNormal) {
                    if (origen.matches(JugadaEnroc.regex)) { // ENROC
                        Jugada jug = new JugadaEnroc(origen);
                        partida.efectuarTiradaOrdinaria(jug);
                    } else { // JUGADA ORDINÀRIA
                        // Crear jugada
                        Posicio p1 = new Posicio(origen);
                        Posicio p2 = new Posicio(desti);
                        Jugada jug = new JugadaOrdinaria(p1, p2);

                        // Efectuar jugada
                        partida.efectuarTiradaOrdinaria(jug);

                        // Comprovar promocio
                        Posicio posPromo = partida.posicioPromocio();
                        if(posPromo != null) { // Hi ha promocio
                            Matcher matcher = Pattern.compile("PROMOCIÓ").matcher(resultat);
                            if (matcher.find()) {
                                int guio = resultat.indexOf("-");
                                String novaPeca = resultat.substring(guio+1);
                                partida.efectuarPromocio(posPromo,novaPeca);
                            }
                        }
                    }
                }
            } catch (ExcepcioJugadaErronia e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error en la càrrega del fitxer: " + e.getMessage());
            }
        }
    }

    /**
     * @brief Validar format i ordre de les dades.
     * @pre \p x != null i \p pattern != null. Les dades contingudes a \p x han de coincidir en valor i ordre amb les de
     *      \p pattern.
     * @post Es comprova el format i ordre de les dades contingudes a \p x comparant-les amb \p pattern.
     * @param x Dades a analitzar.
     * @param pattern Dades que s'assumeixen correctes.
     * @throws IllegalArgumentException Si les dades no coincideixen en ordre o valor.
     */
    private static void validarFormat(Object[] x, String[] pattern) {
        boolean isCorrect = x.length == pattern.length;
        int i = 0;
        while (isCorrect && i < x.length) {
            isCorrect = isCorrect && pattern[i].equals(x[i]);
            i++;
        }
        if(!isCorrect) throw new IllegalArgumentException("Error: format del fitxer incorrecte");
    }
}

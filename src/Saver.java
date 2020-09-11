/**
 * @file Saver.java
 * @brief Mòdul que permet guardar una partida.
 * @author David Pérez Sánchez
 */
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")

/**
 * @class Saver
 * @brief Mòdul que converteix les dades de desenvoulpament d'una partida en un fitxer de dades JSON.
 */
public abstract class Saver {
    private static final String tab = "  "; ///< Espai per indentar les línies.

    /**
     * @brief Guardar el desenvolupament d'una partida en fitxer en format JSON.
     * @pre \p fitxerDesti és el nom d'un fitxer vàlid amb extensió ".json", \p dades != null i \p dades conté les dades
     *      amb el format propi d'un desenvolupament de partida.
     * @post Es guarda el desenvolupament de la partida contingut a \p dades al fitxer amb nom \p fitxerDesti amb un
     *       format JSON.
     * @param fitxerDesti Ruta del fitxer on es guardarà el desenvolupament.
     * @param dades Dades del desenvolupament d'una partida organitzades en mapes.
     * @throws IOException Si no s'ha pogut desar correctament el fitxer.
     * @throws IllegalArgumentException Si \p fitxerDesti no té la extensió d'un JSON.
     */
    public static void guardarPartida(String fitxerDesti, LinkedHashMap<String, Object> dades) throws IOException,
            IllegalArgumentException {
        if(!fitxerDesti.matches(".+\\.json"))
            throw new IllegalArgumentException("El fitxer no té extensió \".json\"");
        String json = "{\n";

        for (Map.Entry<String, Object> entry : dades.entrySet()) {
            switch (entry.getKey()) {
                case "fitxerRegles":
                case "proper_torn":
                    json += tab + "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\",\n";
                    break;
                case "posIniBlanques":
                case "posIniNegres":
                case "tirades":
                    json += tab + "\"" + entry.getKey() + "\": " +
                            guardarArray((List<LinkedHashMap<String, String>>) entry.getValue()) + ",\n";
                    break;
                case "resultat_final":
                    json += tab + "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"\n";
                    break;
            }
        }
        json += "}";

        FileWriter f = new FileWriter(fitxerDesti);
        f.write(json);
        f.close();
        System.out.println("S'ha desat el progrés correctament");
    }

    /**
     * @brief Convertir una llista de mapes a una cadena de caràcters en format JSON .
     * @pre \p list != null i conté les dades en el format propi d'un desenvolupament de partida.
     * @post Es retorna una cadena de caràcters amb el contingut de \p list en format JSON.
     * @param list Dades que es volen convertir.
     * @return Una cadena de caràcters en format JSON.
     */
    private static String guardarArray(List<LinkedHashMap<String, String>> list) {
        String res = "[";

        if(!list.isEmpty()) {
            res += "\n";
            Iterator<LinkedHashMap<String, String>> it = list.iterator();
            while (it.hasNext()) {
                res += tab + tab + "{\n";
                LinkedHashMap<String, String> map = it.next();
                Iterator<Map.Entry<String, String>> itMap = map.entrySet().iterator();
                while (itMap.hasNext()) {
                    Map.Entry<String, String> entry = itMap.next();
                    res += tab + tab + tab + "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"";
                    if (itMap.hasNext())
                        res += ",";
                    res += "\n";
                }
                res += tab + tab + "}";
                if (it.hasNext())
                    res += ",";
                res += "\n";
            }
            res += tab;
        }
        res += "]";
        return res;
    }
}

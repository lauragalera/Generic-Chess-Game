/**
 * @file JSONparser.java
 * @brief Analitzador de fitxers JSON.
 * @author David Pérez Sánchez
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@SuppressWarnings("unchecked")

/**
 * @class JSONparser
 * @brief Un analitzador capaç de convertir les dades contingudes en fitxers JSON a estructures de dades de Java
 */
public class JSONparser {
    private final ScriptEngine _parser;   ///< Motor intèrpret de JavaScript a Java.

    /**
     * @brief Constructor per defecte.
     * @pre ---
     * @post Es crea un analitzador de fitxers propis del llenguatge JavaScript.
     */
    public JSONparser() {
        _parser = new ScriptEngineManager().getEngineByName("javascript");
    }

    /**
     * @brief Analitza un fitxer de dades en format JSON i retorna les dades en un mapa.
     * @param path Ruta del fitxer de dades JSON
     * @return Un mapa amb totes les dades del fitxer JSON estructurades segons String, Object.
     * @throws IOException Si hi algun error en obrir i llegir el fitxer de la ruta \p path.
     * @throws IllegalArgumentException Si el fitxer \p path no té la extensió d'un JSON.
     */
    public Map<String,Object> parse(String path) throws IOException, IllegalArgumentException {
        if(!path.matches(".+\\.json")) throw new IllegalArgumentException("El fitxer no és un JSON");
        String data = new String(Files.readAllBytes(Paths.get(path)));
        Object x;
        try { x = _parser.eval("Java.asJSONCompatible(" + data + ")"); }
        catch (ScriptException e) { throw new RuntimeException("Error: No s'ha pogut interpretar el JSON"); }
        return (Map<String,Object>) x;
    }
}
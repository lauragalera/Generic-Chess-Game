/** @file Rajola.java
    @brief Una rajola d'un tauler gràfic.
    @author Laura Galera Alfaro
 */

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.HashMap;

/** @class Rajola
    @brief Una rajola del joc d'escacs basada en una imatge
 */
public class Rajola extends ImageView {
    private final static HashMap<String,Image> _imatges = new HashMap<>();  ///< \c HashMap de \c String - \c Image on la clau representa la ruta de la imatge i com a valor hi té la imatge.
    private final int _pixels;                                              ///< Amplada de la casella.

    /**
     * @brief Es crea una Rajola.
     * @pre   Cert.
     * @param amplada és l'amplada en píxels de la rajola.
     * @param color és el color de la rajola.
     * @post  S'ha creat una rajola gràfica.
     */
    public Rajola(int amplada, Color color) {
        _pixels = amplada;
        setImage(generarImatge(color));
        setFitWidth(_pixels);    // Redimensionament, establim amplada.
        setPreserveRatio(true);  // No volem distorsions.
        setSmooth(true);         // Volem qualitat a la representació de la imatge.
        setCache(true);          // Per millorar l'eficiència.
    }

    //******************************************************************************************************MÈTODES PRIVATS**************************************************************************************

    /**
     * @brief Genera la imatge de la rajola.
     */
    private Image generarImatge(Color color) {
        String path;
        if(color == Color.WHITE) path = "fonsB.png";
        else if(color == Color.BLACK) path = "fonsN.png";
        else if(color == Color.BLUE) path = "fonsE.png";
        else  path = "fonsV.png";
        Image img = _imatges.get(path);
        if(img == null) {
            File f = new File(path);
            img = new Image(f.toURI().toString(),_pixels,_pixels, true,true, true);
            _imatges.put(path,img);
        }

        return img;
    }
}
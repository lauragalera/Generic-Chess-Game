/** @file PecaGrafica.java
    @brief Una peça gràfica del tauler d'escacs.
    @author Laura Galera Alfaro
 */

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.HashMap;

/** @class      PecaGrafica
    @brief      Una peça d'escacs, representada per una imatge.
    @details    Les coordenades oldX() i oldY() fan referència al centre de
                la peça. La peça disposa de gestors d'esdeveniments.
 */

public class PecaGrafica extends StackPane {

    private final static HashMap<String,Image> _imatges = new HashMap<>();  ///< \c HashMap de \c String - \c Image on la clau representa la ruta de la imatge i com a valor hi té la imatge.
    private final int _pixels;                                              ///< Amplada de la casella.
    private double _mouseX;                                                 ///< Coordenada x del click mouse en píxels.
    private double _mouseY;                                                 ///< Coordenada y del click mouse en píxels.
    private double _oldX;                                                   ///< Coordenada x antigua en píxels, abans del moviment.
    private double _oldY;                                                   ///< Coordenada y antigua en píxels, abans del moviment.
    private final Peca _peca;                                               ///< \c Peca a la que representa aquesta \c PecaGrafica.

    /**@brief Retorna la peça a la que representa. */
    public Peca tipus() {
        return _peca;
    }

    /**@brief Retorna la coordenada x en píxels (abans de moure) */
    public double oldX() {
        return _oldX;
    }

    /**@brief Retorna la coordenada y en píxels (abans de moure) */
    public double oldY() {
        return _oldY;
    }

    /**
     * @brief Es crea una PecaGrafica
     * @pre   \p peca != null.
     * @param peca és la peça que representarà aquesta \c PecaGrafica.
     * @param pixels és l'amplada en píxels de la casella contenidora.
     * @param j columna del tauler.
     * @param i fila del tauler.
     */
    public PecaGrafica(Peca peca, int pixels, int j, int i) {
        _peca = peca;
        _pixels = pixels;
        moure(j,i);
        getChildren().add(generarImatge());
        setOnMousePressed((MouseEvent e) -> {
            _mouseX = e.getSceneX();
            _mouseY = e.getSceneY();
            this.toFront();
        });
        setOnMouseDragged((MouseEvent e) -> {
            relocate(_oldX + e.getSceneX() - _mouseX, _oldY + e.getSceneY() - _mouseY);
        });
    }

    /** @brief Retorna la peça al seu lloc. */
    public void abortarMoviment() {
        relocate(_oldX,_oldY);
    }

    //*********************************************************************************************************MÈTODES PRIVATS******************************************************************

    /** @brief Mou la peça al centre de la casella. */
    private void moure(int x, int y) {
        _oldX = x * _pixels;
        _oldY = y * _pixels;
        relocate(_oldX, _oldY);
    }

    /**
     * @brief Genera un node amb la imatge de la peça
     */
    private ImageView generarImatge() {
        Image img = _imatges.get(_peca.pathImg());
        if(img == null) {
            File f = new File(_peca.pathImg());
            img = new Image(f.toURI().toString(),_pixels,_pixels, true,true, true);
            _imatges.put(_peca.pathImg(),img);
        }
        return new ImageView(img);
    }
}

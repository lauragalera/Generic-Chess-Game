/**
 * @file EscacsGrafic.java
 * @brief Interfície gràfica de joc.
 * @author David Pérez Sánchez
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * @class EscacsGrafic
 * @brief Classe que actua com a una interfície d'usuari en mode gràfic i permet jugar una partida d'escacs.
 */
public class EscacsGrafic extends Application {
    private Stage _stage;                           ///< Escenari de l'aplicació.
    private final Group _rajoles = new Group();     ///< Grup de les rajoles del tauler.
    private final Group _rajolesAtac = new Group(); ///< Grup de les rajoles on un peça podrà moure's.
    private final Group _peces = new Group();       ///< Grup de les peces gràfiques que es posicionen sobre el tauler.
    private Partida _partida;                       ///< Partida sobre la qual es juga la partida d'escacs.
    private Label _torn;                            ///< Rètol que indica el torn actual i s'actualitza amb cada jugada.
    private int _pixelsRajola;                      ///< Mida en píxels de les rajoles del tauler.
    private int _pixelsLletra;                      ///< Mida en píxels de la lletra.
    private boolean _blanquesCPU;                   ///< Indica si el jugador de blanques està controlat per la CPU.
    private boolean _negresCPU;                     ///< Indica si el jugador de negres està controlat per la CPU.
    private volatile boolean _calAcabarCPU;
        ///< Indica si, després de la jugada que està efectuant, el jugador CPU ha de parar.
    private Thread _thread;                         ///< Procés on es calculen les tirades del jugador CPU.

    /**
     * @brief Funció principal.
     * @pre ---
     * @post S'executa un joc d'escacs en mode gràfic.
     * @param args Arguments.
     */
    public static void main(String[] args) {
        launch(); // Crida init + start
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        _stage = primaryStage;
        demanarDades();
    }

    /**
     * @brief Inicialitza els atributs bàsics.
     * @pre ---
     * @post S'inicialitza la mida de la rajola i de la lletra segons la mida de la pantalla de l'usuari.
     */
    private void inicialitzarAtr() {
        Rectangle2D r = Screen.getPrimary().getBounds();
        int aux1 = (int)r.getHeight()/_partida.filesTauler();
        int aux2 = (int)r.getWidth()/_partida.columnesTauler();
        _pixelsRajola = (int) (Math.min(aux1, aux2)*0.7);
        _pixelsLletra = (int) (Font.getDefault().getSize()*2);
    }

    /**
     * @brief Finestra de benvinguda a l'aplicació, on es demana el fitxer i els jugadors CPU.
     * @pre ---
     * @post Es mostra una finestra on l'usuari pot iniciar o carregar una partida i, també, escollir els jugadors CPU
     *       que vol.
     */
    private void demanarDades() {
        _stage.setResizable(false);
        _stage.setTitle("Joc Escacs");
        _stage.setScene(new Scene(crearBenvinguda()));
        _stage.show();
    }

    /**
     * @brief Finestra principal de l'aplicació.
     * @pre ---
     * @post Es mostra una finestra on es pot jugar una partida d'escacas.
     */
    private void jugar() {
        _stage = new Stage();
        _stage.setTitle("Joc Escacs");
        _stage.setScene(new Scene(crearFinestraJoc()));
        _stage.setResizable(false);
        _stage.setMaximized(false);
        _stage.show();
        mostrarEstatTauler();
        gestionarCPU();
    }

    /**
     * @brief Creació de la finestra inicial de l'aplicació.
     * @pre ---
     * @post Es retorna un Node que conté el tauler d'escacs i la barra lateral de control.
     * @return Un Node que conté el tauler amb totes les rajoles i fitxes, juntament amb la barra lateral de control,
     *         la qual permet a l'usuari realitzar accions especials sobre la partida.
     */
    private Parent crearBenvinguda() {
        // Crear botons amb fileChooser
        final FileChooser fileChooser = new FileChooser();
        final Button iniciar = new Button();
        iniciar.setText("Iniciar");
        iniciar.setOnAction(
                event -> {
                    configurarFileChooser(fileChooser,true);
                    File file = fileChooser.showOpenDialog(_stage);
                    if (file != null) {
                        obrirFitxer(file, true);
                    }
                }
        );
        final Button carregar = new Button();
        carregar.setText("Carregar");
        carregar.setOnAction(
                event -> {
                    configurarFileChooser(fileChooser,false);
                    File file = fileChooser.showOpenDialog(_stage);
                    if (file != null) {
                        obrirFitxer(file, false);
                    }
                }
        );

        // Crear titol
        final Label titol = new Label();
        titol.setText("Joc d'escacs");
        titol.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR,30));

        // Organitzar botons
        final HBox layout = new HBox();
        iniciar.setMinWidth(150);
        carregar.setMinWidth(150);
        layout.getChildren().addAll(iniciar,carregar);
        layout.setSpacing(20);

        // Crear checkboxs de la CPU
        HBox cpu = crearCPU();

        // Layout final
        final VBox mainLayout = new VBox(50);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(titol,layout,cpu);
        mainLayout.setPadding(new Insets(50, 50, 50, 50));

        return mainLayout;
    }

    /**
     * @brief Configurar el selector de fitxers per carregar o iniciar una partida.
     * @pre \p fileChooser != null.
     * @post Es configura el selector de fitxers \p fileChooser.
     * @param fileChooser Selector de fitxers a configurar.
     * @param esIniciar Indica si s'inicia o es carrega una partida.
     */
    private void configurarFileChooser(FileChooser fileChooser, boolean esIniciar) {
        if(esIniciar) fileChooser.setTitle("Carregar fitxer de regles");
        else fileChooser.setTitle("Carregar fitxer de desenvolupament");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
    }

    /**
     * @brief S'obre el fitxer, es carrega o s'inicia la partida i es comença a jugar.
     * @pre \p file és un fitxer vàlid.
     * @post Es carreguen les dades del fitxer \p file i si \p iniciar és \b true, s'inicia una partida, altrament es
     *       carrega una partida. A continuació es mostra la finestra de joc i es pot començar a jugar.
     * @param file Fitxer que conté les regles o el desenvolupament d'una partida.
     * @param iniciar Indica si cal iniciar una partida nova o si cal carregar-ne una d'ajornada.
     */
    private void obrirFitxer(File file, boolean iniciar) {
        try {
            if(iniciar) _partida = Loader.carregarPartidaNova(file.getAbsolutePath());
            else _partida = Loader.carregarPartidaComencada(file.getAbsolutePath());
            _stage.close();
            jugar();
        } catch (IOException e) {
            crearPopup(e.getMessage(), "Error obrint el fitxer", Alert.AlertType.ERROR).showAndWait();
        } catch (IllegalArgumentException | IllegalStateException | ClassCastException e) {
            crearPopup(e.getMessage(), "", Alert.AlertType.ERROR).showAndWait();
        }
    }

    /**
     * @brief Creació de la finestra principal, on es duu a terme el joc.
     * @pre ---
     * @post Es retorna un Node que conté tots els elements de la finestra principal de joc.
     * @return Un Node amb tots els elements de la finestra de joc i amb els respectius gestors d'esdeveniments
     *         per poder jugar una partida d'escacs.
     */
    private Parent crearFinestraJoc() {
        inicialitzarAtr();

        // Crear el tauler d'escacs
        Pane tauler = crearTauler();

        // Crear el rètol del torn
        TextFlow torns = crearTorn();

        // Crear menú refer/desfer
        VBox referDesfer = crearReferDesfer();

        // Crear controls CPU si hi ha algun jugador CPU
        VBox controlsCPU = new VBox();
        if(_negresCPU || _blanquesCPU)
            controlsCPU.getChildren().addAll(crearSeparador(),crearControlsCPU());

        // Crear controls de la partida
        VBox controls = crearControls();

        // Crear barra lateral
        VBox barraLateral = new VBox();
        barraLateral.getChildren().addAll(torns,crearSeparador(),referDesfer,controlsCPU,crearSeparador(),controls);
        barraLateral.setSpacing(_pixelsLletra*0.5);
        barraLateral.setPadding(new Insets(_pixelsRajola,0,0,0));

        // Layout principal
        BorderPane res = new BorderPane();
        res.setCenter(tauler);
        res.setRight(barraLateral);
        res.setPadding(new Insets(25, 20, 20, 25));

        return res;
    }

    /**
     * @brief Redibuixar les peces del tauler.
     * @pre ---
     * @post S'esborren totes les peces del grup de peces i es tornen a crear.
     */
    private void redibuixarPeces() {
        _peces.getChildren().clear();
        for (int i = _partida.filesTauler(); i>=1; --i) {
            for (int j = 1; j <= _partida.columnesTauler(); ++j) {
                Peca p = _partida.pecaTauler(new Posicio(j,i));
                if(p != null) {
                    PecaGrafica fitxa = crearPecaGrafica(p,j,_partida.filesTauler()+1-i);
                    _peces.getChildren().add(fitxa);
                }
            }
        }
    }

    /**
     * @brief Redibuixar les rajoles on la peca pot fer una jugada ordinària o una jugada enroc.
     * @pre \p posPeca != null i \p tipus != null.
     * @post Es dibuixen sobre el tauler rajoles de color verd per mostrar a l'usuari on pot realitzar una jugada
     *       ordinària amb la peça \p tipus des de la posició \p posPeca. En cas que es pugi fer un enroc, la rajola
     *       serà blava.
     * @param posPeca Posició on es troba la peça.
     * @param tipus Tipus de peça.
     */
    private void redibuixarAtacs(Posicio posPeca, Peca tipus) {
        // Obtenir destins possibles i enrocs possibles
        Set<Posicio> destinsPossibles = _partida.destinsPeca(posPeca);
        Set<Posicio> companyesEnroc = _partida.enrocsPeca(posPeca);

        // Dibuixar destins
        for (Posicio pos : destinsPossibles) {
            if(tipus.color() == _partida.tornActual()) {
                Rajola rajola = new Rajola(_pixelsRajola, Color.GREEN);
                rajola.setX(pos.columna() * _pixelsRajola);
                rajola.setY((_partida.filesTauler()+1-pos.fila()) * _pixelsRajola);
                rajola.toFront();
                _rajolesAtac.getChildren().add(rajola);
            }
        }

        // Dibuixar enrocs
        for(Posicio pos : companyesEnroc){
            if(tipus.color() == _partida.tornActual()){
                Rajola rajola = new Rajola(_pixelsRajola, Color.BLUE);
                rajola.setX(pos.columna() * _pixelsRajola);
                rajola.setY((_partida.filesTauler()+1-pos.fila()) * _pixelsRajola);
                rajola.toFront();
                _rajolesAtac.getChildren().add(rajola);
            }
        }
    }

    /**
     * @brief Desfer un nombre determinat de jugades.
     * @pre 0 <= \p n
     * @post Es desfan jugades fins assolir \p n jugades desfetes o fins que no quedin jugades per desfer. En cas que
     *       no quedin jugades per desfer es mostra un avís per pantalla. S'ha actualitzat la etiqueta del torn.
     * @param n Nombre de jugades que es volen desfer.
     */
    private void desferJugada(int n) {
        try {
            for (int i = 0; i < n; i++)
                _partida.desferJugada();
        } catch (CannotUndoException e) {
            crearPopup("No hi ha res per desfer", "", Alert.AlertType.ERROR).showAndWait();
        }
        actualitzarTorn();
        redibuixarPeces();
    }

    /**
     * @brief Refer una nombre determinat de jugades.
     * @pre 0 <= \p n
     * @post Es refan jugades fins assolir \p n jugades refetes o fins que no quedin jugades per refer. En cas que
     *       no quedin jugades per refer es mostra un avís per pantalla. S'ha actualitzat la etiqueta del torn.
     * @param n Nombre de jugades que es volen refer.
     */
    private void referJugada(int n) {
        try {
            for (int i = 0; i < n; i++)
                _partida.referJugada();
        } catch (CannotRedoException e) {
            crearPopup("No hi ha res per refer", "", Alert.AlertType.ERROR).showAndWait();
        }
        actualitzarTorn();
        redibuixarPeces();
    }

    /**
     * @brief Guardar la partida i tornar a la finestra de benvinguda.
     * @pre ---
     * @post Es torna a la finestra de benvinguda i es mostra un selector de fitxers on cal escollir un fitxer per
     *       guardar la partida. En cas que no es seleccioni cap fitxer es mostra una finestra per confirmar la pèrdua
     *       de les dades de la partida. Si s'accepta es perden les dades i, si es denega, es torna a iniciar el procés
     *       de guardar la partida. En cas d'escollir un fitxer vàlid, es guarda el desenvolupament de la partida.
     */
    private void guardarPartida() {
        demanarDades();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fileChooser.setInitialFileName("*.json");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showSaveDialog(_stage);
        try {
            Saver.guardarPartida(file.getAbsolutePath(), _partida.dadesDesenvolupament());
        } catch (IOException e) {
            crearPopup(e.getMessage(), "Error guardant el fitxer", Alert.AlertType.ERROR).showAndWait();
        } catch (NullPointerException e) {
            // Si no s'ha escollit cap fitxer
            Alert alert = crearPopup("No s'ha escollit cap fitxer per guardar la partida. Segur que vols " +
                            "continuar? (perdràs el progrés)", "", Alert.AlertType.CONFIRMATION);
            Optional<ButtonType> res = alert.showAndWait();
            if(res.isPresent()) {
                if(res.get() != ButtonType.OK)
                    guardarPartida();
            }
        }
    }

    /**
     * @brief S'aplica una jugada de tipus ordinari, moguent i capturant les peces pertinents.
     * @pre \p origen != null, \p desti != null.
     * @post S'aplica la jugada, moguent la peça de \p origen a \p desti i capturant les peces pertinents.
     * @param origen Posició on es troba la peça a la qual es vol aplicar la jugada.
     * @param desti Posició on es vol moure la peça.
     * @return Un booleà que indica si cal acabar la partida, cert en cas afirmatiu, fals altrament.
     * @throws ExcepcioJugadaErronia Si la jugadda no es pot realitzar.
     */
    private boolean aplicarJugOrd(Posicio origen, Posicio desti) throws ExcepcioJugadaErronia {
        Jugada jug = new JugadaOrdinaria(origen, desti);
        return _partida.efectuarTiradaOrdinaria(jug);
    }

    /**
     * @brief S'aplica una jugada de tipus enroc, moguent les peces implicades.
     * @pre \p p1 != null i \p p2 != null
     * @post S'aplica la jugada, moguent les peces que es troben a les posicions \p p1 i \p p2.
     * @param p1 Posició on es troba una peça implicada en l'enroc.
     * @param p2 Posició on es troba l'altra peça implicada en l'enroc.
     * @return Un booleà que indica si cal acabar la partida, cert en cas afirmatiu, fals altrament.
     * @throws ExcepcioJugadaErronia Si la jugadda no es pot realitzar.
     */
    private boolean aplicarJugEnr(Posicio p1, Posicio p2) throws ExcepcioJugadaErronia {
        Jugada jug = new JugadaEnroc(p1, p2);
        return _partida.efectuarTiradaOrdinaria(jug);
    }

    /**
     * @brief Convertir píxel a fila o columna del tauler.
     * @pre ---
     * @post Es retorna la fila o columna del tauler que correspon píxel \p pixel.
     * @param pixel Nombre de píxels que es vol convertir.
     * @return Un enter que representa la fila o columna del tauler que correspon al píxel \p pixel.
     */
    private int posTauler(double pixel) {
        return (int)(pixel + _pixelsRajola / 2) / _pixelsRajola;
    }

    /**
     * @brief Es mostra per pantalla l'estat actual del tauler en una finestra nova.
     */
    private void mostrarEstatTauler() {
        String estat;
        int jugAnt = _partida.tornActual() == 0 ? 1 : 0;
        switch (_partida.estatReiContrincant(jugAnt)) {
            case ESCAC:
                estat = "ESCAC AL REI DE " + Partida.COLOR[_partida.tornActual()] + '!';
                break;
            case ESCAC_MAT:
                estat = "ESCAC I MAT AL REI DE " + Partida.COLOR[_partida.tornActual()] + '!';
                break;
            case REI_OFEGAT:
                estat = "EL REI DE " + Partida.COLOR[_partida.tornActual()] + " ESTÀ OFEGAT!";
                break;
            default:
                estat = "";
                break;
        }
        if(!estat.equals(""))
            crearPopup(estat, null, Alert.AlertType.INFORMATION).showAndWait();
    }

    /**
     * @brief S'actualitza l'etiqueta que indica el torn amb el torn actual de la partida.
     */
    private void actualitzarTorn() {
        _torn.setText(Partida.COLOR[_partida.tornActual()]);
    }

    /**
     * @brief Creació del tauler d'escacs.
     * @pre ---
     * @post Es retorna un Pane que conté totes les rajoles i peces del tauler. A més, els elements tenen els gestors
     *       d'esdeveniments adients per poder jugar una partida d'escacs.
     * @return Un Pane amb rajoles, peces gràfiques, lletres de les columnes i números de fila
     *         del tauler d'escacs.
     */
    private Pane crearTauler() {
        // Crear grup per les etiquetes de numeros i lletres
        Group filesColumnes = new Group();

        // Crear tauler i configurar-lo
        Pane tauler = new Pane();
        tauler.setMinWidth(_partida.columnesTauler() * _pixelsRajola);
        tauler.setMinHeight(_partida.filesTauler() * _pixelsRajola);
        tauler.setPadding(new Insets(25, 25, 25, 25));
        tauler.getChildren().addAll(_rajoles, _rajolesAtac, _peces, filesColumnes);
        tauler.setOnMousePressed((MouseEvent e) -> tauler.requestFocus());

        // Dibuixar tauler i peces
        for (int i = _partida.filesTauler(); i>=1; --i) {
            for (int j = 1; j <= _partida.columnesTauler(); ++j) {
                Rajola rajola;
                if((j+i) % 2 == 0) rajola = new Rajola(_pixelsRajola, Color.BLACK);
                else rajola = new Rajola(_pixelsRajola, Color.WHITE);
                rajola.setX(j * _pixelsRajola);
                rajola.setY(i * _pixelsRajola);
                _rajoles.getChildren().add(rajola);
                Peca peca = _partida.pecaTauler(new Posicio(j,i));
                if(peca != null) {
                    PecaGrafica fitxa = crearPecaGrafica(peca,j,_partida.filesTauler()+1-i);
                    _peces.getChildren().add(fitxa);
                }
            }
        }
        // Dibuixar lletres
        for (int j = 1; j <= _partida.columnesTauler(); ++j) {
            Label l = crearLabel(Character.toString((char)(j+96)),FontWeight.BOLD,(int)(_pixelsRajola * 0.4));
            l.setLayoutX(j * _pixelsRajola + (_pixelsRajola*0.3));

            filesColumnes.getChildren().add(l);
        }
        // Dibuixar numeros
        for (int j = 1; j <= _partida.filesTauler(); ++j) {
            Label l = crearLabel(Integer.toString(_partida.filesTauler()+1-j),FontWeight.BOLD,
                    (int)(_pixelsRajola * 0.4));
            l.setLayoutY(j * _pixelsRajola + (_pixelsRajola*0.3));
            filesColumnes.getChildren().add(l);
        }

        return tauler;
    }

    /**
     * @brief Creació dels elements que mostren el torn actual.
     * @pre ---
     * @post Es retora un TextFlow que conté els elements de text necessaris per representar gràficament el torn
     *       actual.
     * @return Un TextFlow amb dos etiquetes que mostren el torn actual.
     */
    private TextFlow crearTorn() {
        _torn = crearLabel(Partida.COLOR[_partida.tornActual()],FontWeight.SEMI_BOLD,_pixelsLletra);
        _torn.setPrefWidth(Partida.COLOR[0].length()*_pixelsLletra);
        Label tornTitol = crearLabel("TORN: ",FontWeight.BOLD,_pixelsLletra);
        TextFlow tornLayout = new TextFlow();
        tornLayout.getChildren().addAll(tornTitol, _torn);

        return tornLayout;
    }

    /**
     * @brief Creació dels elements que permeten desfer i refer jugades.
     * @pre ---
     * @post Es retorna una VBox que conté dos botons per desfer i refer jugades, configurats amb els gestors
     *       d'esdeveniments per realitzar la tasca. Si un jugador CPU està calculant una tirada, només es mostrarà un
     *       avís.
     * @return Una VBox amb dos botons que permeten desfer i refer.
     */
    private VBox crearReferDesfer() {
        // Crear titol
        Label titol = crearLabel("Refer/Desfer",FontWeight.BOLD,_pixelsLletra);

        final boolean hiHaCPU = _blanquesCPU || _negresCPU;

        // Crear i configurar button de desfer
        Button desfer = crearButton("Desfer", _pixelsLletra*3);
        desfer.setOnAction(e -> {
            if(hiHaCPU && _thread.isAlive()) {
                if(_calAcabarCPU)
                    crearPopup("Acabant la jugada CPU. Espera uns instants...",
                            null, Alert.AlertType.INFORMATION).showAndWait();
                else
                    crearPopup("S'ha d'aturar el jugador CPU per poder refer/desfer",
                            null, Alert.AlertType.INFORMATION).showAndWait();
            }
            else {
                TextInputDialog dialog = new TextInputDialog("1");
                dialog.setTitle("Desfer Jugades");
                dialog.setGraphic(null);
                dialog.setHeaderText(null);
                dialog.setContentText("Entra el nombre de jugades que vols desfer");
                Optional<String> res = dialog.showAndWait();
                res.ifPresent(n -> {
                    desferJugada(Integer.parseInt(n));
                    actualitzarTorn();
                });
            }
        });

        // Crear i configurar button de refer
        Button refer = crearButton("Refer", _pixelsLletra*3);
        refer.setOnAction(e -> {
            if(hiHaCPU && _thread.isAlive()) {
                if(_calAcabarCPU)
                    crearPopup("Acabant la jugada CPU. Espera uns instants...",
                            null, Alert.AlertType.INFORMATION).showAndWait();
                else
                    crearPopup("S'ha d'aturar el jugador CPU per poder refer/desfer",
                            null, Alert.AlertType.INFORMATION).showAndWait();
            }
            else {
                TextInputDialog dialog = new TextInputDialog("1");
                dialog.setTitle("Refer Jugades");
                dialog.setGraphic(null);
                dialog.setHeaderText(null);
                dialog.setContentText("Entra el nombre de jugades que vols refer");
                Optional<String> res = dialog.showAndWait();
                res.ifPresent(n -> {
                    referJugada(Integer.parseInt(n));
                    actualitzarTorn();
                });
            }
        });

        // Posar els botons de costat
        HBox referDesfer;
        referDesfer = new HBox(refer,desfer);
        referDesfer.setSpacing(_pixelsLletra*2);

        // Posar el titol sobre els botons
        VBox res = new VBox(titol,referDesfer);
        res.setSpacing(_pixelsLletra);

        return res;
    }

    /**
     * @brief Creació dels elements de control de la CPU.
     * @pre ---
     * @post Es retorna una VBox que conté un botó per reprendre les tirades automàtiques de la CPU un cop s'han aturat.
     *       En el cas que els dos jugadors siguin CPU, també conté el botó d'aturar.
     * @return Una VBox amb un botó per reprendre les tirades de la CPU. En cas de dos jugadors CPU, també amb el botó
     *         per aturar-los.
     */
    private VBox crearControlsCPU() {
        // Crear titol
        Label titol = crearLabel("Controls CPU",FontWeight.BOLD,_pixelsLletra);

        // Si hi ha dos jugadors CPU
        boolean dosCPU = _blanquesCPU && _negresCPU;

        // Per posar els botons de costat
        HBox box;

        // Crear i configurar button de reprendre
        Button reprendre = crearButton("Reprendre", _pixelsLletra*3);
        reprendre.setOnAction(e -> {
            _calAcabarCPU = false;
            gestionarCPU();
        });

        // Crear i configurar button de aturar
        if(dosCPU) {
            Button aturar = crearButton("Aturar", _pixelsLletra * 3);
            aturar.setOnAction(e -> {
                _calAcabarCPU = true;
                if (_thread.isAlive()) {
                    crearPopup("Acabant jugada CPU...", null, Alert.AlertType.INFORMATION).showAndWait();
                } else {
                    crearPopup("La CPU ja està aturada",
                            null, Alert.AlertType.INFORMATION).showAndWait();
                }
            });
            box = new HBox(aturar,reprendre);
        }
        else box = new HBox(reprendre);
        box.setSpacing(_pixelsLletra*2);

        // Posar el titol sobre els botons
        VBox res = new VBox(titol,box);
        res.setSpacing(_pixelsLletra);

        return res;
    }

    /**
     * @brief Creació dels elements que permeten realitzar accions especials sobre el curs de la partida.
     * @pre ---
     * @post Es retorna una VBox que conté els botons per ajornar, demanar taules i rendir-se, amb els gestors
     *       d'esdeveniments configurats per realitzar aquestes tasques. En cas que jugui CPU vs CPU, només hi ha el
     *       botó d'ajornar.
     * @return Una VBox amb tres botons configurats per poder ajornar la partida, demanar taules al contrincant i
     *         rendir-se.
     */
    private VBox crearControls() {
        // Crear titol
        Label titol = crearLabel("Controls",FontWeight.BOLD,_pixelsLletra);

        final boolean cpu = _blanquesCPU || _negresCPU;

        // Crear i configurar button de ajornar partida
        Button ajornar = crearButton("Ajornar Partida", _pixelsLletra*7);
        ajornar.setOnAction(e -> {
            if(cpu && _thread.isAlive()) {
                if(_calAcabarCPU)
                    crearPopup("Acabant la jugada CPU. Espera uns instants...",
                            null, Alert.AlertType.INFORMATION).showAndWait();
                else
                    crearPopup("S'ha d'aturar el jugador CPU per poder ajornar la partida",
                            null, Alert.AlertType.INFORMATION).showAndWait();
            }
            else {
                _partida.efectuarTiradaEspecial(Partida.TiradaEspecial.AJORNAR);
                guardarPartida();
            }
        });

        VBox res = new VBox();

        if(_blanquesCPU && _negresCPU) res = new VBox(titol,ajornar);
        else {
            // Crear i configurar button de demanar taules
            Button demanarTaules = crearButton("Sol·licitar Taules", _pixelsLletra*7);
            demanarTaules.setOnAction(e -> solicitarTaules());

            // Crear i configurar button de rendir-se
            Button rendir = crearButton("Rendir-se", _pixelsLletra*7);
            rendir.setOnAction(e -> rendirse());

            res.getChildren().addAll(titol,ajornar,demanarTaules,rendir);
        }
        res.setSpacing(_pixelsLletra);

        return res;
    }

    /**
     * @brief Creació dels elements que permeten escollir quins jugadors estaran controlat per la CPU.
     * @pre ---
     * @post Es retorna una HBox que conté les \a checkbox per escollir els jugadors CPU.
     * @return Una HBox amb dues etiquetes i dues \a checkbox per poder escollir quins jugadors seran CPU.
     */
    private HBox crearCPU() {
        // Crear etiquetes
        Label titolB = crearLabel("CPU Blanques:",FontWeight.BLACK,15);
        Label titolN = crearLabel("CPU Negres:",FontWeight.BLACK,15);

        // Crear Checkbox per els dos jugadors
        CheckBox checkBoxB = new CheckBox();
        CheckBox checkBoxN = new CheckBox();
        checkBoxB.setSelected(false);
        checkBoxB.setIndeterminate(false);
        checkBoxN.setSelected(false);
        checkBoxN.setIndeterminate(false);

        // Afegir EventHandlers
        checkBoxB.setOnAction(e -> _blanquesCPU = checkBoxB.isSelected());
        checkBoxN.setOnAction(e -> _negresCPU = checkBoxN.isSelected());

        // Organitzar titols amb checkboxs
        HBox res = new HBox(titolB,checkBoxB,titolN,checkBoxN);
        res.setSpacing(15);
        res.setAlignment(Pos.CENTER);

        return res;
    }

    /**
     * @brief Creació d'una peca gràfica.
     * @pre 0 < j, 0 < i, \p tipus != null
     * @post Es retorna una peca gràfica situada a la columna \p j i a la fila \p i que representa gràficament a la
     *       peca \p tipus.
     * @param tipus Peca que haurà de representar la peca gràfica retornada.
     * @param j Columna del tauler on es representarà la peca gràfica.
     * @param i Fila del tauler on es representarà la peca gràfica.
     * @return Una PecaGrafica que representa la peca \p tipus i està representada a la columna \p j i a la fila del
     *         tauler \p i.
     */
    private PecaGrafica crearPecaGrafica(Peca tipus, int j, int i) {
        // Crear pecaGrafica a partir d'una peca normal
        PecaGrafica peca = new PecaGrafica(tipus, _pixelsRajola, j, i);

        // Configurar Peca per efectuar les tirades
        peca.setOnMouseReleased((MouseEvent e) -> {
            int newX = posTauler(peca.getLayoutX());
            int newY = _partida.filesTauler()+1-posTauler(peca.getLayoutY());
            Posicio pNew = new Posicio(newX,newY);
            int oldX = posTauler(peca.oldX());
            int oldY = _partida.filesTauler()+1-posTauler(peca.oldY());
            Posicio pOld = new Posicio(oldX,oldY);

            gestionarTirada(tipus, peca, pOld, pNew);
        });

        // Configurar peca per mostrar els moviments possibles el posicionar el mouse sobre la peca
        peca.setOnMouseEntered((MouseEvent e) -> {
            int oldX = posTauler(peca.oldX());
            int oldY = _partida.filesTauler()+1-posTauler(peca.oldY());
            Posicio pOld = new Posicio(oldX,oldY);
            redibuixarAtacs(pOld,tipus);
        });

        // Configurar peca per esborrar els moviments possibles en treure el mouse de sobre la peca
        peca.setOnMouseExited((MouseEvent e) -> _rajolesAtac.getChildren().clear());

        return peca;
    }

    /**
     * @brief Rendir-se i cedir la victòria al contrincant.
     * @pre ---
     * @post Es mostra per pantalla una finestra per confirmar que el jugador actual vol rendir-se i cedir la victòria
     *       al jugador contrincant. En cas de confirmar-ho, s'acaba la partida i es permet guardar-ne el
     *       desenvolupament. Altrament es continua la partida.
     */
    private void rendirse() {
        // Crear popup
        Alert alert = crearPopup("Rendir-se",
                Partida.COLOR[_partida.tornActual()]+", vols rendirte? (Cediràs la victòria al contrincant)",
                Alert.AlertType.CONFIRMATION);

        // Mostrar popup i interpretar resultat
        Optional<ButtonType> resultat = alert.showAndWait();
        if(resultat.isPresent()) {
            if(resultat.get() == ButtonType.OK) {
                _partida.efectuarTiradaEspecial(Partida.TiradaEspecial.RENDIR);
                guardarPartida();
            }
        }
    }

    /**
     * @brief Solicitar taules al contrincant.
     * @pre ---
     * @post Es mostra per pantalla una finestra perquè el jugador contrincant pugui decidir si acabar la partida en
     *       taules. En cas d'acceptar, es finalitza la partida i es permet guardar-ne el desenvolupament. Altrament es
     *       continua amb la partida.
     */
    private void solicitarTaules() {
        // Efectuar solicitud de taules
        _partida.efectuarTiradaEspecial(Partida.TiradaEspecial.DEMANAR_TAULES);

        // Variable amb la resposta
        Partida.TiradaEspecial resposta;

        // Gestionar CPU
        boolean tornCPU = _partida.tornActual() == 0 && _blanquesCPU || _partida.tornActual() == 1 && _negresCPU;
        if(tornCPU) {
            resposta = _partida.respondreTaulesCPU();
            if(resposta == Partida.TiradaEspecial.ACCEPTAR_TAULES)
                crearPopup("El contrincant ha acceptat les taules", null, Alert.AlertType.INFORMATION).showAndWait();
            else
                crearPopup("El contrincant ha denegat les taules", null, Alert.AlertType.INFORMATION).showAndWait();
        }
        // Gestionar Jugador
        else {
            // Mostrar popup
            Alert alert = crearPopup("Sol·licitar Taules",
                    Partida.COLOR[_partida.tornActual()] + ", acceptes finalitzar la partida en taules?",
                    Alert.AlertType.CONFIRMATION);
            Optional<ButtonType> resultat = alert.showAndWait();

            // Interpretar dades entrades
            if (resultat.isPresent()) {
                if (resultat.get() == ButtonType.OK)
                    resposta = Partida.TiradaEspecial.ACCEPTAR_TAULES;
                else resposta = Partida.TiradaEspecial.DENEGAR_TAULES;
            } else resposta = Partida.TiradaEspecial.DENEGAR_TAULES;
        }

        // Efectuar resposta de les taules
        _partida.efectuarTiradaEspecial(resposta);
        if(resposta == Partida.TiradaEspecial.ACCEPTAR_TAULES) guardarPartida();
    }

    /**
     * @brief Gestionar la tirada, és a dir, efectuar la jugada del tipus pertinent, efectuar les promocions necessàries
     *        i gestionar l'acabament de la partida.
     * @pre \p tipus != null, \p peca != null, \p pOld != null i \p pNew != null.
     * @post En cas que la peça que es troba a la posició \p pNew sigui del mateix color que la peça \p tipus i
     *       aquesta darrera disposi d'enrocs possibles amb la peça de la posició \p pNew, s'efectua un enroc. Altrament
     *       s'efectua una jugada ordinària. A més d'efectuar la jugada, si hi ha una promoció disponible, es gestiona.
     *       També, si la partida s'acaba amb aquesta jugada, es gestiona l'acabament de la jugada. En cas que la jugada
     *       sigui errònia o s'efectui quan és el torn de la CPU, es recoloca la peca moguda \p peca a la seva posició
     *       original sense haver-se efectuat la jugada.
     * @param tipus Peça que es troba a la posició del tauler \p pOld.
     * @param peca Peça gràfica que conté la peça \p tipus.
     * @param pOld Posició on es troba la peça a la què es vol aplicar la jugada.
     * @param pNew En cas de jugada ordinària, posició on es vol moure la peça. En cas d'enroc, posició de l'altra peça
     *             implicada en l'enroc.
     */
    private void gestionarTirada(Peca tipus, PecaGrafica peca, Posicio pOld, Posicio pNew) {
        try {
            boolean tornCPU = _partida.tornActual() == 0 && _blanquesCPU || _partida.tornActual() == 1 && _negresCPU;
            if(tornCPU) throw new ExcepcioJugadaErronia();

            boolean calAcabar;
            Peca pecaNew = _partida.pecaTauler(pNew);
            boolean esEnroc = pecaNew != null && // Hi ha peca al desti
                    tipus.color() == pecaNew.color() && // Son del mateix color
                    tipus.obtenirEnroc(pecaNew) != null; // Poden enrocar entre elles
            if (esEnroc)
                // Aplicar JugadaEnroc
                calAcabar = aplicarJugEnr(pOld, pNew);
            else {
                // Aplicar JugadaOrdinaria
                calAcabar = aplicarJugOrd(pOld, pNew);

                // Comprovar promocio
                Posicio posPromo = _partida.posicioPromocio();
                if (posPromo != null) { // Si hi ha promocio
                    // Obtenir nom de la peca i les candidates a promocionar
                    String nomPecaVella = _partida.pecaTauler(posPromo).nom();
                    Set<String> nomPromocionables = _partida.nomPromocionables();
                    nomPromocionables.remove(nomPecaVella);

                    // Mostrar dialog i interpretar les dades llegides
                    String message = "La peça \"" + nomPecaVella + "\" que es troba a la posició \"" + posPromo + "\"" +
                            " pot promocionar";
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(null, nomPromocionables);
                    dialog.setTitle("Promocionar Peça");
                    dialog.setGraphic(null);
                    dialog.setHeaderText(message);
                    dialog.setContentText("Selecciona la nova peça:");
                    Optional<String> res = dialog.showAndWait();
                    if (res.isPresent()) {
                        calAcabar = _partida.efectuarPromocio(posPromo, res.get());
                    }
                }
            }

            redibuixarPeces();
            actualitzarTorn();
            mostrarEstatTauler();

            // Gestionar acabament de la partida
            if(calAcabar) {
                crearPopup(_partida.resultatPartida(), null, Alert.AlertType.INFORMATION).showAndWait();
                _stage.close();
                _blanquesCPU = _negresCPU = _calAcabarCPU = false;
                demanarDades();
                _rajolesAtac.getChildren().clear();
                _rajoles.getChildren().clear();
                _peces.getChildren().clear();
                guardarPartida();
            }

            // Passar el torn a la CPU si cal
            gestionarCPU();
        } catch(ExcepcioJugadaErronia e) {
            peca.abortarMoviment();
        }
    }

    /**
     * @brief Gestionar una tirada controlada per la CPU.
     * @pre ---
     * @post Si és el torn de la CPU, s'efectua una tirada de forma automàtica, és a dir, controlada pel programa. També
     *       es gestiona la promoció i l'acabament de la partida, en cas que calgui acabar-la.
     */
    private void gestionarCPU() {
        // Comprovar si es torn de la CPU
        boolean tornCPU = _partida.tornActual() == 0 && _blanquesCPU || _partida.tornActual() == 1 && _negresCPU;

        // Si es torn de la CPU gestionar la tirada de la CPU
        if(tornCPU) {
            // Es posa en segon pla el càlcul del jugadorCPU per evitar retards en la UI
            _thread = new Thread(() -> {
                boolean calAcabar = _partida.efectuarJugadaCPU();

                Platform.runLater(() -> {
                    redibuixarPeces();
                    actualitzarTorn();
                    mostrarEstatTauler();

                    if (!calAcabar && !_calAcabarCPU) gestionarCPU();
                    else if(calAcabar) {
                        crearPopup(_partida.resultatPartida(), null, Alert.AlertType.INFORMATION).showAndWait();
                        _stage.close();
                        _blanquesCPU = _negresCPU = _calAcabarCPU = false;
                        demanarDades();
                        _rajolesAtac.getChildren().clear();
                        _rajoles.getChildren().clear();
                        _peces.getChildren().clear();
                        guardarPartida();
                    }
                });
            });
            _thread.setDaemon(true);
            _thread.start();
        }
    }

    /**
     * @brief Creació d'una alerta.
     * @pre \p type != null
     * @post Es retorna una alerta del tipus \p type que mostra els missatges \p header i \p message.
     * @param header Missatge que es vol mostrar com a títol de l'alerta.
     * @param message Missatge que es vol mostrar al cos de l'alerta.
     * @param type Tipus d'alerta.
     * @return Una alerta amb títol \p header i cos \p message configurada de forma adient.
     */
    private Alert crearPopup(String header, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setResizable(false);
        alert.setTitle(null);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert;
    }

    /**
     * @brief Creació d'una etiqueta.
     * @pre \p weight != null, \p midaLletra > 0
     * @post Es retorna una etiqueta que mostra el missatge \p text amb una mida de lletra segons el valor de
     *       \p midaLletra i un pes de lletra segons \p weight.
     * @param text Missatge que es vol posar a l'etiqueta.
     * @param weight Pes de la lletra.
     * @param midaLletra Mida de la lletra.
     * @return Una etiqueta que mostra el missatge \p text amb una lletra de mida \p midaLletra i amb un pes \p weight.
     */
    private Label crearLabel(String text, FontWeight weight, int midaLletra) {
        Label l = new Label();
        l.setText(text);
        l.setFont(Font.font("arial", weight, FontPosture.REGULAR,midaLletra));
        return l;
    }

    /**
     * @brief Creació d'un botó.
     * @pre ---
     * @post Es retorna un botó que mostra el missatge \p text i té una amplada de \p width píxels.
     * @param text Text que s'ha de mostrar a dins el botó.
     * @param width Amplada del botó.
     * @return Un botó amb el missatge \p text.
     */
    private Button crearButton(String text, int width) {
        Button b = new Button();
        b.setText(text);
        b.setMinWidth(width);
        b.setFont(new Font(_pixelsLletra*0.6));
        return b;
    }

    /**
     * @brief Creació d'un separador.
     * @return Un separador amb les mides configurades.
     */
    private Separator crearSeparador() {
        Separator res = new Separator();
        res.setMinHeight(_pixelsLletra);
        return res;
    }
}

/**
 * DynamisFXClient.java
 *
 * Copyright (c) 2013-2016, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.dynamisfx.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.dynamisfx.DynamisFXSample;
import org.dynamisfx.DynamisFXSampleBase;
import org.dynamisfx.SampleGizmoSupport;
import org.dynamisfx.model.EmptySample;
import org.dynamisfx.model.Project;
import org.dynamisfx.model.SampleTree;
import org.dynamisfx.model.WelcomePage;
import org.dynamisfx.util.SampleScanner;

public class DynamisFXClient extends Application {
    private static final Logger LOG = Logger.getLogger(DynamisFXClient.class.getName());

    public static final String 
            BACKGROUNDS = DynamisFXClient.class.getResource("/org/dynamisfx/client/clientBackgrounds.css").toExternalForm(),
            BLACK_GLASS_BASE = DynamisFXClient.class.getResource("/org/dynamisfx/client/smokeBlackGlassBase.css").toExternalForm();
            //BLACK_GLASS_CONTROLS = DynamisFXClient.class.getResource("/org/dynamisfx/client/smokeBlackGlassControls.css").toExternalForm();
    private static final String TRANSPARENT_WINDOW_PROPERTY = "dynamisfx.client.transparentWindow";
    private static final String HIDDEN_SIDES_PROPERTY = "dynamisfx.client.hiddenSides";

    private static final int MIN_WINDOW_WIDTH = 800;
    private static final int MIN_WINDOW_HEIGHT = 600;

    private static final int INITIAL_WINDOW_WIDTH = 1200;
    private static final int INITIAL_WINDOW_HEIGHT = 768;
    private static final double DEFAULT_TRANSLATION_SNAP = 1.0;
    private static final double DEFAULT_ROTATION_SNAP = 15.0;
    private static final double DEFAULT_SCALE_SNAP = 0.1;

    private static DynamisFXClient rootClientInstance;

    public static DynamisFXClient getRootClientInstance() {
        return rootClientInstance;
    }

    public DynamisFXClient() {
        rootClientInstance = DynamisFXClient.this;
    }

    private Map<String, Project> projectsMap;
    private Stage stage;
    private DynamisFXSample selectedSample;
    private TextField searchBar;
    private CheckBox gizmoEnabled;
    private ComboBox<SampleGizmoSupport.Mode> gizmoMode;
    private CheckBox gizmoSnapEnabled;
    private TextField gizmoTranslationSnap;
    private TextField gizmoRotationSnap;
    private TextField gizmoScaleSnap;
    private TreeView<DynamisFXSample> contentTree;
    private TreeItem<DynamisFXSample> root;

    private VBox leftSideContent, contentControls;
    private StackPane centerContent;
    
    private HiddenSidesClient client;
    private SimpleWindowFrame frame;

    @Override
    public void start(final Stage mainStage) throws Exception {
        
        stage = mainStage;
        stage.getIcons().add(new Image(DynamisFXClient.class.getResource("/org/dynamisfx/images/logo2.png").toExternalForm()));
        
        projectsMap = new SampleScanner().discoverSamples();
        buildProjectTree(null);

        leftSideContent = new VBox();
        leftSideContent.setAlignment(Pos.TOP_CENTER);
        leftSideContent.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        leftSideContent.setSpacing(3);
        leftSideContent.setPadding(new Insets(3));
        leftSideContent.getStyleClass().add("fxyz3d-control");
        
        contentControls = new VBox();
        contentControls.getStyleClass().add("fxyz3d-control");
        contentControls.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
               
        centerContent = new StackPane();
        centerContent.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        searchBar = new TextField();
        searchBar.setFocusTraversable(false);
        searchBar.setPrefSize(USE_COMPUTED_SIZE, USE_PREF_SIZE);
        searchBar.textProperty().addListener((Observable o) -> {
            buildProjectTree(searchBar.getText());
        });
        searchBar.setOnMouseEntered(e->{
            if(client != null && client.getPinnedSide() == null){
                client.setPinnedSide(Side.LEFT);
            }
        });
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        
        final Button ab = new Button();
        ab.setAlignment(Pos.CENTER);
        ab.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        ab.setOnAction(e->{
            if (client != null) {
                client.setPinnedSide(null);
            }
        });

        gizmoEnabled = new CheckBox("Gizmo");
        gizmoEnabled.setFocusTraversable(false);
        gizmoEnabled.setSelected(true);
        gizmoEnabled.selectedProperty().addListener((obs, oldValue, newValue) -> applyGizmoSettings());

        gizmoMode = new ComboBox<>();
        gizmoMode.setFocusTraversable(false);
        gizmoMode.getItems().setAll(SampleGizmoSupport.Mode.values());
        gizmoMode.setValue(SampleGizmoSupport.Mode.ALL);
        gizmoMode.valueProperty().addListener((obs, oldValue, newValue) -> applyGizmoSettings());

        gizmoSnapEnabled = new CheckBox("Snap");
        gizmoSnapEnabled.setFocusTraversable(false);
        gizmoSnapEnabled.setSelected(false);
        gizmoSnapEnabled.selectedProperty().addListener((obs, oldValue, newValue) -> applyGizmoSettings());

        gizmoTranslationSnap = createSnapField(DEFAULT_TRANSLATION_SNAP);
        gizmoRotationSnap = createSnapField(DEFAULT_ROTATION_SNAP);
        gizmoScaleSnap = createSnapField(DEFAULT_SCALE_SNAP);
        
        contentTree = new TreeView<>(root);
        contentTree.getStyleClass().add("fxyz3d-control");
        contentTree.setShowRoot(false);
        contentTree.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        contentTree.setCellFactory(new Callback<TreeView<DynamisFXSample>, TreeCell<DynamisFXSample>>() {
            @Override
            public TreeCell<DynamisFXSample> call(TreeView<DynamisFXSample> param) {
                return new TreeCell<DynamisFXSample>() {
                    @Override
                    protected void updateItem(DynamisFXSample item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getSampleName());
                        }
                    }
                };
            }
        });
        contentTree.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<DynamisFXSample>> observable, TreeItem<DynamisFXSample> oldValue, TreeItem<DynamisFXSample> newSample) -> {
            if (newSample == null) {
                return;
            } else if (newSample.getValue() instanceof EmptySample) {
                DynamisFXSample selectedSample1 = newSample.getValue();
                Project selectedProject = projectsMap.get(selectedSample1.getSampleName());
                LOG.log(Level.FINE, "Selected project {0}", selectedProject);
                if (selectedProject != null) {
                    selectedSample = null;
                    changeToWelcomePage(selectedProject.getWelcomePage());
                }
                syncGizmoControls();
                return;
            }
            selectedSample = newSample.getValue();
            changeContent();
        });
        contentTree.setFocusTraversable(false);
        HBox toolbar = new HBox(6, searchBar, gizmoEnabled, gizmoMode, gizmoSnapEnabled, ab);
        HBox snapBar = new HBox(4,
                new Label("T"), gizmoTranslationSnap,
                new Label("R"), gizmoRotationSnap,
                new Label("S"), gizmoScaleSnap
        );
        leftSideContent.getChildren().addAll(toolbar, snapBar, contentTree);
        VBox.setVgrow(contentTree, Priority.ALWAYS);

        final boolean useHiddenSides = Boolean.getBoolean(HIDDEN_SIDES_PROPERTY);
        final Region appRoot;
        if (useHiddenSides) {
            client = new HiddenSidesClient();
            client.setContent(centerContent);
            client.setLeft(leftSideContent);
            client.setTriggerDistance(20);
            appRoot = client;
        } else {
            client = null;
            SplitPane appSplitPane = new SplitPane(leftSideContent, centerContent);
            appSplitPane.getStyleClass().add("fxyz-split-pane");
            appSplitPane.setDividerPositions(0.30);
            appRoot = appSplitPane;
        }
        
        List<TreeItem<DynamisFXSample>> projects = contentTree.getRoot().getChildren();
        if (!projects.isEmpty()) {
            TreeItem<DynamisFXSample> firstProject = projects.get(0);
            contentTree.getSelectionModel().select(firstProject);
        } else {
            changeToWelcomePage(null);
        }
        syncGizmoControls();

        Scene scene;
        if (Boolean.getBoolean(TRANSPARENT_WINDOW_PROPERTY)) {
            frame = new SimpleWindowFrame(stage, MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);
            frame.setText("DynamisFX Sampler");
            frame.setRootContent(appRoot);
            scene = new Scene(frame, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
            scene.setFill(Color.TRANSPARENT);
            stage.initStyle(StageStyle.TRANSPARENT);
            scene.getStylesheets().addAll(BLACK_GLASS_BASE);
        } else {
            scene = new Scene(appRoot, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
            scene.setFill(Color.web("#2b2b2b"));
            stage.setTitle("DynamisFX Sampler");
        }

        this.stage.setMinWidth(MIN_WINDOW_WIDTH);
        this.stage.setMinHeight(MIN_WINDOW_HEIGHT);
        this.stage.setScene(scene);
        this.stage.show();

        LOG.log(Level.FINE, "Loaded projects: {0}", contentTree.getRoot().getChildren().size());
    }

    /*/SimpleSamplerClient client = new SimpleSamplerClient(stage);  
        
     //Look at the clientBackgrounds.css file in resources for others
     //client.getStyleClass().add("comp-fade-background");
        
     SimpleSliderClient ssc = new SimpleSliderClient(stage, true);
     ssc.getStyleClass().add("blue-fade-background");
        
     Scene scene = new Scene(ssc, 1200, 800);//client, client.getPrefWidth(), client.getPrefHeight(), true, SceneAntialiasing.BALANCED);    
     scene.setCamera(new ParallelCamera());
     scene.setFill(null);
     scene.getStylesheets().addAll(BACKGROUNDS);
        
     stage.setScene(scene);
     stage.show();    
     */
    
    /*==========================================================================
     Load all Items into TreeView
     */
    protected final void buildProjectTree(String searchText) {
        // rebuild the whole tree (it isn't memory intensive - we only scan
        // classes once at startup)
        root = new TreeItem<>(new EmptySample("DynamisFX-Samples"));
        root.setExpanded(true);

        for (String projectName : projectsMap.keySet()) {
            final Project project = projectsMap.get(projectName);
            if (project == null) {
                LOG.log(Level.WARNING, "Project map contained null entry for key {0}", projectName);
                continue;
            }

            // now work through the project sample tree building the rest
            SampleTree.TreeNode n = project.getSampleTree().getRoot();
            root.getChildren().add(n.createTreeItem());
        }

        // with this newly built and full tree, we filter based on the search text
        if (searchText != null) {
            pruneSampleTree(root, searchText);
        }

        // and finally we sort the display a little
        sort(root, (o1, o2) -> o1.getValue().getSampleName().compareTo(o2.getValue().getSampleName()));
        if (contentTree != null) {
            contentTree.setRoot(root);
        }
    }

    private void sort(TreeItem<DynamisFXSample> node, Comparator<TreeItem<DynamisFXSample>> comparator) {
        node.getChildren().sort(comparator);
        node.getChildren().stream().forEach((child) -> {
            sort(child, comparator);
        });
    }

    // true == keep, false == delete
    private boolean pruneSampleTree(TreeItem<DynamisFXSample> treeItem, String searchText) {
        // we go all the way down to the leaf nodes, and check if they match
        // the search text. If they do, they stay. If they don't, we remove them.
        // As we pop back up we check if the branch nodes still have children,
        // and if not we remove them too
        if (searchText == null) {
            return true;
        }

        if (treeItem.isLeaf()) {
            // check for match. Return true if we match (to keep), and false
            // to delete
            return treeItem.getValue().getSampleName().toUpperCase().contains(searchText.toUpperCase());
        } else {
            // go down the tree...
            List<TreeItem<DynamisFXSample>> toRemove = new ArrayList<>();

            treeItem.getChildren().stream().forEach((child) -> {
                boolean keep = pruneSampleTree(child, searchText);
                if (!keep) {
                    toRemove.add(child);
                }
            });

            // remove the unrelated tree items
            treeItem.getChildren().removeAll(toRemove);

            // return true if there are children to this branch, false otherwise
            // (by returning false we say that we should delete this now-empty branch)
            return !treeItem.getChildren().isEmpty();
        }
    }

    public String getSearchString() {
        return searchBar.getText();
    }

    private void changeToWelcomePage(WelcomePage wPage) {
        //change to index above 0 -> 0 will be content header overlay
        centerContent.getChildren().clear();
        if (null == wPage) {
            wPage = getDefaultWelcomePage();
        }
        centerContent.getChildren().addAll(wPage.getContent());
        syncGizmoControls();
    }

    private WelcomePage getDefaultWelcomePage() {
        // line 1
        Label welcomeLabel1 = new Label("Welcome to DynamisFX Sampler!");
        welcomeLabel1.setStyle("-fx-font-size: 2em; -fx-padding: 0 0 0 5; -fx-text-fill: white;");

        // line 2
        Label welcomeLabel2 = new Label(
                "Explore the available UI controls and other interesting projects "
                + "by clicking on the options to the left.");
        welcomeLabel2.setStyle("-fx-font-size: 1.25em; -fx-padding: 0 0 0 5; -fx-text-fill: white;");

        WelcomePage wPage = new WelcomePage("Welcome!", new VBox(5, welcomeLabel1, welcomeLabel2));
        return wPage;
    }

    protected void changeContent() {
        if (selectedSample == null) {
            return;
        }

        contentControls.getChildren().clear();
        
        if (!centerContent.getChildren().isEmpty()) {
            centerContent.getChildren().clear();
        }

        updateContent();
        syncGizmoControls();
    }

    private void updateContent() {
        SplitPane cPane = new SplitPane();
        cPane.getStyleClass().add("fxyz-split-pane");
        cPane.setDividerPositions(0.75);

        cPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        Node content = buildSampleContent(selectedSample);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Node controls = selectedSample.getControlPanel();
        if(controls != null){
            contentControls.getChildren().add(controls);
            VBox.setVgrow(controls, Priority.ALWAYS);
        }
        
        cPane.getItems().addAll(content, contentControls);
        centerContent.getChildren().addAll(cPane);
        centerContent.toBack();
        
    }

    private Node buildSampleContent(DynamisFXSample sample) {
        return DynamisFXSampleBase.buildSample(sample, stage);
    }

    /*==========================================================================
     *                          Source Code Methods
     ==========================================================================*/
    private String getResource(String resourceName, Class<?> baseClass) {
        Class<?> clz = baseClass == null ? getClass() : baseClass;
        return getResource(clz.getResourceAsStream(resourceName));
    }

    private String getResource(InputStream is) {
        if (is == null) {
            LOG.warning("Requested resource stream was null");
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to read resource stream", e);
            return "";
        }
    }

    private String getSourceCode(DynamisFXSample sample) {
        String sourceURL = sample.getSampleSourceURL();

        try {
            // try loading via the web or local file system
            URL url = new URL(sourceURL);
            InputStream is = url.openStream();
            return getResource(is);
        } catch (IOException e) {
            // no-op - the URL may not be valid, no biggy
        }

        return getResource(sourceURL, sample.getClass());
    }

    private String formatSourceCode(DynamisFXSample sample) {
        String sourceURL = sample.getSampleSourceURL();
        String src;
        if (sourceURL == null) {
            src = "No sample source available";
        } else {
            src = "Sample Source not found";
            try {
                src = getSourceCode(sample);
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Failed to format source code for sample " + sample.getSampleName(), ex);
            }
        }

        // Escape '<' by "&lt;" to ensure correct rendering by SyntaxHighlighter
        src = src.replace("<", "&lt;");

        String template = getResource("/org/dynamisfx/util/SourceCodeTemplate.html", null);
        return template.replace("<source/>", src);
    }

    private String formatCss(DynamisFXSample sample) {
        String cssUrl = sample.getControlStylesheetURL();
        String src;
        if (cssUrl == null) {
            src = "No CSS source available";
        } else {
            src = "Css not found";
            try {
                src = getResource(cssUrl, getClass());
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Failed to read stylesheet " + cssUrl, ex);
            }
        }

        // Escape '<' by "&lt;" to ensure correct rendering by SyntaxHighlighter
        src = src.replace("<", "&lt;");

        String template = getResource("/org/dynamisfx/util/CssTemplate.html", null);
        return template.replace("<source/>", src);
    }

    private void syncGizmoControls() {
        SampleGizmoSupport support = selectedSample == null ? null : selectedSample.getGizmoSupport();
        boolean available = support != null;
        gizmoEnabled.setDisable(!available);
        gizmoMode.setDisable(!available);
        gizmoSnapEnabled.setDisable(!available);
        gizmoTranslationSnap.setDisable(!available);
        gizmoRotationSnap.setDisable(!available);
        gizmoScaleSnap.setDisable(!available);
        if (available) {
            applyGizmoSettings();
        }
    }

    private void applyGizmoSettings() {
        SampleGizmoSupport support = selectedSample == null ? null : selectedSample.getGizmoSupport();
        if (support == null) {
            return;
        }
        support.setEnabled(gizmoEnabled.isSelected());
        SampleGizmoSupport.Mode mode = gizmoMode.getValue();
        support.setMode(mode == null ? SampleGizmoSupport.Mode.ALL : mode);
        support.setSnapEnabled(gizmoSnapEnabled.isSelected());
        support.setSnapIncrements(
                parseSnapValue(gizmoTranslationSnap, DEFAULT_TRANSLATION_SNAP),
                parseSnapValue(gizmoRotationSnap, DEFAULT_ROTATION_SNAP),
                parseSnapValue(gizmoScaleSnap, DEFAULT_SCALE_SNAP)
        );
    }

    private TextField createSnapField(double initialValue) {
        TextField field = new TextField(String.valueOf(initialValue));
        field.setFocusTraversable(false);
        field.setPrefColumnCount(4);
        field.setOnAction(e -> applyGizmoSettings());
        field.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (!focused) {
                applyGizmoSettings();
            }
        });
        return field;
    }

    private double parseSnapValue(TextField field, double defaultValue) {
        try {
            double value = Double.parseDouble(field.getText());
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ex) {
            field.setText(String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        System.setProperty("polyglot.js.nashorn-compat", "true");
        launch(args);
    }
}

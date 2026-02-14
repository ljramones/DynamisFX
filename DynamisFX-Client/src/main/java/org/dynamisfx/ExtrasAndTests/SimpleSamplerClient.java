/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.ExtrasAndTests;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.dynamisfx.DynamisFXSample;
import org.dynamisfx.DynamisFXSampleBase;
import org.dynamisfx.model.EmptySample;
import org.dynamisfx.model.Project;
import org.dynamisfx.model.SampleTree;
import org.dynamisfx.model.WelcomePage;
import org.dynamisfx.util.SampleScanner;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class SimpleSamplerClient extends AbstractClientController {

    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private HBox header;
    @FXML
    private StackPane menuPane;
    @FXML
    private VBox leftSide;
    @FXML
    private TextField searchBar;
    @FXML
    private TreeView<DynamisFXSample> contentTree;
    @FXML
    private HBox statusBar;
    @FXML
    private HBox footer;
    @FXML
    private HBox leftStatusContainer;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private HBox rightStatusContainer;
    @FXML
    private Label rightStatusLabel;
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox centerOverlay;
    @FXML
    private HBox sceneTrackerOverlay;
    @FXML
    private VBox rightSide;
    @FXML
    private VBox descriptionPane;
    @FXML
    private Pane leftSlideTrigger;

    private TreeItem<DynamisFXSample> root;
    private final Map<String, Project> projectsMap;
    private DynamisFXSample selectedSample;

    public SimpleSamplerClient(final Stage stage) {
        try {
            FXMLLoader ldr = getUILoader();
            ldr.setController(SimpleSamplerClient.this);
            ldr.setRoot(SimpleSamplerClient.this);
            ldr.load();
        } catch (IOException ex) {
            Logger.getLogger(SimpleSamplerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.stage = stage;
        this.projectsMap = new SampleScanner().discoverSamples();
        buildProjectTree(null);
        initController();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void initController() {
        initHeader();
        initLeftPanel();
        initCenterContentPane();
        initCenterContentHeaderOverlay();
        initRightPanel();
        initFooter();
        initialize();
    }

    /**
     * *************************************************************************
     * Path to FXML (extend later for custom layouts (sample:
     * MainSceneController)
     * ************************************************************************
     */
    @Override
    public final FXMLLoader getUILoader() {
        return new FXMLLoader(getClass().getResource(getFXMLPath()));

    }

    @Override
    protected String getFXMLPath() {
        return "Client.fxml";
    }

    /**
     * *************************************************************************
     * Header Setup
     * ************************************************************************
     */
    @Override
    protected void initHeader() {

    }

    /**
     * *************************************************************************
     * LeftPanel Setup
     * ************************************************************************
     */
    @Override
    protected void initLeftPanel() {
        contentTree.setRoot(root);

        searchBar.textProperty().addListener((Observable o) -> {
            buildProjectTree(searchBar.getText());
        });
        contentTree.setShowRoot(false);
        contentTree.getStyleClass().add("samples-tree");
        contentTree.setMinWidth(USE_PREF_SIZE);
        contentTree.setMaxWidth(Double.MAX_VALUE);
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
        contentTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<DynamisFXSample>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<DynamisFXSample>> observable, TreeItem<DynamisFXSample> oldValue, TreeItem<DynamisFXSample> newSample) {

                if (newSample == null) {
                    return;
                } else if (newSample.getValue() instanceof EmptySample) {
                    DynamisFXSample selectedSample = newSample.getValue();
                    Project selectedProject = projectsMap.get(selectedSample.getSampleName());
                    System.out.println(selectedProject);
                    if (selectedProject != null) {
                        changeToWelcomePage(selectedProject.getWelcomePage());
                    }
                    return;
                }
                selectedSample = newSample.getValue();
                changeContent();
            }
        });

    }

    /**
     * *************************************************************************
     * ContentSetup Setup
     * ************************************************************************
     */
    @Override
    protected void initCenterContentPane() {
        List<TreeItem<DynamisFXSample>> projects = contentTree.getRoot().getChildren();
        if (!projects.isEmpty()) {
            TreeItem<DynamisFXSample> firstProject = projects.get(0);
            contentTree.getSelectionModel().select(firstProject);
        } else {
            changeToWelcomePage(null);
        }
    }

    /**
     * *************************************************************************
     * Content Header Overlay Setup
     * ************************************************************************
     */
    @Override
    protected void initCenterContentHeaderOverlay() {
    }

    /**
     * *************************************************************************
     * Controls Setup
     * ************************************************************************
     */
    @Override
    protected void initRightPanel() {
        
    }

    /**
     * *************************************************************************
     * Footer Setup
     * ************************************************************************
     */
    @Override
    protected void initFooter() {
    }

    /**
     * *************************************************************************
     * Persistent Properties Setup
     * ************************************************************************
     */
    @Override
    protected void loadClientProperties() {
    }

    @Override
    protected void saveClientProperties() {
    }

    /**
     * *************************************************************************
     * ControlsFX FXSampler setup for loading samples
     *
     * /////////////////////////////////////////////////////////////////////////
     */
    /*==========================================================================
     Load all Items into TreeView
     */
    @Override
    protected final void buildProjectTree(String searchText) {
        // rebuild the whole tree (it isn't memory intensive - we only scan
        // classes once at startup)
        root = new TreeItem<>(new EmptySample("DynamisFX Sampler"));
        root.setExpanded(true);

        for (String projectName : projectsMap.keySet()) {
            final Project project = projectsMap.get(projectName);
            if (project == null) {
                continue;
            }

            // now work through the project sample tree building the rest
            SampleTree.TreeNode n = project.getSampleTree().getRoot();
            root.getChildren().add(n.createTreeItem());
        }

        // with this newly built and full tree, we filter based on the search text
        if (searchText != null) {
            pruneSampleTree(root, searchText);

            // FIXME weird bug in TreeView I think
            contentTree.setRoot(null);
            contentTree.setRoot(root);
        }

        // and finally we sort the display a little
        sort(root, (o1, o2) -> o1.getValue().getSampleName().compareTo(o2.getValue().getSampleName()));
    }

    private void sort(TreeItem<DynamisFXSample> node, Comparator<TreeItem<DynamisFXSample>> comparator) {
        node.getChildren().sort(comparator);
        for (TreeItem<DynamisFXSample> child : node.getChildren()) {
            sort(child, comparator);
        }
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

            for (TreeItem<DynamisFXSample> child : treeItem.getChildren()) {
                boolean keep = pruneSampleTree(child, searchText);
                if (!keep) {
                    toRemove.add(child);
                }
            }

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
        contentPane.getChildren().removeIf(index -> contentPane.getChildren().indexOf(index) == 0 && index instanceof StackPane);

        if (null == wPage) {
            wPage = getDefaultWelcomePage();
        }
        contentPane.getChildren().addAll(wPage.getContent());
    }

    private WelcomePage getDefaultWelcomePage() {
        // line 1
        Label welcomeLabel1 = new Label("Welcome to FXSampler!");
        welcomeLabel1.setStyle("-fx-font-size: 2em; -fx-padding: 0 0 0 5;");

        // line 2
        Label welcomeLabel2 = new Label(
                "Explore the available UI controls and other interesting projects "
                + "by clicking on the options to the left.");
        welcomeLabel2.setStyle("-fx-font-size: 1.25em; -fx-padding: 0 0 0 5;");

        WelcomePage wPage = new WelcomePage("Welcome!", new VBox(5, welcomeLabel1, welcomeLabel2));
        return wPage;
    }

    @Override
    protected void changeContent() {
        if (selectedSample == null) {
            return;
        }

        if (!contentPane.getChildren().isEmpty()) {
            contentPane.getChildren().clear();
            rightSide.getChildren().clear();
        }

        updateContent();
    }

    private void updateContent() {
        contentPane.getChildren().addAll(buildSampleTabContent(selectedSample));
        // below add labels / textflow if needed preferably befor controls  
        Node controls = selectedSample.getControlPanel();
        VBox.setVgrow(controls, Priority.ALWAYS);
        rightSide.getChildren().addAll(controls);
        setShowMenuPane(false);
    }

    private Node buildSampleTabContent(DynamisFXSample sample) {
        return DynamisFXSampleBase.buildSample(sample, stage);
    }

    public Map<String, Project> getProjectsMap() {
        return projectsMap;
    }

    public DynamisFXSample getSelectedSample() {
        return selectedSample;
    }

    /*==========================================================================
     Getters and Setters for FXML and SamplerApp Samples
     */
    public BorderPane getRootBorderPane() {
        return rootBorderPane;
    }

    public HBox getHeader() {
        return header;
    }

    public VBox getLeftSide() {
        return leftSide;
    }

    public TextField getSearchBar() {
        return searchBar;
    }

    public TreeView<DynamisFXSample> getContentTree() {
        return contentTree;
    }

    public HBox getStatusBar() {
        return statusBar;
    }

    public HBox getFooter() {
        return footer;
    }

    public HBox getLeftStatusContainer() {
        return leftStatusContainer;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public HBox getRightStatusContainer() {
        return rightStatusContainer;
    }

    public Label getRightStatusLabel() {
        return rightStatusLabel;
    }

    public StackPane getContentPane() {
        return contentPane;
    }

    public VBox getCenterOverlay() {
        return centerOverlay;
    }

    public HBox getSceneTrackerOverlay() {
        return sceneTrackerOverlay;
    }

    public VBox getRightSide() {
        return rightSide;
    }

    //==========================================================================
    //      OPTIONAL Pop-out trays based on Derick Limmerman(?) example
    private final BooleanProperty showMenuPane = new SimpleBooleanProperty(this, "showMenuPane", true);

    public final boolean isShowMenuPane() {
        return showMenuPane.get();
    }

    public final void setShowMenuPane(boolean showMenu) {
        showMenuPane.set(showMenu);
    }

    /**
     * Returns the property used to control the visibility of the menu panel.
     * When the value of this property changes to false then the menu panel will
     * slide out to the left).
     *     
* @return the property used to control the menu panel
     */
    public final BooleanProperty showMenuPaneProperty() {
        return showMenuPane;
    }

    private final BooleanProperty showBottomPane = new SimpleBooleanProperty(this, "showBottomPane", true);

    public final boolean isShowBottomPane() {
        return showBottomPane.get();
    }

    public final void setShowBottomPane(boolean showBottom) {
        showBottomPane.set(showBottom);
    }

    /**
     * Returns the property used to control the visibility of the bottom panel.
     * When the value of this property changes to false then the bottom panel
     * will slide out to the left).
     *     
* @return the property used to control the bottom panel
     */
    public final BooleanProperty showBottomPaneProperty() {
        return showBottomPane;
    }

    public final void initialize() {
        menuPaneLocation.addListener(it -> updateMenuPaneAnchors());
        bottomPaneLocation.addListener(it -> updateBottomPaneAnchors());

        showMenuPaneProperty().addListener(it -> animateMenuPane());
        showBottomPaneProperty().addListener(it -> animateBottomPane());

        menuPane.setOnMouseExited(evt -> setShowMenuPane(false));
        leftSlideTrigger.setOnMouseEntered(evt -> setShowMenuPane(true));
        //menuPane.setOnMouseClicked(evt -> setShowMenuPane(false));

        contentPane.setOnMouseClicked(evt -> {
            //setShowMenuPane(true);
            //setShowBottomPane(true);
        });

        footer.setOnMouseClicked(evt -> setShowBottomPane(false));
    }

    /*
     * The updateMenu/BottomPaneAnchors methods get called whenever the value of
     * menuPaneLocation or bottomPaneLocation changes. Setting anchor pane
     * constraints will automatically trigger a relayout of the anchor pane
     * children.
     */
    private void updateMenuPaneAnchors() {
        setLeftAnchor(menuPane, getMenuPaneLocation());
        setLeftAnchor(contentPane, getMenuPaneLocation() + menuPane.getWidth());
    }

    private void updateBottomPaneAnchors() {
        setBottomAnchor(footer, getBottomPaneLocation());
        setBottomAnchor(contentPane,
                getBottomPaneLocation() + footer.getHeight());
        setBottomAnchor(menuPane,
                getBottomPaneLocation() + footer.getHeight());
    }

    /*
     * Starts the animation for the menu pane.
     */
    private void animateMenuPane() {
        if (isShowMenuPane()) {
            slideMenuPane(0);
        } else {
            slideMenuPane(-leftSide.prefWidth(-1));
        }
    }

    /*
     * Starts the animation for the bottom pane.
     */
    private void animateBottomPane() {
        if (isShowBottomPane()) {
            slideBottomPane(0);
        } else {
            slideBottomPane(-footer.prefHeight(-1));
        }
    }

    /*
     * The animations are using the JavaFX timeline concept. The timeline updates
     * properties. In this case we have to introduce our own properties further
     * below (menuPaneLocation, bottomPaneLocation) because ultimately we need to
     * update layout constraints, which are not properties. So this is a little
     * work-around.
     */
    private void slideMenuPane(double toX) {
        KeyValue keyValue = new KeyValue(menuPaneLocation, toX);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    private void slideBottomPane(double toY) {
        KeyValue keyValue = new KeyValue(bottomPaneLocation, toY);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    private DoubleProperty menuPaneLocation = new SimpleDoubleProperty(this, "menuPaneLocation");

    private double getMenuPaneLocation() {
        return menuPaneLocation.get();
    }

    private DoubleProperty bottomPaneLocation = new SimpleDoubleProperty(this, "bottomPaneLocation");

    private double getBottomPaneLocation() {
        return bottomPaneLocation.get();
    }

}

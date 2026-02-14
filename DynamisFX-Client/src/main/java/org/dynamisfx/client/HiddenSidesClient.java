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

package org.dynamisfx.client;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.HiddenSidesPane;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class HiddenSidesClient extends AnchorPane{
    
    private final HiddenSidesPane rootPane;
    private final StackPane center, left, right, top, bottom;
    
    public HiddenSidesClient() {
        this.rootPane = new HiddenSidesPane();
        this.rootPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        this.center = new StackPane();
        this.center.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        this.bottom = new StackPane();
        this.bottom.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        this.top = new StackPane();
        this.top.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        this.left = new StackPane();
        this.left.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        this.right = new StackPane();
        this.right.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        this.rootPane.setBottom(bottom);
        this.rootPane.setContent(center);
        this.rootPane.setTop(top);
        this.rootPane.setLeft(left);
        this.rootPane.setRight(right);
        this.rootPane.setAnimationDelay(Duration.ZERO);
        this.rootPane.setAnimationDuration(Duration.seconds(0.5));
        this.rootPane.setTriggerDistance(10.0);
        
        AnchorPane.setTopAnchor(rootPane, 0.0);
        AnchorPane.setBottomAnchor(rootPane, 0.0);
        AnchorPane.setLeftAnchor(rootPane, 0.0);
        AnchorPane.setRightAnchor(rootPane, 0.0);
                                
        this.getChildren().add(rootPane);
    }

    public final DoubleProperty triggerDistanceProperty() {
        return rootPane.triggerDistanceProperty();
    }

    public final double getTriggerDistance() {
        return rootPane.getTriggerDistance();
    }

    public final void setTriggerDistance(double distance) {
        rootPane.setTriggerDistance(distance);
    }

    public final ObjectProperty<Node> contentProperty() {
        return rootPane.contentProperty();
    }

    public final Node getContent() {
        return center.getChildren().isEmpty() ? null : center.getChildren().get(0);
    }

    public final void setContent(Node content) {
        center.getChildren().clear();
        center.getChildren().add(content);
    }

    public final ObjectProperty<Node> topProperty() {
        return rootPane.topProperty();
    }

    public final Node getTop() {
        return top;
    }

    public final void setTop(Node top) {
        this.top.getChildren().clear();
        this.top.getChildren().add(top);
    }

    public final ObjectProperty<Node> rightProperty() {
        return rootPane.rightProperty();
    }

    public final StackPane getRight() {
        return right;
    }

    public final void setRight(Node right) {
        this.right.getChildren().clear();
        this.right.getChildren().add(right);
    }

    public final ObjectProperty<Node> bottomProperty() {
        return rootPane.bottomProperty();
    }

    public final StackPane getBottom() {
        return bottom;
    }

    public final void setBottom(Node bottom) {
        this.bottom.getChildren().clear();
        this.bottom.getChildren().add(bottom);
    }

    public final ObjectProperty<Node> leftProperty() {
        return rootPane.leftProperty();
    }

    public final StackPane getLeft() {
        return left;
    }

    public final void setLeft(Node left) {
        this.left.getChildren().clear();
        this.left.getChildren().add(left);
    }

    public final ObjectProperty<Side> pinnedSideProperty() {
        return rootPane.pinnedSideProperty();
    }

    public final Side getPinnedSide() {
        return rootPane.getPinnedSide();
    }

    public final void setPinnedSide(Side side) {
        rootPane.setPinnedSide(side);
    }

    public final ObjectProperty<Duration> animationDelayProperty() {
        return rootPane.animationDelayProperty();
    }

    public final Duration getAnimationDelay() {
        return rootPane.getAnimationDelay();
    }

    public final void setAnimationDelay(Duration duration) {
        rootPane.setAnimationDelay(duration);
    }

    public final ObjectProperty<Duration> animationDurationProperty() {
        return rootPane.animationDurationProperty();
    }

    public final Duration getAnimationDuration() {
        return rootPane.getAnimationDuration();
    }

    public final void setAnimationDuration(Duration duration) {
        rootPane.setAnimationDuration(duration);
    }

    public final String getUserAgentStylesheet() {
        return rootPane.getUserAgentStylesheet();
    }
}

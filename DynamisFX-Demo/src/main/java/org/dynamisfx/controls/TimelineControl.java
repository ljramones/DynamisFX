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
 */

package org.dynamisfx.controls;

import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author Jose Pereda
 */
public class TimelineControl extends ControlBase<Property<Timeline>> {

    @FXML private Label title;

    @FXML private StackPane timelineDisplay;
    @FXML private Pane background;
    @FXML private HBox progressBar;
    @FXML private Pane bar;
    @FXML private Text startTxt;
    @FXML private Text currentTxt;
    @FXML private Text endTxt;

    @FXML private HBox controls;
    @FXML private Button startBtn;
    @FXML private Button rwBtn;
    @FXML private ToggleButton playBtn;
    @FXML private Button ffBtn;
    @FXML private Button endBtn;
    @FXML private ToggleButton loopBtn;

    private final ChangeListener<Number> rateListener = (obs, ov, nv) -> {
        if (nv.intValue() == 0 && playBtn.isSelected()) {
            playBtn.setSelected(false);
        }
    };

    private final DoubleProperty currentTimeAsPercentage = new SimpleDoubleProperty(0);

    public final Timeline getTimeline() { return timeline.get(); }

    private final ObjectProperty<Timeline> timeline = new SimpleObjectProperty<>() {
        private Timeline old;
        @Override protected void invalidated() {
            Timeline t = get();
            if (old != null) {
                currentTimeAsPercentage.unbind();
                currentTxt.textProperty().unbind();
                endTxt.textProperty().unbind();
                bar.prefWidthProperty().unbind();
                old.currentRateProperty().removeListener(rateListener);
            }
            if (t != null) {
                currentTimeAsPercentage.bind(Bindings.createDoubleBinding(
                        () ->  t.getCurrentTime().toMillis() / t.getCycleDuration().toMillis(),
                        t.currentTimeProperty(), t.cycleDurationProperty()));
                endTxt.textProperty().bind(Bindings.createStringBinding(
                        () -> String.format("%.2fs", t.getCycleDuration().toSeconds()),
                        t.cycleDurationProperty()));
                currentTxt.textProperty().bind(Bindings.createStringBinding(
                        () -> String.format("%.2fs", t.getCurrentTime().toSeconds()),
                        t.currentTimeProperty()));
                bar.prefWidthProperty().bind(Bindings.createDoubleBinding(
                        () -> progressBar.getWidth() * currentTimeAsPercentage.get(),
                        currentTimeAsPercentage));

                playBtn.setSelected(t.getCurrentRate() != 0);
                loopBtn.setSelected(t.getCycleDuration().equals(Timeline.INDEFINITE));
                t.currentRateProperty().addListener(rateListener);
            }
            old = t;
        }
    };

    public TimelineControl(Property<Timeline> prop, String name) {
        super("/org/dynamisfx/controls/TimelineControl.fxml", prop);
        timeline.bindBidirectional(prop);
        controls.disableProperty().bind(timeline.isNull());
        timelineDisplay.visibleProperty().bind(timeline.isNotNull());
        title.setText(name);

        background.setCache(true);

        startBtn.setOnAction(e -> {
            Timeline timeline = getTimeline();
            timeline.jumpTo(Duration.ZERO);
            timeline.pause();
        });
        endBtn.setOnAction(e -> {
            Timeline timeline = getTimeline();
            timeline.jumpTo(timeline.getTotalDuration());
            timeline.pause();
        });
        playBtn.setOnAction(e -> {
            Timeline timeline = getTimeline();
            if (playBtn.isSelected()) {
                timeline.play();
            } else {
                timeline.pause();
            }
        });
        ffBtn.setOnMousePressed(e -> setTimelineRate(2));
        ffBtn.setOnMouseReleased(e -> setTimelineRate(1));
        rwBtn.setOnMousePressed(e -> setTimelineRate(-2));
        rwBtn.setOnMouseReleased(e -> setTimelineRate(1));
        loopBtn.setOnAction(e -> {
            Timeline timeline = getTimeline();
            timeline.stop();
            if (loopBtn.isSelected()) {
                timeline.setCycleCount(Timeline.INDEFINITE);
            } else {
                timeline.setCycleCount(1);
            }
            timeline.play();
        });

    }

    private void setTimelineRate(int rate) {
        getTimeline().setRate(rate);
    }

}

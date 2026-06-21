package cn.tealc.ntemaid.ui.base;

import atlantafx.base.util.Animations;
import cn.tealc.ntemaid.util.LanguageManager;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractGroupView<VM extends ViewModel> extends AnchorPane implements JavaView<VM> {

    protected final HBox headerPane;
    protected final HBox buttonBar;
    protected final StackPane content;
    protected final ToggleGroup toggleGroup = new ToggleGroup();

    private final Map<ToggleButton, Class<? extends FxmlView<?>>> childRegistry = new LinkedHashMap<>();
    private final Map<ToggleButton, Parent> childCache = new LinkedHashMap<>();

    protected AbstractGroupView(String titleI18nKey) {
        headerPane = new HBox(20);
        headerPane.getStyleClass().add("tab-header");
        headerPane.setPrefHeight(35);

        Label titleLabel = new Label(LanguageManager.getString(titleI18nKey));
        titleLabel.getStyleClass().add("title-2");

        Separator separator = new Separator(javafx.geometry.Orientation.VERTICAL);

        buttonBar = new HBox();
        headerPane.getChildren().addAll(titleLabel, separator, buttonBar);

        content = new StackPane();
        content.setPadding(new Insets(0, 0, 0, 0));

        getChildren().addAll(headerPane, content);

        AnchorPane.setTopAnchor(headerPane, 0.0);
        AnchorPane.setLeftAnchor(headerPane, 0.0);
        AnchorPane.setRightAnchor(headerPane, 0.0);
        AnchorPane.setTopAnchor(content, 50.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);

        setPadding(new Insets(10));
    }

    protected ToggleButton addTab(String i18nKey, Class<? extends FxmlView<?>> childViewClass, boolean isDefault) {
        ToggleButton btn = new ToggleButton(LanguageManager.getString(i18nKey));
        btn.getStyleClass().add("child-select");
        btn.setToggleGroup(toggleGroup);
        btn.setContentDisplay(javafx.scene.control.ContentDisplay.BOTTOM);

        Pane underline = new Pane();
        underline.setPrefSize(25, 3);
        btn.setGraphic(underline);

        btn.setOnAction(this::switchTab);

        childRegistry.put(btn, childViewClass);
        buttonBar.getChildren().add(btn);

        if (isDefault) {
            selectTab(btn);
        }

        return btn;
    }

    protected void selectTab(ToggleButton button) {
        button.setSelected(true);
        showChild(button);
    }

    private void switchTab(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton button) {
            if (button.isSelected()) {
                showChild(button);
            } else {
                button.setSelected(true);
            }
        }
    }

    private void showChild(ToggleButton button) {
        Class<? extends FxmlView<?>> childClass = childRegistry.get(button);
        if (childClass == null) {
            return;
        }
        Parent child = childCache.computeIfAbsent(button, btn -> {
            ViewTuple<?, ?> tuple = FluentViewLoader.fxmlView(childClass).load();
            return (Parent) tuple.getView();
        });
        content.getChildren().setAll(child);
        Animations.slideInUp(child, Duration.millis(300)).play();
    }
}

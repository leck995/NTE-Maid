package cn.tealc.ntemaid.ui.game.music;

import atlantafx.base.util.Animations;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MusicParentView2 extends BorderPane implements JavaView<MusicParentViewModel> {
    @InjectViewModel
    private MusicParentViewModel viewModel;

    private final ToggleButton toAllMusicListViewBtn;
    private final ToggleButton toMusicPlayerViewBtn;
    private Parent allMusicListView;
    private Parent musicPlayerView;
    private final StackPane center;

    public MusicParentView2() {
        HBox top = new HBox(15.0);
        top.setPrefHeight(45.0);
        top.setAlignment(Pos.CENTER);
        toAllMusicListViewBtn = new ToggleButton("全部歌曲");
        toMusicPlayerViewBtn = new ToggleButton("播放器");
        ToggleGroup toggleGroup = new ToggleGroup();
        toAllMusicListViewBtn.setToggleGroup(toggleGroup);
        toMusicPlayerViewBtn.setToggleGroup(toggleGroup);

        toMusicPlayerViewBtn.setSelected(true);
        toAllMusicListViewBtn.setOnAction(_ -> toAllMusicListView());
        toMusicPlayerViewBtn.setOnAction(_ -> toMusicPlayerView());
        top.getChildren().addAll(toAllMusicListViewBtn, toMusicPlayerViewBtn);

        center = new StackPane();
        setTop(top);
        setCenter(center);


        toMusicPlayerView();
    }

    void toAllMusicListView() {
        if (toAllMusicListViewBtn.isSelected()) {
            if (allMusicListView == null) {
                ViewTuple<AllMusicListView, AllMusicListViewModel> viewTuple = FluentViewLoader.fxmlView(AllMusicListView.class).load();
                allMusicListView = viewTuple.getView();
            }
            center.getChildren().setAll(allMusicListView);
            Animations.slideInUp(allMusicListView, Duration.millis(300)).play();
            center.toFront();
        } else {
            toAllMusicListViewBtn.setSelected(true);
        }
    }

    void toMusicPlayerView() {
        if (toMusicPlayerViewBtn.isSelected()) {
            if (musicPlayerView == null) {
                ViewTuple<MusicPlayerView, MusicPlayerViewModel> viewTuple = FluentViewLoader.fxmlView(MusicPlayerView.class).load();
                musicPlayerView = viewTuple.getView();
            }
            center.getChildren().setAll(musicPlayerView);
            Animations.slideInUp(musicPlayerView, Duration.millis(300)).play();
            center.toFront();
        } else {
            toMusicPlayerViewBtn.setSelected(true);
        }
    }
}

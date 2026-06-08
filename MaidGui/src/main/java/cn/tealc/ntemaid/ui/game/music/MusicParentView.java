package cn.tealc.ntemaid.ui.game.music;

import atlantafx.base.util.Animations;
import de.saxsys.mvvmfx.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MusicParentView extends BorderPane implements FxmlView<MusicParentViewModel>, Initializable {
    @InjectViewModel
    private MusicParentViewModel viewModel;
    @FXML
    private ToggleGroup childSelectedToggle;
    @FXML
    private StackPane content;
    @FXML
    private HBox headerPane;
    @FXML
    private StackPane root;
    @FXML
    private ToggleButton toAllMusicListViewBtn;
    @FXML
    private ToggleButton toMusicPlayerViewBtn;
    @FXML
    private ToggleButton toPlaylistListViewBtn;
    private Parent allMusicListView;
    private Parent musicPlayerView;
    private Parent playlistView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toMusicPlayerViewBtn.setSelected(true);
        toAllMusicListViewBtn.setOnAction(_ -> toAllMusicListView());
        toMusicPlayerViewBtn.setOnAction(_ -> toMusicPlayerView());
        toPlaylistListViewBtn.setOnAction(_ -> toPlaylistListView());
        toMusicPlayerView();
    }
    void toAllMusicListView() {
        if (toAllMusicListViewBtn.isSelected()) {
            if (allMusicListView == null) {
                ViewTuple<AllMusicListView, AllMusicListViewModel> viewTuple = FluentViewLoader.fxmlView(AllMusicListView.class).load();
                allMusicListView = viewTuple.getView();
            }
            content.getChildren().setAll(allMusicListView);
            //content.toFront();
            Animations.slideInUp(allMusicListView, Duration.millis(300)).play();
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
            content.getChildren().setAll(musicPlayerView);
            //content.toFront();
            Animations.slideInUp(musicPlayerView, Duration.millis(300)).play();
        } else {
            toMusicPlayerViewBtn.setSelected(true);
        }
    }

    void toPlaylistListView() {
        if (toPlaylistListViewBtn.isSelected()) {
            if (playlistView == null) {
                ViewTuple<PlaylistView, PlaylistViewModel> viewTuple = FluentViewLoader.fxmlView(PlaylistView.class).load();
                playlistView = viewTuple.getView();
            }
            content.getChildren().setAll(playlistView);
            //content.toFront();
            Animations.slideInUp(playlistView, Duration.millis(300)).play();
        } else {
            toPlaylistListViewBtn.setSelected(true);
        }
    }

}

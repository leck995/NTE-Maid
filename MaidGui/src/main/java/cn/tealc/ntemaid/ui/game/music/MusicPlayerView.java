package cn.tealc.ntemaid.ui.game.music;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.ProgressSliderSkin;
import atlantafx.base.controls.Spacer;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.music.LrcBean;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.util.DialogBuilder;
import cn.tealc.ntemaid.util.TimeFormatUtil;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @program: AsmrPlayer-web
 * @description:
 * @author: Leck
 * @create: 2023-11-23 16:40
 */
public class MusicPlayerView extends StackPane implements FxmlView<MusicPlayerViewModel>, Initializable {
    @InjectViewModel
    private MusicPlayerViewModel viewModel;
    @FXML
    private StackPane root, lrcPane, albumPane;
    @FXML
    private ImageView album;
    @FXML
    private Label currentTimeLabel, totalTimeLabel;
    @FXML
    private AnchorPane infoPane, progressPane;
    @FXML
    private Label singer;
    @FXML
    private Label songName;
    @FXML
    private Button nextBtn, preBtn, showSongListViewBtn;
    @FXML
    private ToggleButton disorderBtn, loopBtn, playBtn, lrcBtn, volumeBtn;
    @FXML
    private Slider songSlider;
    @FXML
    private TextField musicDirField;
    private ModalPane modalPane;
    private LrcView<LrcBean> lrcView;
    public Boolean isMouseMove = false;
    private ContextMenu volumePopup;
    private SimpleDoubleProperty current;
    private ListView<Music> musicListView;




    public MusicPlayerView() {
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        musicDirField.textProperty().bind(viewModel.musicDirProperty());
        album.imageProperty().bind(viewModel.coverProperty());
        Rectangle albumRectangle = new Rectangle();
        albumRectangle.widthProperty().bind(albumPane.widthProperty().add(-10));
        albumRectangle.heightProperty().bind(albumPane.heightProperty().add(-10));
        //albumRectangle.arcWidthProperty().bind(SettingProperties.detailAlbumRadiusSize);
        //albumRectangle.arcHeightProperty().bind(SettingProperties.detailAlbumRadiusSize);
        albumRectangle.setArcHeight(20);
        albumRectangle.setArcWidth(20);
        albumPane.setClip(albumRectangle);

        album.fitWidthProperty().bind(root.widthProperty().multiply(0.45));
        album.fitHeightProperty().bind(root.heightProperty().multiply(0.45));
        infoPane.prefWidthProperty().bind(albumPane.widthProperty());
        progressPane.prefWidthProperty().bind(albumPane.widthProperty());

        disorderBtn.setTooltip(new Tooltip("乱序"));
        disorderBtn.selectedProperty().bindBidirectional(viewModel.disorderProperty());
        loopBtn.setTooltip(new Tooltip("循环"));
        loopBtn.selectedProperty().bindBidirectional(viewModel.loopProperty());
        showSongListViewBtn.setTooltip(new Tooltip("播放列表"));
        showSongListViewBtn.setOnMouseClicked(mouseEvent -> showPlaylistView());
        playBtn.selectedProperty().bindBidirectional(viewModel.playingProperty());
        playBtn.setOnAction(actionEvent -> {
            if (!viewModel.isReady()) {
                playBtn.setSelected(false);
                actionEvent.consume();
            }
        });
        lrcBtn.setTooltip(new Tooltip("桌面歌词"));
        lrcBtn.selectedProperty().bindBidirectional(viewModel.desktopLrcProperty());

        nextBtn.setOnMouseClicked(mouseEvent -> viewModel.next());
        preBtn.setOnMouseClicked(mouseEvent -> viewModel.pre());

        volumeBtn.selectedProperty().bind(viewModel.muteProperty());
        volumeBtn.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                showVolume();
                event.consume();
            }
        });

        songSlider.setSkin(new ProgressSliderSkin(songSlider));
        songSlider.setMin(0);
        songSlider.setMaxWidth(900);
        songSlider.maxProperty().bind(viewModel.totalTimeProperty());

        /*进度条随歌曲时间更改*/
        current = new SimpleDoubleProperty();
        current.bind(viewModel.currentTimeProperty());
        current.addListener((obs, oldValue, newValue) -> {
            if (!isMouseMove) {
                songSlider.setValue(newValue.doubleValue());
            }
        });
        /*进度条按下监听*/
        songSlider.setOnMousePressed(mouseEvent -> {
            isMouseMove = true;
        });
        /*进度条释放监听，跳转到指定时间*/
        songSlider.setOnMouseReleased(mouseEvent -> {
            isMouseMove = false;
            if (!viewModel.isReady())
                return;
            viewModel.seek(songSlider.getValue());
        });

        if (current.getValue() != null)
            songSlider.setValue(current.getValue());

        songName.textProperty().bind(viewModel.titleProperty());
        singer.textProperty().bind(viewModel.artistProperty());
        currentTimeLabel.textProperty().bind(Bindings
                .createStringBinding(() -> (
                        TimeFormatUtil.formatToClock(viewModel.getCurrentTime())
                ), viewModel.currentTimeProperty()));
        totalTimeLabel.textProperty().bind(Bindings
                .createStringBinding(() -> (
                        TimeFormatUtil.formatToClock(viewModel.totalTimeProperty().get())
                ), viewModel.totalTimeProperty()));
        initLrcView();
        lrcView.itemsProperty().bind(viewModel.lrcBeansProperty());
        modalPane = new ModalPane();
        modalPane.setAlignment(Pos.BOTTOM_RIGHT);
        root.getChildren().add(modalPane);


    }


    /**
     * @return void
     * @name: initLrcView
     * @description: 初始化LrcView
     * @author: Leck
     * @param:
     * @date: 2022/12/17
     */
    private void initLrcView() {
        ObservableList<LrcBean> lrcBeanList = FXCollections.observableArrayList();
        lrcView = new LrcView<>(viewModel.lrcSelectedIndexProperty());
        lrcView.getStyleClass().add("lrc-view");
        lrcPane.getChildren().add(lrcView);
        lrcView.setItems(lrcBeanList);
    }


    /**
     * 显示音量控制组件
     *
     * @author leck
     * @date 2026/05/05
     */
    public void showVolume() {
        if (volumePopup != null) {
            Bounds bounds = volumeBtn.localToScreen(volumeBtn.getBoundsInLocal());
            volumePopup.show(volumeBtn.getScene().getWindow(), bounds.getMinX(), bounds.getMinY() - 160);
        } else {
            Bounds bounds = volumeBtn.localToScreen(volumeBtn.getBoundsInLocal());
            volumePopup = new ContextMenu();
            volumePopup.setPrefWidth(100);
            volumePopup.setMaxWidth(100);

            volumePopup.getScene().setRoot(initVolumePopup());
            volumePopup.show(volumeBtn.getScene().getWindow(), bounds.getMinX(), bounds.getMinY() - 160);
        }
    }


    /**
     * 显示播放列表
     *
     * @author leck
     * @date 2026/05/05
     */
    public void showPlaylistView() {
        if (modalPane.getContent() != null) {
            updatePlaylistView();
            modalPane.setDisplay(true);
            return;
        }
        Parent parent = initPlaylistView();
        StackPane playlistPane = new StackPane(parent);
        playlistPane.setMaxWidth(250.0);
        modalPane.setAlignment(Pos.BOTTOM_RIGHT);
        modalPane.usePredefinedTransitionFactories(Side.RIGHT);
        modalPane.show(playlistPane);
        Platform.runLater(()->{
            musicListView.getSelectionModel().select(viewModel.getPlayingIndex());
            musicListView.scrollTo(viewModel.getPlayingIndex());
        });
    }


    /**
     * 初始化音量控制组件
     *
     * @return {@link Parent }
     * @author leck
     * @date 2026/05/05
     */
    private Parent initVolumePopup() {
        Slider volumeSlider = new Slider();
        volumeSlider.setSkin(new ProgressSliderSkin(volumeSlider));
        volumeSlider.valueProperty().bindBidirectional(viewModel.volumeProperty());
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.getStyleClass().add("popup-volume-slider");
        volumeSlider.setOrientation(Orientation.VERTICAL);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setMaxWidth(40);
        ToggleButton muteBtnInChild = new ToggleButton();
        muteBtnInChild.selectedProperty().bindBidirectional(viewModel.muteProperty());
        muteBtnInChild.getStyleClass().addAll("volume-mute-btn");
        muteBtnInChild.setGraphic(new Region());
        VBox volumePane = new VBox(muteBtnInChild, volumeSlider);
        VBox.setVgrow(volumePane, Priority.ALWAYS);
        volumePane.setPrefWidth(50);
        volumePane.setPrefHeight(150);
        volumePane.setSpacing(5);
        volumePane.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        volumePane.setAlignment(Pos.CENTER);
        volumePane.getStyleClass().add("volume-pane");
        volumePane.getStylesheets().addAll(root.getStylesheets());
        return new StackPane(volumePane);
    }


    /**
     * 初始化播放列表
     *
     * @return {@link Parent }
     * @author leck
     * @date 2026/05/05
     */
    private Parent initPlaylistView() {
        musicListView = new ListView<>(viewModel.getMusicList());
        musicListView.setPlaceholder(new Label("播放列表没有歌曲"));
        musicListView.setCellFactory(new Callback<ListView<Music>, ListCell<Music>>() {
            @Override
            public ListCell<Music> call(ListView<Music> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Music item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setText(item.getTitle());
                            setOnMouseClicked(event -> {
                                if (event.getClickCount() == 2) {
                                    viewModel.play(getIndex());
                                }
                            });
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        Card card = new Card();
        Text text = new Text("播放列表");
        text.setFont(Font.font(20));
        Button clearBtn = new Button(null, new FontIcon(Material2OutlinedAL.CLEAR_ALL));
        clearBtn.getStyleClass().add("clear-btn");
        clearBtn.setTooltip(new Tooltip("清空列表"));
        clearBtn.setOnAction(event -> {
            viewModel.clearPlayingList();
        });
        HBox header = new HBox(5.0, text, new Spacer(), clearBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        card.setHeader(header);
        card.setBody(musicListView);
        card.getStyleClass().add("playlist");
        return card;
    }


    /**
     * @return void
     * @description: 更新当前选中歌曲；这种实现有一个bug,那就是当播放列表显示时，无法及时更新index,对我来说时无关痛痒的bug
     * @name: updatePlaylistView
     * @author: Leck
     * @param:
     * @date: 2024/4/11
     */
    private void updatePlaylistView() {
        musicListView.getSelectionModel().select(viewModel.getPlayingIndex());
    }


    /**
     * 设置音乐文件夹
     *
     * @param event
     * @author leck
     * @date 2026/05/05
     */
    @FXML
    void chooseMusicDir(Event event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("设置音乐目录");
        File file = chooser.showDialog(root.getScene().getWindow());
        if (file != null) {
            viewModel.loadMusicListFromDir(file);
        }
    }
}
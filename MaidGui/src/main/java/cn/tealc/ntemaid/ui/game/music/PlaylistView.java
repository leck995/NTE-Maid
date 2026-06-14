package cn.tealc.ntemaid.ui.game.music;

import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.model.game.music.Playlist;
import cn.tealc.ntemaid.util.DialogBuilder;
import cn.tealc.ntemaid.util.TimeFormatUtil;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlaylistView implements FxmlView<PlaylistViewModel>, Initializable {
    private static final Logger log = LoggerFactory.getLogger(PlaylistView.class);
    @InjectViewModel
    private PlaylistViewModel viewModel;
    @FXML
    private TableColumn<Music, String> musicAlbumCol;
    @FXML
    private TableColumn<Music, String> musicArtistCol;
    @FXML
    private TableColumn<Music, Integer> musicDurationCol;
    @FXML
    private TableColumn<Music, Integer> musicIndexCol;
    @FXML
    private TableColumn<Music, String> musicTitleCol;
    @FXML
    private TableView<Music> musicTableView;
    @FXML
    private Button playSelectedPlaylistBtn;
    @FXML
    private AnchorPane root;
    @FXML
    private ListView<Playlist> sheetListview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sheetListview.setItems(viewModel.getPlaylists());
        sheetListview.setCellFactory(sheetDataListView -> new SheetCell());

        musicTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        musicArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        musicAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));

        // 2. 格式化时长列 (Integer 秒 -> 00:00)
        musicDurationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        musicDurationCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(TimeFormatUtil.formatToClock(item.doubleValue()));
                }
            }
        });

        musicIndexCol.setCellFactory(column -> new TableCell<Music, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    getStyleClass().remove("index-cell");
                } else {
                    int index = getTableRow().getIndex() + 1;
                    setText(String.valueOf(index));
                    // 添加样式类
                    getStyleClass().add("index-cell");
                }
            }
        });


        // 3. 设置表格行双击事件
        musicTableView.setRowFactory(tv -> {
            TableRow<Music> row = new TableRow<>();

            // 创建右键菜单
            ContextMenu contextMenu = new ContextMenu();
            MenuItem addToPlaylist = new MenuItem("添加到播放列表", new FontIcon(Material2OutlinedAL.ADD));
            addToPlaylist.setOnAction(event -> {
                Music selected = row.getItem();
                if (selected != null) {
                    viewModel.addToPlayingList(selected);
                }
            });
            // 菜单项：从库中删除 (新增)
            MenuItem deleteMusic = new MenuItem("移除", new FontIcon(Material2OutlinedAL.DELETE_OUTLINE));
            deleteMusic.getStyleClass().add("danger"); // 如果使用了 AtlantaFX，可以加上红色样式
            deleteMusic.setOnAction(event -> {
                Music selected = row.getItem();
                if (selected != null) {
                    JFXDialogLayout build = DialogBuilder.create()
                            .title("提示")
                            .message("确定移除这首歌吗？")
                            .button("确认", event1 -> viewModel.deleteMusicFromPlayList(selected))
                            .cancel("取消")
                            .build();
                    NotificationManager.dialog(build);
                }
            });
            Menu addToPlaylistGroup = new Menu("添加到歌单", new FontIcon(Material2OutlinedMZ.PLAYLIST_ADD));
            MenuItem tip = new MenuItem("加载中..");
            tip.setDisable(true);
            addToPlaylistGroup.getItems().add(tip);
            addToPlaylistGroup.setOnShowing(event -> {
                addToPlaylistGroup.getItems().clear(); // 先清空旧的列表
                Music selected = row.getItem();
                if (selected == null) return;
                // 从 ViewModel 获取所有歌单（假设你有一个获取歌单列表的方法）
                List<Playlist> playlists = viewModel.getAllPlaylists();
                if (playlists.isEmpty()) {
                    MenuItem tip2 = new MenuItem("暂无歌单");
                    tip2.setDisable(true);
                    addToPlaylistGroup.getItems().add(tip2);
                } else {
                    for (Playlist pl : playlists) {
                        MenuItem item = new MenuItem(pl.getName());
                        item.setOnAction(e -> {
                            viewModel.addMusicToPlaylist(selected, pl);
                        });
                        addToPlaylistGroup.getItems().add(item);
                    }
                }
            });
            contextMenu.getItems().addAll(
                    addToPlaylist,
                    addToPlaylistGroup, // 新增的二级菜单
                    new SeparatorMenuItem(),
                    deleteMusic
            );

            // 仅在行不为空时显示菜单
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            // 双击行播放
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Music rowData = row.getItem();
                    viewModel.playSelectedMusic(rowData);
                }
            });
            row.disableProperty().bind(row.itemProperty().isNull());
            return row;
        });
        musicTableView.setItems(viewModel.getMusicList());
    }

    @FXML
    void createPlaylistEvent(ActionEvent event) {
        JFXDialogLayout layout = new JFXDialogLayout();
        Label title = new Label("创建歌单");
        title.getStyleClass().add(Styles.TITLE_3);
        layout.setHeading(title);
        TextField nameField = new TextField();
        nameField.setPromptText("请输入歌单名称");
        layout.setBody(nameField);

        Button okBtn = new Button("创建");
        okBtn.getStyleClass().add(Styles.ACCENT);
        okBtn.setOnAction(event1 -> viewModel.createPlaylist(nameField.getText()));
        Button cancelBtn = new Button("取消");
        cancelBtn.setCancelButton(true);
        layout.setActions(okBtn, cancelBtn);
        NotificationManager.dialog(layout);
    }

    @FXML
    void playSelectedPlaylistEvent(ActionEvent event) {
        viewModel.playSelectedPlaylist();
    }

    @FXML
    void refreshMusicListEvent(ActionEvent event) {
        viewModel.refreshMusicListInPlaylist();
    }


    /**
     * 歌单Cell
     *
     * @author leck
     * @date 2026/05/08
     */
    class SheetCell extends ListCell<Playlist> {
        @Override
        protected void updateItem(Playlist sheetData, boolean b) {
            super.updateItem(sheetData, b);
            if (!b) {
                setDisable(false);
                setText(sheetData.getName());
                setContextMenu(createContextMenu());
                setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        viewModel.loadMusicListPlaylist(sheetData);
                    }
                });
            } else {
                setText(null);
                setDisable(true);
            }
        }

        private ContextMenu createContextMenu() {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem updateMI = new MenuItem("修改", new FontIcon(Material2OutlinedAL.EDIT));
            updateMI.setOnAction(actionEvent -> showAlterDialog());
            MenuItem deleteMI = new MenuItem("删除", new FontIcon(Material2OutlinedAL.DELETE_OUTLINE));
            deleteMI.setOnAction(actionEvent -> {
                viewModel.deletePlaylist(getItem());
            });
            contextMenu.getItems().addAll(updateMI, deleteMI);
            return contextMenu;
        }

        /**
         * 弹出修改歌单名称窗口
         *
         * @author leck
         * @date 2026/05/08
         */
        private void showAlterDialog() {
            JFXDialogLayout layout = new JFXDialogLayout();
            Label title = new Label("修改歌单");
            title.getStyleClass().add(Styles.TITLE_3);
            layout.setHeading(title);
            TextField nameField = new TextField(getItem().getName());
            layout.setBody(nameField);

            Button okBtn = new Button("修改");
            okBtn.getStyleClass().add(Styles.ACCENT);
            okBtn.setOnAction(event1 -> viewModel.updatePlaylist(getItem(), nameField.getText()));
            Button cancelBtn = new Button("取消");
            cancelBtn.setCancelButton(true);
            layout.setActions(okBtn, cancelBtn);
            NotificationManager.dialog(layout);
        }
    }

}

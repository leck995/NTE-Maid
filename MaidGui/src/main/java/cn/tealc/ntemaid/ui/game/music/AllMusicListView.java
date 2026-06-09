package cn.tealc.ntemaid.ui.game.music;

import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.model.game.music.Playlist;
import cn.tealc.ntemaid.util.DialogBuilder;
import cn.tealc.ntemaid.util.TimeFormatUtil;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AllMusicListView implements FxmlView<AllMusicListViewModel>, Initializable {
    @InjectViewModel
    private AllMusicListViewModel viewModel;
    @FXML
    private TableView<Music> allMusicTableView;
    @FXML
    private TableColumn<Music, String> allMusicAlbumCol;
    @FXML
    private TableColumn<Music, String> allMusicArtistCol;
    @FXML
    private TableColumn<Music, Integer> allMusicDurationCol;
    @FXML
    private TableColumn<Music, String> allMusicTitleCol;
    @FXML
    private TableColumn<Music, Integer> allMusicIndexCol;
    @FXML
    private Button playAllBtn;
    @FXML
    private Button clearAllBtn;
    @FXML
    private MenuButton addToPlaylistMenuBtn;
    @FXML
    private Button deleteSelectedBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 1. 设置列映射
        allMusicTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        allMusicArtistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        allMusicAlbumCol.setCellValueFactory(new PropertyValueFactory<>("album"));

        // 2. 格式化时长列 (Integer 秒 -> 00:00)
        allMusicDurationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        allMusicDurationCol.setCellFactory(column -> new TableCell<>() {
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

        allMusicIndexCol.setCellFactory(column -> new TableCell<Music, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    getStyleClass().remove("index-cell");
                } else {
                    int index = getTableRow().getIndex() + 1;
                    setText(String.valueOf(index));
                    getStyleClass().add("index-cell");
                }
            }
        });

        allMusicTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 3. 设置表格行双击事件和右键菜单
        allMusicTableView.setRowFactory(tv -> {
            TableRow<Music> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem addToPlaylist = new MenuItem("添加到播放列表", new FontIcon(Material2OutlinedAL.ADD));
            addToPlaylist.setOnAction(event -> {
                Music selected = row.getItem();
                if (selected != null) {
                    viewModel.addToPlayingList(selected);
                }
            });
            MenuItem deleteMusic = new MenuItem("从库中删除", new FontIcon(Material2OutlinedAL.DELETE_OUTLINE));
            deleteMusic.getStyleClass().add("danger");
            deleteMusic.setOnAction(event -> {
                Music selected = row.getItem();
                if (selected != null) {
                    JFXDialogLayout build = DialogBuilder.create()
                            .title("提示")
                            .message("确定移除这首歌吗？")
                            .button("确认", event1 -> viewModel.deleteMusicFromLibrary(selected))
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
                addToPlaylistGroup.getItems().clear();
                Music selected = row.getItem();
                if (selected == null) return;
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
                    addToPlaylistGroup,
                    new SeparatorMenuItem(),
                    deleteMusic
            );

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())
                        && allMusicTableView.getSelectionModel().getSelectedItems().size() <= 1) {
                    Music rowData = row.getItem();
                    viewModel.playSelectedMusic(rowData);
                }
            });
            row.disableProperty().bind(row.itemProperty().isNull());
            return row;
        });
        allMusicTableView.setItems(viewModel.getAllMusicList());

        // 4. 多选工具栏切换监听
        allMusicTableView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<Music>) change -> updateToolbarForSelection()
        );
        updateToolbarForSelection();

        // 5. 多选添加到歌单 MenuButton 弹窗
        addToPlaylistMenuBtn.setOnShowing(event -> {
            addToPlaylistMenuBtn.getItems().clear();
            List<Playlist> playlists = viewModel.getAllPlaylists();
            ObservableList<Music> selectedItems = allMusicTableView.getSelectionModel().getSelectedItems();
            if (playlists.isEmpty()) {
                MenuItem tip = new MenuItem("暂无歌单");
                tip.setDisable(true);
                addToPlaylistMenuBtn.getItems().add(tip);
            } else {
                for (Playlist pl : playlists) {
                    MenuItem item = new MenuItem(pl.getName());
                    item.setOnAction(e -> viewModel.addMusicToPlaylist(new ArrayList<>(selectedItems), pl));
                    addToPlaylistMenuBtn.getItems().add(item);
                }
            }
        });
    }

    private void updateToolbarForSelection() {
        ObservableList<Music> selected = allMusicTableView.getSelectionModel().getSelectedItems();
        boolean multiSelected = selected.size() > 1;

        playAllBtn.setVisible(!multiSelected);
        playAllBtn.setManaged(!multiSelected);
        clearAllBtn.setVisible(!multiSelected);
        clearAllBtn.setManaged(!multiSelected);
        addToPlaylistMenuBtn.setVisible(multiSelected);
        addToPlaylistMenuBtn.setManaged(multiSelected);
        deleteSelectedBtn.setVisible(multiSelected);
        deleteSelectedBtn.setManaged(multiSelected);
    }

    @FXML
    void importMusicEvent(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("设置音乐目录");
        File file = chooser.showDialog(allMusicTableView.getScene().getWindow());
        if (file != null) {
            viewModel.loadMusicListFromDir(file);
        }
    }

    @FXML
    void playAllEvent(ActionEvent event) {
        viewModel.playAll();
    }

    @FXML
    void deleteAllMusicEvent(ActionEvent event) {
        JFXDialogLayout build = DialogBuilder.create().
                title("提醒").
                message("确认删除全部歌曲吗？").
                button("确认", event1 -> {
                    viewModel.deleteAllMusicFromLibrary();
                })
                .cancel("取消")
                .build();
        NotificationManager.dialog(build);
    }

    @FXML
    void deleteSelectedEvent(ActionEvent event) {
        ObservableList<Music> selectedItems = allMusicTableView.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) return;

        List<Music> toDelete = new ArrayList<>(selectedItems);
        JFXDialogLayout build = DialogBuilder.create()
                .title("提醒")
                .message(String.format("确认删除选中的 %d 首歌曲吗？", toDelete.size()))
                .button("确认", event1 -> viewModel.deleteMusicFromLibrary(toDelete))
                .cancel("取消")
                .build();
        NotificationManager.dialog(build);
    }

    @FXML
    void refreshMusicListEvent(ActionEvent event) {
        viewModel.refreshMusicList();
    }

}

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
                    // 添加样式类
                    getStyleClass().add("index-cell");
                }
            }
        });


        // 3. 设置表格行双击事件
        allMusicTableView.setRowFactory(tv -> {
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
            MenuItem deleteMusic = new MenuItem("从库中删除", new FontIcon(Material2OutlinedAL.DELETE_OUTLINE));
            deleteMusic.getStyleClass().add("danger"); // 如果使用了 AtlantaFX，可以加上红色样式
            deleteMusic.setOnAction(event -> {
                Music selected = row.getItem();
                if (selected != null) {
                    // 确认对话框
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要从曲库中移除这首歌吗？\n文件不会被物理删除。");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            viewModel.deleteMusicFromLibrary(selected);
                        }
                    });
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
            return row;
        });
        allMusicTableView.setItems(viewModel.getAllMusicList());

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
}

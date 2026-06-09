package cn.tealc.ntemaid.ui.game.music;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.model.game.music.LrcBean;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.service.MusicService;
import cn.tealc.ntemaid.service.PlayingListService;
import cn.tealc.teafx.utils.message.MessageInfo;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class MusicPlayerViewModel implements ViewModel, SceneLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MusicPlayerViewModel.class);
    private final SimpleBooleanProperty desktopLrc;
    private final SimpleBooleanProperty disorder;
    private final SimpleBooleanProperty loop;
    private final SimpleBooleanProperty playing;
    private final SimpleBooleanProperty mute;
    private final SimpleBooleanProperty ready;
    private final SimpleStringProperty title;
    private final SimpleStringProperty artist;
    private final SimpleDoubleProperty currentTime;
    private final SimpleDoubleProperty totalTime;
    private final SimpleDoubleProperty volume;
    private final SimpleObjectProperty<ObservableList<LrcBean>> lrcBeans;
    private final ObjectProperty<Image> cover;
    private final BaseAudioPlayer player;
    private final SimpleIntegerProperty playingIndex;
    private final SimpleIntegerProperty lrcSelectedIndex;
    private final SimpleStringProperty musicDir;
    private ObservableList<Music> musicList;

    private final MusicService musicService;
    private final PlayingListService playingListService;

    @Inject
    public MusicPlayerViewModel(MusicService musicService,
                                 PlayingListService playingListService) {
        this.musicService = musicService;
        this.playingListService = playingListService;
        desktopLrc = new SimpleBooleanProperty();
        disorder = new SimpleBooleanProperty();
        loop = new SimpleBooleanProperty();
        playing = new SimpleBooleanProperty();
        mute = new SimpleBooleanProperty();
        ready = new SimpleBooleanProperty();
        title = new SimpleStringProperty();
        artist = new SimpleStringProperty();
        currentTime = new SimpleDoubleProperty();
        totalTime = new SimpleDoubleProperty();
        lrcBeans = new SimpleObjectProperty<>();
        cover = new SimpleObjectProperty<>();
        volume = new SimpleDoubleProperty();
        playingIndex = new SimpleIntegerProperty();
        musicList = FXCollections.observableArrayList();
        musicDir = new SimpleStringProperty();
        lrcSelectedIndex = new SimpleIntegerProperty();

        player = MusicPlayerClient.getInstance().getPlayer();
        desktopLrc.bindBidirectional(player.desktopLrcShowProperty());
        disorder.bindBidirectional(player.disorderProperty());
        loop.bindBidirectional(player.loopProperty());
        playing.bindBidirectional(player.playingProperty());
        mute.bindBidirectional(player.muteProperty());
        ready.bind(player.readyProperty());
        title.bindBidirectional(player.titleProperty());
        artist.bindBidirectional(player.artistProperty());
        currentTime.bind(player.currentTimeProperty());
        totalTime.bind(player.totalTimeProperty());
        cover.bindBidirectional(player.coverProperty());
        lrcBeans.bind(player.lrcBeansProperty());
        volume.bindBidirectional(player.volumeProperty());
        playingIndex.bindBidirectional(player.musicIndexProperty());
        musicList = player.getMusics();
        musicDir.bindBidirectional(Config.setting.musicDirProperty());
        lrcSelectedIndex.bindBidirectional(player.lrcSelectedIndexProperty());
    }


    /**
     * 修改后的加载逻辑：使用 Service 进行异步扫描和入库
     */
    @Deprecated
    public void loadMusicListFromDir(File dir) {
        if (dir == null || !dir.exists()) return;
        Task<Integer> scanTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return musicService.scanAndImportDirectory(dir.getAbsolutePath());
            }
        };

        scanTask.setOnSucceeded(event -> {
            int addedCount = scanTask.getValue();
            List<Music> allMusic = musicService.getAllMusic();
            if (musicList.isEmpty()){
                player.init(allMusic, 0);
                playingListService.updatePlayingListAsync(allMusic);
            }
            NotificationManager.message(MessageInfo.success(
                    String.format("同步完成：新增 %d 首，曲库共 %d 首歌曲", addedCount, allMusic.size())));
            musicDir.set(dir.getAbsolutePath());
        });

        scanTask.setOnFailed(event -> {
            Throwable e = scanTask.getException();
            log.error("加载音乐目录失败", e);
            NotificationManager.message(MessageInfo.error("加载音乐目录失败: " + e.getMessage()));
        });
        Thread.startVirtualThread(scanTask);
    }


    public void clearPlayingList() {
        player.clearPlayingList();
    }


    public void next() {
        player.next();
    }

    public void pre() {
        player.pre();
    }

    public void play() {
        player.playOrPause();
    }

    public void play(int index) {
        player.play(index);
    }

    public void release() {
        player.release();
    }

    public void seek(Double time) {
        player.seek(Duration.seconds(time));
    }

    public boolean isDesktopLrc() {
        return desktopLrc.get();
    }

    public SimpleBooleanProperty desktopLrcProperty() {
        return desktopLrc;
    }

    public void setDesktopLrc(boolean desktopLrc) {
        this.desktopLrc.set(desktopLrc);
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getArtist() {
        return artist.get();
    }

    public SimpleStringProperty artistProperty() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public double getCurrentTime() {
        return currentTime.get();
    }

    public SimpleDoubleProperty currentTimeProperty() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime.set(currentTime);
    }

    public double getTotalTime() {
        return totalTime.get();
    }

    public SimpleDoubleProperty totalTimeProperty() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime.set(totalTime);
    }

    public ObservableList<LrcBean> getLrcBeans() {
        return lrcBeans.get();
    }

    public SimpleObjectProperty<ObservableList<LrcBean>> lrcBeansProperty() {
        return lrcBeans;
    }

    public void setLrcBeans(ObservableList<LrcBean> lrcBeans) {
        this.lrcBeans.set(lrcBeans);
    }

    public Image getCover() {
        return cover.get();
    }

    public ObjectProperty<Image> coverProperty() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover.set(cover);
    }

    public boolean isDisorder() {
        return disorder.get();
    }

    public SimpleBooleanProperty disorderProperty() {
        return disorder;
    }

    public void setDisorder(boolean disorder) {
        this.disorder.set(disorder);
    }

    public boolean isLoop() {
        return loop.get();
    }

    public SimpleBooleanProperty loopProperty() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop.set(loop);
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public SimpleBooleanProperty playingProperty() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public boolean isMute() {
        return mute.get();
    }

    public SimpleBooleanProperty muteProperty() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute.set(mute);
    }

    public boolean isReady() {
        return ready.get();
    }

    public SimpleBooleanProperty readyProperty() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready.set(ready);
    }

    public double getVolume() {
        return volume.get();
    }

    public SimpleDoubleProperty volumeProperty() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume.set(volume);
    }

    public ObservableList<Music> getMusicList() {
        return musicList;
    }

    public int getPlayingIndex() {
        return playingIndex.get();
    }

    public SimpleIntegerProperty playingIndexProperty() {
        return playingIndex;
    }

    public String getMusicDir() {
        return musicDir.get();
    }

    public SimpleStringProperty musicDirProperty() {
        return musicDir;
    }

    public int getLrcSelectedIndex() {
        return lrcSelectedIndex.get();
    }

    public SimpleIntegerProperty lrcSelectedIndexProperty() {
        return lrcSelectedIndex;
    }


    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {
        //release();
    }
}

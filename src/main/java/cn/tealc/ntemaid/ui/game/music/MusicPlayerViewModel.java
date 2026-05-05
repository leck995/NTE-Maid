package cn.tealc.ntemaid.ui.game.music;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.model.game.music.LrcBean;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    public MusicPlayerViewModel() {
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


    public void loadMusicListFromDir(File dir) {
        Path rootPath = dir.toPath();
        try (Stream<Path> stream = Files.walk(rootPath)) {
            // 2. 筛选并转换
            List<Music> playlist = stream
                    .filter(Files::isRegularFile) // 只处理文件
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".mp3") || name.endsWith(".wav");
                    })
                    .map(path -> {
                        // 3. 映射为 Music 对象
                        String absolutePath = path.toAbsolutePath().toString();
                        String fileName = path.getFileName().toString();
                        return new Music(absolutePath, fileName);
                    })
                    .toList();
            player.init(playlist, 0);
            musicDir.set(rootPath.toString());
            NotificationManager.message(MessageInfo.success(String.format("成功添加 %d 首歌曲", playlist.size())));
        } catch (IOException e) {
            log.debug("加载新歌单错误，｛｝", e);
        }
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

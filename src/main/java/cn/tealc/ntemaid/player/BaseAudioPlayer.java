package cn.tealc.ntemaid.player;


import cn.tealc.ntemaid.model.game.music.LrcBean;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.thread.system.music.LrcSeekService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;

/**
 * @description:
 * @author: Leck
 * @create: 2024-09-15 22:02
 */
public abstract class BaseAudioPlayer {
    protected BooleanProperty playing;
    protected BooleanProperty disorder;
    protected BooleanProperty loop;
    protected BooleanProperty isStar;
    protected BooleanProperty mute;
    protected BooleanProperty desktopLrcShow;
    protected SimpleDoubleProperty currentTime;
    protected SimpleDoubleProperty totalTime;
    protected SimpleDoubleProperty bufferedTime;
    protected ObjectProperty<Image> cover;
    protected DoubleProperty volume;
    protected StringProperty title;
    protected StringProperty artist;
    protected ObservableList<Music> musics;//播放列表
    protected ObjectProperty<ObservableList<LrcBean>> lrcBeans;//歌词列
    protected SimpleIntegerProperty lrcSelectedIndex;
    protected SimpleStringProperty lrcSelectedText;
    protected SimpleObjectProperty<Music> playingMusic;
    protected SimpleBooleanProperty ready;
    protected SimpleIntegerProperty musicIndex;
    protected LrcSeekService lrcSeekService;

    public BaseAudioPlayer() {
        playingMusic = new SimpleObjectProperty<>();
        playing = new SimpleBooleanProperty(false);//
        disorder = new SimpleBooleanProperty(false); //
        loop = new SimpleBooleanProperty(false);
        isStar = new SimpleBooleanProperty(false);
        mute = new SimpleBooleanProperty(false);//
        cover = new SimpleObjectProperty<Image>(new Image(this.getClass().getResource("/cn/tealc/ntemaid/image/album.jpg").toExternalForm()));
        title = new SimpleStringProperty("音乐随心");
        artist = new SimpleStringProperty("Pure");
        totalTime = new SimpleDoubleProperty(0.0);
        currentTime = new SimpleDoubleProperty(0.0);
        bufferedTime = new SimpleDoubleProperty(0.0);
        volume = new SimpleDoubleProperty(1.0);
        musics = FXCollections.observableArrayList();
        lrcBeans = new SimpleObjectProperty<>(FXCollections.observableArrayList());
        lrcSelectedIndex = new SimpleIntegerProperty(-1);
        lrcSelectedText = new SimpleStringProperty("无台词");
        desktopLrcShow = new SimpleBooleanProperty(false);
        ready = new SimpleBooleanProperty(false);
        musicIndex = new SimpleIntegerProperty(-1);
        lrcSeekService = new LrcSeekService();
    }


    public abstract void init(List<Music> musics);

    public abstract void init(List<Music> musics, int index);

    public abstract void initAndPlay(List<Music> musics, int index);

    public abstract void initAndPlay(List<Music> musics, int index, boolean autoPlay, Duration time);

    public abstract void add(Music music);

    public abstract void add(List<Music> musics);

    public abstract void addAndPlay(List<Music> musics, int index);

    public abstract void addAndPlay(Music music);

    public abstract void play();

    public abstract void play(int index);

    public abstract void play(int index, Duration time);

    public abstract void play(int index, boolean autoPlay, Duration time);

    public abstract void playOrPause();
    public abstract void playOrPauseWithFade();
    public abstract void pause();
    public abstract void pauseWithFadeOut();

    public abstract void playWithFadeIn();
    public abstract void pre();

    public abstract void next();

    public abstract void seek(Duration time);

    public abstract void dispose();

    public abstract void skipTime(Duration time);

    public abstract void release();

    public abstract void removeMusic(Music music);

    public abstract void removeMusic(int index);

    public abstract void clearPlayingList();

    public abstract int getPlayingIndexInList();

    public boolean isPlaying() {
        return playing.get();
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public boolean isDisorder() {
        return disorder.get();
    }

    public BooleanProperty disorderProperty() {
        return disorder;
    }

    public boolean isLoop() {
        return loop.get();
    }

    public BooleanProperty loopProperty() {
        return loop;
    }

    public boolean isIsStar() {
        return isStar.get();
    }

    public BooleanProperty isStarProperty() {
        return isStar;
    }

    public boolean isMute() {
        return mute.get();
    }

    public BooleanProperty muteProperty() {
        return mute;
    }

    public boolean isDesktopLrcShow() {
        return desktopLrcShow.get();
    }

    public BooleanProperty desktopLrcShowProperty() {
        return desktopLrcShow;
    }

    public double getCurrentTime() {
        return currentTime.get();
    }

    public SimpleDoubleProperty currentTimeProperty() {
        return currentTime;
    }

    public double getTotalTime() {
        return totalTime.get();
    }

    public SimpleDoubleProperty totalTimeProperty() {
        return totalTime;
    }

    public double getBufferedTime() {
        return bufferedTime.get();
    }

    public SimpleDoubleProperty bufferedTimeProperty() {
        return bufferedTime;
    }

    public Image getCover() {
        return cover.get();
    }

    public ObjectProperty<Image> coverProperty() {
        return cover;
    }

    public double getVolume() {
        return volume.get();
    }

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume.set(volume);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getArtist() {
        return artist.get();
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public ObservableList<Music> getMusics() {
        return musics;
    }

    public ObservableList<LrcBean> getLrcBeans() {
        return lrcBeans.get();
    }

    public ObjectProperty<ObservableList<LrcBean>> lrcBeansProperty() {
        return lrcBeans;
    }

    public int getLrcSelectedIndex() {
        return lrcSelectedIndex.get();
    }

    public SimpleIntegerProperty lrcSelectedIndexProperty() {
        return lrcSelectedIndex;
    }

    public String getLrcSelectedText() {
        return lrcSelectedText.get();
    }

    public SimpleStringProperty lrcSelectedTextProperty() {
        return lrcSelectedText;
    }

    public Music getPlayingMusic() {
        return playingMusic.get();
    }

    public SimpleObjectProperty<Music> playingMusicProperty() {
        return playingMusic;
    }

    public boolean isReady() {
        return ready.get();
    }

    public SimpleBooleanProperty readyProperty() {
        return ready;
    }

    public int getMusicIndex() {
        return musicIndex.get();
    }

    public SimpleIntegerProperty musicIndexProperty() {
        return musicIndex;
    }
}
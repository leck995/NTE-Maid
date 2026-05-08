package cn.tealc.ntemaid.player;


import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.music.LrcBean;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.teafx.utils.message.MessageInfo;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @program: AmsrPlayer-old
 * @description:
 * @author: Leck
 * @create: 2023-02-06 22:25
 */
public class FxMediaPlayer extends BaseAudioPlayer {
    private static final Logger LOG = LoggerFactory.getLogger(FxMediaPlayer.class);
    private MediaPlayer mediaPlayer;
    private Timeline fadeTimeline; // 抽取为成员变量，防止多个渐变冲突
    private static final Duration FADE_DURATION = Duration.millis(1000);
    private int lastLrcIndex = -1;

    private final Image defaultCover = new Image(this.getClass().getResource("/cn/tealc/ntemaid/image/album.jpg").toExternalForm());
    public FxMediaPlayer() {
        super();
        playing.addListener((observableValue, aBoolean, t1) -> {
            if (mediaPlayer == null) return;
            if (t1) {
                mediaPlayer.play();
            } else {
                mediaPlayer.pause();
            }
        });
        disorder.addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                Collections.shuffle(musics);
                musicIndex.set(musics.indexOf(playingMusic.get()));
            } else {
                musics.sort(Comparator.comparing(Music::getTitle));
                musicIndex.set(musics.indexOf(playingMusic.get()));
            }
        });

        volume.bindBidirectional(Config.setting.musicVolumeProperty());
        volume.addListener((observable, oldValue, newValue) -> {
            mute.set(newValue.doubleValue() == 0);
        });

        lrcSeekService.valueProperty().addListener((observableValue, lrcBeans1, t1) -> {
            if (t1 != null) {
                lrcBeans.set(FXCollections.observableArrayList(t1));
            } else {
                lrcBeans.get().clear();
                lrcSelectedText.set("当前无歌词");
            }
        });

        playingMusic.addListener((observableValue, media1, t1) -> {
            if (t1 != null) {
                lrcSeekService.init(getPlayingMusic());
                Platform.runLater(()->{
                    lrcSeekService.restart();
                });
            }
        });


        //对歌词进行选择
        currentTime.addListener((observableValue, number, t1) -> {
            if (lrcBeans == null || t1 == null || lrcBeans.get().isEmpty()) return;

            List<LrcBean> beans = lrcBeans.get();
            long millis = (long) (t1.doubleValue() * 1000) + 500; // 500ms 偏移
            int size = beans.size();
            int newIndex = -1;

            // 1. 边界处理：第一行之前
            if (millis < beans.get(0).getLongTime()) {
                newIndex = 0;
            }
            // 2. 边界处理：最后一行之后
            else if (millis >= beans.get(size - 1).getLongTime()) {
                newIndex = size - 1;
            }
            // 3. 中间部分查找
            else {
                // 性能优化：优先检查是否还在当前行或下一行（大部分情况）
                if (lastLrcIndex >= 0 && lastLrcIndex < size - 1) {
                    if (millis >= beans.get(lastLrcIndex).getLongTime() && millis < beans.get(lastLrcIndex + 1).getLongTime()) {
                        newIndex = lastLrcIndex;
                    }
                }

                // 如果不在当前行，再进行二分查找或普通查找
                if (newIndex == -1) {
                    for (int i = 0; i < size - 1; i++) {
                        if (millis >= beans.get(i).getLongTime() && millis < beans.get(i + 1).getLongTime()) {
                            newIndex = i;
                            break;
                        }
                    }
                }
            }

            // 4. 只有当索引发生变化时，才更新 UI 属性
            if (newIndex != -1 && newIndex != lastLrcIndex) {
                lastLrcIndex = newIndex;
                lrcSelectedIndex.set(newIndex);
                LrcBean currentBean = beans.get(newIndex);
                String text = currentBean.getRowText();
                if (currentBean.getTransText() != null) {
                    text += "\n" + currentBean.getTransText();
                }
                lrcSelectedText.set(text);
            }
        });
    }


    @Override
    public void init(List<Music> musics) {
        this.musics.setAll(musics);
    }

    @Override
    public void init(List<Music> musics, int index) {
        init(musics);
        load(musics.get(index), false);
    }

    @Override
    public void initAndPlay(List<Music> musics, int index) {
        init(musics);
        musicIndex.set(index);
        load(musics.get(index), true);
    }

    @Override
    public void initAndPlay(List<Music> musics, int index, boolean autoPlay, Duration time) {
        init(musics);
        musicIndex.set(index);
        load(musics.get(index), autoPlay, time);
    }

    @Override
    public void add(Music music) {
        add(List.of(music));
    }

    @Override
    public void add(List<Music> musics) {
        List<Music> list = musics.stream().filter(music -> !this.musics.contains(music)).toList();
        this.musics.addAll(list);
    }

    @Override
    public void addAndPlay(List<Music> musics, int index) {
        Music music = musics.get(index);
        add(musics);
        int newIndex = musics.indexOf(music);
        musicIndex.set(index);
        load(musics.get(newIndex), true, Duration.ZERO);
    }

    @Override
    public void addAndPlay(Music music) {
        add(music);
        int newIndex = musics.indexOf(music);
        musicIndex.set(newIndex);
        load(musics.get(newIndex), true, Duration.ZERO);
    }

    @Override
    public void play(int index) {
        play(index, true, Duration.ZERO);
    }

    @Override
    public void play(int index, Duration time) {
        play(index, true, time);
    }

    @Override
    public void play(int index, boolean autoPlay, Duration time) {
        musicIndex.set(index);
        load(musics.get(index), autoPlay, time);
    }


    private void load(Music music, boolean autoPlay) {
        musicIndex.set(musics.indexOf(music));
        load(music, autoPlay, Duration.ZERO);
    }

    private void load(Music music, boolean autoPlay, Duration time) {
        playingMusic.set(music);
        Media media;
        try {
            media = new Media(music.getUri().toString());
            if (mediaPlayer != null)
                mediaPlayer.dispose();
            mediaPlayer = new MediaPlayer(media);
        } catch (MediaException mediaException) {
            if (mediaException.getType() == MediaException.Type.MEDIA_UNSUPPORTED) {
                LOG.info("目前软件不支持该音频类型，不支持压缩的wav格式{}", music.getTitle());
                NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning("不支持压缩的wav格式,请使用VLC内核进行播放"));
            } else if (mediaException.getType() == MediaException.Type.MEDIA_UNAVAILABLE) {
                LOG.info("找不到指定文件");
                NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning("找不到指定文件"));
            } else {
                NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.error( "播放器未知错误"));
                LOG.error("播放器未知错误", mediaException);
            }
            return;
        }
        System.gc();
        //歌曲加载完成
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                String tempTitle = (String) media.getMetadata().get("title");
                String tempArtist = (String) media.getMetadata().get("artist");
                Image coverImage = (Image) media.getMetadata().get("image");
                title.set(tempTitle != null ? tempTitle : music.getTitle());
                artist.set(tempArtist);
                if (coverImage == null){
                    cover.set(defaultCover);
                }else {
                    cover.set(coverImage);
                }


                totalTime.set(mediaPlayer.getTotalDuration().toSeconds());
                mediaPlayer.muteProperty().bind(mute);
                mediaPlayer.volumeProperty().bind(volume);
                mediaPlayer.currentTimeProperty().addListener((observableValue, duration, t1) -> {
                    currentTime.set(t1.toSeconds());
                });

                mediaPlayer.bufferProgressTimeProperty().addListener((observableValue, duration, t1) -> {
                    bufferedTime.set(t1.toSeconds() / totalTime.get() * 100);
                });
                seek(time);
                if (autoPlay) {
                    play();

                } else {
                    pause();
                }
                ready.set(true);
            }

        });

        //歌曲播放结束
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (loop.get()) {
                    mediaPlayer.stop();
                    mediaPlayer.play();
                } else {
                    next();
                }
            }
        });
    }

    @Override
    public void pre() {
        if (musics.isEmpty()) return;
        if (musicIndex.get() == 0) {
            musicIndex.set(musics.size() - 1);
        } else {
            musicIndex.set(musicIndex.get() - 1);
        }
        play(musicIndex.get());
    }

    @Override
    public void next() {
        if (musics.isEmpty()) return;
        if (musicIndex.get() == musics.size() - 1) {
            play(0);
            musicIndex.set(0);
        } else {
            musicIndex.set(musicIndex.get() + 1);
            play(musicIndex.get());
        }
    }


    @Override
    public void seek(Duration time) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(time);
            currentTime.set(time.toSeconds());
        }
    }


    @Override
    public void removeMusic(Music music) {
        int i = musics.indexOf(music);
        removeMusic(i);
    }

    @Override
    public void removeMusic(int index) {
        if (musics.size() == 1) {
            clearPlayingList();
        } else {
            if (index == musicIndex.get()) {
                next();
                musicIndex.set(musicIndex.get() - 1);
            } else if (index < musicIndex.get()) {
                musicIndex.set(musicIndex.get() - 1);
            }
            musics.remove(index);
        }
    }


    public boolean ready() {
        if (mediaPlayer != null && mediaPlayer.getMedia() != null)
            return true;
        else
            return false;
    }


    /**
     * @return void
     * @description: 设置播放取消
     * @name: setDispose
     * @author: Leck
     * @param:
     * @date: 2023/2/24
     */
    @Override
    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        playing.set(false);
        currentTime.set(0.0);
        cover.set(defaultCover);
        title.set("音乐随心");
        artist.set("Pure");
        totalTime.set(0);
        playingMusic.set(null);
        //currentTime.set(new Duration(0.0));
        ready.set(false);
        lastLrcIndex = -1;
        lrcSelectedIndex.set(-1);
        lrcSelectedText.set("");
    }

    @Override
    public void clearPlayingList() {
        dispose();
        musics.clear();
        lrcBeans.get().clear();
    }

    @Override
    public int getPlayingIndexInList() {
        return musicIndex.get();
    }



    @Override
    public void skipTime(Duration time) {
        mediaPlayer.seek(time.add(Duration.seconds(currentTime.get())));
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }


    @Override
    public void playOrPause() {
        if (ready()) {
            if (playing.get()) {
                pause();
            } else
                play();
        }
    }

    @Override
    public void playOrPauseWithFade() {
        if (ready()) {
            if (playing.get()) {
                pauseWithFadeOut();
            } else
                playWithFadeIn();
        }
    }


    @Override
    public void pause() {
        if (ready()) {
            mediaPlayer.pause();
            playing.set(false);
        }

    }

    @Override
    public void playWithFadeIn() {
        if (!ready() || isPlaying()) return;
        System.out.println("playWithFadeIn1:" + volume.get());
        if (fadeTimeline != null) fadeTimeline.stop();

        mediaPlayer.volumeProperty().unbind();
        mediaPlayer.setVolume(0.0);
        mediaPlayer.play();
        playing.set(true);
        fadeTimeline = new Timeline(
                new KeyFrame(FADE_DURATION,
                        new KeyValue(mediaPlayer.volumeProperty(), volume.get()) // 渐变到用户设置的当前音量值
                )
        );
        System.out.println("playWithFadeIn2:" + volume.get());
        fadeTimeline.setOnFinished(e -> {
            System.out.println("playWithFadeIn3:" + volume.get());
            mediaPlayer.setVolume(volume.get());
            mediaPlayer.volumeProperty().bind(volume);
        });

        fadeTimeline.play();
    }
/*    @Override
    public void playWithFadeIn() {
        if (!ready()) return;
        if (fadeTimeline != null) fadeTimeline.stop();
        double v = volume.get();
        mediaPlayer.volumeProperty().unbind(); // 必须解绑，否则无法手动控制音量
        mediaPlayer.setVolume(0.0);
        mediaPlayer.play();
        playing.set(true);
        fadeTimeline = new Timeline(
                new KeyFrame(FADE_DURATION,
                        new KeyValue(mediaPlayer.volumeProperty(), v) // 渐变到用户设置的当前音量值
                )
        );

        fadeTimeline.setOnFinished(e -> {
            mediaPlayer.volumeProperty().bindBidirectional(volume);
        });

        fadeTimeline.play();
    }*/

    @Override
    public void pauseWithFadeOut() {
        if (!ready() || !playing.get()) return;
        System.out.println("pauseWithFadeOut1:"+volume.get());
        if (fadeTimeline != null) fadeTimeline.stop();
        mediaPlayer.volumeProperty().unbind();
        //mediaPlayer.volumeProperty().unbindBidirectional(volume);
        System.out.println("pauseWithFadeOut2:"+volume.get());
        // 创建渐出动画
        fadeTimeline = new Timeline(
                new KeyFrame(FADE_DURATION,
                        new KeyValue(mediaPlayer.volumeProperty(), 0.0) // 渐变到 0
                )
        );

        fadeTimeline.setOnFinished(e -> {
            mediaPlayer.pause();
            playing.set(false); // 彻底静音后执行暂停
            // 恢复音量值并重新绑定，确保下次点击 play 时不是静音
            System.out.println("pauseWithFadeOut3:"+volume.get());
            mediaPlayer.setVolume(volume.get());
            mediaPlayer.volumeProperty().bind(volume);
        });

        fadeTimeline.play();
    }
 /*   @Override
    public void pauseWithFadeOut() {
        if (!ready() || !playing.get()) return;

        if (fadeTimeline != null) fadeTimeline.stop();
        double v = volume.get();
        mediaPlayer.volumeProperty().unbind();

        // 创建渐出动画
        fadeTimeline = new Timeline(
                new KeyFrame(FADE_DURATION,
                        new KeyValue(mediaPlayer.volumeProperty(), 0.0) // 渐变到 0
                )
        );

        fadeTimeline.setOnFinished(e -> {
            mediaPlayer.pause();
            playing.set(false); // 彻底静音后执行暂停
            // 恢复音量值并重新绑定，确保下次点击 play 时不是静音
            mediaPlayer.setVolume(v);
            mediaPlayer.volumeProperty().bindBidirectional(volume);
        });

        fadeTimeline.play();
    }*/
    // 注意：建议在普通的 play() 和 pause() 中也加入对 fadeTimeline 的清理
    @Override
    public void play() {
        if (ready()) {
            if (fadeTimeline != null) fadeTimeline.stop();
            // 确保普通播放时音量是正常的
            if (!mediaPlayer.volumeProperty().isBound()) {
                mediaPlayer.volumeProperty().bindBidirectional(volume);
            }
            mediaPlayer.play();
            playing.set(true);
        }
    }

//    @Override
//    public void play() {
//        if (ready()) {
//            mediaPlayer.play();
//            playing.set(true);
//        }
//    }


}



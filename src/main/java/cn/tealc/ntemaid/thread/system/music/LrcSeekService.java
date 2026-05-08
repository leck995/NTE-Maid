package cn.tealc.ntemaid.thread.system.music;

import cn.tealc.ntemaid.model.game.music.LrcBean;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.util.LrcFormatUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;

/**
 * @program: AsmrPlayer-web
 * @description:
 * @author: Leck
 * @create: 2023-11-20 18:36
 */
public class LrcSeekService extends Service<List<LrcBean>> {
    private Music music;
    private String musicPath;
    private final String[] extensions = {".lrc", ".LRC"};
    public void init(Music music) {
        this.music = music;
        this.musicPath = music.getFilePath();

    }

    @Override
    protected Task<List<LrcBean>> createTask() {
        return new Task<List<LrcBean>>() {
            @Override
            protected List<LrcBean> call() throws Exception {
                List<LrcBean> lrc = getLrc();
                if (lrc != null && !lrc.isEmpty()) {
                    LrcBean last = lrc.getLast();
                    for (int i = 1; i <= 3; i++) {
                        LrcBean lrcBean = new LrcBean(last.getLongTime() + 1000 * i, "","             ");
                        lrcBean.setEmpty(true);
                        lrc.add(lrcBean);

                    }
                }

                return lrc;
            }
        };
    }

    private List<LrcBean> getLrc() {
        String lrcPath = filepath();
        for (String ext : extensions) {
            File file = new File(lrcPath + ext);
            if (file.exists()) {
                return LrcFormatUtil.getLrcListFromFile(file);
            }
        }
        return null;
    }

    private String filepath() {
        return musicPath.substring(0, musicPath.lastIndexOf("."));
    }

    private String filename() {
        return musicPath.substring(musicPath.lastIndexOf(File.separator) + 1, musicPath.lastIndexOf("."));
    }
}
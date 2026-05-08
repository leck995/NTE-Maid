package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.MusicDao;
import cn.tealc.ntemaid.model.game.music.Music;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MusicService {
    private static final Logger log = LoggerFactory.getLogger(MusicService.class);
    private final MusicDao musicDao = new MusicDao();

    /**
     * 递归扫描目录及子目录下的所有音乐文件
     */
    public int scanAndImportDirectory(String folderPath) {
        Path rootPath = Paths.get(folderPath);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            log.error("扫描失败：无效路径 {}", folderPath);
            return 0;
        }

        List<Music> pendingList = new ArrayList<>();

        // 使用 Files.walk 自动递归遍历子目录
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile) // 只处理文件
                    .filter(this::isSupportedAudioFile) // 过滤后缀名
                    .forEach(path -> {
                        // 解析每一个发现的音乐文件
                        pendingList.add(parseMetadata(path.toFile()));
                    });
        } catch (IOException e) {
            log.error("遍历目录异常", e);
            return 0;
        }

        if (pendingList.isEmpty()) {
            log.info("未发现可识别的音乐文件");
            return 0;
        }

        int addedCount = musicDao.addMusicBatch(pendingList);
        log.info("扫描任务结束。共解析 {} 个文件，成功入库 {} 首新歌", pendingList.size(), addedCount);
        return addedCount;
    }

    /**
     * 检查文件后缀是否支持
     */
    private boolean isSupportedAudioFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav");
    }

    /**
     * 解析元数据（保持不变，但增加了一些稳定性处理）
     */
    public Music parseMetadata(File file) {
        Music music = new Music();
        String fileName = file.getName();
        String titleBase = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;

        music.setTitle(titleBase);
        music.setFilePath(file.getAbsolutePath());
        music.setArtist("未知艺术家");
        music.setAlbum("未知专辑");
        music.setDuration(0);
        music.setAddTime(System.currentTimeMillis());

        try {
            // Jaudiotagger 解析
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            if (tag != null) {
                String title = tag.getFirst(FieldKey.TITLE);
                if (isNotBlank(title)) music.setTitle(title);

                String artist = tag.getFirst(FieldKey.ARTIST);
                if (isNotBlank(artist)) music.setArtist(artist);

                String album = tag.getFirst(FieldKey.ALBUM);
                if (isNotBlank(album)) music.setAlbum(album);
            }
            // 获取时长
            music.setDuration(audioFile.getAudioHeader().getTrackLength());
        } catch (Exception e) {
            // 解析失败不抛异常，保证后续文件能继续扫描
            log.warn("无法解析元数据，使用默认信息: {}", file.getName());
        }
        return music;
    }


    public List<Music> getAllMusic() {
        return musicDao.getAllMusic();
    }

    public boolean deleteMusic(int id) {
        return musicDao.deleteMusic(id);
    }

    public boolean deleteAllMusic() {
        return musicDao.deleteAllMusic();
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
package cn.tealc.ntemaid.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 图片缓存管理器（Guice 单例）。
 * <p>
 * 两级缓存：内存（5 分钟未访问自动释放）→ 磁盘（resources/cache/image）。
 * 缓存未命中时自动从 URL 后台加载并异步下载到磁盘。
 */
@Singleton
public class ImageCacheManager {
    private static final Logger LOG = LoggerFactory.getLogger(ImageCacheManager.class);

    private static final Duration EXPIRE_AFTER_ACCESS = Duration.ofMinutes(5);
    private static final Path CACHE_DIR = Path.of("resources", "cache", "image");
    private static final long CLEANUP_INTERVAL_SECONDS = 60;

    private final ConcurrentHashMap<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner;

    @Inject
    public ImageCacheManager() {
        cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "image-cache-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleWithFixedDelay(this::evictExpired, CLEANUP_INTERVAL_SECONDS,
                CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 获取图片。缓存未命中时会后台加载并异步下载到磁盘。
     */
    public Image get(String url, double width, double height, boolean preserveRatio, boolean smooth) {
        String key = urlToKey(url);

        CacheEntry entry = memoryCache.get(key);
        if (entry != null) {
            entry.lastAccess = Instant.now();
            return entry.image;
        }

        Path file = CACHE_DIR.resolve(key);
        if (Files.exists(file)) {
            Image img = new Image(file.toUri().toString(), width, height, preserveRatio, smooth, true);
            memoryCache.put(key, new CacheEntry(img));
            return img;
        }

        downloadAsync(url, file);
        Image img = new Image(url, width, height, preserveRatio, smooth, true);
        memoryCache.put(key, new CacheEntry(img));
        return img;
    }

    public Image get(String url) {
        return get(url, 0, 0, true, true);
    }

    public Image get(String url, double width, double height) {
        return get(url, width, height, true, true);
    }

    public int memorySize() {
        return memoryCache.size();
    }

    public void clearMemory() {
        memoryCache.clear();
    }

    private void downloadAsync(String url, Path target) {
        Thread.ofVirtual().start(() -> {
            try {
                Files.createDirectories(CACHE_DIR);
                URI uri = URI.create(url);
                try (InputStream in = uri.toURL().openStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                LOG.debug("图片下载完成: {} -> {}", url, target);
            } catch (IOException e) {
                LOG.warn("图片下载失败: {}", url, e.getMessage());
                try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            }
        });
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(EXPIRE_AFTER_ACCESS);
        Iterator<Map.Entry<String, CacheEntry>> it = memoryCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CacheEntry> e = it.next();
            if (e.getValue().lastAccess.isBefore(cutoff)) {
                it.remove();
            }
        }
    }

    private static String urlToKey(String url) {
        String path = url;
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        String name = path.substring(path.lastIndexOf('/') + 1).toLowerCase();
        if (name.isEmpty()) {
            name = Integer.toHexString(url.hashCode());
        }
        if (!name.contains(".")) {
            name = name + ".png";
        }
        return name;
    }

    private static class CacheEntry {
        final Image image;
        volatile Instant lastAccess;

        CacheEntry(Image image) {
            this.image = image;
            this.lastAccess = Instant.now();
        }
    }
}

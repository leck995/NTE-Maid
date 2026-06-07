package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.jna.Win32KeySender;
import cn.tealc.ntemaid.jna.WindowClientSizeUtil;
import cn.tealc.ntemaid.thread.game.log.LogMonitorForMusicTask;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class OtherEvent implements Consumer<LogMonitorForMusicTask.Event> {
    private static final Logger log = LoggerFactory.getLogger(OtherEvent.class);

    private final Win32KeySender win32KeySender;
    private final Map<String, Point2D> screenMap = new HashMap<>();


    public OtherEvent() {
        win32KeySender = new Win32KeySender();
        screenMap.put("2560*1440", new Point2D(140, 540));
        screenMap.put("1920*1080", new Point2D(140, 410));
        screenMap.put("2560*1080", new Point2D(140, 410));
        screenMap.put("1600*900", new Point2D(90, 340));
        screenMap.put("1792*768", new Point2D(80, 290));
        screenMap.put("1280*720", new Point2D(80, 280));
    }

    @Override
    public void accept(LogMonitorForMusicTask.Event event) {
        if (Objects.requireNonNull(event) == LogMonitorForMusicTask.Event.OPEN_ADVENTURE_MANUAL) {
            win32KeySender.reGetHwnd();
            Point2D size = WindowClientSizeUtil.getSize(win32KeySender.getGameHwnd());
            int width = (int) size.getX();
            int height = (int) size.getY();
            String key = width + "*" + height;
            Point2D point2D = screenMap.get(key);
            if (point2D == null) {
                log.debug("当前窗户尺寸不在预设范围");
                return;
            }
            win32KeySender.clickLeft((int) point2D.getX(), (int) point2D.getY(), Duration.seconds(0.5));
        }
    }

}

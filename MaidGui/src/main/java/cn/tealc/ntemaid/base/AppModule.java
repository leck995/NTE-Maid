package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.StageInitializer;
import cn.tealc.ntemaid.dao.*;
import cn.tealc.ntemaid.jna.key.Win32KeySender;
import cn.tealc.ntemaid.repository.GameDataRepository;
import cn.tealc.ntemaid.repository.NavRepository;
import cn.tealc.ntemaid.service.*;
import cn.tealc.ntemaid.service.gacha.CommonGachaAnalysisService;
import cn.tealc.ntemaid.service.gacha.CommonGachaService;
import cn.tealc.ntemaid.service.gacha.LocalGachaAnalysisService;
import cn.tealc.ntemaid.service.gacha.LocalGachaDataService;
import cn.tealc.ntemaid.service.system.ConfigService;
import cn.tealc.ntemaid.service.system.impl.ConfigServiceImpl;
import cn.tealc.ntemaid.service.system.impl.GameTimeServiceImpl;
import cn.tealc.ntemaid.service.system.GameTimeService;
import cn.tealc.ntemaid.service.system.player.MusicService;
import cn.tealc.ntemaid.service.system.player.PlayingListService;
import cn.tealc.ntemaid.service.system.player.PlaylistService;
import cn.tealc.ntemaid.service.taygedo.TaygedoAccountService;
import cn.tealc.ntemaid.service.taygedo.TaygedoLoginService;
import cn.tealc.ntemaid.service.taygedo.TaygedoSignInService;
import cn.tealc.ntemaid.ui.tray.TrayIconManager;
import cn.tealc.taygedo.TaygedoApi;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.net.http.HttpClient;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // DAO 层
        bind(GameTimeDao.class).in(Singleton.class);
        bind(ConfigDao.class).in(Singleton.class);
        bind(PlayingListDao.class).in(Singleton.class);
        bind(TaygedoAccountDao.class).in(Singleton.class);
        bind(LocalGachaDataDao.class).in(Singleton.class);
        bind(CommonGachaDao.class).in(Singleton.class);
        bind(PlaylistDao.class).in(Singleton.class);
        bind(MusicDao.class).in(Singleton.class);

        // Service 层 — 接口到实现
        bind(GameTimeService.class).to(GameTimeServiceImpl.class).in(Singleton.class);
        bind(ConfigService.class).to(ConfigServiceImpl.class).in(Singleton.class);

        // Service 层 — 具体类
        bind(PlayingListService.class).in(Singleton.class);
        bind(TaygedoSignInService.class).in(Singleton.class);
        bind(TaygedoAccountService.class).in(Singleton.class);
        bind(TaygedoLoginService.class).in(Singleton.class);
        bind(LocalGachaDataService.class).in(Singleton.class);
        bind(LocalGachaAnalysisService.class).in(Singleton.class);
        bind(CommonGachaAnalysisService.class).in(Singleton.class);
        bind(CommonGachaService.class).in(Singleton.class);
        bind(PlaylistService.class).in(Singleton.class);
        bind(MusicService.class).in(Singleton.class);
        bind(NativeProcessService.class).in(Singleton.class);
        bind(AsyncRunner.class).in(Singleton.class);
        bind(GameDataRepository.class).in(Singleton.class);
        bind(NavRepository.class).in(Singleton.class);
        bind(ShutdownManager.class).in(Singleton.class);
        bind(TrayIconManager.class).in(Singleton.class);
        bind(StageInitializer.class).in(Singleton.class);


        bind(Win32KeySender.class).in(Singleton.class);
        bind(AppRuntimeData.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    TaygedoApi provideTaygedoApi(HttpClient httpClient) {
        return new TaygedoApi(httpClient);
    }
}

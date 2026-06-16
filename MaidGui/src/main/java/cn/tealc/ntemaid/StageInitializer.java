package cn.tealc.ntemaid;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.ui.system.MainView;
import cn.tealc.ntemaid.ui.system.MainViewModel;
import cn.tealc.ntemaid.util.LanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public class StageInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(StageInitializer.class);

    @Inject
    public StageInitializer() {}

    public Scene init(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(FXResourcesLoader.load("css/Light.css"));
        ViewTuple<MainView, MainViewModel> viewTuple = FluentViewLoader.fxmlView(MainView.class).load();
        Scene scene = new Scene(viewTuple.getView());
        scene.getStylesheets().add(FXResourcesLoader.load("css/Default.css"));

        stage.setScene(scene);
        stage.getIcons().add(new Image(FXResourcesLoader.load("image/icon.png"), 45, 45, true, true));
        stage.setTitle(LanguageManager.getString("app.title"));
        stage.setWidth(Math.max(1100, Config.getSetting().getAppWidth()));
        stage.setHeight(Math.max(650, Config.getSetting().getAppHeight()));

        Config.getSetting().appWidthProperty().bind(scene.widthProperty());
        Config.getSetting().appHeightProperty().bind(scene.heightProperty());
        stage.initStyle(StageStyle.EXTENDED);

        Font.loadFonts(FXResourcesLoader.loadStream("font/HarmonyOS_Sans_SC_Bold.ttf"), 12);
        scene.getRoot().setStyle("-fx-font-family: \"HarmonyOS Sans SC\"");

        if (!Config.getSetting().isSilentStartup()) {
            stage.show();
        }
        return scene;
    }
}

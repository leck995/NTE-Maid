package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.model.system.nav.NavData;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewTuple;

public class NavLoader {
    public static ViewTuple<?, ?> load(NavData navData) {
        try {
            Class<?> viewClass =  Class.forName(navData.getViewClass());
            if (navData.isFxml()){
                @SuppressWarnings("unchecked")
                ViewTuple<?, ?> viewTuple = FluentViewLoader
                        .fxmlView((Class<? extends FxmlView<?>>) viewClass)
                        .load();
                return viewTuple;
            }else {
                @SuppressWarnings("unchecked")
                ViewTuple<?, ?> viewTuple = FluentViewLoader
                        .javaView((Class<? extends JavaView<?>>) viewClass)
                        .load();
                return viewTuple;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
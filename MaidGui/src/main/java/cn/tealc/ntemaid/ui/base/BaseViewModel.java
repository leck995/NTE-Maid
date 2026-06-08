package cn.tealc.ntemaid.ui.base;

import cn.tealc.ntemaid.base.AppInjector;
import de.saxsys.mvvmfx.ViewModel;

public abstract class BaseViewModel implements ViewModel {
    protected BaseViewModel() {
        AppInjector.getInjector().injectMembers(this);
    }
}

package cn.tealc.ntemaid.ui.taygedo.account;

import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.ntemaid.ui.base.BaseViewModel;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class TaygedoAccountViewModel extends BaseViewModel {
    private final TaygedoAccountService accountService;

    @Inject
    public TaygedoAccountViewModel(TaygedoAccountService accountService) {
        this.accountService = accountService;
    }

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();

    public void initialize() {
        refreshAccountList();
        NotificationManager.subscribe(NotificationKey.TAYGEDO_ACCOUNT_LIST_REFRESH, (s, objects) -> refreshAccountList());
    }


    private void refreshAccountList() {
        List<TaygedoAccount> allAccount = accountService.getAll();
        accountList.addAll(allAccount);
//        List<TaygedoAccount> userInfos = userInfoService.getAllUsers();
//        accountList.setAll(userInfos);
    }





    public boolean deleteAccount(int index, TaygedoAccount account) {
        boolean i = accountService.delete(account.getPhone());
        if (i) {
            accountList.remove(index);
            return true;
        }
        return false;
    }



    public ObservableList<TaygedoAccount> getAccountList() {
        return accountList;
    }

}
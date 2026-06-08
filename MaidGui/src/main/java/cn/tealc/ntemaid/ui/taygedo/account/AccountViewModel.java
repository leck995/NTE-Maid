package cn.tealc.ntemaid.ui.taygedo.account;

import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.TaygedoService;
import cn.tealc.ntemaid.ui.base.BaseViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * @program: WutheringWavesTool
 * @description:
 * @author: Leck
 * @create: 2024-08-04 00:26
 */
public class AccountViewModel extends BaseViewModel {


    private TaygedoService taygedoService = new TaygedoService();

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();

    public void initialize() {
        refreshUserList();
        //NotificationManager.subscribe(NotificationKey.ACCOUNT_UPDATE, (s, objects) -> refreshUserList());
    }


    private void refreshUserList() {
        List<TaygedoAccount> allAccount = taygedoService.getAllAccount();
        accountList.addAll(allAccount);
//        List<TaygedoAccount> userInfos = userInfoService.getAllUsers();
//        accountList.setAll(userInfos);
    }





/*    public boolean deleteAccount(int index, UserInfo userInfo) {
        boolean i = userInfoService.deleteUser(userInfo.getId());
        if (i) {
            accountList.remove(index);
            return true;
        }
        return false;
    }*/

/*    public void getUserInfo(UserInfo userInfo) {
        PlayerBaseDataTask task = new PlayerBaseDataTask(userInfo);
        task.setOnSucceeded(event -> {
        });
    }*/


    public ObservableList<TaygedoAccount> getAccountList() {
        return accountList;
    }

}
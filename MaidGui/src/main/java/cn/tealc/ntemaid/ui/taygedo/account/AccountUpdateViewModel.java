package cn.tealc.ntemaid.ui.taygedo.account;

import cn.tealc.ntemaid.ui.base.BaseViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

@Deprecated
public class AccountUpdateViewModel extends BaseViewModel {
    public static final String EVENT_CLOSE = "EVENT_CLOSE";
    public static final String EVENT_SELECT_ROLE = "EVENT_SELECT_ROLE";



    private final SimpleBooleanProperty loginTabVisible = new SimpleBooleanProperty(true);

    private final SimpleStringProperty title = new SimpleStringProperty("");
    private final SimpleStringProperty token = new SimpleStringProperty("");
    private final SimpleStringProperty did = new SimpleStringProperty("");
    private final SimpleStringProperty phone = new SimpleStringProperty("");
    private final SimpleStringProperty code = new SimpleStringProperty("");
    private final SimpleBooleanProperty mobileSource = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty mainAccount = new SimpleBooleanProperty(true);
    private final boolean isAdd; //判断是添加还是修改

    //添加时的构造方法
    public AccountUpdateViewModel() {
        loginTabVisible.set(true);
        this.isAdd = true;
    }






    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public String getToken() {
        return token.get();
    }

    public SimpleStringProperty tokenProperty() {
        return token;
    }

    public boolean isMobileSource() {
        return mobileSource.get();
    }

    public SimpleBooleanProperty mobileSourceProperty() {
        return mobileSource;
    }

    public boolean isMainAccount() {
        return mainAccount.get();
    }

    public SimpleBooleanProperty mainAccountProperty() {
        return mainAccount;
    }

    public String getPhone() {
        return phone.get();
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }

    public String getCode() {
        return code.get();
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public String getDid() {
        return did.get();
    }

    public SimpleStringProperty didProperty() {
        return did;
    }

    public boolean isLoginTabVisible() {
        return loginTabVisible.get();
    }

    public SimpleBooleanProperty loginTabVisibleProperty() {
        return loginTabVisible;
    }

    public void setLoginTabVisible(boolean loginTabVisible) {
        this.loginTabVisible.set(loginTabVisible);
    }

}
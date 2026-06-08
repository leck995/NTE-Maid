package cn.tealc.ntemaid.ui.taygedo.account;

import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.TaygedoService;
import cn.tealc.taygedo.TaygedoException;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class TaygedoLoginViewModel implements ViewModel, SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoLoginViewModel.class);

    private final TaygedoService service = new TaygedoService();

    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty captcha = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final BooleanProperty passwordMode = new SimpleBooleanProperty(false);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loginSuccess = new SimpleBooleanProperty(false);
    private final StringProperty countdownText = new SimpleStringProperty("获取验证码");
    private final BooleanProperty canSendCaptcha = new SimpleBooleanProperty(true);

    private int countdownSeconds = 0;
    private Timer countdownTimer;

    public void toggleMode() {
        passwordMode.set(!passwordMode.get());
    }

    public void sendCaptcha() {
        String phoneNumber = phone.get().trim();
        if (phoneNumber.isEmpty()) {
            setStatus("请输入手机号");
            return;
        }
        if (!phoneNumber.matches("\\d{11}")) {
            setStatus("请输入正确的11位手机号");
            return;
        }

        TaygedoAccount account = new TaygedoAccount();
        account.setPhone(phoneNumber);

        loading.set(true);
        setStatus("正在发送验证码...");
        startThread(() -> {
            try {
                service.sendCaptcha(phoneNumber, account);
                Platform.runLater(() -> {
                    setStatus("验证码已发送，请查收短信");
                    startCountdown(60);
                });
            } catch (TaygedoException e) {
                Platform.runLater(() -> setStatus("发送失败: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    public void login() {
        String phoneNumber = phone.get().trim();

        if (isPasswordMode()) {
            loginWithPassword(phoneNumber);
        } else {
            loginWithCaptcha(phoneNumber);
        }
    }

    private void loginWithCaptcha(String phoneNumber) {
        String captchaCode = captcha.get().trim();
        if (phoneNumber.isEmpty() || captchaCode.isEmpty()) {
            setStatus("请输入手机号和验证码");
            return;
        }

        loading.set(true);
        setStatus("正在登录...");
        startThread(() -> {
            try {
                TaygedoAccount result = service.login(phoneNumber, captchaCode);
                service.saveAccount(result);
                Platform.runLater(() -> {
                    setStatus("登录成功！");
                    loginSuccess.set(true);
                    NotificationManager.publish(NotificationKey.TAYGEDO_ACCOUNT_LIST_REFRESH);
                });
            } catch (TaygedoException e) {
                Platform.runLater(() -> setStatus("登录失败: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    private void loginWithPassword(String phoneNumber) {
        String pwd = password.get().trim();
        if (phoneNumber.isEmpty() || pwd.isEmpty()) {
            setStatus("请输入手机号和密码");
            return;
        }

        loading.set(true);
        setStatus("正在登录...");
        startThread(() -> {
            try {
                TaygedoAccount result = service.loginWithPassword(phoneNumber, pwd);
                service.saveAccount(result);
                Platform.runLater(() -> {
                    setStatus("登录成功！");
                    loginSuccess.set(true);
                    NotificationManager.publish(NotificationKey.TAYGEDO_ACCOUNT_LIST_REFRESH);
                });
            } catch (TaygedoException e) {
                Platform.runLater(() -> setStatus("登录失败: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    private void startCountdown(int seconds) {
        countdownSeconds = seconds;
        canSendCaptcha.set(false);
        cancelCountdown();

        countdownTimer = new Timer(true);
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                countdownSeconds--;
                if (countdownSeconds <= 0) {
                    Platform.runLater(() -> {
                        countdownText.set("重新获取");
                        canSendCaptcha.set(true);
                    });
                    cancel();
                } else {
                    Platform.runLater(() ->
                            countdownText.set(countdownSeconds + "秒后重发"));
                }
            }
        }, 1000, 1000);
    }

    private void cancelCountdown() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    private void setStatus(String msg) {
        Platform.runLater(() -> statusMessage.set(msg));
    }

    private void startThread(Runnable task) {
        Thread.ofVirtual().start(task);
    }

    public boolean isPasswordMode() {
        return passwordMode.get();
    }

    // ==================== Property Getters ====================

    public StringProperty phoneProperty() { return phone; }
    public StringProperty captchaProperty() { return captcha; }
    public StringProperty passwordProperty() { return password; }
    public BooleanProperty passwordModeProperty() { return passwordMode; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty loginSuccessProperty() { return loginSuccess; }
    public StringProperty countdownTextProperty() { return countdownText; }
    public BooleanProperty canSendCaptchaProperty() { return canSendCaptcha; }

    @Override
    public void onViewAdded() {}
    @Override
    public void onViewRemoved() {
        cancelCountdown();
    }
}

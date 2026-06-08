package cn.tealc.ntemaid.ui.taygedo.account;

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

/**
 * 塔吉多验证码登录 ViewModel
 * 管理登录流程的UI状态：发送验证码 → 输入验证码 → 登录 → 换取令牌
 * 以 TaygedoAccount 为上下文，deviceId 由 Service 内部管理
 */
public class TaygedoLoginViewModel implements ViewModel, SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoLoginViewModel.class);

    private final TaygedoService service = new TaygedoService();

    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty captcha = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loginSuccess = new SimpleBooleanProperty(false);
    private final StringProperty countdownText = new SimpleStringProperty("获取验证码");
    private final BooleanProperty canSendCaptcha = new SimpleBooleanProperty(true);

    /** 登录流程中的账号上下文（deviceId在整个流程中保持一致） */
    private TaygedoAccount account = new TaygedoAccount();

    /** 倒计时剩余秒数 */
    private int countdownSeconds = 0;
    private Timer countdownTimer;

    /**
     * 发送短信验证码
     * 发送成功后启动60秒倒计时，期间不可重复发送
     */
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

        // 每次发送验证码时创建新的账号上下文
        account = new TaygedoAccount();
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

    /**
     * 执行完整登录流程
     * 验证码登录老虎平台 → 换取塔吉多令牌 → 保存账号
     */
    public void login() {
        String phoneNumber = phone.get().trim();
        String captchaCode = captcha.get().trim();
        if (phoneNumber.isEmpty() || captchaCode.isEmpty()) {
            setStatus("请输入手机号和验证码");
            return;
        }

        loading.set(true);
        setStatus("正在登录...");
        startThread(() -> {
            try {
                // 一键登录：sendCaptcha阶段已生成deviceId并保存到account，
                // login内部会用account的deviceId完成全流程，返回完整的账号
                TaygedoAccount result = service.login(phoneNumber, captchaCode);
                service.saveAccount(result);

                LOG.info("登录成功: phone={}, uid={}, deviceId={}",
                        result.getPhone(), result.getUid(), result.getDeviceId());

                Platform.runLater(() -> {
                    setStatus("登录成功！");
                    loginSuccess.set(true);
                });
            } catch (TaygedoException e) {
                Platform.runLater(() -> setStatus("登录失败: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    /** 启动验证码发送倒计时 */
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

    // ==================== Property Getters ====================

    public StringProperty phoneProperty() { return phone; }
    public StringProperty captchaProperty() { return captcha; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty loginSuccessProperty() { return loginSuccess; }
    public StringProperty countdownTextProperty() { return countdownText; }
    public BooleanProperty canSendCaptchaProperty() { return canSendCaptcha; }

    @Override
    public void onViewAdded() {}
    @Override
    public void onViewRemoved() { cancelCountdown(); }
}

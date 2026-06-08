package cn.tealc.ntemaid.ui.system.account;

import atlantafx.base.util.Animations;
import cn.tealc.ntemaid.ui.taygedo.account.TaygedoAccountView;
import cn.tealc.ntemaid.ui.taygedo.account.TaygedoAccountViewModel;
import cn.tealc.ntemaid.ui.taygedo.signin.TaygedoSignInView;
import cn.tealc.ntemaid.ui.taygedo.signin.TaygedoSignInViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AccountGroupView implements FxmlView<AccountGroupViewModel> {
    @InjectViewModel
    private AccountGroupViewModel viewModel;
    @FXML
    private ToggleGroup childSelectedToggle;
    @FXML
    private StackPane content;
    @FXML
    private HBox headerPane;

    private Node taygedoView;
    private Node appAccountView;
    private Node signInView;

    public void initialize() {
        //createAppAccountView();
        ViewTuple<TaygedoAccountView, TaygedoAccountViewModel> viewTuple = FluentViewLoader.fxmlView(TaygedoAccountView.class).load();
        taygedoView = viewTuple.getView();
        content.getChildren().setAll(taygedoView);
        Animations.slideInUp(taygedoView, Duration.millis(300)).play();
    }



    @FXML
    void toAppAccountEvent(ActionEvent event) {
        ToggleButton toggleButton= (ToggleButton) event.getSource();
        if (toggleButton.isSelected()){
            //createAppAccountView();
        }else {
            toggleButton.setSelected(true);
        }
    }

    @FXML
    void toTaygedoEvent(ActionEvent event) {
        ToggleButton toggleButton= (ToggleButton) event.getSource();
        if (toggleButton.isSelected()){
            if (taygedoView == null) {
                ViewTuple<TaygedoAccountView, TaygedoAccountViewModel> viewTuple = FluentViewLoader.fxmlView(TaygedoAccountView.class).load();
                taygedoView = viewTuple.getView();
            }
            content.getChildren().setAll(taygedoView);
            Animations.slideInUp(taygedoView, Duration.millis(300)).play();
        }else {
            toggleButton.setSelected(true);
        }
    }

    @FXML
    void toSignInEvent(ActionEvent event) {
        ToggleButton toggleButton = (ToggleButton) event.getSource();
        if (toggleButton.isSelected()) {
            if (signInView == null) {
                ViewTuple<TaygedoSignInView, TaygedoSignInViewModel> viewTuple = FluentViewLoader.fxmlView(TaygedoSignInView.class).load();
                signInView = viewTuple.getView();
            }
            content.getChildren().setAll(signInView);
            Animations.slideInUp(signInView, Duration.millis(300)).play();
        } else {
            toggleButton.setSelected(true);
        }
    }

    private void toAppAccount(){
//        if (appAccountView == null) {
//            ViewTuple<AppAccountView, AppAccountViewModel> viewTuple = FluentViewLoader.fxmlView(AppAccountView.class).load();
//            appAccountView = viewTuple.getView();
//        }
//        content.getChildren().setAll(appAccountView);
//        Animations.slideInUp(appAccountView, Duration.millis(300)).play();
    }
}
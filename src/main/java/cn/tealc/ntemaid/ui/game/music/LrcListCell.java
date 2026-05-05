package cn.tealc.ntemaid.ui.game.music;


import cn.tealc.ntemaid.model.game.music.LrcBean;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * @program: AsmrPlayer
 * @description: 播放界面歌词显示的Cell
 * @author: Leck
 * @create: 2023-03-01 01:43
 */
public class LrcListCell extends ListCell<LrcBean> {

    private SimpleBooleanProperty showTrans=new SimpleBooleanProperty(true);
/*一开始实现了row，tran的复用，但一旦复用就会出现listview经典的bug：复用导致空值cell显示错误，还解决不掉，平常cell只需要在为空时设置一下就行，但复用仍有*/



    @Override
    protected void updateItem(LrcBean lrcBean, boolean b) {
        if (!b){
            super.updateItem(lrcBean, b);
//                            row.effectProperty().bind(
//                                    Bindings.createObjectBinding(
//                                            ()->
//                                                    (new DropShadow(BlurType.THREE_PASS_BOX,Color.web(SettingProperties.desktopLRCBorderColor.get()),10.0,0,0,0)),
//                                            SettingProperties.desktopLRCBorderColor));
            VBox box=new VBox();
            box.setPadding(new Insets(10,0,10,0));
            box.setSpacing(5);
            //box.setAlignment(SettingProperties.detailLrcAlignment.get() ? Pos.CENTER : Pos.CENTER_LEFT);
            Text row=new Text(lrcBean.getRowText());
            box.getChildren().add(row);

            if (showTrans.get() && lrcBean.getTransText()!=null){
                Text tran=new Text(lrcBean.getTransText());
                box.getChildren().add(tran);
            }

            showTrans.addListener((observableValue, aBoolean, t1) -> {
                if (t1){
                    if (lrcBean.getTransText()!=null){
                        Text tran=new Text(lrcBean.getTransText());
                        box.getChildren().add(tran);
                    }
                }else {
                    if (box.getChildren().size() > 1){
                        box.getChildren().remove(box.getChildren().size() -1);
                    }
                }
            });
            setGraphic(box);

        }else{
           setGraphic(null);
        }
    }

    public boolean isShowTrans() {
        return showTrans.get();
    }

    public SimpleBooleanProperty showTransProperty() {
        return showTrans;
    }

    public void setShowTrans(boolean showTrans) {
        this.showTrans.set(showTrans);
    }
}
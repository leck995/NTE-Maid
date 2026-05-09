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
    private VBox box = new VBox();
    private Text row = new Text();
    private Text tran = new Text();

    public LrcListCell() {
        box.setPadding(new Insets(10, 0, 10, 0));
        box.setSpacing(5);
        // 关键：限制 Text 的包裹宽度，防止横向溢出
        row.wrappingWidthProperty().bind(widthProperty().subtract(20));
        tran.wrappingWidthProperty().bind(widthProperty().subtract(20));

        // 构造函数里只写一次监听
        showTrans.addListener((obs, old, val) -> updateLayout());
    }

    @Override
    protected void updateItem(LrcBean lrcBean, boolean empty) {
        super.updateItem(lrcBean, empty);
        if (empty || lrcBean == null) {
            setGraphic(null);
            setText(null);
        } else {
            row.setText(lrcBean.getRowText());
            tran.setText(lrcBean.getTransText());
            updateLayout();
            setGraphic(box);
        }
    }

    private void updateLayout() {
        box.getChildren().clear();
        box.getChildren().add(row);
        if (showTrans.get() && getItem() != null && getItem().getTransText() != null) {
            box.getChildren().add(tran);
        }
    }

    public boolean isShowTrans() {
        return showTrans.get();
    }

    public SimpleBooleanProperty showTransProperty() {
        return showTrans;
    }
}
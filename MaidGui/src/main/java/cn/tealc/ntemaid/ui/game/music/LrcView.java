package cn.tealc.ntemaid.ui.game.music;



import cn.tealc.ntemaid.model.game.music.LrcBean;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName LrcView
 * @Description: TODO
 * @Author Leck
 * @Date 2022/2/20
 * @Version V1.0
 **/
public class LrcView<T> extends ListView<LrcBean> {
    private static final Logger LOG= LoggerFactory.getLogger(LrcView.class);
    /**
     * 播放时间(进度)
     */
    private SimpleIntegerProperty lineIndex;
    private SimpleBooleanProperty showTrans;
    public LrcView(SimpleIntegerProperty lineIndex) {
        this.lineIndex = lineIndex;
        showTrans=new SimpleBooleanProperty(true);
        Label placeholder=new Label("暂无歌词");
        placeholder.setFont(Font.font(24));
        setPlaceholder(placeholder);

        setCellFactory(lrcBeanListView -> {
            LrcListCell cell = new LrcListCell();
            //cell.setAlignment(Pos.CENTER);
            cell.setWrapText(true);

            cell.showTransProperty().bind(showTrans);
            cell.prefWidthProperty().bind(widthProperty().multiply(0.65));
            return cell;
        });

        lineIndex.addListener((observable, oldValue, newValue) -> {
            if (newValue != null){
                int i = newValue.intValue();
                LrcBean lrcBean = getItems().get(i);
                if (lrcBean.isEmpty())
                    return;
                getSelectionModel().select(i);
                scrollTo(i,false);
            }
        });


        //屏蔽所有事件
        addEventFilter(MouseEvent.ANY, click ->{
            click.consume();
        });



    }

    /**
     * 歌词地址
     * */
    private SimpleStringProperty lrcFilePath;



    /**
     * @Description: 跳转listview到指定行,目前找到我能接收的方法判断是否有翻译，故默认hasTrans没有用
     * @MethodName: scrollTo
     * @param index: 歌词所在的index
     * @param hasTrans: 是否有翻译
     * @Return: void
     * @Author: Leck
     * @Date: 2022/2/15
     */
//    private void scrollTo(int index,boolean hasTrans){
//        if (getItems().size() == 0) return;
//        VirtualFlow<ListCell<LrcBean>> virtualFlow= (VirtualFlow<ListCell<LrcBean>>) getChildren().get(0);
//        if (virtualFlow.getLastVisibleCell() != null){
//            int i = index-(int) ((virtualFlow.getLastVisibleCell().getIndex() - virtualFlow.getFirstVisibleCell().getIndex()) * 0.5)+1;
//            scrollTo(Math.max(i, 0));
//            //virtualFlow.scrollTo(Math.max(i, 0));
//        }
//
//    }

    private void scrollTo(int index, boolean hasTrans) {
        if (getItems().isEmpty()) return;

        VirtualFlow<ListCell<LrcBean>> virtualFlow = (VirtualFlow<ListCell<LrcBean>>) getChildren().get(0);
        if (virtualFlow != null) {
            int totalItems = getItems().size();

            // --- 修改点：最后三行不执行滚动逻辑 ---
            // 假设索引从 0 开始，最后三行的索引范围是 [totalItems-3, totalItems-1]
            if (index >= totalItems - 2) {
                // 这里只执行选择操作，不调用 super.scrollTo
                // 这样最后三行会自然停留在 ListView 的底部可视区域
                getSelectionModel().select(index);
                return;
            }

            ListCell<LrcBean> firstCell = virtualFlow.getFirstVisibleCell();
            ListCell<LrcBean> lastCell = virtualFlow.getLastVisibleCell();

            if (firstCell != null && lastCell != null) {
                int visibleCount = lastCell.getIndex() - firstCell.getIndex();
                // 计算居中目标的偏移量
                int scrollTarget = index - (int) (visibleCount * 0.5);

                // 只有当计算出的目标位置大于 0 时才滚动，防止跳动
                super.scrollTo(Math.max(0, scrollTarget));
            } else {
                // 如果 VirtualFlow 还没准备好，兜底逻辑
                super.scrollTo(index);
            }
        }
    }
    public  String getLrcFilePath() {
        return lrcFilePath.get();
    }

    public  SimpleStringProperty lrcFilePathProperty() {
        return lrcFilePath;
    }

    public  void setLrcFilePath(String lrcFilePath) {
        this.lrcFilePath.set(lrcFilePath);
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

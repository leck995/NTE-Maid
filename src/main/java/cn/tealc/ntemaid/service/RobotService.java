package cn.tealc.ntemaid.service;

import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;

public class RobotService {
    /**
     * 按下数字键2
     * @author leck
     * @date 2026/05/09
     */
    public void clickKeyCodeDigit2(){
        Robot robot = new Robot();
        robot.keyPress(KeyCode.DIGIT2);
        robot.keyRelease(KeyCode.DIGIT2);
    }


    public void clickKeyCodeF(){
        Robot robot = new Robot();
        robot.keyPress(KeyCode.F);
        robot.keyRelease(KeyCode.F);
    }


    public void clickKeyCodeESC(){
        Robot robot = new Robot();
        robot.keyPress(KeyCode.ESCAPE);
        robot.keyRelease(KeyCode.ESCAPE);
    }


}

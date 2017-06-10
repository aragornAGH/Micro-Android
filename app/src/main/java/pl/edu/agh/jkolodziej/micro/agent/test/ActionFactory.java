package pl.edu.agh.jkolodziej.micro.agent.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.weka.test.action.Action;
import pl.edu.agh.jkolodziej.micro.weka.test.action.SingleTest;

/**
 * @author - Jakub Kołodziej
 */
public class ActionFactory {
    private static List<Action> testActions = Lists.newArrayList();

    private static List<Action> tenSimpleAction = Lists.newArrayList();

    static {
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr2.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr3.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr4.jpg", null, null));
//        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr5.jpg", null, null));
//        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr6.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr7.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr8.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr9.jpg", null, null));
        tenSimpleAction.add(new SingleTest(TaskType.OCR, 1, false, "sample_ocr10.jpg", null, null));

        addActionsInConnectionTypeAndTaskDestination(ConnectionType.WIFI, null);
        addActionsInConnectionTypeAndTaskDestination(ConnectionType.UMTS_3G, null);
    }

    private static void addActionsInConnectionTypeAndTaskDestination(ConnectionType connectionType, TaskDestination taskDestination) {
        for (Action test : ImmutableList.copyOf(tenSimpleAction)) {
            SingleTest singleTest = new SingleTest(((SingleTest) test));
            singleTest.setConnectionType(connectionType);
            singleTest.setTaskDestination(taskDestination);
            testActions.add(singleTest);
        }
    }

    public static List<Action> getTestActions() {
        return testActions;
    }
}

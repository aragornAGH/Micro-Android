package pl.edu.agh.jkolodziej.micro.agent.test;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.weka.test.action.Action;
import pl.edu.agh.jkolodziej.micro.weka.test.action.SingleTest;

/**
 * @author - Jakub Kołodziej
 */
public class ActionFactory {
    private static List<Action> singleTests = Lists.newArrayList();

    static {
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr2.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr3.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr4.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr5.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr6.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr7.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr8.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr9.jpg");
        addSingleTestInEachConnectionType(TaskType.OCR, 1, false, "sample_ocr10.jpg");
    }

    private static void addSingleTestInEachConnectionType(TaskType tasktype, int numberOfTest, boolean ignored, String fileName) {
        singleTests.add(new SingleTest(tasktype, numberOfTest, ignored, fileName, ConnectionType.WIFI));
        singleTests.add(new SingleTest(tasktype, numberOfTest, ignored, fileName, ConnectionType.CDMA_2G));
        singleTests.add(new SingleTest(tasktype, numberOfTest, ignored, fileName, ConnectionType.UMTS_3G));
        singleTests.add(new SingleTest(tasktype, numberOfTest, ignored, fileName, ConnectionType.LTE_4G));
        singleTests.add(new SingleTest(tasktype, numberOfTest, ignored, fileName, ConnectionType.NONE));
    }

    private ActionFactory() {
    }

    public static List<Action> getRandomlyActions(int numberOfActions) {
        Collections.shuffle(singleTests);
        return singleTests.subList(0, numberOfActions);
    }
}

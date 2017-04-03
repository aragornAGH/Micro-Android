package pl.edu.agh.jkolodziej.micro.agent.service;

import android.app.IntentService;
import android.content.Intent;

import com.google.common.io.ByteStreams;

import org.nzdis.micro.SystemAgentLoader;
import org.nzdis.micro.bootloader.MicroConfigLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.AndroidFilesSaverHelper;
import pl.edu.agh.jkolodziej.micro.agent.role.requester.FromFileIntentWekaRequestRole;
import pl.edu.agh.jkolodziej.micro.weka.ExecutionPredictorFactory;
import pl.edu.agh.jkolodziej.micro.weka.KnowledgeInstanceManagerFactory;
import pl.edu.agh.jkolodziej.micro.weka.managers.KnowledgeInstanceManager;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import pl.edu.agh.jkolodziej.micro.weka.predictors.ExecutionPredictor;
import pl.edu.agh.jkolodziej.micro.weka.test.ResultsContainer;
import pl.edu.agh.jkolodziej.micro.weka.test.ResultsPrinter;
import pl.edu.agh.jkolodziej.micro.weka.test.TestsConfiguration;
import pl.edu.agh.jkolodziej.micro.weka.test.TestsContext;
import pl.edu.agh.jkolodziej.micro.weka.test.action.Action;
import pl.edu.agh.jkolodziej.micro.weka.test.action.NextRound;
import pl.edu.agh.jkolodziej.micro.weka.test.action.SingleTest;

/**
 * @author - Jakub Kołodziej
 */
public class TestAgentService extends IntentService {

    public static FromFileIntentWekaRequestRole fromFileClient = null;
    private final TestsConfiguration testsConfiguration;
    private static ExecutionPredictor executionPredictor;
    private static TestsContext testsContext;

    public TestAgentService() {
        this("AGENT_TEST_SERVICE");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TestAgentService(String name) {
        super(name);
        testsConfiguration = makeTestConfiguration();
        this.executionPredictor = createExecutionPredictor();
        this.testsContext = new TestsContext(testsConfiguration, this);
    }

    private ExecutionPredictor createExecutionPredictor() {
        KnowledgeInstanceManager timeInstanceManager =
                KnowledgeInstanceManagerFactory.getTimeInstanceManager(
                        AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/time.file",
                        "J48");
        KnowledgeInstanceManager batteryInstanceManager =
                KnowledgeInstanceManagerFactory.getBatteryInstanceManager(
                        AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file",
                        "J48");
        return ExecutionPredictorFactory.createPredictor(timeInstanceManager,
                batteryInstanceManager);
    }

    private TestsConfiguration makeTestConfiguration() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new SingleTest(TaskType.OCR, 3, false));
        return new TestsConfiguration.Builder().setTestDirectory(new File(
                AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/result.csv"))
                .setSeries(1)
                .setRounds(3)
                .setWarmup(1)
                .setTestName("test")
                .setActions(actions)
                .setClassifierName("J48").build();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (fromFileClient == null) {
            fromFileClient = new FromFileIntentWekaRequestRole(this, testsContext);
            SystemAgentLoader.newAgent(fromFileClient, "requester-android-from-file-test");
        }

        ClassLoader classLoader = MicroConfigLoader.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream("ocr/sample_ocr.jpg");
        try {
            fromFileClient.setBytes(ByteStreams.toByteArray(stream));
            execute();
//            fromFileClient.startOCR();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute() throws Exception {
        execute(testsContext);
    }

    public static void execute(TestsContext context) throws Exception {
        Action action = context.getAction();

        if (action == null) {
            ResultsPrinter printer = new ResultsPrinter(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "results.csv"), context.getResultsContainer());
            printer.saveToFile();
            finishTests();
        } else {
            ResultsPrinter printer = new ResultsPrinter(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "results.csv"), context.getResultsContainer());
            printer.saveToFile();
//            if (action instanceof ConnectionChange) {
//                changeNetworkType(context, (ConnectionChange) action);
//            } else
            if (action instanceof NextRound) {
                updateKnowledge(context);
//            } else if (action instanceof NextSeries) {
//                clearKnowledge(context);
            } else {
                SingleTest test = (SingleTest) action;
                if (TaskType.OCR == test.getTaskType()) {
                    executeOcr(context, test, executionPredictor);
                }
//                else {
//                    executeFr(test, context);
//                }
            }
        }
    }

    private static void executeOcr(TestsContext testsContext, SingleTest singleTest, ExecutionPredictor executionPredictor) throws Exception {
        fromFileClient.startOCR(executionPredictor);
        while (FromFileIntentWekaRequestRole.isBusy()) {
            Thread.sleep(1000);
        }
        execute(testsContext);
    }

    private static void updateKnowledge(TestsContext context) throws Exception {

        String knowledgeTimeFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/time.file";
        String knowledgeBatteryFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file";

        KnowledgeInstanceManager knowledgeTimeInstanceManager = KnowledgeInstanceManagerFactory
                .getTimeInstanceManager(knowledgeTimeFileLocation, "J48");
        KnowledgeInstanceManager knowledgeBatteryInstanceManager = KnowledgeInstanceManagerFactory
                .getBatteryInstanceManager(knowledgeBatteryFileLocation, "J48");

        for (ResultsContainer.ResultData data : context.getResultsContainer().getLastRoundResults()) {
            LearningParameters params = data.getParams();
            knowledgeTimeInstanceManager.addNewInstance(params);
            knowledgeBatteryInstanceManager.addNewInstance(params);
        }
        knowledgeTimeInstanceManager.updateClassifier();
        knowledgeBatteryInstanceManager.updateClassifier();

        execute(context);
    }

    private static void finishTests() {
        String knowledgeTimeFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/time.file";
        String knowledgeBatteryFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file";
        KnowledgeInstanceManager batteryInstanceManager = KnowledgeInstanceManagerFactory
                .getBatteryInstanceManager(knowledgeBatteryFileLocation, "J48");
        KnowledgeInstanceManager timeInstanceManager = KnowledgeInstanceManagerFactory
                .getTimeInstanceManager(knowledgeTimeFileLocation, "J48");

        batteryInstanceManager.writeDataFile(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "battery.arff"));
        timeInstanceManager.writeDataFile(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "time.arff"));

//        callback.finishTests();
    }
}

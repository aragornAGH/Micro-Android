package pl.edu.agh.jkolodziej.micro.agent.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;

import com.amazonaws.util.IOUtils;
import com.google.common.io.ByteStreams;

import org.nzdis.micro.SystemAgentLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.AndroidFilesSaverHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.ConnectionTypeHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.ImageRawIdHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.TestSettings;
import pl.edu.agh.jkolodziej.micro.agent.role.requester.FromFileIntentWekaRequestRole;
import pl.edu.agh.jkolodziej.micro.agent.test.ActionFactory;
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
import pl.edu.agh.jkolodziej.micro.weka.test.action.NextSeries;
import pl.edu.agh.jkolodziej.micro.weka.test.action.SingleTest;

import static pl.edu.agh.jkolodziej.micro.agent.helpers.TestSettings.INTERNET_CONNECTION_NEED_TO_CHANGE;
import static pl.edu.agh.jkolodziej.micro.agent.helpers.TestSettings.ROUND_AMOUNT;
import static pl.edu.agh.jkolodziej.micro.agent.helpers.TestSettings.SERIES_AMOUNT;

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
                        TestSettings.CLASSIFIER_NAME);
        KnowledgeInstanceManager batteryInstanceManager =
                KnowledgeInstanceManagerFactory.getBatteryInstanceManager(
                        AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file",
                        TestSettings.CLASSIFIER_NAME);
        return ExecutionPredictorFactory.createPredictor(timeInstanceManager,
                batteryInstanceManager);
    }

    private TestsConfiguration makeTestConfiguration() {
        List<Action> actions = ActionFactory.getTestActions();

        return new TestsConfiguration.Builder().setTestDirectory(new File(
                AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/result.csv"))
                .setSeries(SERIES_AMOUNT)
                .setRounds(ROUND_AMOUNT)
                .setTestName("test")
                .setActions(actions)
                .setClassifierName(TestSettings.CLASSIFIER_NAME).build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (fromFileClient == null) {
            fromFileClient = new FromFileIntentWekaRequestRole(this, testsContext);
            SystemAgentLoader.newAgent(fromFileClient, "requester-android-from-file-test");
        }

        try {
            execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBytesFromFile(String fileName) {
//        ClassLoader classLoader = MicroConfigLoader.class.getClassLoader();
        InputStream stream = getApplicationContext().getResources()
                .openRawResource(ImageRawIdHelper.getRawId(fileName));
//        InputStream stream = classLoader.getResourceAsStream("ocr/" + fileName);
        try {
            fromFileClient.setBytes(ByteStreams.toByteArray(stream));
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

    public void runTestInValidateConnectionType(final TestsContext context, final SingleTest test) throws Exception {
        if (!isConnectionTypeCorrect(context.getContext(), test.getConnectionType())) {
            INTERNET_CONNECTION_NEED_TO_CHANGE = true;

            Intent changeConnectionIntent = new Intent(MainActivity.ChangeConnectionServiceReceiver.CHANGE_CONNECTION);
            changeConnectionIntent.putExtra("test", test);
            LocalBroadcastManager.getInstance(this).sendBroadcast(changeConnectionIntent);

            while (INTERNET_CONNECTION_NEED_TO_CHANGE) {
//                Thread.sleep(1000);
            }
            if (TaskType.OCR == test.getTaskType()) {
                executeOcr(context, test, executionPredictor);
            }
        } else {
            if (TaskType.OCR == test.getTaskType()) {
                executeOcr(context, test, executionPredictor);
            }
        }
    }

    public void execute(TestsContext context) throws Exception {
        Action action = context.getAction();
        while (action != null) {
            executeAction(context, action);
            action = context.getAction();
        }
        ResultsPrinter printer = new ResultsPrinter(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY,
                "results.csv"), context.getResultsContainer());
        printer.saveToFile();
        finishTests();
    }


    private void executeAction(TestsContext context, Action action) throws Exception {
        ResultsPrinter printer = new ResultsPrinter(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "results.csv"), context.getResultsContainer());
        printer.saveToFile();
        if (action instanceof NextRound) {
            updateKnowledge(context);
        } else if (action instanceof NextSeries) {
            clearKnowledge(context);
        } else {
            SingleTest test = (SingleTest) action;
            Logger.getAnonymousLogger().log(Level.INFO, "Filename:" + test.getFileName()
                    + ", connection type:" + test.getConnectionType());
            runTestInValidateConnectionType(context, test);
        }

    }

    public boolean isConnectionTypeCorrect(final Context ctx, final ConnectionType connectionType) {
        try {
            final Flag flag = new Flag();
//            Thread thread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        setNetworkConnection(ctx, connectionType);
                        Thread.sleep(4500);
                        flag.raiseFlag();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.run();
//            );
//            thread.start();
            while (!flag.isFlagRaised()) {
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConnectionType currentConnectionType = ConnectionTypeHelper.getConnectionType(getApplicationContext());
        Logger.getAnonymousLogger().log(Level.INFO, "Current network state: " + currentConnectionType);
        return connectionType == currentConnectionType;
    }


    private void setNetworkConnection(Context ctx, ConnectionType connectionType) throws Exception {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        Thread.sleep(5000);

        if (ConnectionType.WIFI == connectionType) {
            wifiManager.setWifiEnabled(true);
            setMobileDataEnabled(ctx, false);
            Thread.sleep(10000);
        } else if (ConnectionType.NONE == connectionType) {
            wifiManager.setWifiEnabled(false);
            setMobileDataEnabled(ctx, false);
        } else {
            wifiManager.setWifiEnabled(false);
            setMobileDataEnabled(ctx, true);
        }

    }

    private void setMobileDataEnabled(Context context, boolean enabled) throws Exception {
        final ConnectivityManager conman =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(
                iConnectivityManager.getClass().getName());
        Class[] cArg = new Class[2];
        cArg[0] = String.class;
        cArg[1] = Boolean.TYPE;
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                .getDeclaredMethod("setMobileDataEnabled", cArg);
        setMobileDataEnabledMethod.setAccessible(true);


        Object[] pArg = new Object[2];
        pArg[0] = context.getPackageName();
        pArg[1] = enabled;
        setMobileDataEnabledMethod.invoke(iConnectivityManager, pArg);
    }

    private boolean isConnected(String url) {
        try {
            InputStream is = (new URL(url)).openStream();
            String content = IOUtils.toString(is);
            is.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static class Flag {
        private boolean flag = false;

        private void raiseFlag() {
            flag = true;
        }

        private boolean isFlagRaised() {
            return flag;
        }
    }

    private void executeOcr(TestsContext testsContext, SingleTest singleTest, ExecutionPredictor executionPredictor) throws Exception {
        setBytesFromFile(singleTest.getFileName());
        fromFileClient.startOCR(executionPredictor, singleTest);
        while (FromFileIntentWekaRequestRole.isBusy()) {
//            Thread.sleep(1000);
        }
//        execute(testsContext);
    }

    private void updateKnowledge(TestsContext context) throws Exception {

        String knowledgeTimeFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/time.file";
        String knowledgeBatteryFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file";

        KnowledgeInstanceManager knowledgeTimeInstanceManager = KnowledgeInstanceManagerFactory
                .getTimeInstanceManager(knowledgeTimeFileLocation, TestSettings.CLASSIFIER_NAME);
        KnowledgeInstanceManager knowledgeBatteryInstanceManager = KnowledgeInstanceManagerFactory
                .getBatteryInstanceManager(knowledgeBatteryFileLocation, TestSettings.CLASSIFIER_NAME);

        for (ResultsContainer.ResultData data : context.getResultsContainer().getLastRoundResults()) {
            LearningParameters params = data.getParams();
            knowledgeTimeInstanceManager.addNewInstance(params);
            knowledgeBatteryInstanceManager.addNewInstance(params);
        }
        knowledgeTimeInstanceManager.updateClassifier();
        knowledgeBatteryInstanceManager.updateClassifier();

//        execute(context);
    }

    private static void finishTests() {
        String knowledgeTimeFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/time.file";
        String knowledgeBatteryFileLocation = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file";
        KnowledgeInstanceManager batteryInstanceManager = KnowledgeInstanceManagerFactory
                .getBatteryInstanceManager(knowledgeBatteryFileLocation, TestSettings.CLASSIFIER_NAME);
        KnowledgeInstanceManager timeInstanceManager = KnowledgeInstanceManagerFactory
                .getTimeInstanceManager(knowledgeTimeFileLocation, TestSettings.CLASSIFIER_NAME);

        batteryInstanceManager.writeDataFile(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "battery_"
                + new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()) + ".arff"));
        timeInstanceManager.writeDataFile(new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY, "time"
                + new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()) + ".arff"));
        Logger.getAnonymousLogger().log(Level.INFO, "End of TESTS!");
    }

    private void clearKnowledge(TestsContext context) throws Exception {

        KnowledgeInstanceManagerFactory.resetTimeManager(AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/time.file");
        KnowledgeInstanceManagerFactory.resetBatteryManager(AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/battery.file");
        this.executionPredictor = createExecutionPredictor();

//        execute(context);
    }
}

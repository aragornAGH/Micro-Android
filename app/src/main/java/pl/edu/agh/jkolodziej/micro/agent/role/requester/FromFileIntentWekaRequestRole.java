package pl.edu.agh.jkolodziej.micro.agent.role.requester;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.nzdis.micro.MicroMessage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.BatteryUtils;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.CipherDataHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.ConnectionTypeHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.DestinationMapper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.IntentParametersHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.TestSettings;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import pl.edu.agh.jkolodziej.micro.weka.predictors.ExecutionPredictor;
import pl.edu.agh.jkolodziej.micro.weka.test.Measurement;
import pl.edu.agh.jkolodziej.micro.weka.test.TestsContext;
import pl.edu.agh.jkolodziej.micro.weka.test.action.SingleTest;
import pl.edu.agh.mm.energy.PowerTutorFacade;

/**
 * @author - Jakub Kołodziej
 */
public class FromFileIntentWekaRequestRole extends FromFileIntentRequestRole {

    public static boolean IS_BUSY = false;
    public static TestsContext testsContext;

    public FromFileIntentWekaRequestRole(Context mContext, TestsContext testsContext) {
        super(mContext);
        this.testsContext = testsContext;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void release() {

    }

    protected void setDataAndSendMessage(ServiceIntent intent, TaskType taskType, ExecutionPredictor predictor, SingleTest singleTest) throws Exception {
        IS_BUSY = true;
        MicroMessage message = new MicroMessage();
        intent.setData(CipherDataHelper.encryptByteArray(bytes));
        intent.setStartTime(System.currentTimeMillis());
        intent.setStartBattery(PowerTutorFacade.getInstance(mContext, "energy").getTotalPowerForUid());
        intent.setConnectionType(ConnectionTypeHelper.getConnectionType(mContext));
        if (((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 1000;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            intent.setWifiPowerSignal(WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels));
        } else {
            intent.setWifiPowerSignal(0);
        }
        intent.setTaskType(taskType);
        IntentParametersHelper.setFromFileIntentParameters(intent, bytes);

        LearningParameters learningParameters = new LearningParameters(taskType);
        learningParameters.setWifiStrength(intent.getWifiPowerSignal());
        learningParameters.setResolution(intent.getResolution());
        learningParameters.setFileSize(intent.getFileSize());
        learningParameters.setConnectionType(intent.getConnectionType());
        TaskDestination taskDestination = singleTest.getTaskDestination() != null ? singleTest.getTaskDestination()
                : predictor.getTaskDestination(learningParameters, TestSettings.TIME_WEIGHT, TestSettings.BATTERY_WEIGHT);
        intent.setTaskDestination(taskDestination);

        message.setRecipient(DestinationMapper.getAgentNameByDestination(taskDestination));

        Logger.getAnonymousLogger().log(Level.INFO, "CHOOSE DESTINATION(" + bytes.length + "b): "
                + taskDestination.name() + "->" + message.getRecipient());


        message.setIntent(intent);
        send(message);
    }

    public void startOCR(ExecutionPredictor executionPredictor, SingleTest singleTest) throws Exception {
        setDataAndSendMessage(new OCRIntent(), TaskType.OCR, executionPredictor, singleTest);
    }

    @Override
    public void handleMessage(MicroMessage message) {
        ServiceIntent intent = message.getIntent();
        LearningParameters params = new LearningParameters(intent.getTaskType());
        params.setDestination(intent.getTaskDestination().name());
        params.setConnectionType(intent.getConnectionType());
        params.setFileSize(intent.getFileSize());
        params.setResolution(intent.getResolution());
        params.setWifiStrength(intent.getWifiPowerSignal());

        long endTime = intent.getEndTime() != null ? intent.getEndTime() : System.currentTimeMillis();
        long batteryState = intent.getEndBattery() != 0L ? intent.getEndBattery() :
                PowerTutorFacade.getInstance(mContext, "energy").getTotalPowerForUid();


        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        double voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0;
        double percentageUsageOfbattery = BatteryUtils.getPercentageUsageOfBattery(mContext, voltage,
                batteryState - intent.getStartBattery());

        testsContext.appendResult(params,
                new Measurement.Result(false, endTime - intent.getStartTime(),
                        batteryState - intent.getStartBattery(),
                        percentageUsageOfbattery,
                        intent.getConnectionType(),
                        params),
                intent.getResult());
        Logger.getAnonymousLogger().log(Level.INFO, "Response: time - " + (endTime - intent.getStartTime()) + " ms, battery - "
                + (batteryState - intent.getStartBattery()) + "mJ");
        try {
            Files.append("Time;" + (endTime - intent.getStartTime()) + ";Batterry;" + (batteryState - intent.getStartBattery()) +"\n",
                    TestSettings.RESULT_WRITER_FILE, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        IS_BUSY = false;
    }

    public static boolean isBusy() {
        return IS_BUSY;
    }
}

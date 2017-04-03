package pl.edu.agh.jkolodziej.micro.agent.role.requester;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.collect.Maps;

import org.nzdis.micro.DefaultSocialRole;
import org.nzdis.micro.MicroMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.PowerTutorHelper;
import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.CipherDataHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.ConnectionTypeHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.DestinationMapper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.IntentParametersHelper;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ConvertPngToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;
import pl.edu.agh.mm.energy.PowerTutorFacade;

/**
 * Created by Kołacz.
 */

public class FromFileIntentRequestRole extends DefaultSocialRole {
    private static final Map<Class, IntentType> CLASS_INTENT_MAP = Maps.newHashMap();

    static {
        CLASS_INTENT_MAP.put(AddingFromFileIntent.class, IntentType.ADDING_FROM_FILE);
        CLASS_INTENT_MAP.put(ConvertPngToPDFIntent.class, IntentType.PNG_TO_PDF);
        CLASS_INTENT_MAP.put(OCRIntent.class, IntentType.OCR);
    }

    protected final Context mContext;
    protected byte[] bytes;

    public FromFileIntentRequestRole(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void initialize() {
    }

    @Override
    protected void release() {
    }

    protected void setDataAndSendMessage(ServiceIntent intent, TaskType taskType) throws Exception {
        MicroMessage message = new MicroMessage();
        intent.setData(CipherDataHelper.encryptByteArray(bytes));
        intent.setStartTime(System.nanoTime());
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

        message.setIntent(intent);
        sendGlobalBroadcast(message);
    }

    public void startAddingFromFile() throws Exception {
        setDataAndSendMessage(new AddingFromFileIntent(), TaskType.OCR);
    }

    public void startPNGToPDF() throws Exception {
        setDataAndSendMessage(new ConvertPngToPDFIntent(), TaskType.PNG_TO_PDF);
    }

    public void startOCR() throws Exception {
        setDataAndSendMessage(new OCRIntent(), TaskType.OCR);
    }

    @Override
    public void handleMessage(MicroMessage message) {
        ServiceIntent intent = message.getIntent();
        String worker = intent.getWorker();
        String result = intent.getResult();
        Logger.getAnonymousLogger().log(Level.INFO, worker + ": " + result);

        Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
        responseToClient.putExtra("worker", worker);
        responseToClient.putExtra("result", result);
        responseToClient.putExtra("duration", System.nanoTime() - intent.getStartTime());
        responseToClient.putExtra("batteryPercentage", PowerTutorHelper.getPercentageUsageOfBattery(mContext, intent.getStartBattery()));
        responseToClient.putExtra("intentType", CLASS_INTENT_MAP.get(message.getIntent().getClass()));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(responseToClient);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}

package pl.edu.agh.jkolodziej.micro.agent.role.provider;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.collect.Maps;

import org.nzdis.micro.DefaultSocialRole;
import org.nzdis.micro.MicroMessage;

import java.util.Map;

import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ConvertPngToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;

/**
 * @author - Jakub Kołodziej
 */

public class AWSProviderRole extends DefaultSocialRole {

    private static final Map<Class<?>, String> CLASS_INTENT_MAP = Maps.newHashMap();

    static {
        CLASS_INTENT_MAP.put(AddIntent.class, "addIntent");
        CLASS_INTENT_MAP.put(AddingFromFileIntent.class, "addingFromFileIntent");
        CLASS_INTENT_MAP.put(ConvertPngToPDFIntent.class, "convertingPNGToPDF");
        CLASS_INTENT_MAP.put(OCRIntent.class, "OCR");

    }

    private final Context context;

    public AWSProviderRole(Context context) {
        this.context = context;
    }


    @Override
    public void handleMessage(MicroMessage message) {
        ServiceIntent intent = message.getIntent();
        intent.setWorker("AWS");
        Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
        responseToClient.putExtra(CLASS_INTENT_MAP.get(message.getIntent().getClass()), intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(responseToClient);
//        if (message.getIntent().getClass().equals(AddIntent.class)) {
//            AddIntent intentFromMessage = message.getIntent();
//            intentFromMessage.setWorker("AWS");
//            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
//            responseToClient.putExtra("addIntent", intentFromMessage);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(responseToClient);
//        } else if (message.getIntent().getClass().equals(AddingFromFileIntent.class)) {
//            AddingFromFileIntent intentFromMessage = message.getIntent();
//            intentFromMessage.setWorker("AWS");
//            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
//            responseToClient.putExtra("addingFromFileIntent", intentFromMessage);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(responseToClient);
//        } else if (message.getIntent().getClass().equals(ConvertPngToPDFIntent.class)) {
//            ConvertPngToPDFIntent intentFromMessage = message.getIntent();
//            intentFromMessage.setWorker("AWS");
//            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
//            responseToClient.putExtra("convertingPNGToPDF", intentFromMessage);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(responseToClient);
//        } else if (message.getIntent().getClass().equals(OCRIntent.class)) {
//            OCRIntent intentFromMessage = message.getIntent();
//            intentFromMessage.setWorker("AWS");
//            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
//            responseToClient.putExtra("OCR", intentFromMessage);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(responseToClient);
//        }
    }

    @Override
    protected void initialize() {
        addApplicableIntent(AddIntent.class);
    }

    @Override
    protected void release() {
    }
}

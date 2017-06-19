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
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.PNGToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;

/**
 * @author - Jakub Kołodziej
 */

public class AWSProviderAgent extends DefaultSocialRole {

    private static final Map<Class<?>, String> CLASS_INTENT_MAP = Maps.newHashMap();

    static {
        CLASS_INTENT_MAP.put(AddIntent.class, "addIntent");
        CLASS_INTENT_MAP.put(AddingFromFileIntent.class, "addingFromFileIntent");
        CLASS_INTENT_MAP.put(PNGToPDFIntent.class, "convertingPNGToPDF");
        CLASS_INTENT_MAP.put(OCRIntent.class, "OCR");

    }

    private final Context context;

    public AWSProviderAgent(Context context) {
        this.context = context;
    }


    @Override
    public void handleMessage(MicroMessage message) {
        ServiceIntent intent = message.getIntent();
        intent.setWorker("AWS");
        Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
        responseToClient.putExtra(CLASS_INTENT_MAP.get(message.getIntent().getClass()), intent);
        responseToClient.putExtra("message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(responseToClient);
    }

    @Override
    protected void initialize() {
        addApplicableIntent(AddIntent.class);
    }

    @Override
    protected void release() {
    }
}

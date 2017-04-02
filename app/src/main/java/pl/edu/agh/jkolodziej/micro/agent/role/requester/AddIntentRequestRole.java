package pl.edu.agh.jkolodziej.micro.agent.role.requester;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.nzdis.micro.DefaultSocialRole;
import org.nzdis.micro.MicroMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.PowerTutorHelper;
import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddIntent;
import pl.edu.agh.mm.energy.PowerTutorFacade;

/**
 * @author - Jakub Kołodziej
 */

public class AddIntentRequestRole extends DefaultSocialRole {

    private Double sub1;
    private Double sub2;
    private final Context mContext;

    public AddIntentRequestRole(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void initialize() {

    }

    public void start() {
        MicroMessage message = new MicroMessage();
        AddIntent intent = new AddIntent();
        intent.setData(sub1 + ";" + sub2);
        intent.setStartTime(System.nanoTime());
        intent.setStartBattery(PowerTutorFacade.getInstance(mContext, "energy").getTotalPowerForUid());
        message.setIntent(intent);
        sendGlobalBroadcast(message);
    }

    @Override
    public void handleMessage(MicroMessage message) {
        AddIntent intent = message.getIntent();
        String worker = intent.getWorker();
        String result = intent.getResult();
        Logger.getAnonymousLogger().log(Level.INFO, worker + ": " + result);
        Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
        responseToClient.putExtra("worker", worker);
        responseToClient.putExtra("result", result);
        responseToClient.putExtra("duration", System.nanoTime() - intent.getStartTime());
        responseToClient.putExtra("batteryPercentage", PowerTutorHelper.getPercentageUsageOfBattery(mContext, intent.getStartBattery()));
        responseToClient.putExtra("intentType", IntentType.ADDING);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(responseToClient);
    }

    @Override
    protected void release() {
    }

    public void setSub2(Double sub2) {
        this.sub2 = sub2;
    }

    public void setSub1(Double sub1) {
        this.sub1 = sub1;
    }
}

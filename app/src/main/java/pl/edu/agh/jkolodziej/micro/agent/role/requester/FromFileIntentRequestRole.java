package pl.edu.agh.jkolodziej.micro.agent.role.requester;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.nzdis.micro.DefaultSocialRole;
import org.nzdis.micro.MicroMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.CipherDataHelper;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ConvertPngToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;

/**
 * Created by Kołacz.
 */

public class FromFileIntentRequestRole extends DefaultSocialRole {
    private final Context mContext;
    private byte[] bytes;

    public FromFileIntentRequestRole(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void initialize() {
    }

    @Override
    protected void release() {
    }

    private void setDataAndSendMessage(ServiceIntent intent) throws Exception {
        MicroMessage message = new MicroMessage();
        intent.setData(CipherDataHelper.encryptByteArray(bytes));
        intent.setStartTime(System.nanoTime());
        message.setIntent(intent);
        sendGlobalBroadcast(message);
    }

    public void startAddingFromFile() throws Exception {
        setDataAndSendMessage(new AddingFromFileIntent());
//        MicroMessage message = new MicroMessage();
//        AddingFromFileIntent intent = new AddingFromFileIntent();
//        intent.setData(CipherDataHelper.encryptByteArray(bytes));
//        intent.setStartTime(System.nanoTime());
//        message.setIntent(intent);
//        sendGlobalBroadcast(message);
    }

    public void startPNGToPDF() throws Exception {
        setDataAndSendMessage(new ConvertPngToPDFIntent());
//        MicroMessage message = new MicroMessage();
//        ConvertPngToPDFIntent intent = new ConvertPngToPDFIntent();
//        intent.setData(CipherDataHelper.encryptByteArray(bytes));
//        intent.setStartTime(System.nanoTime());
//        message.setIntent(intent);
//        sendGlobalBroadcast(message);
    }

    public void startOCR() throws Exception {
        setDataAndSendMessage(new OCRIntent());
//        MicroMessage message = new MicroMessage();
//        OCRIntent intent = new OCRIntent();
//        intent.setData(CipherDataHelper.encryptByteArray(bytes));
//        intent.setStartTime(System.nanoTime());
//        message.setIntent(intent);
//        sendGlobalBroadcast(message);
    }

    @Override
    public void handleMessage(MicroMessage message) {
        if (message.getIntent().getClass().equals(AddingFromFileIntent.class)) {
            String worker = ((AddingFromFileIntent) message.getIntent()).getWorker();
            String result = ((AddingFromFileIntent) message.getIntent()).getResult();
            Logger.getAnonymousLogger().log(Level.INFO, worker + ": " + result);
            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
            responseToClient.putExtra("worker", worker);
            responseToClient.putExtra("result", result.toString());
            responseToClient.putExtra("duration", System.nanoTime() - ((AddingFromFileIntent) message.getIntent()).getStartTime());
            responseToClient.putExtra("intentType", IntentType.ADDING_FROM_FILE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(responseToClient);
        } else if (message.getIntent().getClass().equals(ConvertPngToPDFIntent.class)) {
            String worker = ((ConvertPngToPDFIntent) message.getIntent()).getWorker();
            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
            responseToClient.putExtra("worker", worker);
            responseToClient.putExtra("result", "Done ;-)");
            responseToClient.putExtra("duration", System.nanoTime() - ((ConvertPngToPDFIntent) message.getIntent()).getStartTime());
            responseToClient.putExtra("intentType", IntentType.PNG_TO_PDF);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(responseToClient);
        } else if (message.getIntent().getClass().equals(OCRIntent.class)) {
            String worker = ((OCRIntent) message.getIntent()).getWorker();
            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
            responseToClient.putExtra("worker", worker);
            responseToClient.putExtra("result", ((OCRIntent) message.getIntent()).getResult());
            responseToClient.putExtra("duration", System.nanoTime() - ((OCRIntent) message.getIntent()).getStartTime());
            responseToClient.putExtra("intentType", IntentType.OCR);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(responseToClient);
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}

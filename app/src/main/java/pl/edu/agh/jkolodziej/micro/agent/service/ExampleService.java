package pl.edu.agh.jkolodziej.micro.agent.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.io.ByteStreams;

import org.nzdis.micro.SystemAgentLoader;
import org.nzdis.micro.bootloader.MicroConfigLoader;

import java.io.IOException;
import java.io.InputStream;

import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.Action;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.role.provider.AWSProviderRole;
import pl.edu.agh.jkolodziej.micro.agent.role.provider.AndroidProviderRole;
import pl.edu.agh.jkolodziej.micro.agent.role.requester.AddIntentRequestRole;
import pl.edu.agh.jkolodziej.micro.agent.role.requester.FromFileIntentRequestRole;

/**
 * @author - Jakub Kołodziej
 */

public class ExampleService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ExampleService(String name) {
        super(name);
    }

    public ExampleService() {
        super("SERVICE");
    }

    public static AddIntentRequestRole addingClient = null;
    public static FromFileIntentRequestRole fromFileClient = null;

    @Override
    protected void onHandleIntent(Intent intent) {
        Action action = (Action) intent.getSerializableExtra("action");
        IntentType intentType = (IntentType) intent.getSerializableExtra("intentType");
        if (Action.RUN_PROVIDER == action) {
            SystemAgentLoader.newAgent(new AndroidProviderRole("android"), "android");
            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
            responseToClient.putExtra("provider_run", true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseToClient);
        } else if (Action.RUN_CLIENT == action) {
            makeClientAction(intentType, intent);
        } else if (Action.RUN_AWS_PROVIDER == action) {
            SystemAgentLoader.newAgent(new AWSProviderRole(this), "AWS");
            Intent responseToClient = new Intent(MainActivity.ResponseFromServiceReceiver.RESPONSE);
            responseToClient.putExtra("provider_aws_run", true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(responseToClient);
        }
    }

    public void makeClientAction(IntentType intentType, Intent intent) {
        ClassLoader classLoader;
        InputStream stream;
        byte[] bytes;
        switch (intentType) {
            case ADDING:
                if (addingClient == null) {
                    addingClient = new AddIntentRequestRole(this);
                    SystemAgentLoader.newAgent(addingClient, "requester-android-add");
                }
                addingClient.setSub1(intent.getDoubleExtra("sub1", 0.0));
                addingClient.setSub2(intent.getDoubleExtra("sub2", 0.0));
                addingClient.start();
                break;
            case ADDING_FROM_FILE:
                if (fromFileClient == null) {
                    fromFileClient = new FromFileIntentRequestRole(this);
                    SystemAgentLoader.newAgent(fromFileClient, "requester-android-from-file");
                }
                classLoader = MicroConfigLoader.class.getClassLoader();
                stream = classLoader.getResourceAsStream("add/test1.add");
                try {
                    bytes = new byte[stream.available()];
                    stream.read(bytes);
                    fromFileClient.setBytes(bytes);
                    fromFileClient.startAddingFromFile();
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
                break;
            case PNG_TO_PDF:
                if (fromFileClient == null) {
                    fromFileClient = new FromFileIntentRequestRole(this);
                    SystemAgentLoader.newAgent(fromFileClient, "requester-android-from-file");
                }
                classLoader = MicroConfigLoader.class.getClassLoader();
                stream = classLoader.getResourceAsStream("png/sample4.png");
                try {
                    fromFileClient.setBytes(ByteStreams.toByteArray(stream));
                    fromFileClient.startPNGToPDF();
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
                break;
            case OCR:
                if (fromFileClient == null) {
                    fromFileClient = new FromFileIntentRequestRole(this);
                    SystemAgentLoader.newAgent(fromFileClient, "requester-android-from-file");
                }
                classLoader = MicroConfigLoader.class.getClassLoader();
                String fileName = intent.getStringExtra("fileName");
                stream = classLoader.getResourceAsStream("ocr/" + fileName);
                try {
                    fromFileClient.setBytes(ByteStreams.toByteArray(stream));
                    fromFileClient.startOCR();
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
                break;
            default:
                break;
        }
    }
}
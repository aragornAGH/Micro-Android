package pl.edu.agh.jkolodziej.micro.agent.aws;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.agent.BatteryUtils;
import pl.edu.agh.jkolodziej.micro.agent.PowerTutorHelper;
import pl.edu.agh.jkolodziej.micro.agent.R;
import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.helpers.TestSettings;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ConvertPngToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;
import pl.edu.agh.jkolodziej.micro.agent.role.requester.FromFileIntentWekaRequestRole;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import pl.edu.agh.jkolodziej.micro.weka.test.Measurement;
import pl.edu.agh.mm.energy.PowerTutorFacade;

/**
 * @author Jakub Kołodziej
 */

public class AWSLambdaRequester {
    private static ServiceIntent serviceIntent = null;

    public static boolean requestIfNeeded(final Intent intent, final LambdaInterface myInterface, final Context mContext, final ListView list) {

        if (intent.getSerializableExtra("addIntent") != null) {
            AddIntent addIntent = (AddIntent) intent.getSerializableExtra("addIntent");
            serviceIntent = addIntent;
            new AsyncTask<AddIntent, Void, AddIntent>() {
                @Override
                protected AddIntent doInBackground(AddIntent... params) {
                    try {
                        return myInterface.handleRequest(params[0]);
                    } catch (LambdaFunctionException lfe) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(AddIntent result) {
                    AWSLambdaRequester.onPostExecute(intent, serviceIntent, result, mContext, IntentType.ADDING, list);
                }
            }.execute(addIntent);
            return true;

        } else if (intent.getSerializableExtra("addingFromFileIntent") != null) {
            AddingFromFileIntent addingFromFileIntent = (AddingFromFileIntent) intent.getSerializableExtra("addingFromFileIntent");
            serviceIntent = addingFromFileIntent;
            new AsyncTask<AddingFromFileIntent, Void, AddingFromFileIntent>() {
                @Override
                protected AddingFromFileIntent doInBackground(AddingFromFileIntent... params) {
                    try {
                        return myInterface.handleRequest(params[0]);
                    } catch (LambdaFunctionException lfe) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(AddingFromFileIntent result) {
                    AWSLambdaRequester.onPostExecute(intent, serviceIntent, result, mContext, IntentType.ADDING_FROM_FILE, list);
                }
            }.execute(addingFromFileIntent);
            return true;
        } else if (intent.getSerializableExtra("convertingPNGToPDF") != null) {
            ConvertPngToPDFIntent convertPNGToPDFIntent = (ConvertPngToPDFIntent) intent.getSerializableExtra("convertingPNGToPDF");
            serviceIntent = convertPNGToPDFIntent;
            new AsyncTask<ConvertPngToPDFIntent, Void, ConvertPngToPDFIntent>() {
                @Override
                protected ConvertPngToPDFIntent doInBackground(ConvertPngToPDFIntent... params) {
                    try {
                        return myInterface.handleRequest(params[0]);
                    } catch (LambdaFunctionException lfe) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(ConvertPngToPDFIntent result) {
                    AWSLambdaRequester.onPostExecute(intent, serviceIntent, result, mContext, IntentType.PNG_TO_PDF, list);
                }
            }.execute(convertPNGToPDFIntent);
            return true;
        } else if (intent.getSerializableExtra("OCR") != null) {
            OCRIntent ocrIntent = (OCRIntent) intent.getSerializableExtra("OCR");
            serviceIntent = ocrIntent;
            new AsyncTask<OCRIntent, Void, OCRIntent>() {
                @Override
                protected OCRIntent doInBackground(OCRIntent... params) {
                    try {
                        return myInterface.handleRequest(params[0]);
                    } catch (LambdaFunctionException lfe) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(OCRIntent result) {
                    AWSLambdaRequester.onPostExecute(intent, serviceIntent, result, mContext, IntentType.OCR, list);
                }
            }.execute(ocrIntent);
            return true;
        }
        return false;
    }


    public static void onPostExecute(Intent intent, ServiceIntent serviceIntent, ServiceIntent result, Context mContext, IntentType intentType, ListView list) {
        if (result == null) {
            return;
        }
        Long endTime = System.currentTimeMillis();
        Long duration = endTime - result.getStartTime();
        if (intent.getSerializableExtra("message") != null && serviceIntent.getTaskDestination() != null) {
            LearningParameters params = new LearningParameters(serviceIntent.getTaskType());
            params.setDestination(serviceIntent.getTaskDestination().name());
            params.setConnectionType(serviceIntent.getConnectionType());
            params.setFileSize(serviceIntent.getFileSize());
            params.setResolution(serviceIntent.getResolution());
            params.setWifiStrength(serviceIntent.getWifiPowerSignal());

            long batteryState = PowerTutorFacade.getInstance(mContext, "energy").getTotalPowerForUid();
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = mContext.registerReceiver(null, ifilter);
            double voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0;
            double percentageUsageOfbattery = BatteryUtils.getPercentageUsageOfBattery(mContext, voltage,
                    batteryState - serviceIntent.getStartBattery());

            FromFileIntentWekaRequestRole.testsContext.appendResult(params,
                    new Measurement.Result(false, endTime - serviceIntent.getStartTime(),
                            batteryState - serviceIntent.getStartBattery(),
                            percentageUsageOfbattery,
                            serviceIntent.getConnectionType(),
                            params),
                    result.getResult());
            Logger.getAnonymousLogger().log(Level.INFO, "Response: time - " + (endTime - serviceIntent.getStartTime()) + " ms, battery - "
                    + (batteryState - serviceIntent.getStartBattery()) + "mJ");
            try {
                Files.append("Time;" + (endTime - serviceIntent.getStartTime()) + ";Batterry;" + (batteryState - serviceIntent.getStartBattery()) + "\n",
                        TestSettings.RESULT_WRITER_FILE, Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FromFileIntentWekaRequestRole.IS_BUSY = false;
        }
        Double batteryPercentage = PowerTutorHelper.getPercentageUsageOfBattery(mContext, result.getStartBattery());
        Toast.makeText(mContext, intentType + " - " + result.getWorker() + " - " + duration + " ms; battery: " + batteryPercentage + "%", Toast.LENGTH_SHORT).show();
        MainActivity.results.add(intentType + " - " + result.getWorker() + " - " + duration + " ms; battery: " + batteryPercentage + "%");
        MainActivity.adapter = new ArrayAdapter<String>(mContext, R.layout.row_list_view, MainActivity.results);
        list.setAdapter(MainActivity.adapter);
        list.refreshDrawableState();
    }
}

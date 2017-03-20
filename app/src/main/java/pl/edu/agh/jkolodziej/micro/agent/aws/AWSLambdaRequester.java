package pl.edu.agh.jkolodziej.micro.agent.aws;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;

import pl.edu.agh.jkolodziej.micro.agent.PowerTutorHelper;
import pl.edu.agh.jkolodziej.micro.agent.R;
import pl.edu.agh.jkolodziej.micro.agent.act.MainActivity;
import pl.edu.agh.jkolodziej.micro.agent.enums.IntentType;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.AddingFromFileIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ConvertPngToPDFIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;

/**
 * Created by Kołacz.
 */

public class AWSLambdaRequester {
    public static boolean requestIfNeeded(Intent intent, final LambdaInterface myInterface, final Context mContext, final ListView list) {
        if (intent.getSerializableExtra("addIntent") != null) {
            AddIntent addIntent = (AddIntent) intent.getSerializableExtra("addIntent");
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
                    AWSLambdaRequester.onPostExecute(result, mContext, IntentType.ADDING, list);
                }
            }.execute(addIntent);
            return true;

        } else if (intent.getSerializableExtra("addingFromFileIntent") != null) {
            AddingFromFileIntent addingFromFileIntent = (AddingFromFileIntent) intent.getSerializableExtra("addingFromFileIntent");
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
                    AWSLambdaRequester.onPostExecute(result, mContext, IntentType.ADDING_FROM_FILE, list);
                }
            }.execute(addingFromFileIntent);
            return true;
        } else if (intent.getSerializableExtra("convertingPNGToPDF") != null) {
            ConvertPngToPDFIntent addingFromFileIntent = (ConvertPngToPDFIntent) intent.getSerializableExtra("convertingPNGToPDF");
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
                    AWSLambdaRequester.onPostExecute(result, mContext, IntentType.PNG_TO_PDF, list);
                }
            }.execute(addingFromFileIntent);
            return true;
        } else if (intent.getSerializableExtra("OCR") != null) {
            OCRIntent addingFromFileIntent = (OCRIntent) intent.getSerializableExtra("OCR");
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
                    AWSLambdaRequester.onPostExecute(result, mContext, IntentType.OCR, list);
                }
            }.execute(addingFromFileIntent);
            return true;
        }
        return false;
    }


    public static void onPostExecute(ServiceIntent result, Context mContext, IntentType intentType, ListView list) {
        if (result == null) {
            return;
        }
        Long duration = System.nanoTime() - result.getStartTime();
        Double batteryPercentage = PowerTutorHelper.getPercentageUsageOfBattery(mContext, result.getStartBattery());
        Toast.makeText(mContext, intentType + " - " + result.getWorker() + " - " + duration / Math.pow(10.0, 6) + " ms; battery: " + batteryPercentage + "%", Toast.LENGTH_SHORT).show();
        MainActivity.results.add(intentType + " - " + result.getWorker() + " - " + duration / Math.pow(10.0, 6) + " ms; battery: " + batteryPercentage + "%");
        MainActivity.adapter = new ArrayAdapter<String>(mContext, R.layout.row_list_view, MainActivity.results);
        list.setAdapter(MainActivity.adapter);
        list.refreshDrawableState();
    }
}

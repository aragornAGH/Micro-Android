package pl.edu.agh.jkolodziej.micro.agent.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import pl.edu.agh.jkolodziej.micro.agent.intents.ServiceIntent;

/**
 * @author - Jakub Kołodziej
 */
public class IntentParametersHelper {

    public static void setFromFileIntentParameters(ServiceIntent intent, byte[] file) {
        intent.setFileSize(Integer.valueOf(file.length).longValue());

        Bitmap bitmap = BitmapFactory.decodeByteArray(file, 0, file.length);
        intent.setResolution(Integer.valueOf(bitmap.getWidth() * bitmap.getHeight()).longValue());
        bitmap.recycle();
    }
}

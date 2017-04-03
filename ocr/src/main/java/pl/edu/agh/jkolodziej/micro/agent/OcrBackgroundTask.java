package pl.edu.agh.jkolodziej.micro.agent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * @author - Jakub Kołodziej
 */
public class OcrBackgroundTask {

    public String doInBackground(byte[] imageFile, String tessDataPath) {
        TessBaseAPI baseAPI = new TessBaseAPI();
        baseAPI.init(tessDataPath, "pol", TessBaseAPI.OEM_TESSERACT_ONLY);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        baseAPI.setImage(BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length, options));

        return baseAPI.getUTF8Text().replaceAll("[^A-Za-z0-9 \n]", "");
    }
}

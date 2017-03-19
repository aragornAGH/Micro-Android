package pl.edu.agh.jkolodziej.micro.agent.wrapper;

import com.google.common.io.Files;

import org.nzdis.micro.constants.OperatingSystems;
import org.nzdis.micro.constants.PlatformConstants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.edu.agh.jkolodziej.micro.agent.helpers.AWSFileKeeper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.AndroidFilesSaverHelper;
import pl.edu.agh.jkolodziej.micro.agent.helpers.CipherDataHelper;
import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.OcrBackgroundTask;

import static org.nzdis.micro.bootloader.MicroBootProperties.bootProperties;

/**
 * @author Jakub Kołodziej
 *         Wrapper class to wrap OCR mechanism in Android environment with usage TessTwo library
 */

public class AndroidOCRWrapper {
    private static final String TMP_FILENAME = "tmp_%s.jpg";

    private final OCRIntent ocrIntent;

    public AndroidOCRWrapper(OCRIntent ocrIntent) {
        this.ocrIntent = ocrIntent;
    }

    public void makeService() {
        File tmpFile = null;
        try {
            byte[] bytes = CipherDataHelper.decryptByteArray(ocrIntent.getData());
            String filePath = null;
            if (!AWSFileKeeper.DIRECTORY.equals("")) {
                filePath = AWSFileKeeper.DIRECTORY + "/";
            } else {
                filePath = bootProperties.getProperty(PlatformConstants.OPERATING_SYSTEM).equals(
                        OperatingSystems.ANDROID) ? (AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/") : "";
            }
            tmpFile = new File(filePath + String.format(TMP_FILENAME, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())));
            Files.write(bytes, tmpFile);
            ocrIntent.setResult(ocr(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    public String ocr(byte[] imageBytes) throws Exception {
        return new OcrBackgroundTask().doInBackground(imageBytes, AndroidFilesSaverHelper.INTERNAL_DIRECTORY + File.separator + "test" + File.separator);
    }

    public OCRIntent getOcrIntent() {
        return ocrIntent;
    }
}

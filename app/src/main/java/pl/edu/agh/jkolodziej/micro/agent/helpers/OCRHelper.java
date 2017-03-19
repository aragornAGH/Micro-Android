package pl.edu.agh.jkolodziej.micro.agent.helpers;

import com.google.common.io.Files;

import org.nzdis.micro.bootloader.MicroConfigLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Kołodziej
 *         Helper class which copy TessData file for OCR algorithm from resources to SD Card
 */

public class OCRHelper {
    public static final String TESS_DATA_FILENAME = "pol.traineddata";

    public static void copyTessData() {
        ClassLoader classLoader = MicroConfigLoader.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(TESS_DATA_FILENAME);
        byte[] buffer;
        try {
            buffer = new byte[stream.available()];

            stream.read(buffer);

            new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/tessdata").mkdir();

            String pathName = AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/tessdata/" + TESS_DATA_FILENAME;

            File file = new File(pathName);
            Files.write(buffer, file);
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Error during copy tessdata file");
            e.printStackTrace();
        }
    }
}

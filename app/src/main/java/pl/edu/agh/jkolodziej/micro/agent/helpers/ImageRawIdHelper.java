package pl.edu.agh.jkolodziej.micro.agent.helpers;

import pl.edu.agh.jkolodziej.micro.agent.R;

/**
 * @author - Jakub Kołodziej
 */
public final class ImageRawIdHelper {

    public static int getRawId(String fileName) {
        if (fileName.equals("sample_ocr.jpg")) {
            return R.raw.sample_ocr;
        } else if (fileName.equals("sample_ocr2.jpg")) {
            return R.raw.sample_ocr2;
        } else if (fileName.equals("sample_ocr3.jpg")) {
            return R.raw.sample_ocr3;
        } else if (fileName.equals("sample_ocr4.jpg")) {
            return R.raw.sample_ocr4;
        } else if (fileName.equals("sample_ocr5.jpg")) {
            return R.raw.sample_ocr5;
        } else if (fileName.equals("sample_ocr6.jpg")) {
            return R.raw.sample_ocr6;
        } else if (fileName.equals("sample_ocr7.jpg")) {
            return R.raw.sample_ocr7;
        } else if (fileName.equals("sample_ocr8.jpg")) {
            return R.raw.sample_ocr8;
        } else if (fileName.equals("sample_ocr9.jpg")) {
            return R.raw.sample_ocr9;
        } else if (fileName.equals("sample_ocr10.jpg")) {
            return R.raw.sample_ocr10;
        }
        return 0;
    }
}

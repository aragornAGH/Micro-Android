package pl.edu.agh.jkolodziej.micro.agent.role.provider;

import org.nzdis.micro.MicroMessage;

import pl.edu.agh.jkolodziej.micro.agent.intents.OCRIntent;
import pl.edu.agh.jkolodziej.micro.agent.roles.ProviderRole;
import pl.edu.agh.jkolodziej.micro.agent.wrapper.AndroidOCRWrapper;

/**
 * @author Jakub Kołodziej.
 *         Provider which is resposible for  make specific services on Android side
 *         eg. OCR
 */

public class AndroidProviderRole extends ProviderRole {

    public AndroidProviderRole(String workerName) {
        super(workerName);
    }

    @Override
    public MicroMessage getReply(MicroMessage message) {
        MicroMessage reply = super.getReply(message);
        if (reply.getIntent() == null) {
            if (message.getIntent().getClass().equals(OCRIntent.class)) {
                OCRIntent intent = message.getIntent();
                intent.setWorker(workerName);
                AndroidOCRWrapper ocrWrapper = new AndroidOCRWrapper(intent);
                ocrWrapper.makeService();
                intent = ocrWrapper.getOcrIntent();
                reply.setIntent(intent);
            }
        }
        return reply;
    }
}

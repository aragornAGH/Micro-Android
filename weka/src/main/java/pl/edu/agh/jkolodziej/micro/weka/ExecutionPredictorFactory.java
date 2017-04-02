package pl.edu.agh.jkolodziej.micro.weka;

import pl.edu.agh.jkolodziej.micro.weka.managers.KnowledgeInstanceManager;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import pl.edu.agh.jkolodziej.micro.weka.predictors.ExecutionPredictor;
import pl.edu.agh.jkolodziej.micro.weka.predictors.ExecutionPredictorImpl;

/**
 * @author - Jakub Kołodziej
 */
public class ExecutionPredictorFactory {

    private ExecutionPredictorFactory() {
    }

    public static ExecutionPredictor<LearningParameters> createPredictor(KnowledgeInstanceManager timeInstanceManager,
                                                                         KnowledgeInstanceManager batteryInstanceManager) {
        return new ExecutionPredictorImpl(timeInstanceManager, batteryInstanceManager);
    }
}

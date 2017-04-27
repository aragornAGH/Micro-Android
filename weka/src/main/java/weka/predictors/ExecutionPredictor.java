package weka.predictors;

import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;

/**
 * @author - Jakub Kołodziej
 */
public interface ExecutionPredictor<T> {

    TaskDestination getTaskDestination(T params, double timeWeight, double batteryWeight);
}

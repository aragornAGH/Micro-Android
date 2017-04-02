package pl.edu.agh.jkolodziej.micro.weka.predictors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;
import pl.edu.agh.jkolodziej.micro.weka.managers.KnowledgeInstanceManager;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import weka.core.Instance;

/**
 * @author - Jakub Kołodziej
 */
public class ExecutionPredictorImpl implements ExecutionPredictor<LearningParameters> {

    private final KnowledgeInstanceManager batteryInstanceManager;
    private final KnowledgeInstanceManager timeInstanceManager;
    private final Random random = new Random();

    public ExecutionPredictorImpl(KnowledgeInstanceManager timeInstanceManager, KnowledgeInstanceManager batteryInstanceManager) {
        this.batteryInstanceManager = batteryInstanceManager;
        this.timeInstanceManager = timeInstanceManager;
    }

    @Override
    public TaskDestination getTaskDestination(LearningParameters params, double timeWeight, double batteryWeight) {
        // normalize weights
        double weightSum = timeWeight + batteryWeight;
        timeWeight = timeWeight / weightSum;
        batteryWeight = batteryWeight / weightSum;

        List<TaskDestination> taskDestinations = new ArrayList<>();
        taskDestinations.add(TaskDestination.CLOUD);
        taskDestinations.add(TaskDestination.DOCKER);
        taskDestinations.add(TaskDestination.MOBILE);

        double cloudResult = getExecutionResult(params, TaskDestination.CLOUD.name(), timeWeight, batteryWeight);
        double mobileResult = getExecutionResult(params, TaskDestination.MOBILE.name(), timeWeight, batteryWeight);
        double dockerResult = getExecutionResult(params, TaskDestination.DOCKER.name(), timeWeight, batteryWeight);

        // equals result
        if (BigDecimal.valueOf(cloudResult).equals(BigDecimal.valueOf(mobileResult)) &&
                BigDecimal.valueOf(mobileResult).equals(BigDecimal.valueOf(dockerResult))) {
            return taskDestinations.get(random.nextInt(3));
        } else {
            // cloud the best
            if (BigDecimal.valueOf(Math.min(cloudResult, Math.min(dockerResult, mobileResult))).equals(BigDecimal.valueOf(cloudResult))) {
                if (cloudResult < dockerResult && cloudResult < mobileResult) {
                    return TaskDestination.CLOUD;
                    // cloud && mobile the best
                } else if (cloudResult < dockerResult && BigDecimal.valueOf(cloudResult).equals(BigDecimal.valueOf(mobileResult))) {
                    return random.nextBoolean() ? TaskDestination.CLOUD : TaskDestination.MOBILE;
                    // cloud && docker the best
                } else if (cloudResult < mobileResult && BigDecimal.valueOf(cloudResult).equals(BigDecimal.valueOf(dockerResult))) {
                    return random.nextBoolean() ? TaskDestination.CLOUD : TaskDestination.DOCKER;
                }
            }

            if (BigDecimal.valueOf(Math.min(cloudResult, Math.min(dockerResult, mobileResult))).equals(BigDecimal.valueOf(dockerResult))) {
                // docket the best
                if (dockerResult < cloudResult && dockerResult < mobileResult) {
                    return TaskDestination.DOCKER;
                    // docker && mobile the best
                } else if (dockerResult < cloudResult && BigDecimal.valueOf(dockerResult).equals(BigDecimal.valueOf(mobileResult))) {
                    return random.nextBoolean() ? TaskDestination.DOCKER : TaskDestination.MOBILE;
                    // docker && cloud the best
                } else if (dockerResult < mobileResult && BigDecimal.valueOf(dockerResult).equals(BigDecimal.valueOf(cloudResult))) {
                    return random.nextBoolean() ? TaskDestination.DOCKER : TaskDestination.CLOUD;
                }
            }


            if (BigDecimal.valueOf(Math.min(cloudResult, Math.min(dockerResult, mobileResult))).equals(BigDecimal.valueOf(mobileResult))) {
                // mobile the best
                if (mobileResult < cloudResult && mobileResult < dockerResult) {
                    return TaskDestination.MOBILE;
                    // mobile && docker the best
                } else if (mobileResult < cloudResult && BigDecimal.valueOf(mobileResult).equals(BigDecimal.valueOf(dockerResult))) {
                    return random.nextBoolean() ? TaskDestination.MOBILE : TaskDestination.DOCKER;
                    // mobile && cloud the best
                } else if (mobileResult < dockerResult && BigDecimal.valueOf(mobileResult).equals(BigDecimal.valueOf(cloudResult))) {
                    return random.nextBoolean() ? TaskDestination.MOBILE : TaskDestination.CLOUD;
                }
            }
        }
        return null;
    }

    private double getExecutionResult(LearningParameters params, String destination, double timeWeight, double batteryWeight) {
        params.setDestination(destination);

        Instance timeInstance = timeInstanceManager.createInstanceForPrediction(params);
        double timeResult = Double.MAX_VALUE;
        try {
            timeResult = timeInstanceManager.classify(timeInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instance batteryInstance = batteryInstanceManager.createInstanceForPrediction(params);
        double batteryResult = Double.MAX_VALUE;
        try {
            batteryResult = batteryInstanceManager.classify(batteryInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return timeWeight * timeResult + batteryWeight * batteryResult;
    }
}

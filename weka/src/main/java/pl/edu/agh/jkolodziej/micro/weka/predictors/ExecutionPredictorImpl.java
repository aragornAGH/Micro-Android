package pl.edu.agh.jkolodziej.micro.weka.predictors;

import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
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
        if (ConnectionType.WIFI == params.getConnectionType()
                || ConnectionType.LTE_4G == params.getConnectionType()
                || ConnectionType.UMTS_3G == params.getConnectionType()
                || ConnectionType.CDMA_2G == params.getConnectionType()) {
            taskDestinations.add(TaskDestination.CLOUD);
        }
        if (ConnectionType.WIFI == params.getConnectionType()) {
            taskDestinations.add(TaskDestination.DOCKER);
        }
        taskDestinations.add(TaskDestination.MOBILE);

        if (random.nextInt(10) == 2) {
            return taskDestinations.get(random.nextInt(taskDestinations.size()));
        }

        Map<TaskDestination, Double> unnormalizedTimeResults = Maps.newHashMap();

        double mobileTimeResult = getTimeResult(params, TaskDestination.MOBILE.name());
        double cloudTimeResult = Double.MAX_VALUE;
        double dockerTimeResult = Double.MAX_VALUE;

        unnormalizedTimeResults.put(TaskDestination.MOBILE, mobileTimeResult);

        if (taskDestinations.contains(TaskDestination.CLOUD)) {
            cloudTimeResult = getTimeResult(params, TaskDestination.CLOUD.name());
        }
        unnormalizedTimeResults.put(TaskDestination.CLOUD, cloudTimeResult);
        if (taskDestinations.contains(TaskDestination.DOCKER)) {
            dockerTimeResult = getTimeResult(params, TaskDestination.DOCKER.name());
        }
        unnormalizedTimeResults.put(TaskDestination.DOCKER, dockerTimeResult);

        Map<TaskDestination, Double> unnormalizedBatteryResults = Maps.newHashMap();

        double mobileBatteryResult = getBatteryResult(params, TaskDestination.MOBILE.name());
        double cloudBatteryResult = Double.MAX_VALUE;
        double dockerBatteryResult = Double.MAX_VALUE;

        unnormalizedBatteryResults.put(TaskDestination.MOBILE, mobileBatteryResult);

        if (taskDestinations.contains(TaskDestination.CLOUD)) {
            cloudBatteryResult = getBatteryResult(params, TaskDestination.CLOUD.name());
        }
        unnormalizedTimeResults.put(TaskDestination.CLOUD, cloudBatteryResult);
        if (taskDestinations.contains(TaskDestination.DOCKER)) {
            dockerBatteryResult = getBatteryResult(params, TaskDestination.DOCKER.name());
        }
        unnormalizedTimeResults.put(TaskDestination.DOCKER, dockerBatteryResult);

        Map<TaskDestination, Double> normalizedBatteryResults = normalizeResults(unnormalizedBatteryResults);
        Map<TaskDestination, Double> normalizedTimeResults = normalizeResults(unnormalizedTimeResults);

        double mobileResult = timeWeight * normalizedTimeResults.get(TaskDestination.MOBILE) + batteryWeight * normalizedBatteryResults.get(TaskDestination.MOBILE);
        double cloudResult = timeWeight * normalizedTimeResults.get(TaskDestination.CLOUD) + batteryWeight * normalizedBatteryResults.get(TaskDestination.CLOUD);
        double dockerResult = timeWeight * normalizedTimeResults.get(TaskDestination.DOCKER) + batteryWeight * normalizedBatteryResults.get(TaskDestination.DOCKER);

        // equals result
        if (BigDecimal.valueOf(cloudResult).equals(BigDecimal.valueOf(mobileResult)) &&
                BigDecimal.valueOf(mobileResult).equals(BigDecimal.valueOf(dockerResult))) {
            return taskDestinations.get(random.nextInt(taskDestinations.size()));
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

    private Map<TaskDestination, Double> normalizeResults(Map<TaskDestination, Double> unnormalizedResults) {
        double maxValue = 0.0;
        for (Map.Entry<TaskDestination, Double> entry : unnormalizedResults.entrySet()) {
            if (Double.MAX_VALUE != entry.getValue() && maxValue < entry.getValue()) {
                maxValue = entry.getValue();
            }
        }
        for (Map.Entry<TaskDestination, Double> entry : unnormalizedResults.entrySet()) {
            if (Double.MAX_VALUE != entry.getValue()) {
                unnormalizedResults.put(entry.getKey(), entry.getValue() / maxValue);
            }
        }
        return unnormalizedResults;
    }

    private double getTimeResult(LearningParameters params, String destination) {
        params.setDestination(destination);

        Instance timeInstance = timeInstanceManager.createInstanceForPrediction(params);
        try {
            return timeInstanceManager.classify(timeInstance);
        } catch (Exception e) {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
    }

    private double getBatteryResult(LearningParameters params, String destination) {
        params.setDestination(destination);

        Instance batteryInstance = batteryInstanceManager.createInstanceForPrediction(params);
        try {
            return batteryInstanceManager.classify(batteryInstance);
        } catch (Exception e) {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
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

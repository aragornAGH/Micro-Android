package pl.edu.agh.jkolodziej.micro.weka.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;

/**
 * @author - Jakub Kołodziej
 */
public class AggregatedRoundResult {
    private final int roundNumber;
    private final Map<Integer, List<Long>> results = new HashMap<>();
    private int maxSequence = 0;
    private Set<Integer> sequences = new HashSet<>();
    private int nrOfCloudTasks = 0;
    private int nrOfLocalTasks = 0;
    private int nrOfDockerTasks = 0;

    public AggregatedRoundResult(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void addResultForSequence(int sequence, long result, String destination) {
        initIfNeeded(sequence);
        results.get(sequence).add(result);
        if (sequence > maxSequence)
            maxSequence = sequence;
        sequences.add(sequence);

        if (destination.equals(TaskDestination.CLOUD.name())) {
            nrOfCloudTasks++;
        } else if (destination.equals(TaskDestination.MOBILE.name())) {
            nrOfLocalTasks++;
        } else {
            nrOfDockerTasks++;
        }
    }

    private void initIfNeeded(int sequence) {
        if (results.get(sequence) == null) {
            results.put(sequence, new ArrayList<Long>());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(roundNumber);
        sb.append(";");

        double sum = 0;

        for (int i = 0; i <= maxSequence; i++) {
            if (!sequences.contains(i))
                continue;

            List<Long> actionResults = results.get(i);
            if (actionResults == null)
                continue;

            double average = getAverage(actionResults);
            sum += average;
            sb.append(average);
            sb.append(";");
        }

        sb.append(sum / results.keySet().size());

        sb.append(";" + nrOfCloudTasks);
        sb.append(";" + nrOfLocalTasks);
        sb.append(";" + nrOfDockerTasks);

        sb.append("\n");

        return sb.toString();
    }

    private double getAverage(List<Long> result) {
        long sum = 0;
        for (Long singleResult : result) {
            sum += singleResult;
        }
        return sum / result.size();
    }
}

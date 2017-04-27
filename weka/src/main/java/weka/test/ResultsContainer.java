package weka.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;

/**
 * @author - Jakub Kołodziej
 */
public class ResultsContainer {
    private Map<Pointer, ResultData> results = new LinkedHashMap<>();

    private int lastRound;
    private int lastSeries;

    public void addResult(Pointer p, LearningParameters params, Measurement.Result result, Object taskResult) {
        lastRound = p.round >= lastRound ? p.round : lastRound;
        if (p.series > lastSeries) {
            lastSeries = p.series;
            lastRound = 0;
        }
        results.put(p, new ResultData(params, result, taskResult));
    }

    public Map<Pointer, ResultData> getResults() {
        fixResults();
        return results;
    }

    public List<ResultData> getLastRoundResults() {
        fixResults();
        return getSpecificRoundResults(lastRound, lastSeries);
    }

    public void fixResults() {
        Map<Pointer, ResultData> fixedResult = new LinkedHashMap<>();
        for (Map.Entry<Pointer, ResultData> entry : results.entrySet()) {
            Pointer pointer = entry.getKey();
            Measurement.Result result = entry.getValue().getResult();

            if (!result.isErrorOccured()) {
                fixedResult.put(entry.getKey(), entry.getValue());
            } else {
                LearningParameters params = result.getParams();
                CalculatedPenalty penalty = calculatePenaltyForRound(pointer.getRound(), pointer.getSeries(),
                        params.getFileSize(), params.getTaskType());
                Measurement.Result resultWithPenalty = new Measurement.Result(result.isIgnored(),
                        penalty.getTime(), penalty.getEnergy(), penalty.getPercentOfBattery(), result.getConnectionType(), params);

                fixedResult.put(entry.getKey(), new ResultData(params, resultWithPenalty, entry.getValue().getTaskResult()));
            }
        }
        results = fixedResult;
    }

    private CalculatedPenalty calculatePenaltyForRound(int round, int series, long problemSize, TaskType type) {
        long maxTime = 0;
        long maxEnergy = 0;
        double maxPercentOfBattery = 0;
        List<ResultData> results = getSpecificRoundResults(round, series);
        for (ResultData resultData : results) {
            Measurement.Result result = resultData.getResult();
            if (!result.isErrorOccured() && result.getParams().getFileSize() == problemSize
                    && result.getParams().getTaskType() == type) {
                maxTime = result.getTime();
                maxEnergy = result.getEnergy();
                maxPercentOfBattery = result.getPercentageUsageOfBattery();
            }
        }
        if (maxEnergy == 0) {
            throw new IllegalStateException(String.format("Unable to calculate penalty. " +
                    "No correct result in round: %d, series: %d", round, series));
        }
        return new CalculatedPenalty(maxTime, maxEnergy, maxPercentOfBattery);
    }

    private List<ResultData> getSpecificRoundResults(int round, int series) {
        List<ResultData> resultsList = new ArrayList<>();
        for (Map.Entry<Pointer, ResultData> entry : results.entrySet()) {
            Pointer p = entry.getKey();
            if (p.getRound() == round && p.getSeries() == series) {
                resultsList.add(results.get(p));
            }
        }
        return resultsList;
    }

    public static class ResultData {
        private final LearningParameters params;
        private final Measurement.Result result;
        private final Object taskResult;

        public ResultData(LearningParameters params, Measurement.Result result, Object taskResult) {
            this.params = params;
            this.result = result;
            this.taskResult = taskResult;
        }

        public LearningParameters getParams() {
            return params;
        }

        public Measurement.Result getResult() {
            return result;
        }

        public Object getTaskResult() {
            return taskResult;
        }
    }

    public static class Pointer {
        private final int round;
        private final int series;
        private final int sequenceNumber;

        public Pointer(int round, int series, int sequenceNumber) {
            this.round = round;
            this.series = series;
            this.sequenceNumber = sequenceNumber;
        }

        public int getRound() {
            return round;
        }

        public int getSeries() {
            return series;
        }

        public int getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public String toString() {
            return "Pointer{" +
                    "round=" + round +
                    ", series=" + series +
                    ", sequenceNumber=" + sequenceNumber +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pointer pointer = (Pointer) o;

            if (round != pointer.round) return false;
            if (series != pointer.series) return false;
            return sequenceNumber == pointer.sequenceNumber;

        }

        @Override
        public int hashCode() {
            int result = round;
            result = 31 * result + series;
            result = 31 * result + sequenceNumber;
            return result;
        }
    }

}

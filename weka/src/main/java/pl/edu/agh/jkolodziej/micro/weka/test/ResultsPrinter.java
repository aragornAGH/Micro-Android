package pl.edu.agh.jkolodziej.micro.weka.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author - Jakub Kołodziej
 */
public class ResultsPrinter {
    private final ResultsContainer results;
    private final File output;
    private Map<Integer, AggregatedRoundResult> timeResults;
    private Map<Integer, AggregatedRoundResult> batteryResults;
    private int maxRound = 0;
    private int maxSequence = 0;
    private Set<Integer> sequences;

    public ResultsPrinter(File output, ResultsContainer results) {
        this.output = output;
        this.results = results;
    }

    public void saveToFile() throws FileNotFoundException {
        generateResults();

        PrintWriter printWriter = new PrintWriter(output);
        printWriter.write("Time result\n");
        writeHeader(printWriter);
        writeResults(printWriter, timeResults);
        printWriter.write("Battery result\n");
        writeHeader(printWriter);
        writeResults(printWriter, batteryResults);

        printWriter.flush();
        printWriter.close();
    }

    private void writeHeader(PrintWriter printWriter) {
        StringBuilder sb = new StringBuilder();
        sb.append("Round;");

        for (int i = 0; i <= maxSequence; i++) {
            if (!sequences.contains(i))
                continue;

            sb.append("Action ");
            sb.append(i);
            sb.append(" average;");
        }

        sb.append("Round average");
        sb.append(";Cloud tasks");
        sb.append(";Local tasks");
        sb.append(";Docker tasks\n");

        printWriter.write(sb.toString());
    }

    private void writeResults(PrintWriter printWriter, Map<Integer, AggregatedRoundResult> results) {
        for (int i = 0; i <= maxRound; i++) {
            AggregatedRoundResult roundResult = results.get(i);
            if (roundResult != null)
                printWriter.write(roundResult.toString());
        }
    }

    private void generateResults() {
        timeResults = new HashMap<>();
        batteryResults = new HashMap<>();
        maxRound = 0;
        maxSequence = 0;
        sequences = new HashSet<>();

        for (Map.Entry<ResultsContainer.Pointer, ResultsContainer.ResultData> entry : results.getResults().entrySet()) {
            ResultsContainer.Pointer pointer = entry.getKey();
            ResultsContainer.ResultData data = entry.getValue();

            int round = pointer.getRound();
            int sequence = pointer.getSequenceNumber();
            String destination = data.getParams().getDestination();

            if (round > maxRound)
                maxRound = round;
            if (sequence > maxSequence)
                maxSequence = sequence;
            sequences.add(sequence);

            long time = data.getResult().getTime();
            long battery = data.getResult().getEnergy();

            initIfNeeded(round, timeResults);
            initIfNeeded(round, batteryResults);

            timeResults.get(round).addResultForSequence(sequence, time, destination);
            batteryResults.get(round).addResultForSequence(sequence, battery, destination);
        }
    }

    private void initIfNeeded(int round, Map<Integer, AggregatedRoundResult> results) {
        if (results.get(round) == null) {
            results.put(round, new AggregatedRoundResult(round));
        }
    }

}

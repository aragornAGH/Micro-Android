package pl.edu.agh.jkolodziej.micro.weka.test;

import android.content.Context;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import pl.edu.agh.jkolodziej.micro.weka.test.action.Action;
import pl.edu.agh.jkolodziej.micro.weka.test.action.NextRound;
import pl.edu.agh.jkolodziej.micro.weka.test.action.SingleTest;

/**
 * @author - Jakub Kołodziej
 */
public class TestsContext implements Serializable{
    private final ResultsContainer resultsContainer = new ResultsContainer();
    private final TestsConfiguration testsConfiguration;
    private final Context context;
    private Cursor cursor;

    private final int numberOfRounds;
    //    private final int numberOfSeries;
    private int round = 0;
    private int series = 0;
    private int sequenceInRound = 0;

    public TestsContext(TestsConfiguration testsConfiguration, Context context) {
        this.testsConfiguration = testsConfiguration;
        this.context = context;
        this.numberOfRounds = testsConfiguration.getRounds();
//        this.numberOfSeries = testsConfiguration.getSeries();
        this.cursor = new Cursor(testsConfiguration.getWarmup(), testsConfiguration.getActions());
    }

    public TestsConfiguration getTestsConfiguration() {
        return testsConfiguration;
    }

    public int getTotalNumberOfTests() {
        int numberOfTests = 0;
        for (Action action : testsConfiguration.getActions()) {
            if (action instanceof SingleTest) {
                numberOfTests += ((SingleTest) action).getNumberOfTests();
            }
        }
        return numberOfTests * numberOfRounds /** numberOfSeries*/;
    }

    public int getNumberOfPerformedTests() {
        return cursor.getNumberOfPerformedTest();
    }

    public Context getContext() {
        return context;
    }

    public ResultsContainer getResultsContainer() {
        return resultsContainer;
    }

    public void appendResult(LearningParameters params, Measurement.Result result, Object taskResult) {
        resultsContainer.addResult(new ResultsContainer.Pointer(round, series, sequenceInRound), params, result, taskResult);
    }

    public int getRound() {
        return round;
    }

    public int getSeries() {
        return series;
    }

    public Action getAction() {
        Action action = cursor.getAction();
        if (action == null) {
            if (round < numberOfRounds) {
                round++;
                shiftCursor();
                if (round != numberOfRounds)
                    return new NextRound();
            }
//            if (series < numberOfSeries) {
//                series++;
//                round = 0;
//                shiftCursor();
//                return series == numberOfSeries ? null : new NextSeries();
//            }
        } else {
            if (action instanceof SingleTest) {
                SingleTest singleTest = (SingleTest) action;
                if (!singleTest.isIgnored())
                    sequenceInRound++;
            } else {
                sequenceInRound++;
            }
            Logger.getAnonymousLogger().log(Level.INFO, "--> Sequence in round " + round + ": " + sequenceInRound);
        }
        return action;
    }

    private void shiftCursor() {
        sequenceInRound = 0;
        cursor = cursor.resetCursor(cursor.getNumberOfPerformedTest());
    }

    private static class Cursor {
        private final int numberOfWarmups;
        private final List<Action> actions;

        private int actionNumber = 0;
        private int sequence = 0;
        private int numberOfPerformedTest = 0;
        private int warmupsLeftToPerform = 0;

        public Cursor(int warmup, List<Action> actions) {
            this.numberOfWarmups = warmup;
            this.warmupsLeftToPerform = warmup;

            this.actions = actions;
        }

        public Cursor resetCursor(int numberOfPerformedTest) {
            Cursor cursor = new Cursor(numberOfWarmups, actions);
            cursor.setNumberOfPerformedTest(numberOfPerformedTest);
            return cursor;
        }

        public void setNumberOfPerformedTest(int numberOfPerformedTest) {
            this.numberOfPerformedTest = numberOfPerformedTest;
        }

        public int getSequence() {
            return sequence;
        }

        public Action getAction() {
            Action action = null;
            while (actionNumber < actions.size()) {
                action = actions.get(actionNumber);

                if (action instanceof SingleTest) {
                    if (warmupsLeftToPerform > 0) {
                        warmupsLeftToPerform--;
                        SingleTest warmupTest = new SingleTest((SingleTest) action);
                        warmupTest.setIgnored();
                        return warmupTest;
                    }
                    if (sequence < ((SingleTest) action).getNumberOfTests()) {
                        sequence++;
                        numberOfPerformedTest++;
                        if (sequence == ((SingleTest) action).getNumberOfTests()) {
                            actionNumber++;
                            sequence = 0;
                            warmupsLeftToPerform = numberOfWarmups;
                        }
                        return action;
                    }
                }
                //if (action instanceof ConnectionChange) {
                //actionNumber++;
                //return action;
                //}
                actionNumber++;
                sequence = 0;
                warmupsLeftToPerform = numberOfWarmups;

//                if (action instanceof ConnectionChange) {
//                    return action;
//                }
            }
            return action;
        }

        public int getNumberOfPerformedTest() {
            return numberOfPerformedTest;
        }
    }
}

package weka.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import pl.edu.agh.jkolodziej.micro.weka.test.action.Action;

/**
 * @author - Jakub Kołodziej
 */
public class TestsConfiguration {
    private final File testDirectory;
    private final String testName;
    private final int warmup;
    private final int series;
    private final int rounds;
    private final List<Action> actions;

    private final String classifierName;

    private TestsConfiguration(File testDirectory, int warmup, int series, int rounds,
                               String testName, List<Action> actions, String classifierName) {
        this.testDirectory = testDirectory;
        this.warmup = warmup;
        this.testName = testName;
        this.series = series;
        this.rounds = rounds;
        this.actions = actions;
        this.classifierName = classifierName;
    }

    public File getTestDirectory() {
        return testDirectory;
    }

    public String getTestName() {
        return testName;
    }

    public int getWarmup() {
        return warmup;
    }

    public int getSeries() {
        return series;
    }

    public int getRounds() {
        return rounds;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public static class Builder {
        private File testDirectory;
        private String testName;
        private int warmup;
        private int series;
        private int rounds;
        private Map<String, String> ocrSpecificProps;
        private Map<String, String> frSpecificProps;
        private List<Action> actions;
        private String classifierName;

        public Builder setTestDirectory(File testDirectory) {
            this.testDirectory = testDirectory;
            return this;
        }

        public Builder setTestName(String testName) {
            this.testName = testName;
            return this;
        }

        public Builder setWarmup(int warmup) {
            this.warmup = warmup;
            return this;
        }

        public Builder setSeries(int series) {
            this.series = series;
            return this;
        }

        public Builder setRounds(int rounds) {
            this.rounds = rounds;
            return this;
        }

        public Builder setOcrSpecificProps(Map<String, String> ocrSpecificProps) {
            this.ocrSpecificProps = ocrSpecificProps;
            return this;
        }

        public Builder setFrSpecificProps(Map<String, String> frSpecificProps) {
            this.frSpecificProps = frSpecificProps;
            return this;
        }

        public Builder setActions(List<Action> actions) {
            this.actions = actions;
            return this;
        }

        public Builder setClassifierName(String classifierName) {
            this.classifierName = classifierName;
            return this;
        }

        public TestsConfiguration build() {
            return new TestsConfiguration(testDirectory, warmup, series, rounds, testName,
                    actions, classifierName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestsConfiguration that = (TestsConfiguration) o;

        if (warmup != that.warmup) return false;
        if (series != that.series) return false;
        if (rounds != that.rounds) return false;
        if (testDirectory != null ? !testDirectory.equals(that.testDirectory) : that.testDirectory != null)
            return false;
        if (testName != null ? !testName.equals(that.testName) : that.testName != null)
            return false;
        return actions != null ? actions.equals(that.actions) : that.actions == null;

    }

    @Override
    public int hashCode() {
        int result = testDirectory != null ? testDirectory.hashCode() : 0;
        result = 31 * result + (testName != null ? testName.hashCode() : 0);
        result = 31 * result + warmup;
        result = 31 * result + series;
        result = 31 * result + rounds;
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        return result;
    }
}

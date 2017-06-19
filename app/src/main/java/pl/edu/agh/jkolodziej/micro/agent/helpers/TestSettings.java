package pl.edu.agh.jkolodziej.micro.agent.helpers;

import java.io.File;
import java.util.List;

import pl.edu.agh.jkolodziej.micro.agent.test.ActionFactory;
import pl.edu.agh.jkolodziej.micro.weka.test.action.Action;

/**
 * @author - Jakub Kołodziej
 */
public class TestSettings {
    public static String J48_CLASSIFIER_NAME = "J48";
    public static String RANDOM_FOREST_CLASSIFIER_NAME = "RandomForest";
    public static String LOGISTIC_REGRESSION_CLASSIFIER_NAME = "LogisticRegression";
    public static String NAIVE_BAYES_CLASSIFIER_NAME = "NaiveBayes";
    public static String MULTILAYER_PERCEPTRON_CLASSIFIER_NAME = "MultilayerPerceptron";
    public static String KNN_CLASSIFIER_NAME = "KNN";

    public static String CLASSIFIER_NAME = MULTILAYER_PERCEPTRON_CLASSIFIER_NAME;

    public static double TIME_WEIGHT = 5.0;
    public static double BATTERY_WEIGHT = 5.0;

    public static int ROUND_AMOUNT = 8;
    public static int SERIES_AMOUNT = 10;

    public static List<Action> SERVICES = ActionFactory.getTestActions();

    public static File RESULT_WRITER_FILE = new File(AndroidFilesSaverHelper.INTERNAL_DIRECTORY + "/result_time5_bat5_values_neuron.csv");

    public static boolean INTERNET_CONNECTION_NEED_TO_CHANGE = false;

    public static void setJ48Classifier() {
        CLASSIFIER_NAME = J48_CLASSIFIER_NAME;
    }

    public static void setRandomForestClassifier() {
        CLASSIFIER_NAME = RANDOM_FOREST_CLASSIFIER_NAME;
    }

    public static void setLogisticRegressionClassifier() {
        CLASSIFIER_NAME = LOGISTIC_REGRESSION_CLASSIFIER_NAME;
    }

    public static void setNaiveBayesClassifier() {
        CLASSIFIER_NAME = NAIVE_BAYES_CLASSIFIER_NAME;
    }

    public static void setMultilayerPerceptronClassifier() {
        CLASSIFIER_NAME = MULTILAYER_PERCEPTRON_CLASSIFIER_NAME;
    }

    public static void setBatterryWeight(int batteryWeight) {
        BATTERY_WEIGHT = batteryWeight;
    }

    public static void setTimeWeight(int timeWeight) {
        TIME_WEIGHT = timeWeight;
    }
}

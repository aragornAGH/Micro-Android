package pl.edu.agh.jkolodziej.micro.agent.helpers;

/**
 * @author - Jakub Kołodziej
 */
public class TestSettings {
    public static String J48_CLASSIFIER_NAME = "J48";
    public static String RANDOM_FOREST_CLASSIFIER_NAME = "RandomForest";
    public static String LOGISTIC_REGRESSION_CLASSIFIER_NAME = "LogisticRegression";
    public static String NAIVE_BAYES_CLASSIFIER_NAME = "NaiveBayes";
    public static String MULTILAYER_PERCEPTRON_CLASSIFIER_NAME = "MultilayerPerceptron";

    public static String CLASSIFIER_NAME = J48_CLASSIFIER_NAME;

    public static double TIME_WEIGHT = 1.0;
    public static double BATTERY_WEIGHT = 9.0;

    public static int ACTION_PER_ROUND_AMOUNT = 10;
    public static int ROUND_AMOUNT = 3;
    public static int SERIES_AMOUNT = 1;

    public static boolean INTERNET_CONNECTION_NEED_TO_CHANGE = false;

}

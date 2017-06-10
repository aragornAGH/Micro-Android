package pl.edu.agh.jkolodziej.micro.weka;

import java.util.HashMap;
import java.util.Map;

import pl.edu.agh.jkolodziej.micro.weka.managers.BatteryInstanceManager;
import pl.edu.agh.jkolodziej.micro.weka.managers.ExecutionTimeInstanceManager;
import pl.edu.agh.jkolodziej.micro.weka.managers.KnowledgeInstanceManager;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.KStar;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

/**
 * @author - Jakub Kołodziej
 */
public class KnowledgeInstanceManagerFactory {
    private static Map<String, KnowledgeInstanceManager> timeInstanceManagers = new HashMap<>();
    private static Map<String, KnowledgeInstanceManager> batteryInstanceManagers = new HashMap<>();

    private KnowledgeInstanceManagerFactory() {
    }

    public static KnowledgeInstanceManager getTimeInstanceManager(String path, String classifierName) {
        KnowledgeInstanceManager knowledgeInstanceManager = timeInstanceManagers.get(path);
        if (knowledgeInstanceManager == null) {
            Classifier classifier = getClassifierByName(classifierName);
            knowledgeInstanceManager = new ExecutionTimeInstanceManager();
            knowledgeInstanceManager.setClassifier(classifier);
            timeInstanceManagers.put(path, knowledgeInstanceManager);
        }
        return knowledgeInstanceManager;
    }


    public static KnowledgeInstanceManager getBatteryInstanceManager(String path, String classifierName) {
        KnowledgeInstanceManager knowledgeInstanceManager = batteryInstanceManagers.get(path);
        if (knowledgeInstanceManager == null) {
            Classifier classifier = getClassifierByName(classifierName);
            knowledgeInstanceManager = new BatteryInstanceManager();
            knowledgeInstanceManager.setClassifier(classifier);
            batteryInstanceManagers.put(path, knowledgeInstanceManager);
        }
        return knowledgeInstanceManager;
    }


    private static Classifier getClassifierByName(String classifierName) {
        if (classifierName == null)
            return new MultilayerPerceptron();
        else if (classifierName.equals("J48"))
            return new J48();
        else if (classifierName.equals("RandomForest")) {
            return new RandomForest();
        } else if (classifierName.equals("LogisticRegression")) {
            return new SimpleLogistic();
        } else if (classifierName.equals("NaiveBayes")) {
            return new NaiveBayes();
        } else if (classifierName.equals("KNN")) {
            return new KStar();
        } else if (classifierName.equals("MultilayerPerceptron")) {
            return new MultilayerPerceptron();
        } else {
            return new MultilayerPerceptron();
        }
    }

    /**
     * clearing data and building classifier causes Exception (no instances)
     * so we must create new instances of managers
     */
    public static void resetTimeManager(String path) {
        timeInstanceManagers.remove(path);
    }

    public static void resetBatteryManager(String path) {
        batteryInstanceManagers.remove(path);
    }
}

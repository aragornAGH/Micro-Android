package weka.managers;

import java.io.File;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author - Jakub Kołodziej
 */
public interface KnowledgeInstanceManager<T> {
    void readOrCreateDataFile(String dataFilePath, double percentage, Classifier classifier);

    void readOrCreateDataFile(File dataFile, double percentage, Classifier classifier);

    void writeDataFile(File dataFile);

    void addNewInstance(T parameters);

    Instances getInstancesData();

    void injectObjectsForBatteryConsumptionTests(Instances instances, Classifier classifier);

    void setClassifier(Classifier classifier);

    void updateClassifier();

    double classify(Instance instance) throws Exception;

    Instance createInstanceForPrediction(T parameters);

    void clearData();
}

package weka.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.KStar;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;

/**
 * @author - Jakub Kołodziej
 */
public abstract class AbstractKnowledgeInstanceManager<S> implements KnowledgeInstanceManager<S> {
    protected Instances data;
    protected Instances trainingData;

    protected Classifier classifier;

    @Override
    public void readOrCreateDataFile(String dataFilePath, double percentage, Classifier classifier) {
        File dataFile = new File(dataFilePath);
        readOrCreateDataFile(dataFile, percentage, classifier);
    }

    @Override
    public void readOrCreateDataFile(File dataFile, double percentage, Classifier classifier) {
        data = null;
        if (dataFile.exists()) {
            try {
                data = new Instances(new BufferedReader(new FileReader(dataFile)));
                if (percentage < 100.0) {
                    int trainSize = (int) Math.round(data.numInstances() * percentage / 100.0);
                    data = new Instances(data, 0, trainSize);
                }
                data.setClass(data.attribute(data.numAttributes() - 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (data == null) {
            data = buildNewWekaData();
            writeDataFile(dataFile);
        }
        this.classifier = classifier;
    }

    @Override
    public void writeDataFile(File dataFile) {
        try {
            if (!dataFile.exists()) {
                File containingDir = dataFile.getParentFile();
                containingDir.mkdir();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
            bw.write(data.toString());
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Instances getInstancesData() {
        return data;
    }

    @Override
    public void injectObjectsForBatteryConsumptionTests(Instances instances, Classifier classifier) {
        this.data = data;
        this.classifier = classifier;
    }

    @Override
    public void setClassifier(Classifier classifier) {
        data = buildNewWekaData();
        this.classifier = classifier;
    }

    @Override
    public void updateClassifier() {
        try {
            if (classifier instanceof J48
                    || classifier instanceof RandomForest
                    || classifier instanceof NaiveBayes
                    || classifier instanceof KStar) {
                Discretize filter = getDiscretizeFilter(data);
                trainingData = Filter.useFilter(data, filter);
                trainingData.setClassIndex(data.numAttributes()-1);
            } else {
                trainingData = data;
            }
            classifier.buildClassifier(trainingData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Discretize getDiscretizeFilter(Instances data) throws Exception {
        Discretize filter = new Discretize();
        filter.setIgnoreClass(true);
        filter.setInputFormat(data);
        filter.setBins(6);
        filter.setUseEqualFrequency(true);
        filter.setOptions(new String[]{"-R", "last"});
        return filter;
    }

    protected abstract Instances buildNewWekaData();

    @Override
    public double classify(Instance instance) throws Exception {
        return classifier.classifyInstance(instance);
    }

    @Override
    public void clearData() {
        data.clear();
    }
}

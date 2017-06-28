package pl.edu.agh.jkolodziej.micro.weka.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;
import pl.edu.agh.jkolodziej.micro.weka.WekaConstants;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author - Jakub Kołodziej
 */
public class BatteryInstanceManager extends AbstractKnowledgeInstanceManager<LearningParameters> {
    @Override
    protected Instances buildNewWekaData() {
        List<String> taskTypes = Arrays.asList(TaskType.PNG_TO_PDF.name(), TaskType.OCR.name());
        Attribute taskType = new Attribute(WekaConstants.TASK_TYPE, taskTypes);

        Attribute problemSize = new Attribute(WekaConstants.PROBLEM_SIZE);
        Attribute resolution = new Attribute(WekaConstants.RESOLUTION);

        List<String> destinations = Arrays.asList(TaskDestination.MOBILE.name(),
                TaskDestination.PC.name(), TaskDestination.CLOUD.name());
        Attribute destination = new Attribute(WekaConstants.DESTINATION, destinations);

        List<String> connectionTypes = Arrays.asList(ConnectionType.NONE.name(), ConnectionType.CDMA_2G.name(),
                ConnectionType.UMTS_3G.name(), ConnectionType.LTE_4G.name(), ConnectionType.WIFI.name());
        Attribute connectionType = new Attribute(WekaConstants.CONNECTION_TYPE, connectionTypes);

        Attribute wifiStrength = new Attribute(WekaConstants.WIFI_STRENGTH);
        Attribute batteryUsage = new Attribute(WekaConstants.BATTERY_USAGE);

        ArrayList<Attribute> wekaAttributes = new ArrayList<>();
        wekaAttributes.add(taskType);
        wekaAttributes.add(problemSize);
        wekaAttributes.add(resolution);
        wekaAttributes.add(destination);
        wekaAttributes.add(connectionType);
        wekaAttributes.add(wifiStrength);
        wekaAttributes.add(batteryUsage);

        Instances trainingSet = new Instances("conversion", wekaAttributes, 0);
        trainingSet.setClass(batteryUsage);
        return trainingSet;
    }

    @Override
    public void addNewInstance(LearningParameters parameters) {
        Instance newInstance = new DenseInstance(data.numAttributes());

        newInstance.setValue(data.attribute(0), parameters.getTaskType().name());
        newInstance.setValue(data.attribute(1), parameters.getFileSize());
        newInstance.setValue(data.attribute(2), parameters.getResolution());
        newInstance.setValue(data.attribute(3), parameters.getDestination());
        newInstance.setValue(data.attribute(4), parameters.getConnectionType().name());
        newInstance.setValue(data.attribute(5), parameters.getWifiStrength());
        newInstance.setValue(data.attribute(6), parameters.getBatteryConsumption());

        data.add(newInstance);
    }

    @Override
    public Instance createInstanceForPrediction(LearningParameters parameters) {
        Instance newInstance = new DenseInstance(data.numAttributes());

        if (trainingData != null) {
            newInstance.setDataset(trainingData);
        } else {
            newInstance.setDataset(data);
        }

        newInstance.setValue(data.attribute(0), parameters.getTaskType().name());
        newInstance.setValue(data.attribute(1), parameters.getFileSize());
        newInstance.setValue(data.attribute(2), parameters.getResolution());
        newInstance.setValue(data.attribute(3), parameters.getDestination());
        newInstance.setValue(data.attribute(4), parameters.getConnectionType().name());
        newInstance.setValue(data.attribute(5), parameters.getWifiStrength());
        newInstance.setMissing(6);

        return newInstance;
    }
}

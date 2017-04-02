package pl.edu.agh.jkolodziej.micro.weka.params;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;

/**
 * @author - Jakub Kołodziej
 */
public final class LearningParameters {

    private final TaskType taskType;
    private long fileSize;
    private long resolution;
    private String destination;
    private ConnectionType connectionType;
    private long executionTime;
    private long batteryConsumption;
    private long wifiStrength;

    public LearningParameters(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getResolution() {
        return resolution;
    }

    public void setResolution(long resolution) {
        this.resolution = resolution;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getBatteryConsumption() {
        return batteryConsumption;
    }

    public void setBatteryConsumption(long batteryConsumption) {
        this.batteryConsumption = batteryConsumption;
    }

    public long getWifiStrength() {
        return wifiStrength;
    }

    public void setWifiStrength(long wifiStrength) {
        this.wifiStrength = wifiStrength;
    }
}

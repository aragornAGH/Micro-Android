package pl.edu.agh.jkolodziej.micro.weka.test.action;

import java.io.Serializable;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskDestination;
import pl.edu.agh.jkolodziej.micro.agent.enums.TaskType;

/**
 * @author - Jakub Kołodziej
 */
public class SingleTest implements Action, Serializable {
    public static final String TAG_NAME = "test";
    public static final String NUMBER_OF_TESTS_ATTRIBUTE = "numberOfTests";
    public static final String IGNORED_ATTRIBUTE = "ignored";
    public static final String TASK_TYPE_ATTRIBUTE = "type";
    public static final String PROPERTIES_ATTRIBUTE = "properties";

    private final TaskType taskType;

    private boolean ignored;

    private final int numberOfTests;

    private String fileName;

    private ConnectionType connectionType;

    private TaskDestination taskDestination;

//    private final Map<String, String> props;

    public SingleTest(TaskType taskType, int numberOfTests, boolean ignored, String fileName, ConnectionType connectionType, TaskDestination taskDestination /*,Map<String, String> props*/) {
        this.taskType = taskType;
        this.numberOfTests = numberOfTests;
        this.ignored = ignored;
        this.fileName = fileName;
        this.connectionType = connectionType;
        this.taskDestination = taskDestination;
//        this.props = props;
    }

    public SingleTest(SingleTest test) {
        this(test.getTaskType(), test.getNumberOfTests(), test.isIgnored(), test.getFileName(), test.getConnectionType(), test.getTaskDestination()/*,test.getProps()*/);
    }

    public String getFileName() {
        return fileName;
    }

    public int getNumberOfTests() {
        return numberOfTests;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored() {
        ignored = true;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    //    public Map<String, String> getProps() {
//        return props;
//    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SingleTest && numberOfTests == ((SingleTest) o).getNumberOfTests()
                && ignored == ((SingleTest) o).isIgnored();
//                && props.equals(((SingleTest) o).getProps());
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = hash * 17 + numberOfTests;
//        hash = hash * 19 + props.hashCode();
        return hash * 23 + (ignored ? 29 : 31);
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public TaskDestination getTaskDestination() {
        return taskDestination;
    }
}

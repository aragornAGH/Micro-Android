package pl.edu.agh.jkolodziej.micro.weka.test;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import pl.edu.agh.jkolodziej.micro.agent.BatteryUtils;
import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;
import pl.edu.agh.jkolodziej.micro.weka.params.LearningParameters;
import pl.edu.agh.mm.energy.PowerTutorFacade;

/**
 * @author - Jakub Kołodziej
 */
public class Measurement {
    public static State beginMeasurement(Context context, String id, boolean ignored) {
        long begin = System.currentTimeMillis();
        PowerTutorFacade powerTutorFacade = PowerTutorFacade.getInstance(context, "tests");
        long batteryBegin = powerTutorFacade.getTotalPowerForUid();

        return new State(begin, batteryBegin, context, id, ignored);
    }

    public static Result getResults(State state, ConnectionType connectionType, LearningParameters params) {
        long end = System.currentTimeMillis();
        PowerTutorFacade powerTutorFacade = PowerTutorFacade.getInstance(state.getContext(), "tests");
        long batteryEnd = powerTutorFacade.getTotalPowerForUid();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = state.getContext().registerReceiver(null, ifilter);
        double voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0;
        double percentageUsageOfbattery = BatteryUtils.getPercentageUsageOfBattery(state.getContext(), voltage,
                batteryEnd - state.getBatteryState());

        return new Result(state.isIgnored(), end - state.getBeginTime(), batteryEnd - state.getBatteryState(), percentageUsageOfbattery, connectionType, params);
    }

    public static class Result {
        private final boolean ignored;
        private final long time;
        private final long energy;
        private final double percentageUsageOfBattery;
        private final ConnectionType connectionType;
        private final LearningParameters params;
        private boolean errorOccured = false;

        public Result(boolean ignored, long time, long energy, double percentageUsageOfBattery,
                      ConnectionType connectionType, LearningParameters params) {
            this.ignored = ignored;
            this.time = time;
            this.energy = energy;
            this.percentageUsageOfBattery = percentageUsageOfBattery;
            this.connectionType = connectionType;
            this.params = params;
            params.setExecutionTime(time);
            params.setBatteryConsumption(energy);
        }

        public boolean isIgnored() {
            return ignored;
        }

        public long getTime() {
            return time;
        }

        public long getEnergy() {
            return energy;
        }

        public double getPercentageUsageOfBattery() {
            return percentageUsageOfBattery;
        }

        public ConnectionType getConnectionType() {
            return connectionType;
        }

        public LearningParameters getParams() {
            return params;
        }

        public boolean isErrorOccured() {
            return errorOccured;
        }

        public void setErrorOccured() {
            this.errorOccured = true;
        }
    }

    public static class State {
        private final long beginTime;
        private final long batteryState;
        private final Context context;
        private final String id;
        private final boolean ignored;

        public State(long beginTime, long batteryState, Context context, String id, boolean ignored) {
            this.beginTime = beginTime;
            this.batteryState = batteryState;
            this.context = context;
            this.id = id;
            this.ignored = ignored;
        }

        public long getBeginTime() {
            return beginTime;
        }

        public long getBatteryState() {
            return batteryState;
        }

        public Context getContext() {
            return context;
        }

        public String getId() {
            return id;
        }

        public boolean isIgnored() {
            return ignored;
        }
    }
}

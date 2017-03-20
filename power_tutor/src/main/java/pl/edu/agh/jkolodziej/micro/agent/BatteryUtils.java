package pl.edu.agh.jkolodziej.micro.agent;

import android.content.Context;

/**
 * @author - Jakub Kołodziej
 */
public class BatteryUtils {
    private BatteryUtils() {
    }

    public static double getPercentageUsageOfBattery(Context context, double voltage, long batteryConsumption) {
        double batteryCapacity = getBatteryCapacityInMiliJoules(context, voltage);
        return ((double) batteryConsumption * 100.0d) / batteryCapacity;
    }

    public static double getBatteryCapacityInMiliJoules(Context context, double voltage) {
        double batteryCapacityInMAh = getBatteryCapacity(context);
        double power = voltage * batteryCapacityInMAh;
        return power * 3600;
    }

    private static double getBatteryCapacity(Context context) {
        Object mPowerProfile_ = null;
        double batteryCapacity = 0.0;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return batteryCapacity;
    }
}

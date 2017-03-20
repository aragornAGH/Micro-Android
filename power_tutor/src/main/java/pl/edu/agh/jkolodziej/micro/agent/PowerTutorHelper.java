package pl.edu.agh.jkolodziej.micro.agent;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import pl.edu.agh.mm.energy.PowerTutorFacade;

/**
 * @author - Jakub Kołodziej
 */
public class PowerTutorHelper {

    public static double getPercentageUsageOfBattery(Context context, long startingBatteryLevel) {
        PowerTutorFacade powerTutorFacade = PowerTutorFacade.getInstance(context, "energy");
        long batteryState = powerTutorFacade.getTotalPowerForUid();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        double voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0;
        double percentageUsageOfbattery = BatteryUtils.getPercentageUsageOfBattery(context, voltage,
                batteryState - startingBatteryLevel);
        return percentageUsageOfbattery;
    }
}

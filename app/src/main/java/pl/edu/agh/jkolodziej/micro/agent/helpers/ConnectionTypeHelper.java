package pl.edu.agh.jkolodziej.micro.agent.helpers;

import android.content.Context;
import android.net.wifi.WifiManager;

import pl.edu.agh.jkolodziej.micro.agent.enums.ConnectionType;

/**
 * @author - Jakub Kołodziej
 */
public class ConnectionTypeHelper {


    public static ConnectionType getConnectionType(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            return ConnectionType.WIFI;
        } else {
            return ConnectionType.parse(Connectivity.getConnectionName(context));
        }
    }
}

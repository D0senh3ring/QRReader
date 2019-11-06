package de.dosenhering.qrreader.net

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager

public class WifiManagerHelper {

    private val manager : WifiManager;

    public constructor(manager: WifiManager) {
        this.manager = manager;
    }

    public fun connectWIFI(ssid: String, password: String, security: WifiSecurity) : Boolean {
        val config = WifiConfiguration();
        config.SSID = "\"$ssid\"";
        config.status = WifiConfiguration.Status.ENABLED;

        when(security) {
            WifiSecurity.WEP -> this.setWEPSecurity(config, password);
            WifiSecurity.WPA -> this.setWPASecurity(config, password);
            WifiSecurity.OPEN -> this.setOpenWifiConfig(config);
            else -> throw NotImplementedError("Unknown Wifi-security-type: $security");
        }

        this.manager.addNetwork(config);

        val networks = this.manager.configuredNetworks;
        for(network in networks) {
            if(network.SSID == "\"$ssid\"") {
                this.manager.disconnect();
                this.manager.enableNetwork(network.networkId, true);
                return this.manager.reconnect();
            }
        }
        return false;
    }

    private fun setOpenWifiConfig(config: WifiConfiguration) {
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedAuthAlgorithms.clear();
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
    }

    private fun setWPASecurity(config: WifiConfiguration, password: String) {
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.preSharedKey = "\"$password\"";
    }

    private fun setWEPSecurity(config: WifiConfiguration, password: String) {
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.wepKeys[0] = if(password.matches(Regex("^[0-9a-fA-F]+$]"))) { password; } else { "\"$password\""; }
        config.wepTxKeyIndex = 0;
    }
}
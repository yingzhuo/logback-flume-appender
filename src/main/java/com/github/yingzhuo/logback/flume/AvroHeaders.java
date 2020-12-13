/*
 _             _                _          __ _                                                             _
| | ___   __ _| |__   __ _  ___| | __     / _| |_   _ _ __ ___   ___        __ _ _ __  _ __   ___ _ __   __| | ___ _ __
| |/ _ \ / _` | '_ \ / _` |/ __| |/ /____| |_| | | | | '_ ` _ \ / _ \_____ / _` | '_ \| '_ \ / _ \ '_ \ / _` |/ _ \ '__|
| | (_) | (_| | |_) | (_| | (__|   <_____|  _| | |_| | | | | | |  __/_____| (_| | |_) | |_) |  __/ | | | (_| |  __/ |
|_|\___/ \__, |_.__/ \__,_|\___|_|\_\    |_| |_|\__,_|_| |_| |_|\___|      \__,_| .__/| .__/ \___|_| |_|\__,_|\___|_|
         |___/         https://github.com/yingzhuo/logback-flume-appender       |_|   |_|
*/
package com.github.yingzhuo.logback.flume;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 应卓
 * @since 0.2.0
 */
public class AvroHeaders {

    private String application;
    private String tier;
    private String type;
    private String hostname;

    public AvroHeaders() {
        this.init();
    }

    private void init() {
        if (hostname == null || hostname.isEmpty()) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // nop
            }
        }
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}

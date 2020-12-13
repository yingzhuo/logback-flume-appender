/*
 _             _                _          __ _                                                             _
| | ___   __ _| |__   __ _  ___| | __     / _| |_   _ _ __ ___   ___        __ _ _ __  _ __   ___ _ __   __| | ___ _ __
| |/ _ \ / _` | '_ \ / _` |/ __| |/ /____| |_| | | | | '_ ` _ \ / _ \_____ / _` | '_ \| '_ \ / _ \ '_ \ / _` |/ _ \ '__|
| | (_) | (_| | |_) | (_| | (__|   <_____|  _| | |_| | | | | | |  __/_____| (_| | |_) | |_) |  __/ | | | (_| |  __/ |
|_|\___/ \__, |_.__/ \__,_|\___|_|\_\    |_| |_|\__,_|_| |_| |_|\___|      \__,_| .__/| .__/ \___|_| |_|\__,_|\___|_|
         |___/         https://github.com/yingzhuo/logback-flume-appender       |_|   |_|
*/
package com.github.yingzhuo.logback.flume;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteFlumeAgent {

    private static final Logger log = LoggerFactory.getLogger(RemoteFlumeAgent.class);
    private final String hostname;
    private final Integer port;

    public RemoteFlumeAgent(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static RemoteFlumeAgent fromString(String input) {
        if (StringUtils.isEmpty(input)) {
            log.error("Empty flume agent entry, an extra comma?");
            return null;
        }

        String[] parts = input.split(":");
        if (parts.length != 2) {
            log.error("Not a valid [host]:[port] configuration: " + input);
            return null;
        }

        try {
            final String hostname = parts[0];
            final int port = Integer.parseInt(parts[1]);
            return new RemoteFlumeAgent(hostname, port);
        } catch (NumberFormatException e) {
            log.error("Not a valid int: " + parts[1]);
            return null;
        }
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

}

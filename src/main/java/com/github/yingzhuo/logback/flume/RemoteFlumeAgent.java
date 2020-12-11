package com.github.yingzhuo.logback.flume;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoteFlumeAgent {

    private static final Logger log = LoggerFactory.getLogger(RemoteFlumeAgent.class);

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

    private final String hostname;
    private final Integer port;

    public RemoteFlumeAgent(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }
}

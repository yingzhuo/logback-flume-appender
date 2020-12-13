/*
 _             _                _          __ _                                                             _
| | ___   __ _| |__   __ _  ___| | __     / _| |_   _ _ __ ___   ___        __ _ _ __  _ __   ___ _ __   __| | ___ _ __
| |/ _ \ / _` | '_ \ / _` |/ __| |/ /____| |_| | | | | '_ ` _ \ / _ \_____ / _` | '_ \| '_ \ / _ \ '_ \ / _` |/ _ \ '__|
| | (_) | (_| | |_) | (_| | (__|   <_____|  _| | |_| | | | | | |  __/_____| (_| | |_) | |_) |  __/ | | | (_| |  __/ |
|_|\___/ \__, |_.__/ \__,_|\___|_|\_\    |_| |_|\__,_|_| |_| |_|\___|      \__,_| .__/| .__/ \___|_| |_|\__,_|\___|_|
         |___/         https://github.com/yingzhuo/logback-flume-appender       |_|   |_|
*/
package com.github.yingzhuo.logback.flume;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.event.EventBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author 应卓
 * @since 0.1.0
 */
public class FlumeAvroAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    protected static final Charset UTF_8 = StandardCharsets.UTF_8;

    protected Layout<ILoggingEvent> layout;
    private FlumeAvroManager flumeManager;
    private String flumeAgents;
    private String flumeProperties;
    private Long reportingWindow;
    private Integer batchSize;
    private Integer reporterMaxThreadPoolSize;
    private Integer reporterMaxQueueSize;

    // 以下内容会被作为Header发送出去
    private Map<String, String> additionalAvroHeaders;
    private AvroHeaders avroHeaders;

    public void setAvroHeaders(AvroHeaders avroHeaders) {
        this.avroHeaders = avroHeaders;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setFlumeAgents(String flumeAgents) {
        this.flumeAgents = flumeAgents;
    }

    public void setFlumeProperties(String flumeProperties) {
        this.flumeProperties = flumeProperties;
    }

    public void setAdditionalAvroHeaders(String additionalHeaders) {
        this.additionalAvroHeaders = extractProperties(additionalHeaders);
    }

    public void setBatchSize(String batchSizeStr) {
        try {
            this.batchSize = Integer.parseInt(batchSizeStr);
        } catch (NumberFormatException nfe) {
            addWarn("Cannot set the batchSize to " + batchSizeStr, nfe);
        }
    }

    public void setReportingWindow(String reportingWindowStr) {
        try {
            this.reportingWindow = Long.parseLong(reportingWindowStr);
        } catch (NumberFormatException nfe) {
            addWarn("Cannot set the reportingWindow to " + reportingWindowStr, nfe);
        }
    }

    public void setReporterMaxThreadPoolSize(String reporterMaxThreadPoolSizeStr) {
        try {
            this.reporterMaxThreadPoolSize = Integer.parseInt(reporterMaxThreadPoolSizeStr);
        } catch (NumberFormatException nfe) {
            addWarn("Cannot set the reporterMaxThreadPoolSize to " + reporterMaxThreadPoolSizeStr, nfe);
        }
    }

    public void setReporterMaxQueueSize(String reporterMaxQueueSizeStr) {
        try {
            this.reporterMaxQueueSize = Integer.parseInt(reporterMaxQueueSizeStr);
        } catch (NumberFormatException nfe) {
            addWarn("Cannot set the reporterMaxQueueSize to " + reporterMaxQueueSizeStr, nfe);
        }
    }

    @Override
    public void start() {
        if (layout == null) {
            addWarn("Layout was not defined, will only log the message, no stack traces or custom layout");
        }

        if (this.avroHeaders == null) {
            this.avroHeaders = new AvroHeaders();
        }

        if (StringUtils.isNotEmpty(flumeAgents)) {
            String[] agentConfigs = flumeAgents.split(",");

            List<RemoteFlumeAgent> agents = new ArrayList<>(agentConfigs.length);
            for (String conf : agentConfigs) {
                RemoteFlumeAgent agent = RemoteFlumeAgent.fromString(conf.trim());
                if (agent != null) {
                    agents.add(agent);
                } else {
                    addWarn("Cannot build a Flume agent config for '" + conf + "'");
                }
            }
            Properties overrides = new Properties();
            overrides.putAll(extractProperties(flumeProperties));
            flumeManager = FlumeAvroManager.create(agents, overrides,
                    batchSize, reportingWindow, reporterMaxThreadPoolSize, reporterMaxQueueSize, this);
        } else {
            addError("Cannot configure a flume agent with an empty configuration");
        }
        super.start();
    }

    private Map<String, String> extractProperties(String propertiesAsString) {
        final Map<String, String> props = new HashMap<>();
        if (StringUtils.isNotEmpty(propertiesAsString)) {
            final String[] segments = propertiesAsString.split(";");
            for (final String segment : segments) {
                final String[] pair = segment.split("=");
                if (pair.length == 2) {
                    final String key = pair[0].trim();
                    final String value = pair[1].trim();
                    if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                        props.put(key, value);
                    } else {
                        addWarn("Empty key or value not accepted: " + segment);
                    }
                } else {
                    addWarn("Not a valid {key}:{value} format: " + segment);
                }
            }
        } else {
            addInfo("Not overriding any flume agent properties");
        }

        return props;
    }

    @Override
    public void stop() {
        try {
            if (flumeManager != null) {
                flumeManager.stop();
            }
        } catch (FlumeException fe) {
            addWarn(fe.getMessage(), fe);
        }
    }

    @Override
    protected void append(ILoggingEvent eventObj) {
        if (flumeManager != null) {
            try {
                String body = layout != null ? layout.doLayout(eventObj) : eventObj.getFormattedMessage();
                final Map<String, String> headers = new HashMap<>();
                if (additionalAvroHeaders != null) {
                    headers.putAll(additionalAvroHeaders);
                }
                headers.putAll(extractHeaders(eventObj));

                Event event = EventBuilder.withBody(body.trim(), UTF_8, headers);

                flumeManager.send(event);
            } catch (Exception e) {
                addError(e.getLocalizedMessage(), e);
            }
        }
    }

    private Map<String, String> extractHeaders(ILoggingEvent eventObj) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("timestamp", Long.toString(eventObj.getTimeStamp()));
        headers.put("thread", eventObj.getThreadName());
        headers.put("logger-name", eventObj.getLoggerName());
        headers.put("logger-level", eventObj.getLevel().toString());
//        headers.put("logger-msg", eventObj.getMessage());

        if (StringUtils.isNotBlank(avroHeaders.getApplication())) {
            headers.put("application", avroHeaders.getApplication());
        }

        if (StringUtils.isNotBlank(avroHeaders.getTier())) {
            headers.put("tier", avroHeaders.getTier());
        }

        if (StringUtils.isNotBlank(avroHeaders.getType())) {
            headers.put("type", avroHeaders.getType());
        }

        if (StringUtils.isNotBlank(avroHeaders.getHostname())) {
            headers.put("hostname", avroHeaders.getHostname());
        }

        return headers;
    }

}

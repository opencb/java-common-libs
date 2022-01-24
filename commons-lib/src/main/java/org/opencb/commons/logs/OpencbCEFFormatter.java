package org.opencb.commons.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OpencbCEFFormatter extends Formatter {

    private static final int MIN_SEVERITY = 0;
    private static final int MAX_SEVERITY = 10;

    /**
     * The version of the CEF format.
     */
    private final int cefVersion;
    /**
     * The CEF product field.
     */
    private final String product;
    /**
     * The CEF vendor field.
     */
    private final String vendor;
    /**
     * The CEF version field.
     */
    private final String version;
    private final Date date = new Date();
    /**
     * The CEF severity field.
     */
    private int severity;
    /**
     * The CEF name field.
     */
    private String name;
    /**
     * The CEF id field.
     */
    private String id;
    /**
     * The CEF extension field.
     */
    private String extension;


    public OpencbCEFFormatter(final int cefVersion, final String vendor, final String product, final String version) {
        this.cefVersion = cefVersion;
        this.vendor = vendor;
        this.product = product;
        this.version = version;


    }

    @Override
    public String format(LogRecord record) {
        severity = getSeverity(record);
        name = record.getLoggerName();
        id = "" + record.getThreadID();
        date.setTime(record.getMillis());
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm:ss.SSSZ");
        SimpleDateFormat standardTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        extension = "date=" + day.format(date) + " time=" + hour.format(date) + " msg=" + record.getMessage();
        assert vendor != null : "The vendor cannot be null";
        assert product != null : "The product cannot be null";
        assert version != null : "The version cannot be null";
        assert id != null : "The id cannot be null";
        assert name != null : "The name cannot be null";
        assert ((severity >= MIN_SEVERITY) && (severity <= MAX_SEVERITY))
                : "The severity must be between 0 and 10";
        final StringBuilder sb = new StringBuilder();
        sb.append("CEF:");
        sb.append(cefVersion);
        sb.append("|");
        sb.append(vendor);
        sb.append("|");
        sb.append(product);
        sb.append("|");
        sb.append(version);
        sb.append("|");
        sb.append(id);
        sb.append("|");
        sb.append(name);
        sb.append("|");
        sb.append(severity);
        sb.append("|");
        sb.append(extension + "\n");
        return sb.toString();
    }

    private int getSeverity(LogRecord record) {

        switch (record.getLevel().getName()) {
            case "FINE":
                return 1;
            case "INFO":
                return 2;
            case "WARN":
                return 3;
            case "SEVERE":
            default:
                return 4;
        }
    }
}

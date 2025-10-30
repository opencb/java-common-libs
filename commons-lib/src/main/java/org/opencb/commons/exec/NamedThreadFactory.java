package org.opencb.commons.exec;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;
    private Integer priority;
    private boolean daemon;

    public NamedThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        prefix = namePrefix + "-"
                + POOL_NUMBER.getAndIncrement()
                + "-thread-";
        priority = Thread.NORM_PRIORITY;
        daemon = false;
    }

    public NamedThreadFactory setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Marks this thread as either a {@linkplain #isDaemon daemon} thread
     * or a user thread. The Java Virtual Machine exits when the only
     * threads running are all daemon threads.
     *
     * @param daemon true to mark this thread as a daemon thread;
     * @return this thread factory
     */
    public NamedThreadFactory setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public boolean isDaemon() {
        return daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, prefix + threadNumber.getAndIncrement());

        t.setDaemon(daemon);

        if (priority != null) {
            if (t.getPriority() != priority) {
                t.setPriority(priority);
            }
        }
        return t;
    }
}

package com.odnovolov.forgetmenot.domain.architecturecomponents;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h2>Simple Unique Identifier</h2>
 * please use DIFFERENT instanceId in multi-instance applications
 * <p>
 * <pre>
 * id = timestamp+increment+instanceId
 * +-----------+--------------+------------+
 * | 63...32   | 31...8       | 7...0      |
 * +-----------+--------------+------------+
 * | timestamp | increment    | instanceId |
 * +-----------+--------------+------------+
 * </pre>
 *
 * @author Stanford Shi
 */
public class SUID {
    private static final long xFFFFFF = 0xFFFFFF;
    private static final int xFF = 0xFF;

    private static final DateFormat SDF_MED = SimpleDateFormat.getDateTimeInstance( //
            SimpleDateFormat.MEDIUM, //
            SimpleDateFormat.MEDIUM);
    private static final SUID[] INSTANCES = new SUID[xFF + 1];

    private AtomicLong INC = new AtomicLong();
    private int instanceId = 0; // instanceId for different applications

    static { // initiate 0-255 instances, to avoid duplication
        for (int i = 0; i <= xFF; i++) {
            INSTANCES[i] = new SUID(i);
        }
    }

    private SUID(int instanceId) {
        this.instanceId = instanceId;
    }

    private long next() {
        return ((System.currentTimeMillis() >> 10) << 32) // timestamp
                + ((INC.incrementAndGet() & xFFFFFF) << 8) // auto incremental
                + instanceId // instance id
                ;
    }

    public static long id() {
        return INSTANCES[0].next();
    }

    public static long id(int instanceId) {
        if (instanceId < 0 || instanceId > xFF)
            return INSTANCES[0].next();
        else
            return INSTANCES[instanceId].next();
    }

    public static String toString(long id) {
        String hex = Long.toHexString(id);
        return hex.subSequence(0, 8) //
                + "-" + hex.substring(8, 14) //
                + "-" + hex.substring(14);
    }


    public static String toStringLong(long id) {
        long time = (System.currentTimeMillis() >> 42 << 42) + (id >> 22);
        long inc = (id >> 8) & xFFFFFF;
        long instanceId = id & xFF;

        return id + " (DEC)"//
                + "\n" + toString(id) + "  (HEX)" //
                + "\ntime=" + SDF_MED.format(new Date(time)) + ", instanceId=" + instanceId + ", inc=" + inc;
    }

    public static void main(String[] args) {
        System.out.println("\n--- get id");
        System.out.println(toStringLong(id()));
        System.out.println(toStringLong(id()));

        System.out.println("\n--- get id by app id");
        System.out.println(toStringLong(id(13)));
        System.out.println(toStringLong(id(13)));

        System.out.println("\n--- duplication test ...");
        long id = 0, ts = System.currentTimeMillis();
        Set<Long> set = new HashSet<Long>();
        for (int i = 0; i < 1000000; i++) {
            id = id(i & 1);
            if (!set.add(id))
                System.out.println("!!!!!! duplication found:" + toStringLong(id));
        }
        System.out.println("--- duplication test passed, cost " + (System.currentTimeMillis() - ts) + "ms");
    }
}

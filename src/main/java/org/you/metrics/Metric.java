package org.you.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Metric
{
    private static int idx0 = 0;
    private static int idx1 = 0;
    private static int idx2 = 0;
    private static final int maxIdx = 16;

    private static String template;

    private Map<String,String> tags;
    private String name;


    public Metric()
    {
        this.name = Metric.nextName();
        this.tags = new HashMap<>();
    }

    public void addTag(String key, String value)
    {
        this.tags.put(key, value);

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String,String> entry: this.tags.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append(',');
            }
            sb.append('"');
            sb.append(entry.getKey());
            sb.append("\":");
            sb.append('"');
            sb.append(entry.getValue());
            sb.append('"');
        }

        Metric.template = String.format("{\"metric\":\"%s\",\"tags\":{%s},\"timestamp\":%s,\"value\":%s}",
                this.name, sb.toString(), "%d", "%d");
    }

    public String nextDataPoint()
    {
        long ts = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        int value = ThreadLocalRandom.current().nextInt(100);

        return String.format(Metric.template, ts, value);
    }

    private static String nextName()
    {
        idx2++;

        if (idx2 >= maxIdx)
        {
            idx2 = 0;
            idx1++;

            if (idx1 >= maxIdx) {
                idx1 = 0;
                idx0++;
            }
        }

        return String.format("metric%05d.metric%03d.metric%03d", idx0, idx1, idx2);
    }
}

package org.you.metrics;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Metric implements Cloneable
{
    private static final Logger logger = LoggerFactory.getLogger(Metric.class);
    private static final int defaultTagKeyCount = 2;

    private static int idx0 = 0;
    private static int idx1 = 0;
    private static int idx2 = 0;
    private static final int maxIdx = 16;

    private String template;
    private Set<Tag> tags;
    private String name;


    public Metric()
    {
        // decide how many tags we will have
        int tagCount = Config.getInstance().getInt("tag.key.count", defaultTagKeyCount);
        tagCount = ThreadLocalRandom.current().nextInt(tagCount+1);

        this.name = Metric.nextName();
        this.tags = new HashSet<>(tagCount+1);
        logger.debug("Created metric: {}", this.name);

        for (int t = 0; t < tagCount; t++)
        {
            this.tags.add(new Tag());
        }
        this.tags.add(new Tag("host", this.name));

        this.template = String.format("{\"metric\":\"%s\",\"tags\":{%s},\"timestamp\":%s,\"value\":%s}",
                this.name, "%s", "%d", "%d");
    }

    // set or update host tag
    public void setHost(String host)
    {
        this.tags.removeIf(t -> StringUtils.equals(t.name(), "host"));
        this.tags.add(new Tag("host", host));
    }

    public String nextDataPoint()
    {
        long ts = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        int value = ThreadLocalRandom.current().nextInt(100);

        return String.format(this.template, this.nextTags(), ts, value);
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

    private String nextTags()
    {
        StringBuilder sb = new StringBuilder();

        for (Tag tag: this.tags)
        {
            if (sb.length() > 0)
            {
                sb.append(',');
            }

            sb.append(tag.next());
        }

        return sb.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Metric clone = (Metric)super.clone();
        clone.name = this.name;
        clone.tags = new HashSet<>(this.tags);
        return clone;
    }
}

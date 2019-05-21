package org.you.metrics;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Tag implements Cloneable
{
    private static final Logger logger = LoggerFactory.getLogger(Tag.class);
    private static final int defaultTagKeyLength = 8;
    private static final int defaultTagValueLength = 10;
    private static final int defaultTagValueCount = 2;

    private String name;
    private ArrayList<String> values;   // possible values
    private String template;

    public Tag()
    {
        int tagKeyLength = Config.getInstance().getInt("tag.key.length", defaultTagKeyLength);
        int tagValueLength = Config.getInstance().getInt("tag.value.length", defaultTagValueLength);
        int tagValueCount = Config.getInstance().getInt("tag.value.count", defaultTagValueCount);

        tagValueCount = ThreadLocalRandom.current().nextInt(tagValueCount);

        this.name = RandomStringUtils.randomAlphanumeric(tagKeyLength);
        this.values = new ArrayList<>(tagValueCount);

        for (int i = 0; i < tagValueCount; i++)
        {
            this.values.add(RandomStringUtils.randomAlphanumeric(tagValueLength));
        }

        this.template = String.format("\"%s\":\"%s\"", this.name, "%s");
    }

    public Tag(String key, String value)
    {
        this.name = key;
        this.values = new ArrayList<>(1);
        this.values.add(value);
        this.template = String.format("\"%s\":\"%s\"", this.name, "%s");
    }

    public String name()
    {
        return this.name;
    }

    // "key":"value"
    public String next()
    {
        int size = this.values.size();

        if (size == 0)
        {
            return "";
        }
        else
        {
            int i = ThreadLocalRandom.current().nextInt(size);
            return String.format(template, this.values.get(i));
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Tag clone = (Tag)super.clone();
        clone.name = this.name;
        clone.values = this.values;
        clone.template = this.template;
        return clone;
    }
}

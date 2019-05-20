package org.you.metrics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config
{
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final Config theInstance = new Config();

    private String propertyFile;
    private Properties properties = new Properties();

    public static Config getInstance()
    {
        return Config.theInstance;
    }

    public static void init(String propertyFile) throws IOException
    {
        Config.getInstance().propertyFile = propertyFile;
        Config.getInstance().reload();
    }

    public void reload() throws IOException
    {
        if (StringUtils.isEmpty(this.propertyFile))
        {
            throw new IllegalArgumentException("empty propertyFile");
        }

        try (FileInputStream stream = new FileInputStream(this.propertyFile))
        {
            this.properties.load(stream);
            logger.info("Loaded config from {}", this.propertyFile);
        }
    }

    public String getString(String propertyName)
    {
        return this.properties.getProperty(propertyName);
    }

    public int getInt(String propertyName, int defaultValue)
    {
        int value = defaultValue;
        String strValue = this.getString(propertyName);

        if (StringUtils.isNotBlank(strValue))
        {
            try
            {
                value = Integer.parseInt(strValue);
            }
            catch (NumberFormatException nfex)
            {
                value = defaultValue;
                logger.debug("Failed to parse config {} in file {}", propertyName, this.propertyFile);
            }
        }

        return value;
    }
}

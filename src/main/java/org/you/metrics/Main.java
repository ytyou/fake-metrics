package org.you.metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // Thread pool
    private static ScheduledThreadPoolExecutor executor;

    // config default values
    private static final int defaultThreadPoolSize = 10;
    private static final int defaultHostCount = 2;
    private static final int defaultMetricsCount = 10;
    private static final int defaultTagCount = 2;
    private static final int defaultIntervalSec = 30;
    private static final int defaultOpentsdbPort = 4242;

    private static final Random random = new Random(System.currentTimeMillis());


    public static void main(String[] args)
    {
        String configFile = "fake.metrics.properties";
        CommandLine cmd = parseCmdLineOptions(args);
        configFile = cmd.getOptionValue("config", configFile);

        // load config
        try
        {
            logger.info("Loading config from {}", configFile);
            Config.init(configFile);
        }
        catch (IOException ioex)
        {
            logger.error("Failed to load config from {}", configFile);
            return;
        }

        // initialize OpentsdbClient
        try
        {
            String host = Config.getInstance().getString("opentsdb.host");
            int port = Config.getInstance().getInt("opentsdb.port", defaultOpentsdbPort);
            String token = cmd.getOptionValue("token");
            OpentsdbClient.init(host, port, token);
        }
        catch (Exception ex)
        {

        }

        // create thread pool
        int poolSize = Config.getInstance().getInt("thread.count", defaultThreadPoolSize);
        executor = new ScheduledThreadPoolExecutor(poolSize);

        // generate metrics to be shared among all hosts
        int metricsCount = Config.getInstance().getInt("metrics.count", defaultMetricsCount);
        int tagCount = Config.getInstance().getInt("tag.count", defaultTagCount);
        ArrayList<Metric> metrics = generateMetrics(metricsCount, tagCount);

        // schedule hosts to send metrics
        int hostCount = Config.getInstance().getInt("host.count", defaultHostCount);
        int intervalSec = Config.getInstance().getInt("interval.seconds", defaultIntervalSec);

        for (int i = 0; i < hostCount; i++)
        {
            Host host = new Host(metrics);
            long initDelay = random.nextInt(intervalSec);
            executor.scheduleAtFixedRate(host, initDelay, intervalSec, TimeUnit.SECONDS);
        }

        // wait for all hosts to finish
        try
        {
            String duration = cmd.getOptionValue("duration", "60");
            logger.info("Waiting for {} seconds", duration);
            Thread.sleep(TimeUnit.SECONDS.toMillis(Integer.parseInt(duration)));
        }
        catch (InterruptedException iex)
        {
        }
        finally
        {
            logger.info("Shutting down...");
            executor.shutdown();
        }
    }

    private static CommandLine parseCmdLineOptions(String[] args)
    {
        Options options = new Options();

        Option config = new Option("c", "config", true, "config file path");
        config.setRequired(true);
        options.addOption(config);

        Option duration = new Option("d", "duration", true, "duration in seconds");
        duration.setRequired(false);
        options.addOption(duration);

        Option token = new Option("t", "token", true, "backend token");
        token.setRequired(false);   // required when sending metrics to backend
        options.addOption(token);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pex) {
            logger.error("Invalid command line option", pex);
        }

        return cmd;
    }

    private static ArrayList<Metric> generateMetrics(int metricsCount, int tagCount)
    {
        ArrayList<Metric> metrics = new ArrayList<>();

        for (int m = 0; m < metricsCount; m++)
        {
            Metric metric = new Metric();
            int cnt = random.nextInt(tagCount);

            for (int t = 0; t < cnt; t++)
            {
                String key = RandomStringUtils.randomAlphanumeric(8);
                String value = RandomStringUtils.randomAlphanumeric(10);
                metric.addTag(key, value);
            }

            metrics.add(metric);
        }

        return metrics;
    }
}

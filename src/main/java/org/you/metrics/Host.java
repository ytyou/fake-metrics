package org.you.metrics;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Host implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(Host.class);
    private static int id = 0;  // used to generate hostname

    private String name;
    private ArrayList<Metric> metrics;

    public Host(ArrayList<Metric> metrics)
    {
        this.name = String.format("host%05d", Host.id);
        Host.id++;
        this.metrics = cloneMetrics(metrics);
        logger.debug("Host {} created", this.name);
    }

    @Override
    public void run()
    {
        logger.debug("Entered Host.run({})", this.name);
        OpentsdbClient client = OpentsdbClient.getInstance();

        for (Metric metric: this.metrics)
        {
            client.send(metric);
        }
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    private ArrayList<Metric> cloneMetrics(ArrayList<Metric> metrics)
    {
        ArrayList<Metric> clones = new ArrayList<>(metrics.size());

        try
        {
            for (Metric metric : metrics) {
                Metric clone = (Metric)metric.clone();
                clone.setHost(this.name);
                clones.add(clone);
                logger.debug("Added metric {} for host {}", clone.nextDataPoint(), this.name);
            }
        }
        catch (CloneNotSupportedException cnsex)
        {
            logger.error("Failed to clone metrics: ", cnsex);
        }

        return clones;
    }
}

package org.you.metrics;

import java.util.ArrayList;


public class Host implements Runnable
{
    private static int id = 0;  // used to generate hostname

    private String name;
    private ArrayList<Metric> metrics;

    public Host(ArrayList<Metric> metrics)
    {
        this.name = String.format("host%05d", Host.id);
        Host.id++;

        this.metrics = metrics;

        for (Metric metric: this.metrics)
        {
            metric.addTag("host", this.name);
        }
    }

    @Override
    public void run()
    {
        OpentsdbClient client = OpentsdbClient.getInstance();

        for (Metric metric: this.metrics)
        {
            client.send(metric);
        }
    }
}

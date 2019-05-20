package org.you.metrics;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class OpentsdbClient
{
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String template = "{\"metrics\":[%s],\"token\":\"0f320e7db4a2d8ba0a3229753bf7c90d821479da\"}";
    private static final int maxBuff = 10;

    private static OpentsdbClient[] instances;
    private static int instanceIdx;
    private static int instanceCnt;

    private static String host;    // if null, output to console
    private static int port;

    private HttpClient client;
    private ArrayList<String> dps;


    public static OpentsdbClient getInstance()
    {
        synchronized (instances) {
            instanceIdx = (instanceIdx + 1) % instanceCnt;
            return OpentsdbClient.instances[instanceIdx];
        }
    }

    public static void init(String host, int port)
    {
        OpentsdbClient.host = host;
        OpentsdbClient.port = port;

        instanceIdx = 0;
        instanceCnt = Config.getInstance().getInt("opentsdb.client.count", 1);
        instances = new OpentsdbClient[instanceCnt];
        for (int i = 0; i < instanceCnt; i++)
        {
            instances[i] = new OpentsdbClient();
            instances[i].client = null;
            instances[i].dps = new ArrayList<>(maxBuff);
        }
    }

    public synchronized void send(Metric metric)
    {
        try
        {
            this.dps.add(metric.nextDataPoint());
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.out);
        }

        if (this.dps.size() >= maxBuff)
        {
            String metrics = String.format(template, String.join(",", this.dps));

            this.dps.clear();

            if (this.client == null)
            {
                System.out.println(metrics);
            }
        }
    }
}

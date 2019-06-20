package org.you.metrics;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class OpentsdbClient
{
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String template = "{\"metrics\":[%s],\"token\":\"%s\"}";
    private static final String uri = "api/put?details";

    // one instance per thread
    private static ThreadLocal<OpentsdbClient> opentsdbClient;

    private static String host;    // if null, output to console
    private static int port;
    private static String token;
    private static int batchSize;
    private static Set<BasicHeader> headers;

    private CloseableHttpClient client;
    private ArrayList<String> dps;
    private HttpPost request;


    public static OpentsdbClient getInstance()
    {
        return opentsdbClient.get();
    }

    private OpentsdbClient()
    {
        this.dps = new ArrayList<>(batchSize);

        if (StringUtils.isBlank(host) || StringUtils.isBlank(token))
        {
            this.client = null;
            this.request = null;
        }
        else
        {
            HttpClientBuilder builder = HttpClientBuilder.create().setDefaultHeaders(headers);
            this.client = builder.build();
            this.request = new HttpPost(String.format("http://%s:%d/%s", host, port, uri));
        }
    }

    public static void init(String host, int port, int batchSize, String token)
    {
        OpentsdbClient.host = host;
        OpentsdbClient.port = port;
        OpentsdbClient.token = token;
        OpentsdbClient.batchSize = batchSize;

        // create default http headers
        headers = new HashSet<>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        headers.add(new BasicHeader("_token", OpentsdbClient.token));

        // create one OpentsdbClient per thread in the thread pool
        opentsdbClient = ThreadLocal.withInitial(OpentsdbClient::new);
    }

    public void send(Metric metric)
    {
        try
        {
            this.dps.add(metric.nextDataPoint());
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.out);
        }

        if (this.dps.size() >= batchSize)
        {
            String metrics = String.format(template, String.join(",", this.dps), token);

            this.dps.clear();

            if (this.client == null)
            {
                System.out.println(metrics);
            }
            else
            {
                CloseableHttpResponse response = null;
                InputStream istream = null;

                try
                {
                    request.setEntity(new StringEntity(metrics));
                    response = this.client.execute(request);
                    logger.debug("response: {}", response);

                    HttpEntity entity = response.getEntity();
                    istream = entity.getContent();
                    istream.skip(Long.MAX_VALUE);
                }
                catch (Exception ex)
                {
                    logger.error("Failed to send metrics: ", ex);
                }
                finally
                {
                    try
                    {
                        // these must be closed in order for those
                        // keep-alive connections to be re-used
                        if (istream != null)
                        {
                            istream.close();
                        }

                        if (response != null)
                        {
                            response.close();
                        }
                    }
                    catch (IOException ioex)
                    {
                        logger.debug("Failed to close response.", ioex);
                    }
                }
            }
        }
    }
}

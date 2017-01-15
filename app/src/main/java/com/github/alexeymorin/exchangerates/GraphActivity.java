package com.github.alexeymorin.exchangerates;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.LegendRenderer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // данный url для только отладки
        URL url = createURL(getIntent().getExtras().getString("ID",""), "01/11/2016", "31/12/2016");

        GetValCursSeriesTask getValCursSeriesTask = new GetValCursSeriesTask();
        getValCursSeriesTask.execute(url);

    }

    private URL createURL(String id, String date1, String date2) {
        String urlString  = getString(R.string.web_service_dynamic_url) + "?date_req1=" + date1 + "&date_req2=" + date2 + "&VAL_NM_RQ=" + id;
        Log.i("GraphActivity", "urlString=" + urlString);
        try {
            URL url = new URL(urlString);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void plotGraph(Document document) {
        // to do
        if (document == null)
            return;
        ArrayList<DataPoint> dataPointList = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Node root = document.getDocumentElement();
        NodeList recordsNodeList = root.getChildNodes();
        Log.i("GraphActivity", "valCursNodeList.getLength()=" + recordsNodeList.getLength());
        for (int i=0; i < recordsNodeList.getLength(); i++) {
            Node recordNode = recordsNodeList.item(i);
            if (recordNode.getNodeName().equals("Record")) {
                String dateString = recordNode.getAttributes().getNamedItem("Date").getTextContent();
                NodeList recordNodeChilds = recordNode.getChildNodes();
                for (int j=0; j < recordNodeChilds.getLength(); j++) {
                    Node recordChild = recordNodeChilds.item(j);
                    if (recordChild.getNodeName().equals("Value")) {
                        String valueString = recordChild.getTextContent();
                        valueString = valueString.replace(',', '.');

                        Date date = new Date();
                        try {
                            date = dateFormat.parse(dateString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Log.i("GraphActivity", "dateString=" + dateString + "     date=" + date);
                        Log.i("GraphActivity", "valueString=" + valueString);
                        DataPoint dataPoint = new DataPoint(date, Double.parseDouble(valueString));
                        dataPointList.add(dataPoint);
                    }
                }
            }
        }
        DataPoint[] dataPoints = dataPointList.toArray(new DataPoint[dataPointList.size()]);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(series);
        series.setTitle(getIntent().getExtras().getString("CharCode", ""));
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getGridLabelRenderer().setTextSize(13);
        if (dataPoints.length > 0) {
            graph.getViewport().setMinX(dataPoints[0].getX());
            graph.getViewport().setMaxX(dataPoints[dataPoints.length - 1].getX());
        }
        graph.getViewport().setXAxisBoundsManual(true);

    }

    private class GetValCursSeriesTask extends AsyncTask<URL, Void, Document> {
        @Override
        protected Document doInBackground(URL... params) {

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document document = documentBuilder.parse(connection.getInputStream());
                    return document;
                } else {
                    Log.i("GraphActivity", "response != HttpURLConnection.HTTP_OK");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.activity_graph), R.string.connection_error, Snackbar.LENGTH_LONG).show();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Document document) {
            plotGraph(document);
        }
    }

}

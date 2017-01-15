package com.github.alexeymorin.exchangerates;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    private List<Valute> valuteList = new ArrayList<>();
    private ValuteArrayAdapter valuteArrayAdapter;
    private ListView valuteListView;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        valuteArrayAdapter = new ValuteArrayAdapter(this, valuteList);
        valuteListView = (ListView) findViewById(R.id.valuteListView);
        valuteListView.setAdapter(valuteArrayAdapter );

        URL url = createURL();
        Log.i(TAG, "url = " + url.toString());
        if (url != null) {
            GetValCursTask getValCursTask = new GetValCursTask();
            getValCursTask.execute(url);

        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
        };


    }

    private URL createURL() {
        try {
            URL url = new URL(getString(R.string.web_service_daily_url));
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fillValuteListView(Document document) {
        if (document == null)
            return;
        Node root = document.getDocumentElement();
        NodeList valutesNodeList = root.getChildNodes();
        for(int i = 0; i < valutesNodeList.getLength(); i++) {
            Node valuteNode = valutesNodeList.item(i);
            if (valuteNode.getNodeName().equals("Valute")) {
                String id = valuteNode.getAttributes().getNamedItem("ID").getTextContent();
                Log.i(TAG, "id="+id);
                NodeList valuteProperties = valuteNode.getChildNodes();
                String numCode = "";
                String charCode = "";
                String nominal = "";
                String name = "";
                String value = "";
                for (int j = 0; j < valuteProperties.getLength(); j++) {
                    Node valuteProperty = valuteProperties.item(j);
                    if (valuteProperty.getNodeName().equals("NumCode")) {
                        numCode = valuteProperty.getTextContent();
                        Log.i(TAG, "numCode = " + numCode);
                        continue;
                    }
                    if (valuteProperty.getNodeName().equals("CharCode")) {
                        charCode = valuteProperty.getTextContent();
                        Log.i(TAG, "charCode = " + charCode);
                        continue;
                    }
                    if (valuteProperty.getNodeName().equals("Nominal")) {
                        nominal = valuteProperty.getTextContent();
                        Log.i(TAG, "nominal = " + nominal);
                        continue;
                    }
                    if (valuteProperty.getNodeName().equals("Name")) {
                        name = valuteProperty.getTextContent();
                        Log.i(TAG, "name = " + name);
                        continue;
                    }
                    if (valuteProperty.getNodeName().equals("Value")) {
                        value = valuteProperty.getTextContent();
                        Log.i(TAG, "Value = " + value);
                        continue;
                    }
                }
                valuteList.add(new Valute(id, numCode, charCode, nominal, name, value));
            }
        }
    }

    private class GetValCursTask extends AsyncTask<URL, Void, Document> {
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
                    Log.i("MainActivity", "response != HttpURLConnection.HTTP_OK");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connection_error, Snackbar.LENGTH_LONG).show();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Document valCursDocument) {
            fillValuteListView(valCursDocument);
            valuteArrayAdapter.notifyDataSetChanged();
            valuteListView.smoothScrollToPosition(0);
        }
    }
}

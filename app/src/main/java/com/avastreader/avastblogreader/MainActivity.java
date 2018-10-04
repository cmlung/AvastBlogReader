package com.avastreader.avastblogreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView RSS;
    ArrayList<String> titles;
    ArrayList<String> links;
    ArrayList<String> descriptions;

    String title = null;    //test
    String link = null;     //test
    String description = null; //test

    ArrayList<String> items; //test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titles = new ArrayList<String>();
        links = new ArrayList<String>();
        descriptions = new ArrayList<String>();

        items = new ArrayList<String>();  //test

        RSS = (ListView) findViewById(R.id.LV_RSS);
        RSS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (links.size() != 0) {
                    System.out.println("Link is not null && onItemClock start - for debug");
                    Uri uri = Uri.parse(links.get(position));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        new BGTask().execute();
    }

    public InputStream getInputStream(URL url)
    {
        try{
            return url.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public class BGTask extends AsyncTask<Integer, Void, Exception>
    {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading rss feed, please wait...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... params) {

            String avast_rss = "https://blog.avast.com/rss.xml";
            //Boolean a = false;
            //Boolean b = false;
            //Boolean c = false;

            try
            {
                URL url = new URL(avast_rss);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xmlparser = factory.newPullParser();
                xmlparser.setInput(getInputStream(url),"UTF_8");

                boolean insideItem = false; // indicate when the parser get into <item> block
                int count = 1;
                int eventType = xmlparser.getEventType(); // indicate where the xmlparser is

                while (eventType != XmlPullParser.END_DOCUMENT && count < 6 )
                {
                    if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xmlparser.getName().equalsIgnoreCase("item"))
                        {
                           insideItem = true; // parser now is inside the item tag
                        }
                        else if (xmlparser.getName().equalsIgnoreCase("title") && insideItem)
                        {
                            //titles.add(xmlparser.nextText());
                            title = count+". "+xmlparser.nextText()+"\n\n"; //test
                            // a=true; // for debug
                        }
                        else if (xmlparser.getName().equalsIgnoreCase("link") && insideItem)
                        {
                            //links.add(xmlparser.nextText());
                            link = xmlparser.nextText()+"\n\n"; //test
                            // b=true; // for debug
                        }
                        else if (xmlparser.getName().equalsIgnoreCase("description") && insideItem)
                        {
                            //descriptions.add(xmlparser.nextText());
                            description = xmlparser.nextText()+"\n"; //test
                            // c=true; // for debug
                        }

                    }
                    else if (eventType == XmlPullParser.END_TAG && xmlparser.getName().equalsIgnoreCase("item"))
                    {
                        items.add(title+link+description); // test
                        insideItem = false; // parser leave item block
                        count++;
                    }

                    eventType = xmlparser.next();
                }

            }
            catch(MalformedURLException e)
            {
                exception = e;
            }
            catch(XmlPullParserException e)
            {
                exception = e;
            }
            catch(IOException e)
            {
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,items);
            RSS.setAdapter(adapter);

            progressDialog.dismiss();
        }
    }

}


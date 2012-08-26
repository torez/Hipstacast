package com.ifrins.hipstacast.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.ifrins.hipstacast.Hipstacast;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class UpgradeTask extends AsyncTask<Void, Void, Void> {
	
	private static final String IMAGE_XPATH = "rss/channel/image/@href";


	Context context;
	OnTaskCompleted onTaskCompletedListener;
	int fromVersion;
	
	public UpgradeTask(Context context, OnTaskCompleted onTaskCompletedListener, int fromVersion) {
		this.context = context;
		this.onTaskCompletedListener = onTaskCompletedListener;
		this.fromVersion = fromVersion;
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		
		if (fromVersion == 0 && fromVersion <= 8) {
			File _f = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img/.nomedia");
			if (!_f.exists()) {
				try {
					_f.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Cursor c = context.getContentResolver().query(Hipstacast.SUBSCRIPTIONS_PROVIDER_URI, new String[]{"_id", HipstacastProvider.PODCAST_IMAGE, HipstacastProvider.PODCAST_FEED}, null, null, null);
			while (c.moveToNext() == true) {
				String[] imagePath = c.getString(c.getColumnIndex(HipstacastProvider.PODCAST_IMAGE)).split("/");
				String imgName = imagePath[imagePath.length-1];
				imgName = imgName.substring(0, imgName.length()-3) + "w.jpg";
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(false);
				DocumentBuilder builder = null;
				try {
					builder = factory.newDocumentBuilder();
				} catch (ParserConfigurationException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				Document doc = null;
				try {
					doc = builder.parse(c.getString(c.getColumnIndex(HipstacastProvider.PODCAST_FEED)));
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				XPath xpath = XPathFactory.newInstance().newXPath();
				try {
					storeImage(xpath.compile(IMAGE_XPATH).evaluate(doc, XPathConstants.STRING).toString(), imgName);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;

		
	}
	
	@Override
	protected void onPostExecute(Void v) {
		if (onTaskCompletedListener != null)
			onTaskCompletedListener.onTaskCompleted(Hipstacast.TASK_UPGRADE);
	}
	// TODO Auto-generated method stub

	private String storeImage (String imageUrl, String fileName) {
		Log.d("HIP-URL", "The image url is " + imageUrl);
		   try {
	           
	           File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/hipstacast/img");
	           if(dir.exists()==false) {
	                dir.mkdirs();
	           }
	           	           
	           URL url = new URL(imageUrl); //you can write here any link
	           File file = new File(dir, fileName);

	           long startTime = System.currentTimeMillis();
	           Log.d("DownloadManager", "download begining");
	           Log.d("DownloadManager", "download url:" + url);

	           /* Open a connection to that URL. */
	           URLConnection ucon = url.openConnection();

	           /*
	            * Define InputStreams to read from the URLConnection.
	            */
	           InputStream is = ucon.getInputStream();
	           BufferedInputStream bis = new BufferedInputStream(is);

	           /*
	            * Read bytes to the Buffer until there is nothing more to read(-1).
	            */
	           ByteArrayBuffer baf = new ByteArrayBuffer(5000);
	           int current = 0;
	           while ((current = bis.read()) != -1) {
	              baf.append((byte) current);
	           }


	           /* Convert the Bytes read to a String. */
	           FileOutputStream fos = new FileOutputStream(file);
	           fos.write(baf.toByteArray());
	           fos.flush();
	           fos.close();
	           bis.close();
	           
	           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
	           Log.d("HIP-DW", file.getAbsolutePath());
	           return file.getAbsolutePath();

	   } catch (IOException e) {
	       Log.d("DownloadManager", "Error: " + e);
	       Log.d("HIP-URL-IMG", imageUrl);
	       return "";
	   }

	}


}

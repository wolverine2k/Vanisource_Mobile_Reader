/**
 * @author      Naresh Mehta <nareshtechs @ gmail.com>
 * @version     0.1                   
 * @since       2012-08-30          (a date or the version number of this program)
 * 
 * Vanisource - Dedicated Reader for Android devices. Released under GPLv2
 * 
 */
package com.naresh.vanisource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * File belongs to Vanisource (Dedicated Reader for Android devices).
 * Copyright (C) 2012 Naresh Mehta
 * See file "COPYING" for details!
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA * 
 *
 * @version 0.1
 * <br/>
 * @author nareshtechs@gmail.com
 * <br/>
 * Program: Vanisource.
 * <br/>
 * Class: MainActivity.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Main entry of the reader application.
 * <br/>
 * Hint: Dedicated reader for the Vanisource.org website.
 */
public class MainActivity extends Activity {
	/**
	 * Tag to be used in logging.
	 */
	private static final String strLogTag = "VaniSource";
	/**
	 * Main Vanisource URL
	 */
	private static final String strVaniSourceURL = "http://www.vanisource.org/";
	/**
	 * Vanisource Host needed to check if we are still connecting to the same site
	 */
	private static final String strVaniHost = "www.vanisource.org";
	/**
	 * Pointer to the main activity
	 */
	private final Activity activity = this;	
	private WebView webView;
	private ProgressDialog progressDialog;
	
	/**
	 * Basic onCreate overridden from the parent activity called on
	 * application launch
	 *
	 * @param  savedInstanceState Previous state of the activity
	 * @return void
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	initializeWebview();
    }

	/**
	 * Basic onCreate overridden from the parent activity called on
	 * application launch
	 *
	 * @param  savedInstanceState Previous state of the activity
	 * @return void
	 */
    private void initializeProgressDialog() {
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.setIndeterminate(true);
    	progressDialog.setTitle(getResources().getString(R.string.loading));
    	progressDialog.setMessage(getResources().getString(R.string.please_wait));
    }

	/**
	 * Called after an application comes to the foreground from the background
	 *
	 * @return void
	 */
	@Override
	protected void onResume() {
		super.onStart();
		webView.removeAllViews();
	}

	/**
	 * Called when the Menu key is pressed
	 *
	 * @return boolean	true to handle the options menu creation
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	/**
	 * Called when the back button is pressed
	 *
	 * @return void	
	 */
    @Override
    public void onBackPressed () {
    	Log.d(strLogTag, "Back button Pressed...");
        if (webView.isFocused() && webView.canGoBack()) {
        	webView.goBack();       
        }
        else {
        	super.onBackPressed();
        }
    }

	/**
	 * Initialize the webview control of the view. Gets the view handle,
	 * sets some settings like cache, sets the webview client and the
	 * webchrome client. Also launches the base URL
	 *
	 * @return void	
	 */
    private void initializeWebview() {
    	webView = (WebView) findViewById(R.id.webview_main);
    	webView.getSettings().setBuiltInZoomControls(true);
    	webView.getSettings().setCacheMode(WebView.DRAWING_CACHE_QUALITY_AUTO);
    	webView.getSettings().setAppCacheEnabled(true);
    	webView.getSettings().setDefaultTextEncodingName("UTF-8");
    	webView.setWebViewClient(new MyWebViewClient());
    	webView.setWebChromeClient(new MyWebChromeClient());
    	
    	/**
    	 * Enabling Javascript might enable certain XSS vulnerabilities.
    	 * Keep it disabled for the time being.
    	 */
//    	webView.getSettings().setJavaScriptEnabled(true);

    	/**
    	 * Create an Async class instance and get it to fetch the page.
    	 * "1" is passed to let the AsynTask know that this is the first/base page.
    	 */
    	RetreivePageContents page = new RetreivePageContents();
    	page.execute(strVaniSourceURL, "1");
    }
    
	/**
	 * Extends the AsyncTask for performing page fetching outside the 
	 * UI thread so that UI does not freeze.
	 *
	 * @extends	AsyncTask	Used for executing Asyncrhonous tasks 
	 * @param 	String	Default template parameters
	 * @param	Void	Default template parameters
	 * @param	String	Default template parameters
	 * Program: Vanisource.
	 * <br/>
	 * Class: RetreivePageContents.
	 * <br/>
	 * License: GPLv2.
	 */
    private class RetreivePageContents extends AsyncTask<String, Void, String> {
    	/**
    	 * Called before the Async task is executed. Shows the progress window.
    	 *
    	 * @return void	
    	 */
    	@Override
    	protected void onPreExecute() {
            initializeProgressDialog();
            progressDialog.show();
    	}

    	/**
    	 * Called after the Async task is executed. Loads the webview with the parsed HTML & dismisses
    	 * the progress dialog window.
    	 * 
    	 * @param	String	The parsed/cleaned HTML file 
    	 * @return void	
    	 */
    	@Override
        protected void onPostExecute(String strResult) {
    		// The task has finished executing. Now just load it into the UI view
    		// loadData does not work with UTF-8 characters...
    		webView.loadDataWithBaseURL(null, strResult, "text/html", "UTF-8", null);
    		progressDialog.dismiss();
    		/* No refresh needed */    		
//    		webView.refreshDrawableState();
        }

    	/**
    	 * The actual background task which is doing the processing
    	 * 
    	 * @param	String...	A variable argument/String array holding the URL to be parsed & also
    	 * 						identifying if the URL is root or internal to root i.e. Vanisource 
    	 * @return	String		A variable holding the clean & parsed HTML that will be loaded in the webview	
    	 */
    	@Override
    	protected String doInBackground(String... strURLs) {
    		String strData = null;
    		CleanerProperties props = new CleanerProperties();
    		// set some properties to non-default values
    		props.setTranslateSpecialEntities(true);
    		props.setTransResCharsToNCR(true);
    		props.setRecognizeUnicodeChars(true);
    		props.setOmitComments(true);

    		HtmlCleaner cleaner = new HtmlCleaner(props);
    		
    		TagNode rootNode = null;
    		URL url = null;
    		try {
    			url = new URL(strURLs[0]);
    			rootNode = cleaner.clean(url, "utf-8");
    			Object[] foundList = null;
    			
    			/*
    			 * An extra String parameter is passed to indicate the root node. The parsing for the root node
    			 * and sub-nodes/sub-pages in the wikipedia are different
    			 */
    			if(strURLs.length == 1) {
        			foundList = rootNode.evaluateXPath("//div[@id='content']");
    			} else {
    				foundList = rootNode.evaluateXPath("//div[@id='bodyContent']");
    			}

    			/*
    			 * A check to ensure that we don't segfault on null data if the respective tags are not found
    			 */
    			if(foundList == null || foundList.length < 1) {
    			    return null;
    			}
    			rootNode = (TagNode) foundList[0];
    			strData = new PrettyHtmlSerializer(props).getAsString(rootNode,"utf-8");
    			/*
    			 * Replace all the relative paths to absolute paths so that proper fetch from the source
    			 * occurs when the links in webview are clicked
    			 */
    			strData = strData.replaceAll("a href=\"/wiki/", "a href=\"http://www.vanisource.org/wiki/");
//    			Log.d(strLogTag, strData);
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (XPatherException e) {
    			e.printStackTrace();
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		return strData;
    	}
    }
    
	/**
	 * Extends the WebChrome client to show a web browser like loading functionality 
	 *
	 * @extends	WebChromeClient	 
	 * Program: Vanisource.
	 * <br/>
	 * Class: MyWebChromeClient.
	 * <br/>
	 * License: GPLv2.
	 */
    private class MyWebChromeClient extends WebChromeClient {
    	//TBD: Make the proper implementation & check this class
    	/**
    	 * Shows a progress change for the webpage loading on the webview
    	 * 
    	 * @param	Webview		Pointer to the webview window
    	 * @param	int			Tells about the progress of the loading process in terms of percentage
    	 * @return	void	
    	 */
    	@Override
        public void onProgressChanged(WebView view, int progress)
        {
        	activity.setTitle(R.string.loading);
            activity.setProgress(progress * 100);
            if(progress == 100)
            	activity.setTitle(R.string.app_name);
        }
    }

	/**
	 * Extends the Webview client which intercepts the URL and allows us to fetch the HTML, clean it up
	 * and show the interesting parts in the local webview. It also checks if the URL is a part of the
	 * root site (i.e. Vanisource). If by chance we slip onto something else (in future?) then it will
	 * open a normal web browser to render that page 
	 *
	 * @extends	WebViewClient	 
	 * Program: Vanisource.
	 * <br/>
	 * Class: MyWebViewClient.
	 * <br/>
	 * License: GPLv2.
	 */
    private class MyWebViewClient extends WebViewClient {
    	/**
    	 * Allows for overriding the URL which is being loaded in the webview
    	 * 
    	 * @param	Webview		Pointer to the webview window
    	 * @param	String		The URL which is about to be loaded in the webview
    	 * @return	boolean		Return false if the local webview is doing the URL rendering else true
    	 * 						if an external activity (web browser) is going to do the rendering
    	 */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        	Log.i(strLogTag, "Host is " + Uri.parse(url).getHost());
            if (Uri.parse(url).getHost().equals(strVaniHost)) {
                // This is our site, so do not override; let my WebView load the page after processing
            	view.clearView();
            	view.stopLoading();
            	RetreivePageContents page = new RetreivePageContents();
            	page.execute(url);
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
            
        }
/*
 * NOT AVAILABLE IN 2.2        
        @Override
        public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
            if (url.contains(".css")) {
                return getCssWebResourceResponseFromAsset();
            } else {
                return super.shouldInterceptRequest(view, url);
            }
        }
*/        
    }    
}



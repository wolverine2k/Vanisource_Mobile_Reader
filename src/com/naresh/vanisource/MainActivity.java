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
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
	private static String strLogTag = "VaniSource";
//	private String strVaniSourceURL = "http://www.vanisource.org/wiki/Bhagavad-gita_As_It_Is_(1972)";
	private String strVaniSourceURL = "http://www.vanisource.org/";
	private String strVaniHost = "www.vanisource.org";
	
	private WebView webView;
	private ProgressDialog progressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	initializeWebview();
    }
    
    private void initializeProgressDialog() {
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.setIndeterminate(true);
    	progressDialog.setTitle(getResources().getString(R.string.loading));
    	progressDialog.setMessage(getResources().getString(R.string.please_wait));
    }
    
	@Override
	protected void onResume() {
		super.onStart();
		webView.removeAllViews();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
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
    
    private void initializeWebview() {
    	webView = (WebView) findViewById(R.id.webview_main);
    	webView.getSettings().setBuiltInZoomControls(true);
    	webView.getSettings().setCacheMode(WebView.DRAWING_CACHE_QUALITY_AUTO);
    	webView.getSettings().setAppCacheEnabled(true);
    	webView.getSettings().setDefaultTextEncodingName("UTF-8");
    	webView.setWebViewClient(new MyWebViewClient());
//    	webView.getSettings().setJavaScriptEnabled(true);
    	
    	RetreivePageContents page = new RetreivePageContents();
    	page.execute(strVaniSourceURL, "1");
   	
//    	webView.loadUrl(strVaniSourceURL);
    }
    
    private class RetreivePageContents extends AsyncTask<String, Void, String> {
   	
    	@Override
    	protected void onPreExecute() {
            initializeProgressDialog();
            progressDialog.show();
    	}
    	
    	@Override
        protected void onPostExecute(String strResult) {
    		// The task has finished executing. Now just load it into the UI view
    		// loadData does not work with UTF-8 characters...
    		webView.loadDataWithBaseURL(null, strResult, "text/html", "UTF-8", null);
    		progressDialog.dismiss();
//    		webView.refreshDrawableState();
        }

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
    			
    			if(strURLs.length == 1) {
        			foundList = rootNode.evaluateXPath("//div[@id='content']");
    			} else {
    				foundList = rootNode.evaluateXPath("//div[@id='bodyContent']");
    			}
    			
    			if(foundList == null || foundList.length < 1) {
    			    return null;
    			}
    			rootNode = (TagNode) foundList[0];
    			strData = new PrettyHtmlSerializer(props).getAsString(rootNode,"utf-8");
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

    private class MyWebViewClient extends WebViewClient {
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



/**
 * 
 */
package com.naresh.vanisource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.PrettyHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Environment;
import android.util.Log;

/**
 * @author Naresh Mehta
 * @category Utility class to parse the HTML pages and format it to be shown in the mobile device
 * We will extract only the main content from the wiki site
 */
public class ParserUtility {
	private static String strLogTag = "VaniSource:ParserUtility";
	/**
	 * Static class which will return the HTML from the webpage 
	 */
	public static String getHTMLFromUrl(String url) {
        String strHTML = null;
 
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            strHTML = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strHTML;
    }

	public static List<HtmlNode> getLinksByClass(String CSSClassname)
    {
		HtmlCleaner cleaner = new HtmlCleaner();
		HtmlNode rootNode = null;
		URL url = null;
		try {
			url = new URL("http://www.vanisource.org/");
			rootNode = cleaner.clean(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
        List<HtmlNode> linkList = new ArrayList<HtmlNode>();

        HtmlNode linkElements[] = ((TagNode) rootNode).getChildTags();
        
        for (int i = 0; linkElements != null && i < linkElements.length; i++)
        {
            linkList.add((HtmlNode) linkElements[i]);
        }
        
        return linkList;
    }	
	
	public static String getRootNode(String strURL)
	{
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);

		HtmlCleaner cleaner = new HtmlCleaner(props);
		
		TagNode rootNode = null;
		URL url = null;
		try {
			url = new URL(strURL);
			rootNode = cleaner.clean(url);
			Object[] foundList = rootNode.evaluateXPath("//div[@id='content']");
			if(foundList == null || foundList.length < 1) {
			    return null;
			}
			rootNode = (TagNode) foundList[0];
			return new PrettyHtmlSerializer(props).getAsString(rootNode);
			
/*			
			TagNode[] tagNodeArray = rootNode.getAllElements(true);
			for (int i = 0; i < tagNodeArray.length; i++) {
				rootNode = tagNodeArray[i];
				Log.i(strLogTag, rootNode.getName() + " YIHO " + rootNode.getText());
			}
			String myPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/abc.html";
			File file = new File(myPath);
			OutputStream outStream = null;
			outStream = new BufferedOutputStream(new FileOutputStream(file));
			
*/			
            // returns html body div#myDiv.foo.bar p#tID 			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (XPatherException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Static class which will return the bodyContent from the URL directly 
	 */
	public static String getBodyContent(String url) {

/*
 * 		List<HtmlNode> links = ParserUtility.getLinksByClass("bodyContent");
	
		for (Iterator<HtmlNode> iterator = links.iterator(); iterator.hasNext();)
		{
			ContentNode divElement = (ContentNode) iterator.next();
			Log.i(strLogTag, (divElement.getContent().toString()));
		}
*/				
		return ParserUtility.getRootNode(url);
	}
}

package com.joshua.deepsentiment.facebook;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.joshua.deepsentiment.SocialGrabber;
import com.joshua.deepsentiment.twitter.TwitterGrabber;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

public class FacebookGrabber extends SocialGrabber {
	
	private static final String APP_ID = "318966438471131";
	private static final String APP_SECRET = "52fdbb175c28d7e15a3b54fc3fa1c8eb";
	private static final String ACCESS_TOKEN = "318966438471131|ow6_-gdU3c3zulyOeh2SqdshDxw";
	
	// Instance for the facebook API.
	private Facebook _facebook;

	public FacebookGrabber(String handleListFilePath) {
		super(handleListFilePath);
		_facebook = new FacebookFactory(createDefaultConfiguration()).getInstance();		
		
		// TODO Auto-generated constructor stub
	}
	
	private Configuration createDefaultConfiguration() {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.setDebugEnabled(true);
		
		configBuilder.setOAuthAppId(APP_ID); 
		configBuilder.setOAuthAppSecret(APP_SECRET); 
		configBuilder.setOAuthAccessToken(ACCESS_TOKEN); 
		
		configBuilder.setOAuthPermissions("email,publish_stream, id, name, first_name, last_name, generic"); 
		configBuilder.setUseSSL(true); 
		configBuilder.setJSONStoreEnabled(true); 
		
		return configBuilder.build();
	}

	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void grab() throws Exception {
		for (String handle : _handleList) {
			System.out.println("Reading facebook posts from user: " + handle);
			
			Vector<Post> handlePosts = new Vector<Post>();
			
			try {
				Reading reading = new Reading();
				reading.limit(100);
				if (_latestIdForHandle.get(handle) != -1) {
					reading.since(new Date(_latestIdForHandle.get(handle)));
				}
				
				ResponseList<Post> results = _facebook.getPosts(handle, reading);
				Paging<Post> paging;
				int numberOfPagesRead = 1;
				// We will limit ourselves to read only 20*100 posts.
				do {
					for (Post p : results) {
						handlePosts.addElement(p);
					}
					numberOfPagesRead++;
					paging = results.getPaging();
				} while (numberOfPagesRead <= 20 && paging != null && (results = _facebook.fetchNext(paging)) != null);
				
				
			} catch (FacebookException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(
						TwitterGrabber.class.getName()).log(
								Level.WARNING, "Unable to read tweets of user: " + handle, e);
			}
			
			FileWriter fw = new FileWriter(filePathForHandle(handle), true);
			for (int i = handlePosts.size()-1; i >= 0; i--) {
				String csvString = postToCSVString(handlePosts.elementAt(i));
				if (csvString != null) {
					fw.write(csvString);
				}
			}
			fw.close();
			System.out.println(handlePosts.size() + " posts from user " + handle + " written to file.");
		}
	}

	private String postToCSVString(Post post) {
		if (post.getMessage() != null) {
			try {
				String postText = URLEncoder.encode(post.getMessage(), "UTF-8");
				return  post.getCreatedTime().getTime() + "," + "\"" + DATE_FORMAT.format(post.getCreatedTime()) + "\"" + "," + "\"" + postText + "\"\n";
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	protected String filePathForHandle(String handle) {
		return "data/social-data/"+handle+"-facebook.csv";
	}
}

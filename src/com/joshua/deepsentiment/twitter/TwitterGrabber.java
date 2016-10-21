/**
 * 
 */
package com.joshua.deepsentiment.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.joshua.deepsentiment.SocialGrabber;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
 
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
 
public class TwitterGrabber extends SocialGrabber{
	private static final String OAUTH_TOKEN = "JQrpJj696sYQM17B2KR8lv8j8";
	private static final String OAUTH_KEY = "lwrcfaTI1diA4TWYOMp2sNE18ZBGDl3S6JKWv9DxDTc9H8Rz5R";
	
	// Instance of the twitter API
	private Twitter _twitter;
	private AccessToken _accessToken;
	
	public TwitterGrabber(String handleListFilePath) {
		super(handleListFilePath);
		_twitter = new TwitterFactory().getInstance();
		_accessToken = null;
	}
	
	protected String filePathForHandle(String handle) {
		return "data/social-data/"+handle+"-tweets.csv";
	}
	
	private String twitterStatusToCSVString(Status status) {
		try {
			String tweetText = URLEncoder.encode(status.getText(), "UTF-8");
			return  status.getId() + "," + "\"" + DATE_FORMAT.format(status.getCreatedAt()) + "\"" + "," + "\"" + tweetText + "\"\n";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void loadAccessTokenIfAvailable() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("auth/acesstoken.csv")));
			String accessTokenCSVString = br.readLine();
			String[] parts = accessTokenCSVString.split(",");
			_accessToken = new AccessToken(parts[0], parts[1]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			_accessToken = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void storeAccessToken(AccessToken accessToken) {
		String accessTokenCSVString = accessToken.getToken() + "," + accessToken.getTokenSecret();
        System.out.println(accessTokenCSVString);
        try {
			FileWriter fw = new FileWriter("auth/acesstoken.csv");
			fw.write(accessTokenCSVString + "\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void connect() throws TwitterException {
		// The factory instance is re-useable and thread safe.
        _twitter.setOAuthConsumer(OAUTH_TOKEN, OAUTH_KEY);

        RequestToken requestToken = _twitter.getOAuthRequestToken();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        loadAccessTokenIfAvailable();
        while (null == _accessToken)
        {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = null;
            try
            {
                pin = br.readLine();
            } catch (IOException ex)
            {
                Logger.getLogger(TwitterGrabber.class.getName()).log(Level.SEVERE, null, ex);
            }
            try
            {
                _accessToken = pin.length() > 0 ? 
                		_twitter.getOAuthAccessToken(requestToken, pin) 
                		: _twitter.getOAuthAccessToken();
            } catch (TwitterException te)
            {
                if (401 == te.getStatusCode())
                {
                    System.out.println("Unable to get the access token.");
                }
                te.printStackTrace();
                throw te;
            }
        }
        _twitter.setOAuthAccessToken(_accessToken);
        storeAccessToken(_accessToken);
        _twitter.verifyCredentials();
	}
	
	public void grab() throws TwitterException, IOException {
		for (String handle : _handleList) {
			System.out.println("Reading tweets from user: " + handle);
			Vector<Status> handleStatuses = new Vector<Status>();
			long maxId = -1;
			try {
				while (true) {
					Paging paging = new Paging();
					paging.setCount(200);
					if (maxId != -1) {
						paging.setMaxId(maxId-1);
					}
					if (_latestIdForHandle.get(handle) != null && _latestIdForHandle.get(handle) != -1) {
						paging.setSinceId( _latestIdForHandle.get(handle));
					}
					List<Status> statuses = _twitter.getUserTimeline(handle,paging);
					if (statuses.size() > 0) {
						handleStatuses.addAll(statuses);
						maxId = statuses.get(statuses.size()-1).getId();
					} else {
						break;
					}
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(
						TwitterGrabber.class.getName()).log(
								Level.WARNING, "Unable to read tweets of user: " + handle, e);
			}
			
			FileWriter fw = new FileWriter(filePathForHandle(handle), true);
			for (int i = handleStatuses.size()-1; i >= 0; i--) {
				fw.write(twitterStatusToCSVString(handleStatuses.elementAt(i)));
			}
			fw.close();
			System.out.println(handleStatuses.size() + " tweets from user " + handle + " written to file.");
		}
	}
}

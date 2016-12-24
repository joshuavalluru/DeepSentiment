package com.joshua.deepsentiment;

import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.joshua.deepsentiment.facebook.FacebookGrabber;
import com.joshua.deepsentiment.nasdaq.NasdaqReader;
import com.joshua.deepsentiment.twitter.TwitterGrabber;

import twitter4j.TwitterException;

public class DeepSentiment {
	public static void main(String args[]) throws IOException
    {
        try
        {
        	NasdaqReader nasdaqReader = new NasdaqReader();
        	nasdaqReader.read();
        	
        	// Now we download all the tweets
        	System.out.println ("***************** TWITTER **********************");
        	TwitterGrabber twitterGrabber = new TwitterGrabber("data/handles/twitter-economists.txt");
        	twitterGrabber.connect();
        	twitterGrabber.grab();
        	
        	System.out.println ("***********************************************");
        	
        	// Now we download all the facebook posts
        	System.out.println ("***************** FACEBOOK *********************");
        	FacebookGrabber facebookGrabber = new FacebookGrabber("data/handles/facebook-page-handles.txt");
        	facebookGrabber.connect();
        	facebookGrabber.grab();
        	
        	System.out.println ("***********************************************");
        	
        	Vector<SocialGrabber> grabbers = new Vector<SocialGrabber>();
        	grabbers.addElement(twitterGrabber);
        	grabbers.addElement(facebookGrabber);
        	
        	PostParser parser = new PostParser(grabbers, "data/whitelist-words.txt", nasdaqReader);
        	parser.parse();
        } catch (Exception ex)
        {
            Logger.getLogger(DeepSentiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

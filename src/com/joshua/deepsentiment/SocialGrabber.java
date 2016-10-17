package com.joshua.deepsentiment;

import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Abstract class for grabbing social media posts (Tweets, FB etc)
 *
 */
public abstract class SocialGrabber {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
	
	// File path for the handle list
	protected String _handleListFilePath;
	
	// List of handles currently being tracked by this grabber.
	protected Vector<String> _handleList; 
	public Vector<String> handleList() {
		return _handleList;
	}
	
	/**
	 * Initializer
	 * @param handleListFilePath
	 */
	public SocialGrabber(String handleListFilePath) {
		this._handleListFilePath = handleListFilePath;
	}
	
	/**
	 * Connects to the API
	 * @throws Exception
	 */
	public abstract void connect() throws Exception;
	/**
	 * Downloads the stores the posts into files. The file is sorted in reverse chronological order.
	 * @throws Exception
	 */
	public abstract void grab() throws Exception;
	
	/**
	 * Returns the path of the file which contains the posts for a given handle.
	 * @param handle
	 * @return
	 */
	protected abstract String filePathForHandle(String handle);
}

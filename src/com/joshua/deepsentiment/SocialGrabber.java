package com.joshua.deepsentiment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;

/**
 * Abstract class for grabbing social media posts (Tweets, FB etc)
 *
 */
public abstract class SocialGrabber {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
	
	// File path for the handle list
	protected String _handleListFilePath;
	
	// Latest ID that has been read so far, for each handle.
	protected HashMap<String, Long> _latestIdForHandle;
	
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
		readHandleList();
		readLatestIds();
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
	
	/**
	 * Reads the list of handles from the handlelistFile. The file has one handle per line
	 * @return
	 */
	private boolean readHandleList() {
		_handleList = new Vector<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_handleListFilePath)));
			for (String handle = br.readLine(); handle != null; handle = br.readLine()) {
				// Line starting with # are comments.
				if (handle.startsWith("#")) {
					continue;
				} else if (handle.startsWith("@")) {
					_handleList.addElement(handle.substring(1));
				} else {
					_handleList.addElement(handle);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Reads the latest ID that has been read so far, for each handle. This ID is the last line in the handle file.
	 */
	private void readLatestIds() {
		_latestIdForHandle = new HashMap<String, Long>();
		for (String handle : _handleList) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePathForHandle(handle))));
				String lastLine = null; 
				String line;
				// Rend until the end of the file (we are looking for the last tweet in the file.
				while ((line = br.readLine()) != null) {
					lastLine = line;
				}
				if (lastLine != null) {
					String[] parts = lastLine.split(",");
					_latestIdForHandle.put(handle, Long.parseLong(parts[0]));
				} else {
					_latestIdForHandle.put(handle, (long) -1);
				}
				br.close();
			} catch (FileNotFoundException e) {
				System.out.println("File not found for " + handle);
				_latestIdForHandle.put(handle, (long) -1);
				
			} catch (IOException e) {
				System.out.println("File couldn't be read found for " + handle);
				_latestIdForHandle.put(handle, (long) -1);
			}
			System.out.println (handle + " " + _latestIdForHandle.get(handle));
		}
	}
}

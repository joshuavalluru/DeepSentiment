package com.joshua.deepsentiment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.joshua.deepsentiment.nasdaq.NasdaqReader;

public class PostParser {
	private Vector<SocialGrabber> _grabbers;
	
	// Path to the file containing the whitelist of words we are interested in.
	private String _whitelistFilePath;
	
	private Vector<String> _whitelist;
	
	private NasdaqReader _nasdaqReader;
	
	// For each date, we have a word count map. 
	private HashMap<String, HashMap<String, Integer>> _wordCount;
	
	/**
	 * Initializer
	 * @param grabbers - All the social grabbers to read the posts from.
	 * @param whitelistFilePath - A list of white-listed words to calculate the word counts for
	 * @param nasdaqReader - NASDAQ data as ground truth
	 */
	public PostParser(Vector<SocialGrabber> grabbers, String whitelistFilePath, NasdaqReader nasdaqReader) {
		_grabbers = grabbers;
		_nasdaqReader = nasdaqReader;
		_whitelistFilePath = whitelistFilePath;
		loadWhitelist();
	}
	
	/**
	 * Parses all the posts from all social grabbers and calculated the word count.
	 * @throws IOException
	 */
	public void parse() throws IOException {
		_wordCount = new HashMap<String, HashMap<String, Integer>>();
		
		for (SocialGrabber grabber : _grabbers) {
			for (String handle : grabber._handleList) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(grabber.filePathForHandle(handle))));
				String line;
				while ((line = br.readLine()) != null) {
					parseLine(line);
				}
				br.close();
			}
		}
		
		printWordCount();
	}
	
	/**
	 * Prints the entire word count. Also saves it in a CSV file at data/training/training.csv
	 */
	private void printWordCount() {
		try {
			FileWriter fwHumanReadable = new FileWriter("data/training/training-human-readable.csv");
			FileWriter fwTraining = new FileWriter("data/training/training.csv");
			FileWriter fwTesting = new FileWriter("data/training/test.csv");
			
			fwHumanReadable.write("Date,");
			for (String word : _whitelist) {
				fwHumanReadable.write(word + ", ");
			} 
			fwHumanReadable.write("Label\n");
			
			
			for (String date : _wordCount.keySet()) {
				
				// We pick a random ~75% for training and rest for testing.
				FileWriter testOrTraining = Math.random() <= 0.75 ? fwTraining : fwTesting;
				
				Date d = SocialGrabber.DATE_FORMAT.parse(date, new ParsePosition(1));
				System.out.print (date + " : ");
				fwHumanReadable.write(date + ",");
				testOrTraining.write(date + ",");
				
				for (String word : _whitelist) {
					System.out.print(word + "(" + _wordCount.get(date).get(word) + "),");
					fwHumanReadable.write(_wordCount.get(date).get(word)+",");
					testOrTraining.write(_wordCount.get(date).get(word)+",");
				}
				
				System.out.printf("NASDAQ_CHANGE(%.2f)\n",_nasdaqReader.daysChange(d));
				fwHumanReadable.write(_nasdaqReader.daysChange(d) > 0 ? "1\n" : "0\n");
				testOrTraining.write(_nasdaqReader.daysChange(d) > 0 ? "1\n" : "0\n");
			}
			
			fwHumanReadable.close();
			fwTraining.close();
			fwTesting.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Prints the white-list for debugging.
	 */
	private void printWhitelist() {
		for (String word : _whitelist) {
			System.out.print(word + ", ");
		}
		System.out.println();
	}
	
	/**
	 * Parses a single line in the CSV file into word count.
	 * @param line
	 * @throws UnsupportedEncodingException
	 */
	
	private void parseLine(String line) throws UnsupportedEncodingException {
		// This CSV file has the following format <MESSAGE_ID>,<DATE>,<MESSAGE>
		String[] parts = line.split(",");
		String date = parts[1];
		if (_wordCount.get(date) == null) {
			_wordCount.put(date, new HashMap<String, Integer>());
			for (String word : _whitelist) {
				_wordCount.get(date).put(word, 0);
			}
		}
		
		// Second index of , marks the beginning of the post (e.g., tweet).
		String post = line.substring(line.indexOf(',', line.indexOf(',')+1)+2, line.length()-1);
		// Message is encoded with URLEncoder so we must decode to get regular text.
		post = URLDecoder.decode(post, "UTF-8");
		
		// Break it into words by removing all punctuation letters.
		String cleanedUpPost = post.replaceAll("[^a-zA-Z ]", "").toLowerCase();
		String[] words = cleanedUpPost.split("\\s+");
		
		// For each word check the white-list and increment the corresponding word-count if necessary.
		for (String word: words) {
			if (_whitelist.contains(word)) {
				_wordCount.get(date).put(word, _wordCount.get(date).get(word) + 1);
			}
		}
	}

	/**
	 * Reads the white-list from the file. One word per line.
	 */
	private void loadWhitelist() {
		_whitelist = new Vector<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_whitelistFilePath)));
			String line;
			
			while ((line = br.readLine()) != null) {
				String[] words = line.split(" ");
				for (String word : words) {
					if (!_whitelist.contains(word)) { 
						_whitelist.addElement(word);
					}
				}
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		printWhitelist();
	}
}

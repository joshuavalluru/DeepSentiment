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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import com.joshua.deepsentiment.nasdaq.NasdaqReader;

public class PostParser {
	public static final SimpleDateFormat SUFFIX_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
	
	private Vector<SocialGrabber> _grabbers;
	
	// Path to the file containing the whitelist of words we are interested in.
	private String _whitelistFilePath;
	
	private Vector<String> _whitelist;
	
	private NasdaqReader _nasdaqReader;
	
	// For each date, we have a word count map. 
	private HashMap<String, HashMap<String, Integer>> _wordCount;
	
	// For each date, we have a count of number of posts for that day. 
	private HashMap<String, Integer> _postCount;
	
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
		parse(false);
	}
	
	/**
	 * Parses all the posts from all social grabbers and calculated the word count. If cumulative is true, 
	 * then generates cumulative training files for all dates.
	 * @throws IOException
	 */
	public void parse(boolean cumulative) throws IOException {
		_wordCount = new HashMap<String, HashMap<String, Integer>>();
		_postCount = new HashMap<String, Integer>();
		
		int totalPosts = 0;
		
		
		for (SocialGrabber grabber : _grabbers) {
			for (String handle : grabber._handleList) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(grabber.filePathForHandle(handle))));
				String line;
				while ((line = br.readLine()) != null) {
					parseLine(line);
					totalPosts++;
				}
				br.close();
			}
		}
		
		System.out.println("Total Number of posts: " + totalPosts);
		
		saveWordCountBefore(null);
		
		if (cumulative) {
			Vector<Date> dates = getSortedMonths();

			for (Date d : dates) {
				saveWordCountBefore(d);
			}
		}
	}
	
	private Vector<Date> getSortedMonths() {
		Vector<Date> dates = new Vector<Date>();
		for (String date : _wordCount.keySet()) {
			Date d = SocialGrabber.DATE_FORMAT.parse(date, new ParsePosition(1));
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			d = cal.getTime();
			if (!dates.contains(d)) {
				dates.addElement(d);
			}
		}
		Collections.sort(dates);
		return dates;
	}
	/* 
	 * Returns all the dates in a sorted vector.
	 */
	private Vector<Date> getSortedDates() {
		Vector<Date> dates = new Vector<Date>();
		for (String date : _wordCount.keySet()) {
			Date d = SocialGrabber.DATE_FORMAT.parse(date, new ParsePosition(1));
			dates.addElement(d);
		}
		Collections.sort(dates);
		return dates;
	}
	
	/**
	 * Prints the entire word count. Also saves it in a CSV file at data/training/training.csv
	 */
	private void saveWordCountBefore(Date beforeDate) {
		try {
			String dateSuffix = beforeDate == null ? "" : "-"+SUFFIX_DATE_FORMAT.format(beforeDate);
			if (beforeDate != null) {
				System.out.println("Generating training data until : "+ dateSuffix);
			}
			FileWriter fwHumanReadable = new FileWriter("data/training/training-human-readable"+dateSuffix+".csv");
			FileWriter fwTraining = new FileWriter("data/training/training"+dateSuffix+".csv");
			FileWriter fwTesting = new FileWriter("data/training/test"+dateSuffix+".csv");
			FileWriter fwPostCounts = new FileWriter("data/post-counts.csv");
			
			fwHumanReadable.write("Date,");
			for (String word : _whitelist) {
				fwHumanReadable.write(word + ", ");
			} 
			fwHumanReadable.write("Label\n");
			
			// Sort the dates first to make the training file readable.
			Vector<Date> dates = getSortedDates();
			
			int numberOfPosts = 0;
			int numberOfDays = 0;
			
			for (Date d : dates) {
				
				if (beforeDate != null && d.after(beforeDate)) {
					continue;
				}
				
				// We pick a random ~75% for training and rest for testing.
				FileWriter testOrTraining = Math.random() <= 0.75 ? fwTraining : fwTesting;
				
				// The date in the map has quotes in it.
				String date = "\"" + SocialGrabber.DATE_FORMAT.format(d) + "\"";
				
				String lineToBeWritten = date + ",";
				
				if (beforeDate == null) {
					System.out.print (date + "(" + _postCount.get(date) + ")" + " : ");
					if (testOrTraining == fwTraining) {
						numberOfPosts += _postCount.get(date);
						numberOfDays++;
						if (numberOfDays % 30 == 0) {
							fwPostCounts.write(numberOfDays+","+numberOfPosts+"\n");
						}
					}
				}
				
				
				
				for (String word : _whitelist) {
					if (beforeDate == null) {
						System.out.print(word + "(" + _wordCount.get(date).get(word) + "),");
					}
					lineToBeWritten += (_wordCount.get(date).get(word)+",");
				}
				
				if (_nasdaqReader.daysChange(d) == null) {
					if (beforeDate == null) {
						System.out.println();
					}
					continue;
				}
				
				if (beforeDate == null) {
					System.out.printf("NASDAQ_CHANGE(%.2f)\n",_nasdaqReader.daysChange(d));
				}
				
				if (_nasdaqReader.daysChange(d) != null && _nasdaqReader.daysChange(d) > 0) {
					lineToBeWritten += "1";
				} else {
					lineToBeWritten += "0";
				}
				fwHumanReadable.write(lineToBeWritten+"\n");
				testOrTraining.write(lineToBeWritten+"\n");
			}
			
			System.out.println(numberOfPosts);
			
			fwPostCounts.close();
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
		System.out.println("Number of words in the whitelist: " + _whitelist.size());
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
			_postCount.put(date, 1);
			for (String word : _whitelist) {
				_wordCount.get(date).put(word, 0);
			}
		} else {
			_postCount.put(date, _postCount.get(date)+1);
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

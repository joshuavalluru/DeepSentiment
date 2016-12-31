package com.joshua.deepsentiment.test;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
public class ReadingCounting {
	public static void main(String[] args) throws IOException {
		ArrayList<String> whitelist = readWhitelist();
		BufferedReader input = new BufferedReader(new FileReader("data/social-data/A_Lusardi-tweets.csv")); // put em in a loop
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<ArrayList<Integer>> whitelistCount = new ArrayList<ArrayList<Integer>>();
		String line;
		while ((line = input.readLine()) != null) {
			String newDate = giveDate(line);
			String originalPost = line.substring(line.indexOf(',', line.indexOf(',') + 1) + 2, line.length() - 1);
			String normalPost = URLDecoder.decode(originalPost, "UTF-8");
			String cleanedUpPost = normalPost.replaceAll("[^a-zA-Z ]", " ").toLowerCase();
			int dateLocation = dates.indexOf(newDate);
			if (dateLocation == -1) { // if date doesn't exist in the ArrayList
				// add it and add a new row for the word
				// count
				dates.add(newDate);
				whitelistCount.add(countWordOccurrences(cleanedUpPost, whitelist));
			} else {
				whitelistCount.set(dateLocation, mergeRows(whitelistCount.get(dateLocation), countWordOccurrences(cleanedUpPost, whitelist)));
			}
		}

		int index1 = 0;
		for (String i : dates) {
			System.out.print(i + " ");
			int index = 0;
			for (int k : whitelistCount.get(index1)) {
				System.out.print(whitelist.get(index) + ": " + k + " ");
				index++;
			}
			System.out.println("");
			index1++;
		}
	}
	// I won't use the read handle list yet until I can successfully examine a
	// single file
	public static ArrayList<String> readHandleList() throws IOException {
		ArrayList<String> handleList = new ArrayList<String>();
		BufferedReader input = new BufferedReader(new FileReader("data/handles/twitter-economists.txt"));
		String line;
		while ((line = input.readLine()) != null) {
			handleList.add(line);
		}
		input.close();
		return handleList;
	}
	// I need to learn how to use a separate private class
	public static ArrayList<String> readWhitelist() throws IOException {
		ArrayList<String> whitelist = new ArrayList<String>();
		BufferedReader input = new BufferedReader(new FileReader("data/whitelist-words.txt"));
		String line;
		while ((line = input.readLine()) != null) {
			whitelist.add(line);
		}
		input.close();
		return whitelist;
	}
	public static String giveDate(String line) {
		return line.substring(20, 30);
	}
	public static ArrayList<Integer> countWordOccurrences(String cleanedUpPost, ArrayList<String> whitelist) {
		ArrayList<Integer> wordOccurrences = new ArrayList<Integer>();
		String[] postParts = cleanedUpPost.split(" ");
		for (String whitelistWord : whitelist) {
			int wordCounter = 0;
			for (String postWord : postParts) {
				if (whitelistWord.equals(postWord)) {
					wordCounter++;
				}
			}
			wordOccurrences.add(wordCounter);
		}
		return wordOccurrences;
	}
	// merges original row from 2d arraylist with word count from an arraylist
	// with corresponding dates
	public static ArrayList<Integer> mergeRows(ArrayList<Integer> originalRow, ArrayList<Integer> newRow) {
		ArrayList<Integer> mergedRow = new ArrayList<Integer>();
		for (int i = 0; i < originalRow.size(); i++) {
			mergedRow.add(originalRow.get(i) + newRow.get(i));
		}
		return mergedRow;
	}
}

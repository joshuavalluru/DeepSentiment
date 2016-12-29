package com.joshua.deepsentiment.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileParserTest {
	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader("data/test/parsedate.csv"));
		String line;
		while ((line = input.readLine()) != null) {
			String[] parts = line.split(", ");
			System.out.print(parts[1] + " ");
			wordCount(parts[2]);
			System.out.println("");
		}
	}
	
	public static void wordCount (String message) {
		int wordCount = 0;
		String[] whitelist = {"good", "bad", "economy"};
		String[] messageParts = message.split(" ");
		for (int i = 0; i < whitelist.length; i++) {
			wordCount = 0;
			for (int j = 0; j < messageParts.length; j++) {
				if (messageParts[j].equals(whitelist[i])) {
					wordCount++;
				}
			}
			
			System.out.print(whitelist[i] + ": " + wordCount + " ");
		}
	}
}

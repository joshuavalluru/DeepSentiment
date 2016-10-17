package com.joshua.deepsentiment.nasdaq;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class NasdaqReader {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private HashMap<Date, Double> _closingValueMap;
	public HashMap<Date, Double> closingValueMap() {
		return _closingValueMap;
	}
	
	/**
	 * Returns the change between closing value of given date and it's previous day. If the data is not available
	 * for either dates (e.g., weekend), then returns zero.
	 * @param date
	 * @return
	 */
	public double daysChange(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, -1);
		Date previousDate = cal.getTime();
		
		if (_closingValueMap.get(date) == null || _closingValueMap.get(previousDate) == null) {
			return 0;
		} else {
			return _closingValueMap.get(date) - _closingValueMap.get(previousDate);
		}
	}
	
	public NasdaqReader() {
	}
	
	/**
	 * Reads the NASDAQ historical data from Yahoo and stores it in a CSV file.
	 * @throws IOException
	 * @throws ParseException
	 */
	public void read() throws IOException, ParseException {
		_closingValueMap = new HashMap<Date, Double>();
		// We use YAHOO api to download the historical NASDAQ index. It's already in CSV format.
		// We will have to calculate the labels (up/down) from this.
		
		URL nasdaqYahooAPI = new URL("http://real-chart.finance.yahoo.com/table.csv?s=%5EIXIC&d=9&e=16&f=2016&g=d&a=1&b=5&c=1971&ignore=.csv");
		BufferedReader br = new BufferedReader(new InputStreamReader(nasdaqYahooAPI.openStream()));
		
		FileWriter fw = new FileWriter("data/nasdaq/history.csv");
		
		int numberOfLinesRead = 0;
		String inputLine;
        while ((inputLine = br.readLine()) != null) {
            fw.write(inputLine + "\n");
            // First line is the header.
            if (numberOfLinesRead > 0) {
	            String[] parts = inputLine.split(",");
	            Date date = DATE_FORMAT.parse(parts[0]);
	            _closingValueMap.put(date, Double.parseDouble(parts[4]));
            }
            
            numberOfLinesRead++;
        }
        
        fw.close();
        br.close();
	}
}

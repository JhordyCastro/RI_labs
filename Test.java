package org.novasearch.tutorials.labs2018;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

	static String queriesFilePath = "./eval/queries.offline.txt";

	public static void main(String[] args) {
		List<Results> textFileResults = new ArrayList<>();
		try
		{
			// create a Buffered Reader object instance with a FileReader
			BufferedReader br = new BufferedReader(new FileReader(queriesFilePath));

			// read the first line from the text file
			String fileRead = br.readLine();

			// loop until all lines are read
			while (fileRead != null)
			{

				// use string.split to load a string array with the values from each line of
				// the file, using a comma as the delimiter
				String[] tokenize = fileRead.split(":");

				// assume file is made correctly
				// and make temporary variables for the three types of data
				//String tempItem = tokenize[0];
				int tempQty = Integer.parseInt(tokenize[0]);
				//float tempPrice = Float.parseFloat(tokenize[2]);

				// creat temporary instance of Inventory object
				// and load with three data values
				Results tempObj = new Results(tempQty);

				// add to array list
				textFileResults.add(tempObj);

				// read next line before looping
				// if end of file reached 
				fileRead = br.readLine();
			}

			// close file stream
			br.close();
		}
		
		// handle exceptions
		catch (FileNotFoundException fnfe)
		{
			System.out.println("file not found");
		}

		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		// display inventory
		for (Results each : textFileResults)
		{
			System.out.println(each);
		}

		}
	
	}



package org.juddholm.sthlmstreetnames;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class StreetFinder {

	private FilterSearcher searcher;
	
	public StreetFinder(FilterSearcher searcher) {
		this.searcher = searcher;
	}	
	
	public boolean isStreet(String addr)
	{
		return searcher.contains(addr);
	}	
	
	private void checkIsStreet(ArrayList<String> streets, StringBuilder namebuilder) {
		// Check if name buffer content is a street then reset buffer.
		String potential_street = namebuilder.toString();
		
		
		if(potential_street.length() > 0)
		{				
			System.out.println(potential_street);
			if(searcher.contains(potential_street))
				streets.add(potential_street);
		}
		
	}
	
	/**
	 * Finds all streets in the FilterSearcher assuming the are named by convention of big letter first.
	 * Should be able to find streets with more than one word if each word in the name are capitalized.
	 * @param text
	 * @return
	 */
	public List<String> getStreets(String text)
	{
		StringTokenizer tokenizer = new StringTokenizer(text," \t\n\r\f");
		
		ArrayList<String> streets = new ArrayList<String>();
		String regex = "[\\!\\,\\.\\?\\/\\;\\:\\-]";
		StringBuilder namebuilder = new StringBuilder();
		while(tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			
		
			if(!Character.isUpperCase(token.charAt(0)))
			{
				checkIsStreet(streets, namebuilder);	
				namebuilder = new StringBuilder();
			} else {
				// Is capitalized word. Add to buffer
				if(namebuilder.length() > 0)
					namebuilder.append(" ");
				
				if(Pattern.matches(regex, token))
				{
					token = token.replaceAll(regex, "");
					namebuilder.append(token);
					checkIsStreet(streets, namebuilder);
					namebuilder = new StringBuilder();
				} else {
					namebuilder.append(token);
				}
				
			}
			
		}
		
		// Check last part
		String potential_street = namebuilder.toString();
		if(searcher.contains(potential_street))
			streets.add(potential_street);
		
		return streets;		
	}
	
	public static void main(String[] args)
	{
		if(args.length < 2) {
			System.out.println("USAGE: streetfinder <input bloom filter file> <input text file>");
			return;
		}
		
		FilterSearcher searcher = null;
		try {
			searcher = new FilterSearcher(args[0]);
		} catch (IOException e1) {
			System.out.println("Failed to open bloom filter file!");
			return;
		}
		
		StringBuilder txtbuilder = new StringBuilder();
		
		try {
			FileInputStream stream = new FileInputStream(args[1]);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			
			String line = null;
			while((line = reader.readLine()) != null)
			{
				txtbuilder.append(line);
			}
			reader.close();			
			
		} catch (FileNotFoundException e) {
			System.out.println("No such file: " + args[1]);
			return;
		} catch (IOException e) {
			System.out.println("Failed to read input file!");
			e.printStackTrace();
			return;
		} 
		
		StreetFinder finder = new StreetFinder(searcher);
		List<String> streets = finder.getStreets(txtbuilder.toString());
		
		System.out.println("Found " + streets.size() + " streets in input text");
		for(int i = 0; i < streets.size(); i++)
		{
			System.out.println("["+(i+1)+"] " + streets.get(i));
		}
		
		
		
	}
}

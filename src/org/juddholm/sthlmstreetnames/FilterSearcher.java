package org.juddholm.sthlmstreetnames;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import javax.management.RuntimeErrorException;

import com.skjegstad.utils.BloomFilter;

/**
 * Basic class to load a serialized bloom filter and search for entries.
 * @author Tethik
 */
public class FilterSearcher {
	
	private BloomFilter<String> filter;

	public FilterSearcher(String filename) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		try {
			filter = (BloomFilter<String>) in.readObject();
		} catch (ClassNotFoundException e) {					
			throw new RuntimeException(e);			
		}
	}
	
	public boolean contains(String item)
	{
		return filter.contains(item);
	}	
	
	public static void main(String[] args) {		
		if(args.length < 1)
		{
			System.out.println("USAGE: FilterSearch <filter file> [search term]");
			return;
		}
		
		FilterSearcher search;
		try {
			search = new FilterSearcher(args[0]);
		} catch (IOException e) {			
			e.printStackTrace();
			System.err.println("Failed to load bloom filter!");
			return;
		}
		
		if(args.length > 1)
		{
			for(int i = 1; i < args.length; i++)
				System.out.println(args[i] + " " + (search.contains(args[i]) ? "FOUND" : "NOT FOUND"));			
			return;
		}
		
		// Read lines from stdin.
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		for(;;)
		{			
			try {
				String line = reader.readLine();
				System.out.println(line + " " + (search.contains(line) ? "FOUND" : "NOT FOUND"));
			} catch (IOException e) {		
				e.printStackTrace();
				System.out.println("Hatar java ibland..");
			}					
		}		
	}
}

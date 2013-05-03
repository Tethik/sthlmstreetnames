import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skjegstad.utils.BloomFilter;


/**
 * Builds a bloom filter from a open street maps osm file.
 * @author Tethik
 */
public class FilterBuilder {
	
	private BloomFilter<String> filter;
	private double falsePositive;
	private File osmfile;
	private ArrayList<String> matches = new ArrayList<String>();
	
	public FilterBuilder(String filename, double falsePositive)
	{
		this.falsePositive = falsePositive;
		osmfile = new File(filename);
		
		if(!osmfile.exists() || !osmfile.canRead())
			throw new IllegalArgumentException("Input file could not be opened!");
	}
	
	public void parse() throws IOException
	{				
		// lazy reading. Assume any <tag k="name" v="?"> line is a street name
		// and grab the value of v
		FileInputStream in = new FileInputStream(osmfile);		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Pattern pattern = Pattern.compile("<tag k=\"name\" v=\"(.*)\"/>");
		
		String line;
		while((line = reader.readLine()) != null)
		{
			Matcher matcher = pattern.matcher(line);
			if(matcher.find()) {
				matches.add(matcher.toMatchResult().group(1));
				//System.out.println(matches.get(matches.size()-1));
			}
		}
		reader.close();		
		
		// Construct bloom filter	
		System.out.println("Found " + matches.size() + " matches");
		filter = new BloomFilter<String>(falsePositive, matches.size());
		
		// Insert into bloom filter
		for(String match : matches)
			filter.add(match);		
	}
	
	private String randomString()
	{
		StringBuilder builder = new StringBuilder();
		
		Random random = new Random();		
		for(int i = 0; i < 20; i++)
		{
			builder.append((char) (48 +  random.nextInt(74)));
		}
		
		return builder.toString();		
	}

	
	public void test() {
		int hits = 0;
		for(String addr : matches)
			if(filter.contains(addr))
				hits++;
		
		System.out.println("Positive hit rate: " + hits + " / " + matches.size() + " = " + ((double) hits / (double) matches.size()));
		
		hits = 0;
		for(int i = 0; i < matches.size() * 10; i++)
		{
			String str = randomString();
			if(filter.contains(str) && !matches.contains(str))
				hits++;
		}
		System.out.println("Negative hit rate: " + hits + " / " + matches.size() * 10 + " = " + ((double) hits / (double) (matches.size() * 10)));
	}
	
	
	public BloomFilter<String> getFilter()
	{
		return filter;
	}
	
	
	public static void main(String[] args) {
		if(args.length < 2) 
		{
			System.out.println("USAGE: filterbuilder <input osm file> <output binary filter file> [false positive probability]");
			System.out.println("What it does: builds a bloom filter for street names from a osm file.");
			System.out.println("This filter can be used to perform quick lookups for street names.");
			return;
		}
		
		double falsePositive = 0.1;
		if(args.length > 2)
		{
			falsePositive = Double.parseDouble(args[2]);
		}
		File outputfile = new File(args[1]);
		try {
			outputfile.createNewFile();			
		} catch (IOException e1) {
			e1.printStackTrace();
			
			System.err.println("Could not create output file. I will quit before wasting your time.");
			return;
		}
		
		FilterBuilder builder = new FilterBuilder(args[0], falsePositive);
		try {
			builder.parse();
			builder.test();
		} catch (IOException e) {			
			e.printStackTrace();
			System.err.println("IO Error while parsing osm file :(");
			return;
		}
		
		System.out.println("Writing to file");
		try {
			FileOutputStream os = new FileOutputStream(outputfile, false);
			ObjectOutputStream objos = new ObjectOutputStream(os);
			objos.writeObject(builder.getFilter());
			objos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Failed to write to outputfile :(");
			return;
		}
		
		System.out.println("Done!");
	}
	
	
}

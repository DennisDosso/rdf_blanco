package it.unipd.dei.ims.precompute;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.log4j.Logger;

import it.unipd.dei.ims.main.Utilities;

/**This class is part of the implementation of Elbassuoni and Blanco's paper 'Keyword Search Over RDF Graphs'.
 * 
 * After the retrieval of the subgraphs G from the query graph E,
 * this class transforms the .nt files that describes the subgraphs G
 * in XML files for the ranking process to take place.
 * 
 * 
 *@author Dennis Dosso, University of Padua
 *
 **/
public class FromRetrievedRDFGraphsToXMLDocumentConverter {


	private static Properties prop = new Properties();
	private static InputStream input = null;
	static Logger log = Logger.getLogger(FromRetrievedRDFGraphsToXMLDocumentConverter.class);

	private static final String INPUT_DIRECTORY = "input_directory";
	private static final String OUTPUT_DIRECTORY = "output_directory";

	public static void main (String[] args) {

		log.debug("------ Starting conversion from rdf to xml ----- \n");
		Stopwatch timer = Stopwatch.createStarted();

		try {
			//read the paths: the directory path where are located the .nt files and the directory where to write the xml file
			Map<String, String> pathMap = getPaths ();

			executeConversion(pathMap);

			System.out.println("done");
			System.out.println(" ----- convertion terminated  in " + timer.stop() 
			+ " ------- \n");


		} catch (IOException e) {
			e.printStackTrace();
			log.error("File not found");
		}
	}


	/**
	 * Executes the conversion from several .nt files containing RDF subgraphs of the
	 * query graph E in one xml document containing their conversion in simple documents
	 * following the Blanco-Elbassouni paper.
	 * 
	 * @input pathMap map with input and output directories paths
	 * 
	 * */
	private static void executeConversion( Map<String, String> pathMap) {

		//get input path
		Path inputPath = Paths.get(pathMap.get(INPUT_DIRECTORY));
		Path outputPath = Paths.get(pathMap.get(OUTPUT_DIRECTORY));
		List<Path> fileList = new ArrayList<Path>();

		try (Stream<Path> paths = Files.walk(inputPath)) {//read the files in the directory
			//create a list of paths
			//only with Java 8
			fileList = paths.filter(Files::isRegularFile)
					.filter(s -> ! s.getFileName().startsWith(".DS_Store") )
					.collect(Collectors.toList());
			
			//output writer
			BufferedWriter writer = Files.newBufferedWriter(outputPath, Utilities.ENCODING);
			writer.write("<collection>");
			writer.newLine();
			
			Map <String, String> wordMap = new HashMap<String, String> ();
			
			int counter = 0;

			for ( Path path : fileList ) {//for each file in the directory (= 1 document in the xml file)
				String pathEnd = path.toString();
				String[] parts = pathEnd.split("/");
				String endingPart = parts[parts.length - 1];
				String regex = "[0-9]+";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(endingPart);
				String cou = "xxx";
				if(matcher.find())
					 cou = matcher.group(0);
				
				//input reader
				BufferedReader reader = Files.newBufferedReader(path, Utilities.ENCODING);

				String line = "";
				//this map will contain 3 keys: subject, predicate, object. As values, a long string with the falues found in the triples of the graph
				//initialize the map
				wordMap.put(Utilities.SUBJECT, "");
				wordMap.put(Utilities.PREDICATE, "");
				wordMap.put(Utilities.OBJECT, "");
				

				while ( (line = reader.readLine()) != null) {
					//for each line in this file, read the words and store them in the map
					addWords(wordMap, line);
				}
				
				//now, write a document in the xml file
				writeOneDocumentXML(wordMap, writer, cou);
				counter++;
				
				reader.close();
			}
			
			writer.write("</collection>");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
private static void writeOneDocumentXML(Map<String,String> map, BufferedWriter writer, String id) throws IOException {
		
		//si scrive il documento xml
		writer.write("<document>");
		writer.newLine();
		
		//id del documento
		writer.write("\t<docno>");
		writer.write(id);
		writer.write("</docno>");
		writer.newLine();
		
		//soggetto
		writer.write("\t<subject>" + map.get(Utilities.SUBJECT) + "</subject>");
		writer.newLine();
		
		//predicato
		writer.write("\t<predicate>" + map.get(Utilities.PREDICATE) + "</predicate>");
		writer.newLine();

		//soggetto
		writer.write("\t<object>" + map.get(Utilities.OBJECT) + "</object>");
		writer.newLine();

		
		writer.write("</document>");
		
		writer.newLine();
		
		writer.flush();
		
	}

	private static void addWords (Map<String, String> wordMap, String line) {

		//reguar expression
		String patternString = "<(.*?)>|\"(.*)\"(\\^\\^<(.*?)>)?|([0-9]+)$";

		//compile the pattern to match the strings and store them in the matcher
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(line);

		//counting if we are at the predicate, the subject or the object
		int counter = 0;


		while(matcher.find()) {
			//for each token found
			String work = matcher.group();

			if(work.equals(""))//just in case if the got epsilon (empty string)
				continue;
			else if(work.charAt(0) == '<') {//it's a url
				//take it without parenthesis 
				work = matcher.group(1);
				//work with the url
				work = FromRDFtoXMLFileConverter.elaborateUri(work);

				//get rid of possible not acceptable char in xml
				work = work.replaceAll("\\&", "&amp;");
				work = work.replaceAll("<", "&lt;");
				work = work.replaceAll(">", "&gt;");
				work = work.replaceAll("'", "&apos;");
				work = work.replaceAll("\"", "&quot;");


				if(counter==0) {
					//we are at the subject
					//add the string to the words we already have
					work = wordMap.get(Utilities.SUBJECT) + " " + work.trim();
					wordMap.put(Utilities.SUBJECT, work.trim());
					counter++;
				}
				else if(counter==1) {
					//we are at the predicate
					
					work = wordMap.get(Utilities.PREDICATE) + " " + work.trim();
					wordMap.put(Utilities.PREDICATE, work.trim());
					counter++;
				}
				else if(counter==2) {
					//we are at the object
					work = wordMap.get(Utilities.OBJECT) + " " + work.trim();
					wordMap.put(Utilities.OBJECT, work.trim());
					counter++;
				}
			}
			else if(work.charAt(0) == '"') {
				//literal, it should be an object
				work = matcher.group(2);
				work = work.replaceAll("\\&", "&amp;");
				work = work.replaceAll("<", "&lt;");
				work = work.replaceAll(">", "&gt;");
				
				//add to the words we already have
				work = wordMap.get(Utilities.OBJECT) + " " + work.trim();
				wordMap.put(Utilities.OBJECT, work.trim());
			}
			
		}
	}

	/**
	 * Returns a Map with the paths of the directories used in this class.
	 * */
	private static Map<String, String> getPaths() throws IOException {

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			//where to take .nt files
			String inputDirectory = prop.getProperty("generatedGraphsDirectoryPath");

			//where to save xml files
			String outputDirectory = prop.getProperty("retrievedGraphsInXmlPath");


			Map<String, String> pathMap = new HashMap<String, String>();
			pathMap.put(INPUT_DIRECTORY, inputDirectory);
			pathMap.put(OUTPUT_DIRECTORY, outputDirectory);


			return pathMap;

		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IOException("file not found");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new IOException("cannot close input file");
				}
			}
		}
	}
}

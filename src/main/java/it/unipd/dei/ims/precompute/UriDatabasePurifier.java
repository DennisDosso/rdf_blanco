package it.unipd.dei.ims.precompute;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import it.unipd.dei.ims.main.Utilities;

/**Classe che prende in ingresso un file in n-triple rdf
 * e lo purifica nel caso ci siano delle incorrettezze nella 
 * scrittura degli URI.
 * */
public class UriDatabasePurifier {

	//supponiamo che il charset del file sia UTF_8 e che dio ce la mandi buona
	final static Charset ENCODING = StandardCharsets.UTF_8;

	public static void main(String[] args) throws IOException {

		
		//inizializzo logger
		PropertyConfigurator.configure("log4j.properties");
		Logger log = Logger.getLogger(UriDatabasePurifier.class);

		log.debug(" ----------- Starting filtering ----------- ");

		//leggo l'input file dal file di properties
		String[] filePaths = readInputOutputFilePath(log);

		log.debug("input file: " + filePaths[0] + " ; output file path: " 
				+ filePaths[1]);


		//vai con lo swing (inizia a modificare il file)
		processFile(filePaths[0], filePaths[1], log);
		
		log.debug(" ----------- process has been completed successfuly ------------ ");
	}


	/**Reads the configuration file to retrieve the path of the input file
	 * 
	 * @return an array of two strings with the input and output file paths 
	 * 
	 * */
	private static String[] readInputOutputFilePath(Logger log) throws IOException {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			String[] pair = new String[2];

			// get the property value and print it out
			pair[0] = prop.getProperty("inputfilepath");
			pair[1] = prop.getProperty("outputfilepath");
			return pair;

		} catch (IOException ex) {
			ex.printStackTrace();
			log.error("IOException reading configuration file to retrieve file path");
			throw new IOException("file not found");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					log.error("IOExcpetion closing configuration file");
					throw new IOException("file not found");
				}
			}
		}
	}

	/**
	 * Metodo che esegue il lavoro sporco. Prende riga per riga il file
	 * da sistemare e manda la stringa in elaborazione perché ogni errore venga 
	 * corretto. Poi la riscrive in un file nuovo, aggiungendo un nuovo elemento,
	 * che funge da ID come commento a fine riga
	 * 
	 * @param inputPathString path del file in lettura (da epurare)
	 * @param outputPathString path del file in uscita (corretto)
	 * */
	private static void processFile(String inputPathString, String outputPathString, Logger log) {
		log.debug("Start...");
		
//		Lista con coppie dei valori da convertire negli URL 
		//salvati in un file di properties così da poterli cambiare facilmente
		List<Pair<String, String>> convertingList = Utilities.retrieveCharactersProperties();
		
		//contatore dell'id
		int lineId = 0;

		//creo oggetto path che rappresenta il path al file da leggere
		Path inputPath = Paths.get(inputPathString);
		Path outputPath = Paths.get(outputPathString);

		/*questo tipo di try si chiama try with resource statement, 
		 ed è applicabile ogniqualvolta vuoi definire un qualcosa
		 che deve essere chiuso alla fine del suo utilizzo.
		 La risorsa in questione deve implementare AutoClosable.
		 */
		try (BufferedReader reader = Files.newBufferedReader(inputPath, ENCODING)){

			String line = null;
			BufferedWriter writer = Files.newBufferedWriter(outputPath, ENCODING);

			while ((line = reader.readLine()) != null) {
				line = purifyLine(line, convertingList, log);
				writer.write(line + " #" + lineId++);
				writer.newLine();
			}      

			writer.close();

		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("Error reading input file");
		}
	}

	/**
	 * Metodo che controlla la stringa passata a parametro e sostituisce
	 * eventuali errori di inserimento secondo il protocollo URL.
	 * 
	 * Suppone che le stringe siano scritte secondo lo schema 
	 * 
	 * @param originalLine stringa da controllare
	 * @param list lista di coppie di stringhe (carattere vietato, carattere
	 * con cui sostituirlo)
	 * */
	private static String purifyLine(String originalLine, 
									List<Pair<String, String>> list, 
									Logger log) {
		
		String purifiedString = "";
		String work = "";
		
		//first of all, split the strings
		//see https://www.w3.org/TR/n-triples/ for clarifications
		//(not necessarily the definitive RE, in future may need to change)
		String patternString = "<(.*?)>|.$|\"(.*)\"(\\^\\^<(.*?)>)?";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(originalLine);
		
		while(matcher.find()) {
			//prendo il match
			work = matcher.group();
			
			//capisco con che tipo di stringa ho a che fare:
			//un iri (inizia con <) o una stringa (inizia con ")
			char firstChar = work.charAt(0);
			
			//controllo caratteri errati ed emendamento
			if(firstChar == '<')//è un IRI
			{
				//per ogni carattere vietato, si fa la sostituzione
				for(Pair<String, String> pair: list) {
					String left = pair.getLeft();
					String right = pair.getRight();
					work = work.replaceAll(left, right);
				}
				
//				work = work.replaceAll("\\s+", "%20");//sostituisco spazi
//				work = work.replaceAll("\"", "%22");//sostituisco virgolette
//				work = work.replaceAll("\\{", "%7B");//sostituisco parentesi graffe
//				work = work.replaceAll("\\}", "%7D");
//				work = work.replaceAll("`", "%60");
			}
			//altrimenti è un literal semplice, e mi sta bene "qualunque" cosa ci
			//sia dentro
			
			//XXX non si controlla qui se la stringa che segue 
			//il literal, quella di formattazione dopo ^^, sia scritta giusta
			//eventualmente aggiungere in futuro se serve

			//se siamo dentro la frase si aggiunge spazio, altrimenti si va a capo
			if(work.equals("."))
				purifiedString = purifiedString + work;
			else
				purifiedString = purifiedString + work + " ";
		}
		
		return purifiedString;

	}

}


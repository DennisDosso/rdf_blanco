package it.unipd.dei.ims.precompute;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import it.unipd.dei.ims.main.Utilities;

/**Legge in ingresso il file RDF e crea tanti file di testo bag of 
 * word pronti per essere indicizati*/
public class TripleToDocumentsConverter {

	private static Properties prop = new Properties();
	private static InputStream input = null;
	static Logger log = Logger.getLogger(TripleToDocumentsConverter.class);

	public static void main(String[] args) throws IOException {

		log.debug("\n ------ Starting conversion from rdf to simple text ----- \n");
		//si leggono path dalle properties
		Pair<String, String> pathPair = getPaths();

		System.out.println(pathPair.toString());

		//si esegue la conversione in semplici file di testo
		doTextConversion(pathPair);


	}


	/**Legge dalle properties e restituisce in una coppia i path
	 * del file in lettura e quello della cartella in scrittura.
	 * 
	 * @return Un oggetto Pair contenente come elemento sinistro il 
	 * path del file da leggere e come elemento destro
	 * il path della cartella su cui andare a scrivere.
	 * */
	private static Pair<String, String> getPaths() throws IOException {

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			//path da cui prendere il file rdf enorme di 
//			String originalFile = prop.getProperty("singlerdffilepath");
			//TODO mentre sono in prova
			String originalFile = prop.getProperty("inputfilepathtest");
			
			//dove salvare i file di testo
			String targetDirectory = prop.getProperty("outputsimpletextcollectionpath");

			Pair<String, String> pair = Pair.of(originalFile, targetDirectory);

			log.debug("found the following paths: " + pair.toString());

			return pair;

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

	/**Esegue la conversione dal file di testo pieno di triple RDF
	 * in tanti file di semplice test "bag of words" ottenute 
	 * dalle triple.
	 * @param pair una coppia di stringhe con la prima (left) contenente path di lettura
	 * e la seconda (right) path di scrittura.*/
	private static void doTextConversion(Pair<String, String> pair) {

		Path inputPath = Paths.get(pair.getLeft());
		Path outputPath = Paths.get(pair.getRight());

		//open and read the file with a BufferedReader
		try (BufferedReader reader = Files.newBufferedReader(inputPath, Utilities.ENCODING)){

			String line = null;
//			BufferedWriter writer = Files.newBufferedWriter(outputPath, Utilities.ENCODING);

			while ((line = reader.readLine()) != null) {
				//si scrivono nuovi file
				
				//dalla line si ricava l'id della tripla e il documento ad essa associato
				Pair<String, String> pairIdWords = createBagOfWords(line);
				//dato l'id, identifichiamo il nome e il path del nuovo documento txt che vogliamo creare
				String currentPath = pair.getRight() + "/" + pairIdWords.getLeft() + ".txt";
			}      

//			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Problemi nella lettura del file RDF da convertire");
		}
	}


	/**prende la stringa a parametro, passata in formato RDF n-triple, 
	 * e la converte in una semplice stringa di testo separata da spazi.
	 * 
	 *  @param line la stringa da trasformare in documento*/
	public static Pair<String, String> createBagOfWords(String line) {

		//divido le stringhe. Due casi mi interessa: uri tra <...> o 
		//literal tra "..."
		//usiamo le fighissime espressioni regolari che spaccano
		
		//espressione regolare
		String patternString = "<(.*?)>|\"(.*)\"(\\^\\^<(.*?)>)?|([0-9]+)$";
		/*Questa regex prende gli url tra le brackets, le stringhe e anche l'id*/

		MutablePair<String, String> pair = MutablePair.of("", "");
		String bagOfWord = "";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(line);
		
		
		while(matcher.find()) {
			String work = matcher.group();
			if(work.equals(""))//tanto per sicurezza nel caso l'espressione porti a epsilon
				continue;
			else if(work.charAt(0) == '<') {
				//è un url
				//lo prendo senza le parentesi
				work = matcher.group(1);
				//lavoro sull'URI
				work = elaborateUri(work);
				bagOfWord = bagOfWord + " " + work;
			}
			else if(work.charAt(0) == '"') {
				//è un literal
				work = matcher.group(2);
				//si può aggiungere subito quando trovato alla bag of words
				bagOfWord = bagOfWord + " " + work;
			}
			else { 
				//è un id
				work = matcher.group();
				//lo setto come nome del documento
				pair.setLeft(work);
			}
		}
		
		//completata l'elaborazione della riga, possiamo settare il campo
		pair.setRight(bagOfWord);
		
		return pair;
	}
	
	/**Prende l'URI e, trattandolo come un url, ritorna la stringa che è l'ultimo
	 * elemento del path.*/
	private static String elaborateUri(String elaborandum) {
		
		try {
			URL urlString = new URL(elaborandum);
			String elaboratum = urlString.getPath();
			
			String[] splitStrings = elaboratum.split("/");//prima prendiamo l'ultima parte del path
			
			//questa potrebbe essere composta da più parole separate da '_', che si dividono
			String[] secondSplit = splitStrings[splitStrings.length-1].split("_");
			
			//si mettono i caratteri in un'unica stringa, separati da spazi
			String returnandum = "";
			for(String s : secondSplit) {
				returnandum = returnandum + " " + s;
			}
			
			return returnandum;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.error("URL scritto male");
			return "";
		}
	}
}



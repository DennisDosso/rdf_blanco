package it.unipd.dei.ims.precompute;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.log4j.Logger;

import it.unipd.dei.ims.main.Utilities;

/**Legge in ingresso un file RDF con varie triple
 * e ne crea un file XML con i tag necessari*/
public class FromRDFtoXMLFileConverter {

	private static Properties prop = new Properties();
	private static InputStream input = null;
	static Logger log = Logger.getLogger(FromRDFtoXMLFileConverter.class);

	public static void main(String[] args) throws IOException {

		log.debug("------ Starting conversion from rdf to xml ----- \n");
		Stopwatch timer = Stopwatch.createStarted();
		
		//si leggono path dalle properties
		Pair<String, String> pathPair = getPaths();

		//si esegue la conversione in semplici file di testo
		doTextConversion(pathPair);
		
		
		System.out.println("done");
		System.out.println(" ----- convertion terminated  in " + timer.stop() 
				+ " ------- \n");
//		log.debug(" ----- convertion terminated  in " + timer.stop() 
//				+ " nanoseconds ------- \n");


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

			//path da cui prendere il file rdf enorme di RDF
			//singlerdffilepath
			String originalFile = prop.getProperty("singlerdffilepath");
			
			//dove salvare il file xml
			String targetDirectory = prop.getProperty("outputXmlCollectionPath");

			
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
		
		//tiene conto di quanti documenti ho già scritto
		int counter = 0;

		//open and read the file with a BufferedReader
		try (BufferedReader reader = Files.newBufferedReader(inputPath, Utilities.ENCODING)){

			String line = null;
			
			//tiene conto di quanti file xml ho già scritto
			int powCounter = 0;
			
			//path del file xml 
			String filePath = pair.getRight() + "/" + "xml_dataset" + powCounter + ".xml";
			Path outputFile = Paths.get(filePath);
			
			//writer in uscita
			BufferedWriter writer = Files.newBufferedWriter(outputFile, Utilities.ENCODING);

			while ((line = reader.readLine()) != null) {//per ogni tripla RDF/documento
				//si scrivono nuovi file
				
				if(counter%32768 == 0) {

					//ho appena finito un file e ne devo creare un apro
					//scrivo l'ultima riga alla fine del file che sto chiudendo
					writer.write("</collection>");
					//nuovo file ( per aiutare il file system)
					//finisco di scrivere quello che sto scrivendo altrimenti il file rischia di diventare incompleto
					writer.flush();
					writer.close();
					
					//aggiorniamo il path
					filePath = pair.getRight() + "/" + "xml_dataset" + powCounter + ".xml";
					outputFile = Paths.get(filePath);

					//creo un nuovo file
					writer = Files.newBufferedWriter(outputFile, Utilities.ENCODING);
					writer.write("<collection>");
					writer.newLine();
					
					System.out.println("written " + (powCounter+1) + " xml files with 32768 documents each");
					powCounter++;
				}
				
				//dalla line si ricava l'id della tripla e le parole associate a subj, obj e predicato
				Map<String, String> mapOfWords = createBagOfWords(line);
				
				writeOneDocumentXML(mapOfWords, writer);
				
				counter++;
				
			}   
			
			//srivo l'ultima riga sull'ultimo file
			writer.write("</collection>");
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Problemi nella lettura del file RDF da convertire");
		}
	}


	/**Prende la stringa a parametro, passata in formato RDF n-triple, 
	 * e la converte in una semplice stringa di testo separata da spazi.
	 * 
	 *  @param line la stringa da trasformare in documento*/
	public static Map<String, String> createBagOfWords(String line) {

		Map<String, String> map = new HashMap<String, String>();
		
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
		
		int counter = 0;//contatore che tiene conto se siamo al soggetto, a predicato o all'oggetto
		
		
		while(matcher.find()) {
			String work = matcher.group();
			
			
			if(work.equals(""))//tanto per sicurezza nel caso l'espressione porti a epsilon
				continue;
			else if(work.charAt(0) == '<') {//è un url
				//è un url
				//lo prendo senza le parentesi
				work = matcher.group(1);
				//lavoro sull'URI
				work = elaborateUri(work);
				
				//si rimpiazzano possibili caratteri non accettabili dentro un tag xml
				work = work.replaceAll("\\&", "&amp;");
				work = work.replaceAll("<", "&lt;");
				work = work.replaceAll(">", "&gt;");
				work = work.replaceAll("'", "&apos;");
				work = work.replaceAll("\"", "&quot;");
				
				
				if(counter==0) {
					//è soggetto
					map.put(Utilities.SUBJECT, work.trim());
					counter++;
				}
				else if(counter==1) {
					//è predicato
					map.put(Utilities.PREDICATE, work.trim());
					counter++;
				}
				else if(counter==2) {
					//è oggetto
					map.put(Utilities.OBJECT, work.trim());
					counter++;
				}
			}
			else if(work.charAt(0) == '"') {
				//è un literal e dovrebbe essere solo oggetto
				work = matcher.group(2);
				work = work.replaceAll("\\&", "&amp;");
				work = work.replaceAll("<", "&lt;");
				work = work.replaceAll(">", "&gt;");
				
				//si può aggiungere subito quando trovato alla bag of words
				bagOfWord = bagOfWord + " " + work;
				
				map.put(Utilities.OBJECT, work.trim());
			}
			else { 
				//è un id
				work = matcher.group();
				//lo setto come nome del documento
				pair.setLeft(work);
				
				map.put(Utilities.ID, work);
			}
		}
		
		//completata l'elaborazione della riga, possiamo settare il campo
		pair.setRight(bagOfWord);
		
		return map;
	}
	
	/**Prende l'URI e, trattandolo come un url, ritorna la stringa che è l'ultimo
	 * elemento del path.*/
	private static String elaborateUri(String elaborandum) {
		
		try {
			URL urlString = new URL(elaborandum);
			String elaboratum = urlString.getPath();
			
			//potrebbe anche esserci una reference alla fine dell'URL. 
			//è quella che ci interessa
			String ref = urlString.getRef();
			if(ref != null)
				elaboratum = ref;
			
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
	
	/**Scrive nel file xml un documento
	 * @param map Una mappa contenente come valori i campi da scrivere sull'xml e come chiavi id, subject, object e predicate
	 * @param writer Un BufferedWriter già aperto sul file da scrivere
	 * 
	 * @throws IOException 
	*/
	private static void writeOneDocumentXML(Map<String,String> map, BufferedWriter writer) throws IOException {
		
		//si scrive il documento xml
		writer.write("<document>");
		writer.newLine();
		
		//id del documento
		writer.write("\t<docno>");
		writer.write(map.get(Utilities.ID));
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
		
	}
}



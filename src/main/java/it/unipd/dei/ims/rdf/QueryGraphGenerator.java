package it.unipd.dei.ims.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.log4j.Logger;

import it.unipd.dei.ims.main.Utilities;

/**Una volta ottenuta in qualche modo la run corrispondente
 * alla query sottomessa dall'utente, si ha la lista delle triple 
 * in cui le parole della query sono contenute.
 * 
 * Da esse, possiamo creare il query graph 'E' su cui poi si potrà operare.
 * 
 * La classe genera il grafo e lo scrive come file di testo in formato nt.
 * */
public class QueryGraphGenerator {

	private static Properties prop = new Properties();
	private static InputStream input = null;
	static Logger log = Logger.getLogger(QueryGraphGenerator.class);

	public static void main (String[] args) throws IOException {

		//leggi in ingresso il file
		List<Integer> list = getListOfId();

		//si vanno a produrre le liste RDF come string
		List<String[]> stringList = produceRdfStrings(list);
		
		//si vanno a creare gli statement RDF con JENA
		List<Statement> statementList = createStatementList(stringList);
		
		Model model = ModelFactory.createDefaultModel();
		model.add(statementList);
		
		//prendiamo il path di output
		String outputPath = getOutputPath();
		
		File f = new File(outputPath);
		OutputStream out = new FileOutputStream(f);
		model.write(out, Utilities.NT);
		
		System.out.println("Model done");

	}
	
	/** Data una lista di array di stringhe [soggetto, predicato, oggetto] va a generare
	 * una lista di Statement Jena RDF.
	 * 
	 * */
	private static List<Statement> createStatementList(List<String[]> stringList) {
		
		List<Statement> statementList = new ArrayList<Statement>();
		
		Iterator<String[]> iter = stringList.iterator();
		
		while(iter.hasNext()) {
			String[] stringArray = iter.next();
			
			//creo oggetto
			Resource subject = ResourceFactory.createResource(stringArray[0]);
			Property property = ResourceFactory.createProperty(stringArray[1]);
			
			//devo capire se l'object è una risorsa o un literal
			String obj = stringArray[2];
			RDFNode object;
			
			//TODO da gestire i literal con i format ^^ alla fine
			
			if(obj.charAt(0) == '"') {
				//è un literal
				obj = obj.replaceAll("\"", "");
				object = ResourceFactory.createStringLiteral(obj);
			}
			else {
				//è un url
				object = ResourceFactory.createResource(obj);
			}
			
			Statement statement = ResourceFactory.createStatement(subject, property, object);
			statementList.add(statement);
		}
		
		return statementList;
	}

	/** Genera una lista con stringhe RDF prese dal file "corretto".
	 * 
	 * @param list una lista contenente gli id delle triple rdf che si desiderano
	 * estrapolare dal file. LE triple sono identificate dalle loro posizioni di riga,
	 * quindi gli id nella lista sono le righe delle triple che si desiderano prendere
	 * nel file .nt di interesse.
	 * */
	private static List<String[]> produceRdfStrings(List<Integer> list) {

		//lista che ritornerò
		List<String[]> stringList = new ArrayList<String[]>();

		//apro il file dump purificato per poter leggere riga per riga
		try {
			//path del file RDF in lettura
			String path = getRdfFilePath();

			//reader sul path
			Path inputFile = Paths.get(path);
			BufferedReader reader = Files.newBufferedReader(inputFile, Utilities.ENCODING);
			String line;
			
			//la regex che mi serve per isolare i token con cui isolerò le risorse RDF
			//qui prendiamo gli url tra <...> e le stringhe
			String patternString = "<(.*?)>|\"(.*)\"(\\^\\^<(.*?)>)?";
			Pattern pattern = Pattern.compile(patternString);

			//tiene traccia dell'indice di linea su cui ci troviamo in ogni momento
			int lineCounter = 0;
			//iteratore sulla lista con gli indici che mi interessano
			Iterator<Integer> iter = list.iterator();

			//prendiamo il primo indice
			Integer seekingLine = iter.next();

			while ((line = reader.readLine()) != null) {
				//per ogni riga del grande file rdf
				if(lineCounter == seekingLine) {
					//ci troviamo su una linea che ci interessa
					
					String[] buildingLine = new String[3];
					//conta 0, 1 e 2 per tenere conto di soggetto, predicato ed oggetto
					int rdfCounter = 0;
					
					//individuiamo i nostri token
					Matcher matcher = pattern.matcher(line);
					while(matcher.find()) {
						String work = matcher.group();
						
						//guardiamo al primo carattere
						char firstChar = work.charAt(0);
						if(firstChar == '<' ) {
							//è un url - si prende senza <...>
							buildingLine[rdfCounter] = matcher.group(1);
							rdfCounter++;
						}
						else {
							//è un literal (viene salvato con già le virgolette)
							buildingLine[rdfCounter] = work;
							rdfCounter++;
						}
					}

					//aggiungiamo la tripla alla lista da ritornare
					stringList.add(buildingLine);

					//aggiorniamo l'indice della linea che vogliamo leggere
					if(iter.hasNext())
						//se ci sono ancora triple da aggiunge si procede
						seekingLine = iter.next();
					else
						break;
				}//fine if in cui abbiamo trovato una linea che ci interessa

				lineCounter++;
			}

			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stringList;
	}


	/** Restituisce una lista (ordinata) con gli id delle tripe RDF che sono state indicate 
	 * nel file .res di terrier come di interesse per la query dell'utente.
	 * 
	 * */
	private static List<Integer> getListOfId() {
		List<Integer> list = new ArrayList<Integer>();

		//apri il file
		try {
			//piglia il path
			String path = getPath();

			Path inputFile = Paths.get(path);
			BufferedReader reader = Files.newBufferedReader(inputFile, Utilities.ENCODING);

			//leggi riga per riga
			String line;
			while ((line = reader.readLine()) != null) {
				String[] pieces = line.split("\\s+");

				//per il formato che si sta utilizzando, l'id si trova all'indice 2
				String id = pieces[2];
				Integer idInt = Integer.parseInt(id);

				list.add(idInt);
			}

			//pulizie 
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Errori nella lettura della lista di id");
		}

		Collections.sort(list);
		return list;

	}

	private static String getPath() throws IOException {

		prop = new Properties();
		input = null;

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			String path = "";

			path = prop.getProperty("inputRunPath");
			input.close();
			return path;

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
	
	/**Restituisce il path di output */
	private static String getOutputPath() throws IOException {

		prop = new Properties();
		input = null;

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			String path = "";

			path = prop.getProperty("modelOutputPath");
			return path;

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

	/**Restituisce il path del file .nt con le triple rdf da estrapolare
	 * @throws IOException 
	 * 
	 * */
	private static String getRdfFilePath() throws IOException{

		prop = new Properties();
		input = null;

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			String path = "";

			path = prop.getProperty("rdfPurifiedFilePath");
			return path;

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
}

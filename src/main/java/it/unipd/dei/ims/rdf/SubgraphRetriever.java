package it.unipd.dei.ims.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

import it.unipd.dei.ims.main.Utilities;

/**Questa classe opera nel query graph E generato in precedenza ed estrapola tutti i sottografi necessari.*/
public class SubgraphRetriever {

	static Logger log = Logger.getLogger(SubgraphRetriever.class);

	//chiavi che si usano nella classe
	private static String INPUT = "input";
	private static String OUTPUT_DIRECTORY = "output_directory";
	private static String inputPath;
	private static String outputDirectoryPath;

	public static void main (String[] args) {

		//piglio i path di cui necessito
		try {
			Map<String, String> stringMap = getPaths();

			inputPath = stringMap.get(INPUT);
			outputDirectoryPath = stringMap.get(OUTPUT_DIRECTORY);

			retrieveSubgraphs(inputPath, outputDirectoryPath);
			System.out.println("done");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.debug("Errori nella lettura dei path in ingresso");
		}
	}


	/** 
	 * Implementazione dell'algoritmo di Blanco. Consiste nel generare sottografi dal 
	 * query graph E ottenuto nelle elaborazioni precedenti. Questi sottografi vengono generati
	 * in maniera da garantire le proprietà richieste dal paper di Blanco di essere massimali,
	 * unici, e tali per cui ogni tripla in un grafo abbia la lista di parole del documento associata
	 * che non sia sottoinsieme della lista di parole delle restanti triple
	 * 
	 * @param inputPath path del file con il query graph E
	 * @param outputDirectoryPath path della cartella in cui verranno salvati i grafi individuati
	 * */
	private static void retrieveSubgraphs(String inputPath, String outputDirectoryPath) {

		//si crea un modello vuoto di input
		Model model = ModelFactory.createDefaultModel();

		//devo tenere traccia in qualche modo dei model già creati in maniera da non avere duplicati
		List<Model> modelList = new ArrayList<Model>();

		InputStream in = FileManager.get().open( inputPath );

		if (in == null) {
			throw new IllegalArgumentException( "File: " + inputPath + " not found");
		}

		//carico modello in memoria
		model.read(in, null, Utilities.NT);


		//serve ora operare su ogni lato del grafo
		//iteratore su ogni lato del grafo
		StmtIterator iter = model.listStatements();
		int graphCounter = 0;

		while (iter.hasNext()) {//per ogni tripla rdf
			Statement t = iter.nextStatement(); 
			//creiamo il nuovo modello 
			Model extendingModel = ModelFactory.createDefaultModel();

			//creiamo un sottografo da questo statement
			extendSubgraph(extendingModel, t);

			if( ! checkForDuplicates(extendingModel, modelList) ) {
				// se non ci sono duplicati
				// aggiungo il modello alla lista
				modelList.add(extendingModel);

				//lo stampo
				File f = new File(outputDirectoryPath + graphCounter + ".nt");
				OutputStream out;
				try {
					out = new FileOutputStream(f);
					extendingModel.write(out, Utilities.NT);
					graphCounter++;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}


		}
	}

	/**
	 * Controlla se un Model/RDF graph è già contenuto nella lista di grafi passata a parametro
	 * 
	 * @param modelList lista di grafi su cui si controlla se il modello a parametro è già presente
	 * @param extendingModel modello per cui si vuole controllare se esiste già nella lista
	 * 
	 * @return true se nella lista è presente un duplicato 
	 * */
	private static boolean checkForDuplicates( Model extendingModel, List<Model> modelList ) {

		for(Model model : modelList) { //per ogni modello a disposizione
			if( model.containsAll(extendingModel) ) {
				//se troviamo un modello duplicato 
				return true;
			}
		}
		return false;
	}



	/**Crea un sottografo secondo l'algoritmo del paper Keyword Search over RDF Graphs
	 * di Blanco partendo da un singolo lato/statement/tripla RDF. */
	private static void extendSubgraph(Model extendingModel, Statement t) {

		extendingModel.add(t);

		//struttura di supporto per sapere quante parole il documento corrispondente al grafo contiene
		List<String> graphList = new ArrayList<String>();
		addWordsFromGraphToList(extendingModel, graphList);

		//–––––––––––– fase preliminare: si riempie la nuvola iniziale con i vicini del primo statement –––––––––––––––

		Queue<Statement> neighboursQueue = new LinkedList<Statement>();
		addNeighboursToQueue(neighboursQueue, t);

		//–––––––––– fase greedy: si controllano i vicini ––––––––––––––– 
		//per ognuno dei vicini, serve verificare se va bene aggiungerlo
		while ( ! neighboursQueue.isEmpty() ) {//fino a che non abbiamo finito i vicini

			//si prende la prossima tripla
			Statement nextStatement = neighboursQueue.remove();

			//si estrapolano le parole che formano il suo documento
			//si estrapolano le stringe contenute nello statement e le si mette in una lista
			List<String> statementList = new ArrayList<String>();
			addWordsFromStatementToList(nextStatement, statementList);

			if( checkForInsertion(graphList, statementList) ) {//controllo se si può inserire, ossia se nel grafo sono già presenti le mie parole
				//aggiungo al grafo lo statement
				extendingModel.add(nextStatement);
				//aggiungo i vicini dello statement alla queue
				addNeighboursToQueue(neighboursQueue, nextStatement);
				//aggiungo le nuove parole alla lista di appoggio				
				addNewWordsToList(graphList, statementList);
			}

		}
	}

	/**Aggiunge alla lista raffigurante il documento grafo le parole della lista dello statement 
	 * e solo quelle non già contenute*/
	private static void addNewWordsToList(List<String> graphList, List<String> statementList) {

		for(String s : statementList) {
			if(! graphList.contains(s)) {
				graphList.add(s);
			}
		}
	}

	private static void addNeighboursToQueue(Queue<Statement> queue, Statement t) {

		Resource subject = t.getSubject();
		RDFNode object = t.getObject();

		//aggiungo i vicini del soggetto
		StmtIterator iter = subject.listProperties();
		while (iter.hasNext()) {
			queue.add(iter.next());
		}

		//aggiungo i vicini dell'oggetto
		// (solo se l'oggetto è di tipo risorsa. Se è un literal non avrà altri vicini)
		if(object instanceof Resource) {
			iter = ((Resource) object).listProperties();
			while (iter.hasNext()) {
				queue.add(iter.next());
			}
		}
	}

	/**Metodo accessorio che aggiunge alla lista L le parole corrispondenti alle triple RDF contenute nel grafo G
	 * */
	private static void addWordsFromGraphToList(Model G, List<String> L) {
		//iteratore sugli statement/triple del grafo
		StmtIterator iter = G.listStatements();

		while(iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			addWordsFromStatementToList(stmt, L);
		}
	}

	/** Aggiunge parole da uno statement RDF, di qualunque natura esso sia,
	 * ad una lista L di String
	 * */
	private static void addWordsFromStatementToList(Statement stmt, List<String> L) {

		//prendo i tre elementi della tripla
		Resource subject = stmt.getSubject();
		Property predicate = stmt.getPredicate(); 
		RDFNode object = stmt.getObject();

		addWordsFromResourceToList(subject, L, false);
		boolean lookAhead = addWordsFromResourceToList(predicate, L, false);
		addWordsFromResourceToList(object, L, lookAhead);

	}


	/**Legge da file di properties le stringhe RDF per cui serve
	 * tenere una gestione particolare*/
	private static List<String> getLookAheadList() {

		Properties prop = new Properties();
		InputStream input = null;
		List<String> list = new ArrayList<String>();

		try {
			input = new FileInputStream("properties/rdf_tag.properties");
			prop.load(input);
			
			//si leggono tutti gli elementi nel file di properties
			Enumeration<?> enumeration = prop.propertyNames();
			while(enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String value = prop.getProperty(key);

				list.add(value);
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e ) {
			e.printStackTrace();
		}
		
		return list;

	}

	/**Aggiunge parole da una risorsa RDF r ad una list L di String
	 * 
	 * @param lookAhead flag booleano che indica al metodo se è necessario inserire tra le parole della lista anche
	 * l'url al completo. Questo può rivelarsi necessario nel caso in cui vi siano url molto simili che cambiano solo all'interno del 
	 * path e non nel valore finale.
	 * */
	private static boolean addWordsFromResourceToList(RDFNode r, List<String> L, boolean lookAhead) {

		String insertingString = r.toString();
		//si estrapola la stringa dall'uri o dal literal
		insertingString = elaborateRDFString(insertingString);
		boolean lookahead = false;
		
		//si gestiscono i caratteri particolari
		List<String> lookAheadList = Utilities.getPropertiesValues("properties/rdf_tag.properties");

		//per ognuno dei caratteri particolari
		for(String s : lookAheadList) {
			if(insertingString.equals(s)) {
				//se il nostro termine è uno di quelli da starci attenti, tipo sameAs
				lookahead = true;
				//con questo flag, avviso che è necessario tenere conto dell'url object che verrà dopo
			}
		}

		
		if(lookAhead) {
			//se siamo stati avvisati, aggiungiamo l'intera stringa (un url) alle stringhe che stiamo gestendo
			L.add(r.toString());
		}

		//la stringa potrebbe avere degli spazi
		String[] parts = insertingString.split("\\s+");

		for(String s : parts) {//per ogni stringa che vorrei inserire

			if( ! L.contains(s))//sto facendo un insieme: non voglio duplicati
				L.add(s);
		}
		
		return lookahead;

	}


	/**Prende una stringa RDF (cioè un URI o un literal) e lo scompone nella maniera necessaria 
	 * affinché si ottenga la stringa-documento corrispondente
	 * 
	 * @param line La stringa da elaborare. Se è un uri, si vuole la parte finale del path o la ref.
	 * Se è già un literal ci basta averlo tutto*/
	private static String elaborateRDFString(String line) {

		//controllo se la stringa è un url
		final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

		Pattern p = Pattern.compile(URL_REGEX);
		Matcher m = p.matcher(line);//replace with string to compare
		if(m.find()) {
			//è un uri
			//si ritorna la parte finale del path
			return Utilities.elaborateUri(line).trim();
		}
		else {
			//è un literal
			return line.replaceAll("\"", "").trim();
		}
	}

	/** Controlla la condizione individuata da Blanco  riguardo l'inserimento di 
	 * una nuova tripla rdf. Essa viene aggiunta se e solo se porta significato semantico, ossia
	 * se la lista ad essa associata porta parole nuove al grafo G.
	 * 
	 * @param listOfWords lista con le parole del documento attualmente rappresentante il grafo G
	 * @param t Statement che si vuol provare ad aggiungere*/
	private static boolean checkForInsertion( List<String> listOfWords, Statement t) {

		//si estrapolano le stringe contenute nello statement e le si mette in una lista
		List<String> statementList = new ArrayList<String>();
		addWordsFromStatementToList(t, statementList);


		//ritorno true se le parole non sono contenute
		return ( ! listOfWords.containsAll(statementList) );
	}

	/** Controlla che una lista non sia contenuta in un'altra
	 * 
	 * @param graphList la lista contenente le parole del grafo
	 * @param statement list la lista contenente le parole dello statement che si vorrebbe aggiungere*/
	private static boolean checkForInsertion( List<String> graphList, List<String> statementList) {

		//ritorno true se le parole non sono contenute
		return ( ! graphList.containsAll(statementList) );
	}

	private static Map<String, String> getPaths() throws IOException {
		Properties prop = new Properties();
		InputStream input = null;
		Map<String, String> map = new HashMap<String, String>();

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			//si ottiene path dell'input
			String path = prop.getProperty("modelInputPath");
			map.put(INPUT, path);

			path = prop.getProperty("generatedGraphsDirectoryPath");
			map.put(OUTPUT_DIRECTORY, path);

			return map;

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



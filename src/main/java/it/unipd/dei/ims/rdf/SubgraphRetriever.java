package it.unipd.dei.ims.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

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

		//si crea un modello vuoto
		Model model = ModelFactory.createDefaultModel();

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

		while (iter.hasNext()) {
			Statement t = iter.nextStatement(); // get next statement
			//creiamo il nuovo modello 
			Model extendingModel = ModelFactory.createDefaultModel();

			//creiamo un sottografo da questo statement
			extendSubgraph(extendingModel, t);
			
			//TODO si scrive il model su di un file
			File f = new File(outputDirectoryPath + "graph_" + graphCounter + ".nt");
			OutputStream out;
			try {
				out = new FileOutputStream(f);
				extendingModel.write(out, Utilities.NT);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	/**Crea un sottografo secondo l'algoritmo del paper Keyword Search over RDF Graphs
	 * di Blanco partendo da un singolo lato/statement/tripla RDF. */
	private static void extendSubgraph(Model extendingModel, Statement t) {

		extendingModel.add(t);

		//struttura di supporto per sapere quante parole il documento corrispondente al grafo contiene
		List<String> listOfWords = new ArrayList<String>();
		addWordsFromGraphToList(extendingModel, listOfWords);

		//–––––––––––– fase preliminare: si riempie la nuvola iniziale –––––––––––––––

		//si prendono le triple vicine a t

		//e si prendono i loro vicini
		Queue<Statement> neighboursQueue = new LinkedList<Statement>();
		addNeighboursToQueue(neighboursQueue, t);

		//–––––––––– fase greedy: si controllano i vicini ––––––––––––––– 
		//per ognuno dei vicini, serve verificare se va bene aggiungerlo
		while ( neighboursQueue.isEmpty() ) {//fino a che non abbiamo finito i vicini

			//si prende la prossima tripla
			Statement nextStatement = neighboursQueue.remove();
			if( checkForInsertion(listOfWords, nextStatement) ) {
				//aggiungo al grafo lo statement
				extendingModel.add(nextStatement);
				//aggiungo i vicini dello statement alla queue
				addNeighboursToQueue(neighboursQueue, nextStatement);
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
	 * ad una lista L di Strng
	 * */
	private static void addWordsFromStatementToList(Statement stmt, List<String> L) {

		//prendo i tre elementi della tripla
		Resource subject = stmt.getSubject();
		Property predicate = stmt.getPredicate(); 
		RDFNode object = stmt.getObject();

		addWordsFromResourceToList(subject, L);
		addWordsFromResourceToList(predicate, L);
		addWordsFromResourceToList(object, L);

	}

	/**Aggiunge parole da una risorsa RDF r ad una list L di String
	 * */
	private static void addWordsFromResourceToList(RDFNode r, List<String> L) {

		String insertingString = r.toString();
		//si estrapola la stringa dall'uri o dal litera
		insertingString = elaborateRDFString(insertingString);
		//la stringa potrebbe avere degli spazi
		String[] parts = insertingString.split("\\s+");
		for(String s : parts) {//er ogni stringa che vorrei inserire
			if( ! L.contains(s))//sto facendo un insieme: non voglio duplicati
				L.add(s);
		}

	}


	/**Prende una stringa RDF (cioè un URI o un literal) e lo scompone nella maniera necessaria 
	 * affinché si ottenga la stringa-documento corrispondente
	 * 
	 * @param line La stringa da elaborare. Se è un uri, si vuole la parte finale del path o la ref.
	 * Se è già un literal ci basta averlo tutto*/
	private static String elaborateRDFString(String line) {

		if(line.charAt(0) == '"') {//è un literal
			return line.replaceAll("\"", "");
		}
		else {
			//è un uri
			//si ritorna la parte finale del path
			return Utilities.elaborateUri(line);
		}

	}

	/** Controlla la condizione individuata da Blanco et. Al. riguardo l'inserimento di 
	 * una nuova tripla rdf. Essa viene aggiunta se e solo se porta significato semantico, ossia
	 * se la lista ad essa associata porta parole nuove al grafo G.
	 * 
	 * @param listOfWords lista con le parole del documento attualmente rappresentante il grafo G
	 * @param t Statement che si vuol provare ad aggiungere*/
	private static boolean checkForInsertion( List<String> listOfWords, Statement t) {

		//estrapolo la stringa
		String rdfStatement = t.toString();
		rdfStatement = elaborateRDFString(rdfStatement);

		//ritorno true se la parola non è contenuta
		return ( ! listOfWords.contains(rdfStatement) );
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



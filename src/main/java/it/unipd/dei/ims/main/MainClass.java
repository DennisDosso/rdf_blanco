package it.unipd.dei.ims.main;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.util.FileManager;

/** Per ora, classe di partenza del mio progetto.
 * */
public class MainClass {

	//nome del file in cui è presente il grafo rdf da leggere
	static final String inputFileName = "datasets/rdf_datasets/IMdb-dump-purified.nt";
//	static final String inputFileName = "dump-di-prova.nt";

	public static void main(String[] args) {

		//creazione del grafo vuoto
		Model model = ModelFactory.createDefaultModel();

		InputStream in = FileManager.get().open(inputFileName);
		if (in == null) {
			throw new IllegalArgumentException( "File: " + inputFileName + " not found");
		}
		
		//lettura del file
		model.read(new InputStreamReader(in), null, Utilities.NT);
		System.out.println("done");
//		System.out.println(model);
	}
}

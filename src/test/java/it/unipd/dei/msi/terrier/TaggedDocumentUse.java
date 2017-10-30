package it.unipd.dei.msi.terrier;


import java.io.IOException;
import java.util.HashMap;

import org.terrier.indexing.Document;
import org.terrier.indexing.TaggedDocument;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

public class TaggedDocumentUse {
	
	private static final String PATH = "/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/retrieved_graphs_in_xml/retrieved_graphs_xml.xml";
	/**Qui testo un po' terrier sui documenti xml*/
	public static void main (String[] args) {
		
		try {
			ApplicationSetup.setProperty("trec.collection.class", "SimpleXMLCollection");
			ApplicationSetup.setProperty("xml.doctag", "document");
			ApplicationSetup.setProperty("xml.idtags", "docno");
			ApplicationSetup.setProperty("xml.terms", "object,predicate,subject");
			
			//this way I'm going to read the whole xml file as one docuent
			Document document = new TaggedDocument(Files.openFileReader(PATH), new HashMap(), Tokeniser.getTokeniser());
			
			
			//print the document terms one by one
			while(! document.endOfDocument()) {
				String term = document.getNextTerm();
				if(term != null)
					System.out.println(term);
			}
			
			//sembra che le parole con più di tot termini ripetuti o più di tot cifre siano scartati
			TaggedDocument.check("9999");
			TaggedDocument.dumpDocument(document);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

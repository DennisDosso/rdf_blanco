package it.unipd.dei.ims.main;

import org.terrier.matching.models.WeightingModel;

/**Implementation of the Ranking Model described by Blanco and Elbassuoni
 * in their paper 'Keyword Search over RDF Graphs' - CIKM '11
 * 
 * 
 * @author Dennis Dosso*/
public class BlancoElbassuoniLM extends WeightingModel {

	public BlancoElbassuoniLM() {
		super();
		c = 2500;
	}
	
	@Override
	public double score(double tf, double docLength) {
		return 0;
	}

	
	
	
	@Override
	public String getInfo() {
		return "BlancoElbassuoniLM";
	}
}

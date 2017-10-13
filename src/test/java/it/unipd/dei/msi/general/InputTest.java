package it.unipd.dei.msi.general;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/*Test per vedere se java riesce quantomeno ad aprire un file in Eclipse
 * 
 * La scoperta è stata che per andare sul sicuro serve mettere il path assoluto.*/
public class InputTest {
	
	static final String inputFileName = "vc-db-1.rdf";
	
	public static void main (String args[]) {
		
		//cerco di leggere un file
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/Users/dennisdosso/eclipse-workspace/mvn_prova/src/test/java/vc-db-1.rdf"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("File non trovato");
		}
		
	}

}

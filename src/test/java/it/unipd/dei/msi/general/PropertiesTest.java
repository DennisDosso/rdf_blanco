package it.unipd.dei.msi.general;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**Classe di test per provare un po' ad accedere a delle properties.
 * 
 * Il test ha mostrato anche che fintanto che il file si trova in cartelle interne 
 * al progetto, Eclipse non lo legge. In pratica, la classe opera mantenendo come
 * radice il progetto. Il file di properties, collocato in mvn_prova, viene letto.
 * Spostato da altre parti, necessita della specifica del path*/
public class PropertiesTest {

	public static void main(String[] args) {
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {

			//tenta di leggere il file
	        input = new FileInputStream("configuration.properties");

	        // load a properties file
	        prop.load(input);

	        // get the property value and print it out
	        System.out.println(prop.getProperty("database"));
	        System.out.println(prop.getProperty("dbuser"));
	        System.out.println(prop.getProperty("dbpassword"));

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
}

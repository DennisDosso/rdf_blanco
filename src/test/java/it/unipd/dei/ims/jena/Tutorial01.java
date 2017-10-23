/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unipd.dei.ims.jena ;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.VCARD;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//import org.terrier.indexing.Collection;

/** Tutorial 1 creating a simple model
 */

public class Tutorial01 extends Object {
	// some definitions
	static String personURI    = "http://somewhere/JohnSmith";
	static String fullName     = "John Smith";

	public static void main (String args[]) {

		//si setta il logger altrimenti jena tira fuori warning fastidiosi
//		Log.setLog4j("jena-log4j.properties");

		System.out.println(fullName);
		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// create the resource
		Resource johnSmith = model.createResource(personURI);

		// add the property
		johnSmith.addProperty(VCARD.FN, fullName);

		//il metodo toString della Resource restituisce l'URI della risorsa
		System.out.println(johnSmith);
		System.out.println(VCARD.FN);
		
		//un po' di logging
		PropertyConfigurator.configure("src/test/java/log4j.properties");
		Logger log = Logger.getLogger(Tutorial01.class);
		
		log.debug("messaggio fatto comparire nel logger dalla classe Tutorial01 a livello debug");
		log.error("questo invece a livello errore");
		
		
	}
}

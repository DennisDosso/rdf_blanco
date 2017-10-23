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

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;


/** Tutorial 3 Statement attribute accessor methods
 * 
 * Crea un piccolo grafo RDF, crea un iteratore per esso
 * e mostra come iterare sugli elementi di un grafo.
 */
public class Tutorial03 extends Object {
	public static void main (String args[]) {

		// some definitions
		String personURI    = "http://somewhere/JohnSmith";
		String givenName    = "John";
		String familyName   = "Smith";
		String fullName     = givenName + " " + familyName;
		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// create the resource
		//   and add the properties cascading style
		Resource johnSmith 
		= model.createResource(personURI)
		.addProperty(VCARD.FN, fullName)
		.addProperty(VCARD.N, 
				model.createResource()
				.addProperty(VCARD.Given, givenName)
				.addProperty(VCARD.Family, familyName));

		// list the statements in the graph
		//creazione dell'iteratore
		StmtIterator iter = model.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			/*SI prende la prossima tripla e se ne prendono soggetto, 
			 * predicato ed oggetto*/
			Statement stmt      = iter.nextStatement();         // get next statement

			Resource  subject   = stmt.getSubject();   // get the subject
			Property  predicate = stmt.getPredicate(); // get the predicate
			RDFNode   object    = stmt.getObject();    // get the object

			//si stampano i valori di soggetto, predicato ed oggetto
			System.out.print(subject.toString());
			System.out.print(" " + predicate.toString() + " ");
			
			//secondo standard RDF, un oggetto potrebbe essere
			//una risorsa/nodo oppure una stringa.
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}
			System.out.println(" .");
		}
	}
}

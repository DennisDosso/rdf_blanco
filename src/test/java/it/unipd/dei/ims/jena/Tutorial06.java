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
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.*;

import java.io.*;

/** Tutorial navigating a model
 */
public class Tutorial06 extends Object {
    
    static final String inputFileName = "vc-db-1.rdf";
    //NB: risorsa da cercare (preidcato). Scaricato dal web 
    // era senza / finale, invece è necessario
    static final String johnSmithURI = "http://somewhere/JohnSmith/";
    
    public static void main (String args[]) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
       
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file
        //attenzione a mettere il breakpoint su questo metodo. Manda in vacca l'esecuzione
        //in fase di debug
        model.read(new InputStreamReader(in), "");
        
        // retrieve the Adam Smith vcard resource from the model
        //attenzione, l'URI deve essere presente nel model. Altrimenti lancia eccezione
        Resource vcard = model.getResource(johnSmithURI);//ritorna il nodo corrispondente all'uri

        // retrieve the value of the N property
        Resource name = (Resource) vcard.getRequiredProperty(VCARD.N)//ritorna la tripla che ha come soggetto vcard e come predicato VCAR.N
                                        .getObject();//ritorna l'oggtto della tripla. Di suo ritorna un RDFNode
        												//per questo il casting
        
        // retrieve the given name property
        String fullName = vcard.getRequiredProperty(VCARD.FN)//ritorna la tripla intera
                               .getString();//ritorna l'oggetto sapendo che è un literal
        
        // add two nick name properties to vcard
        vcard.addProperty(VCARD.NICKNAME, "Smithy")//aggiungiamo al soggetto due rami con lo stesso predicato
             .addProperty(VCARD.NICKNAME, "Adman");//ma due oggetti diversi
        
        // set up the output
        System.out.println("The nicknames of \"" + fullName + "\" are:");
        
        // list the nicknames
        StmtIterator iter = vcard.listProperties(VCARD.NICKNAME);//dato il soggetto vcard e il predicato, ritorna
        															//un iteratore sugli oggetti con tale predicato
        while (iter.hasNext()) {
            System.out.println("    " + iter.nextStatement().getObject()
                                            .toString());//prendiamo sempre come RDFNode e poi invochiamo il toString per essere generali
        }
    }
}

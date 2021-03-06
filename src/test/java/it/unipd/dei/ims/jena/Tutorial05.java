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

import java.io.*;

/** Tutorial 5 - read RDF XML from a file and write it to standard out.
 * 
 * Si è scoperto che gli input prendono come radice la cartella del progetto.
 * Se vuoi indicare semplicemente il nome del file, devi avercelo nella cartella del progetto,
 * non in sottocartelle.
 */
public class Tutorial05 extends Object {

    /**
        NOTE that the file is loaded from the class-path and so requires that
        the data-directory, as well as the directory containing the compiled
        class, must be added to the class-path when running this and
        subsequent examples.
    */    
    static String inputFileName  = "vc-db-1.rdf";
    //modelInputPath=/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/rdf_datasets/E-model.nt
    
    		/* Ho avuto problemi con Eclipse nel riconoscimento di file. 
     * Si utilizza qui il path assoluto.*/
    static final String absolutePathInputFileName = "/Users/dennisdosso/eclipse-workspace/mvn_prova/src/test/java/vc-db-1.rdf";
    
    public static void main (String args[]) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        
        inputFileName = "/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/rdf_datasets/E-model.nt";
        InputStream in = FileManager.get().open( inputFileName );
//        InputStream in = FileManager.get().open( absolutePathInputFileName );
        
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file
        //e crea un Model su di esso
        //model.read(in, "");
        model.read(in, "N_TRIPLE");
                    
        // write it to standard out (per cambiare si usa N-TRIPLE. 
        //se non indichi nulla riscrive come XML)
        model.write(System.out, "N-TRIPLE");            
    }
}

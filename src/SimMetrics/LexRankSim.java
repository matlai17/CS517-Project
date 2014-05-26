/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SimMetrics;

import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author Matthew Lai
 */
public class LexRankSim implements SimMetric {
    
    UndirectedSparseMultigraph<List<String>, Double> graph;
    
    public LexRankSim(List<List<String>> documents, double threshold)
    {
        graph = new UndirectedOrderedSparseMultigraph<>();
        CosineSim cS = new CosineSim();
        for(List<String> document : documents)
        {
            graph.addVertex(document);
            
        }
        
    }

    @Override
    public double documentRank(List<String> document, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

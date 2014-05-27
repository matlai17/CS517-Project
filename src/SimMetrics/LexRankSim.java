/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SimMetrics;

import Matrix.FrequencyMatrix;
import Matrix.IDFMatrix;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Matthew Lai
 */
public class LexRankSim implements SimMetric {
    
    UndirectedSparseMultigraph<List<String>, Double> graph;
    IDFMatrix iDF;
    FrequencyMatrix tf;
    public LexRankSim(List<List<String>> documents, double simThreshold)
    {
        graph = new UndirectedSparseMultigraph<>();
        for(List<String> document : documents)
        {
//            Collection<List<String>> asdf = graph.getVertices();
            CopyOnWriteArrayList<List<String>> cOWAL = new CopyOnWriteArrayList<>(graph.getVertices());
            java.util.Iterator<List<String>> it = cOWAL.iterator();
            while(it.hasNext())
            {
                List<String> vertex = it.next();
                double similarity = CosineSim.documentSimilarity(document, vertex);
                if(similarity > simThreshold) 
                    try
                    {
                        graph.addEdge(similarity, vertex, document);
                    } catch(IllegalArgumentException e){}
                
            }
            if(!graph.containsVertex(document)) graph.addVertex(document);
        }
        iDF = new IDFMatrix(documents);
    }
    
    public LexRankSim(List<List<String>> documents, IDFMatrix iDF, double simThreshold)
    {
        graph = new UndirectedSparseMultigraph<>();
        for(List<String> document : documents)
        {
            for( List<String> vertex : graph.getVertices() )
            {
                double similarity = CosineSim.documentSimilarity(document, vertex);
                if(similarity > simThreshold) graph.addEdge(similarity, document, vertex);
            }
            if(!graph.containsVertex(document)) graph.addVertex(document);
        }
        this.iDF = iDF;
    }
    
    public IDFMatrix getIDFMatrix()
    {
        return iDF;
    }
    
    public int retSize() { return graph.getVertexCount(); }
    
    private double relavence(List<String> document, List<String> query)
    {
        FrequencyMatrix tfDocument = new FrequencyMatrix(document);
        FrequencyMatrix tfQuery = new FrequencyMatrix(query);
        double score = 0;
        for(String queryWord : query)
            score += Math.log(tfDocument.getFreq(queryWord) + 1)
                    * Math.log(tfQuery.getFreq(queryWord) + 1)
                    * iDF.getIDF(queryWord);
        return score;
    }
    
    public double documentRank(List<String> document, List<String> query, double queryBias)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double documentRank(List<String> document, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query, int resultAmount) {
//        
//    }
}

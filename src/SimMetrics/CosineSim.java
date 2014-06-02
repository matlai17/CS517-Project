package SimMetrics;


import Matrix.FrequencyMatrix;
import Matrix.IDFMatrix;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author Matthew Lai
 */
public class CosineSim implements SimMetric {
    
    IDFMatrix iDFMatrix;
    
    public CosineSim()
    {
        iDFMatrix = null;
    }
    
    public CosineSim(IDFMatrix iDFM)
    {
        iDFMatrix = iDFM;
    }
    
    @Override
    public Double documentRank(List<String> document, List<String> query) {
        
        if(iDFMatrix != null) return documentRankIDFWeighted(document, query, iDFMatrix);
        if(document.isEmpty() && query.isEmpty()) return 1.0;
        if(document.isEmpty() && !query.isEmpty()) return 0.0;
        if(query.isEmpty() && !document.isEmpty()) return 0.0;
        
        FrequencyMatrix documentFrequencyMatrix = new FrequencyMatrix(document);
        FrequencyMatrix queryFrequencyMatrix = new FrequencyMatrix(query);
        double denominator;
        int numerator = 0;
        int aMagnitudeTotal = 0;
        int bMagnitudeTotal = 0;
        
        for(String word : document)
        {
            numerator += documentFrequencyMatrix.getFreq(word) * queryFrequencyMatrix.getFreq(word);
            aMagnitudeTotal += Math.pow(documentFrequencyMatrix.getFreq(word), 2);
        }
        for(String word : query)
            bMagnitudeTotal += Math.pow(queryFrequencyMatrix.getFreq(word), 2);
        
        denominator = Math.sqrt(aMagnitudeTotal) * Math.sqrt(bMagnitudeTotal);
        
        double sim = (double)numerator / denominator;
        return sim;
    }
    
    public static double documentSimilarity(List<String> document, List<String> query) {
        return new CosineSim().documentRank(document, query);
    }
    
    public static double documentRankIDFWeighted(List<String> document, List<String> query, IDFMatrix iDF) {
        
        if(document.isEmpty() && query.isEmpty()) return 1;
        if(document.isEmpty() && !query.isEmpty()) return 0;
        if(query.isEmpty() && !document.isEmpty()) return 0;
        
        FrequencyMatrix documentFrequencyMatrix = new FrequencyMatrix(document);
        FrequencyMatrix queryFrequencyMatrix = new FrequencyMatrix(query);
        
        double denominator;
        double numerator = 0;
        double aMagnitudeTotal = 0;
        double bMagnitudeTotal = 0;
        
        for(String word : document)
        {
            numerator += documentFrequencyMatrix.getFreq(word) * queryFrequencyMatrix.getFreq(word) * Math.pow(iDF.getIDF(word),2);
            aMagnitudeTotal += Math.pow(documentFrequencyMatrix.getFreq(word) * iDF.getIDF(word), 2);
        }
        for(String word : query)
            bMagnitudeTotal += Math.pow(queryFrequencyMatrix.getFreq(word) * iDF.getIDF(word), 2);
        
        denominator = Math.sqrt(aMagnitudeTotal) * Math.sqrt(bMagnitudeTotal);
        
        Double sim = numerator / denominator;
        if(denominator == 0) return 0;
        return sim;
    }
}

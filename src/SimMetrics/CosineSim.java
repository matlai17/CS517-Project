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
    public double documentRank(List<String> document, List<String> query) {
        
        if(iDFMatrix != null) return documentRankIDFWeighted(document, query, iDFMatrix);
        if(document.isEmpty() && query.isEmpty()) return 1;
        if(document.isEmpty() && !query.isEmpty()) return 0;
        if(query.isEmpty() && !document.isEmpty()) return 0;
        
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
        int numerator = 0;
        int aMagnitudeTotal = 0;
        int bMagnitudeTotal = 0;
        
        for(String word : document)
        {
            numerator += documentFrequencyMatrix.getFreq(word) * queryFrequencyMatrix.getFreq(word) * Math.pow(iDF.getIDF(word),2);
            aMagnitudeTotal += Math.pow(documentFrequencyMatrix.getFreq(word) * iDF.getIDF(word), 2);
        }
        for(String word : query)
            bMagnitudeTotal += Math.pow(queryFrequencyMatrix.getFreq(word) * iDF.getIDF(word), 2);
        
        denominator = Math.sqrt(aMagnitudeTotal) * Math.sqrt(bMagnitudeTotal);
        
        double sim = (double)numerator / denominator;
        return sim;
    }

    @Override
    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args) {
        ArrayList<ArrayList<String>> documents = new ArrayList<>();
        ArrayList<String> query = new ArrayList<>();
        ArrayList<String> document = new ArrayList<>();
        
        String documentA[] = {"Divis", "of", "Univers", "Advancement"};
        String queryA[] = {"Office", "of", "Public", "Affair", "New", "Releas" };
        
        for(String s : documentA) document.add(s);
        for(String s : queryA) query.add(s);
        
        System.out.println(new CosineSim().documentRank(document, query));
//        String documentText[] = { "abcdef" , "wxyz" };
//        String queryText = "abcdzyx";
        
//        for (int i = 0; i < documentText.length; i++) {
//            ArrayList<String> document = new ArrayList<>();
//            for(int j = 0; j < documentText[i].length(); j++){
//                document.add(documentText[i].charAt(j) + "");
//            }
//            documents.add(document);
//        }
//        
//        for(int i = 0; i < queryText.length(); i++) query.add(queryText.charAt(i) + "");
        
//        for(ArrayList<String> document : documents)
//        {
//            for(String word : document) System.out.print(word + " ");
//            System.out.print("\t");
//            for(String word : query) System.out.print(word + " ");
//            System.out.print(" : \t");
//            System.out.println(new CosineSim().documentRank(document, query));
//        }
    }
    
}

package SimMetrics;


import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedMap;

/**
 *
 * @author Matthew Lai
 */
public class CosineSim implements SimMetric {
    
    private class FrequencyMatrix
    {
        ArrayList<String> vectAtt;
        int[][] matrix;
        
        public FrequencyMatrix(List<String> document1, List<String> document2)
        {
            HashSet<String> setOfVectorAttributes = new HashSet<>();
            for(String s : document1) setOfVectorAttributes.add(s); 
            for(String s : document2) setOfVectorAttributes.add(s); 
            
            vectAtt = new ArrayList<>(setOfVectorAttributes);
            matrix = new int[vectAtt.size()][2];
            
            for(String s : document1) matrix[vectAtt.indexOf(s)][0]++;
            for(String s : document2) matrix[vectAtt.indexOf(s)][1]++;
        }
        
        public int matrixLength() { return vectAtt.size(); }
        public int getFreqA(int index) { return matrix[index][0]; }
        public int getFreqB(int index) { return matrix[index][1]; }
    }
    
    @Override
    public double documentRank(List<String> document, List<String> query) {
        
        if(document.isEmpty() && query.isEmpty()) return 1;
        if(document.isEmpty() && !query.isEmpty()) return 0;
        if(query.isEmpty() && !document.isEmpty()) return 0;
        
        FrequencyMatrix fM = new FrequencyMatrix(document, query);
        double denominator;
        int numerator = 0;
        int aMagnitudeTotal = 0;
        int bMagnitudeTotal = 0;
        
        for(int i = 0; i < fM.matrixLength(); i++)
        {
            numerator += fM.getFreqA(i) * fM.getFreqB(i);
            aMagnitudeTotal += Math.pow(fM.getFreqA(i), 2);
            bMagnitudeTotal += Math.pow(fM.getFreqB(i), 2);
        }
        
        denominator = Math.sqrt(aMagnitudeTotal) * Math.sqrt(bMagnitudeTotal);
        
        double sim = (double)numerator / denominator;
        return sim;
    }

    @Override
    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args) {
        ArrayList<String> document = new ArrayList<>();
        ArrayList<String> query = new ArrayList<>();
        
        document.add("I");
        document.add("like");
        document.add("to");
        document.add("eat");
        document.add("pie");
        document.add("for");
        document.add("breakfast");
        document.add("lunch");
        document.add("and");
        document.add("dinner");
        
// This block of query statement should get near 1        
//        query.add("I");
//        query.add("like");
//        query.add("to");
//        query.add("eat");
//        query.add("pie");
//        query.add("for");
//        query.add("breakfast");
//        query.add("lunch");
//        query.add("and");
//        query.add("dinner");

// This block of query statement should get higher than 0
        query.add("can");
        query.add("I");
        query.add("get");
        query.add("pie");
        query.add("for");
        query.add("dinner");

// This block of query statement should get higher than the one above       
//        query.add("like");
//        query.add("pie");
//        query.add("breakfast");
//        query.add("lunch");
//        query.add("dinner");
        
// This block of query statement should get lower than the one above       
//        query.add("john");        
//        query.add("like");
//        query.add("pie");
//        query.add("breakfast");
//        query.add("lunch");
//        query.add("dinner");

// This block of query statement should get 0        
//        query.add("whats");
//        query.add("up");
//        query.add("doc");
        
        System.out.println(new CosineSim().documentRank(document, query));
    }
    
}

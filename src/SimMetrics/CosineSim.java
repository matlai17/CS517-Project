package SimMetrics;


import Matrix.FrequencyMatrix;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author Matthew Lai
 */
public class CosineSim implements SimMetric {
    
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
        ArrayList<ArrayList<String>> documents = new ArrayList<>();
        ArrayList<String> query = new ArrayList<>();
        
        String documentText[] = { "abcdef" , "wxyz" };
        String queryText = "abcdzyx";
        
        for (int i = 0; i < documentText.length; i++) {
            ArrayList<String> document = new ArrayList<>();
            for(int j = 0; j < documentText[i].length(); j++){
                document.add(documentText[i].charAt(j) + "");
            }
            documents.add(document);
        }
        
        for(int i = 0; i < queryText.length(); i++) query.add(queryText.charAt(i) + "");
        
        for(ArrayList<String> document : documents)
        {
            for(String word : document) System.out.print(word + " ");
            System.out.print("\t");
            for(String word : query) System.out.print(word + " ");
            System.out.print(" : \t");
            System.out.println(new CosineSim().documentRank(document, query));
        }
    }
    
}

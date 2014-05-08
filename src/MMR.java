
import SimMetrics.CosineSim;
import SimMetrics.SimMetric;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Matthew Lai
 */
public class MMR {
    
    class Document_Score
    {
        List<String> document;
        double score;
        public Document_Score(List<String> document, double score)
        {
            this.document = document;
            this.score = score;
        }
        public List<String> getDocument() { return document; }
        public double getScore() { return score; }
    }
    
    SimMetric sim1;
    SimMetric sim2;
    static final int GRAPH_SIM = 1;
    static final int COSINE_SIM = 2;
    static final int PSEUDO_CODE_SIM = 3;
    
    public MMR(int sim1Type, int sim2Type)
    {
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
        
        switch(sim1Type) {
            case 1: 
                sim1 = new CosineSim();
                break;
            case 2:
                
                break;
            case 3:
                
                break;
        }
        switch(sim2Type) {
            case 1: 
                sim2 = new CosineSim();
                break;
            case 2:
                
                break;
            case 3:
                
                break;
        }
    }
    
    public List<List<String>> resultList(List<List<String>> sentenceList, List<String> query, double lambda, int maxResults)
    {
        TreeSet<Map.Entry<List<String>,Integer>> selectedDocuments = new TreeSet<>(new Comparator<Map.Entry<List<String>, Integer>>() {

            @Override
            public int compare(Map.Entry<List<String>, Integer> o1, Map.Entry<List<String>, Integer> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });
        
        ArrayList<List<String>> clonedList = new ArrayList<List<String>>(sentenceList);
        
        
        
        return new ArrayList<List<String>>();
    }
    
    public Document_Score retrieveDocument(List<List<String>> unselectedDocuments, List<List<String>> selectedDocuments, List<String> query, double lambda)
    {
        List<String> bestDocument = unselectedDocuments.get(0);
        double maxScore = Double.MIN_VALUE;
        for(List<String> document : unselectedDocuments)
        {
            double score = lambda * (sim1.sentenceRank(document, query) - ((1 - lambda) * maxSim2(document, selectedDocuments)));
            if(score > maxScore) 
            {
                maxScore = score;
                bestDocument = document;
            }
        }
        return new Document_Score(bestDocument, maxScore);
    }
    
    private double maxSim2(List<String> unselectedDocument, List<List<String>> selectedDocuments)
    {
        double maxScore = Double.MIN_VALUE;
        for(List<String> document : selectedDocuments)
        {
            double score = sim2.sentenceRank(unselectedDocument, document);
            if(score > maxScore) maxScore = score;
        }
        return maxScore;
    }
    
//    private static Map<List<String>, Double> sortByValue(Map<List<String>, Double> map)
//    {
//        List<Map.Entry<List<String>, Double>> list = new LinkedList<Map.Entry<List<String>, Double>>(map.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<List<String>, Double>>()
//        {
//            @Override
//            public int compare(Map.Entry<List<String>, Double> o1, Map.Entry<List<String>, Double> o2) 
//            {
//                return -(o1.getValue()).compareTo(o2.getValue());
//            }
//        } );
//        
//        Map<List<String>, Double> result = new LinkedHashMap<List<String>, Double>();
//        for(Map.Entry<List<String>, Double> entry : list)
//        {
//            result.put(entry.getKey(), entry.getValue());
//        }
//        return result;
//    }
    
//    private static Map<String, Double> sortByValue2(Map<String, Double> map)
//    {
//        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
//        {
//
//            @Override
//            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) 
//            {
//                return -(o1.getValue()).compareTo(o2.getValue());
//            }
//        } );
//        
//        Map<String, Double> result = new LinkedHashMap<String, Double>();
//        for(Map.Entry<String, Double> entry : list)
//        {
//            result.put(entry.getKey(), entry.getValue());
//        }
//        return result;
//    }
//    
//    public static void main(String[] args) {
//        HashMap<String, Double> testMap = new HashMap<>();
//        testMap.put("test2", .2);
//        testMap.put("test6", .6);
//        testMap.put("test4", .4);
//        testMap.put("test3", .3);
//        testMap.put("test5", .5);
//        testMap.put("test1", .1);
//        testMap = (HashMap<String, Double>) sortByValue2(testMap);
//        for(Map.Entry<String, Double> entry : testMap.entrySet())
//        {
//            System.out.println(entry.getKey());
//        }
//    }
}

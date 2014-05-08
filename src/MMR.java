
import SimMetrics.CosineSim;
import SimMetrics.SimMetric;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
    
    static class Document_Score
    {
        private List<String> document;
        private double score;
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
    
    public MMR()
    {
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
    }
    
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
    
    public List<List<String>> rankedList(List<List<String>> sentenceList, List<String> query, double lambda, int maxResults)
    {
        TreeSet<Document_Score> sList = new TreeSet<>(new Comparator<Document_Score>() {

            @Override
            public int compare(Document_Score o1, Document_Score o2) {
                return -Double.compare(o1.getScore(), o2.getScore());
            }
        });
        
        ArrayList<List<String>> r_sList = new ArrayList<>(sentenceList);
        
        while(!r_sList.isEmpty())
        {
            Document_Score retrievedDocument = retrieveDocument(r_sList, sList, query, lambda);
            sList.add(retrievedDocument);
            r_sList.remove(retrievedDocument.getDocument());
        }
        
        ArrayList<List<String>> rankedList = new ArrayList<>();
        for(Document_Score ds : sList) 
        {
            rankedList.add(ds.getDocument());
            if(rankedList.size() >= maxResults) break;
        }
        
        return rankedList;
    }
    
    private Document_Score retrieveDocument(List<List<String>> unselectedDocuments, TreeSet<Document_Score> selectedDocuments, List<String> query, double lambda)
    {
        List<String> bestDocument = unselectedDocuments.get(0);
        double maxScore = 0;
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
    
    private double maxSim2(List<String> unselectedDocument, TreeSet<Document_Score> selectedDocuments)
    {
        double maxScore = 0;
        Iterator<Document_Score> it = selectedDocuments.iterator();
        while(it.hasNext())
        {
            List<String> document = it.next().getDocument();
            double score = sim2.sentenceRank(unselectedDocument, document);
            if(score > maxScore) maxScore = score;
        }
        return maxScore;
    }
    
    public static void main(String[] args) {
        
//        TreeSet<Document_Score> selectedDocuments = new TreeSet<>(new Comparator<Document_Score>() {
//
//            @Override
//            public int compare(Document_Score o1, Document_Score o2) {
//                return -Double.compare(o1.getScore(), o2.getScore());
//            }
//        });
//        
//        ArrayList<String> one = new ArrayList<>();
//        one.add("one");
//        ArrayList<String> two = new ArrayList<>();
//        one.add("two");
//        ArrayList<String> three = new ArrayList<>();
//        one.add("three");
//        ArrayList<String> four = new ArrayList<>();
//        one.add("four");
//        
//        selectedDocuments.add(new Document_Score(one, .2));
//        selectedDocuments.add(new Document_Score(one, .4));
//        selectedDocuments.add(new Document_Score(one, .3));
//        selectedDocuments.add(new Document_Score(one, .1));
//        Iterator<Document_Score> it = selectedDocuments.iterator();
//        while(it.hasNext())
//        {
//            Document_Score ds = it.next();
//            System.out.println(ds.getScore());
//        }
        
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
    }
}

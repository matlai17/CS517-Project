
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
    
    SimMetric sim1;
    SimMetric sim2;
    
    public MMR(int sim1Type, int sim2Type)
    {
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
    }
    
    public Map<List<String>, Double> orderedSentences(List<List<String>> sentences, List<String> query, double lambda)
    {
        HashMap<List<String>, Double> unsortedList = new HashMap<>();
        for(List<String> sentence : sentences)
        {
//            double score = lambda * (sim1.sentenceRank(sentence, query) - ((1 - lambda) * ) );
        }
        return sortByValue(unsortedList);
    }
    
    private static Map<List<String>, Double> sortByValue(Map<List<String>, Double> map)
    {
        List<Map.Entry<List<String>, Double>> list = new LinkedList<Map.Entry<List<String>, Double>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<List<String>, Double>>()
        {
            @Override
            public int compare(Map.Entry<List<String>, Double> o1, Map.Entry<List<String>, Double> o2) 
            {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        } );
        
        Map<List<String>, Double> result = new LinkedHashMap<List<String>, Double>();
        for(Map.Entry<List<String>, Double> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
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

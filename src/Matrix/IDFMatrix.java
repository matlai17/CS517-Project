/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Matthew Lai
 */
public class IDFMatrix {
    
    HashMap<String, Integer> vectAtt;
    final int N;
    
    public IDFMatrix(List<List<String>> document)
    {
        N = document.size();
        vectAtt = new HashMap<>();
        for(List<String> sentence : document)
        {
            HashSet<String> indexedSentence = new HashSet<>();
            for(String word : sentence)
            {
                if(vectAtt.containsKey(word))
                {
                    if(!indexedSentence.contains(word))
                    {
                        vectAtt.put(word, vectAtt.get(word) + 1);
                        indexedSentence.add(word);
                    }
                }
                else vectAtt.put(word, 1);
            }
        }
    }

    public double getIDF(String words) {
        Integer count = vectAtt.get(words);
        if(count == null) count = 0;
        return Math.log((N + 1)/(0.5 + count));
    }

    
}

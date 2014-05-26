/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Matthew Lai
 */
public class FrequencyMatrix {
    
    HashMap<String, Integer> vectAtt;

    public FrequencyMatrix(List<String> document)
    {
        vectAtt = new HashMap<>();
        for(String word : document)
            if(vectAtt.containsKey(word)) vectAtt.put(word, vectAtt.get(word)+1);
            else vectAtt.put(word, 1);
        
    }

    public int matrixLength() { return vectAtt.size(); }
    public int getFreq(String word) {
        Integer count = vectAtt.get(word);
        if(count == null) return 0;
        return count;
    }
}

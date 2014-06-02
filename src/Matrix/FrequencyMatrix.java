
package Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Frequency Matrix stores the word frequency of the input document or sentence.
 * Allows for a word query to get the frequency of a word.
 * 
 * @author Matthew Lai
 */
public class FrequencyMatrix {
    
    HashMap<String, Integer> vectAtt;

    public FrequencyMatrix(List<String> document)
    {
        vectAtt = new HashMap<>();
        for(String word : document)
        {
            word = word.toLowerCase();
            if(vectAtt.containsKey(word)) vectAtt.put(word, vectAtt.get(word)+1);
            else vectAtt.put(word, 1);
        }
        
    }

    public int matrixLength() { return vectAtt.size(); }
    
    public int getFreq(String word) {
        Integer count = vectAtt.get(word);
        if(count == null) return 0;
        return count;
    }
}

package SimMetrics;

import java.util.List;
import java.util.HashSet;

/**
 *
 * @author Matthew Lai
 */
public class JaccardSim implements SimMetric{

    @Override
    public Double documentRank(List<String> document, List<String> query) {
        
        if(document.isEmpty() && query.isEmpty()) return 1.0;
        if(document.isEmpty() && !query.isEmpty()) return 0.0;
        if(query.isEmpty() && !document.isEmpty()) return 0.0;
        
        HashSet<String> union = new HashSet<>();
        HashSet<String> intersection = new HashSet<>();
        
        for(String s : document)
        {
            union.add(s);
            if(query.contains(s)) intersection.add(s);
        }
        
        for(String s : query) union.add(s);
        
        return (double)intersection.size() / union.size();
    }
}

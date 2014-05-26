/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Matrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Matthew Lai
 */
public class FrequencyMatrix {
    
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

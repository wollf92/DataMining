/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mars;

import java.util.ArrayList;

/**
 *
 * @author hessel
 */
public class MARSTerm {
    
    public double Coëff;
    public ArrayList<Double> Knot;
    public ArrayList<Integer> VarRow;
    public ArrayList<Boolean> NegHinge;
    
    public MARSTerm(double c){
        Coëff = c;
        NegHinge = new ArrayList<>();
        Knot = new ArrayList<>();
        VarRow = new ArrayList<>();
    }
    
    public double ComputeTermValue(int index, double[][] XAXIS){
        double result = 0;
        if(Knot.isEmpty())
            result += Coëff;
        else{
            double prod = 1;
            for(int i = 0; i < Knot.size(); i++){
                if(NegHinge.get(i))
                    prod *= Math.max(0,Knot.get(i) - XAXIS[VarRow.get(i)][index]);
                else
                    prod *= Math.max(0,XAXIS[VarRow.get(i)][index] - Knot.get(i));
            }
            result += Coëff*prod;
        }
        return result;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mars;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hessel
 */
public class MARSTerm {
    
    private String[] dataNames;
    public double Coëff;
    public ArrayList<Double> Knot;
    public ArrayList<Integer> VarRow;
    public ArrayList<Boolean> NegHinge;
    
    public MARSTerm(double c, String[] dn){
        Coëff = c;
        NegHinge = new ArrayList<>();
        Knot = new ArrayList<>();
        VarRow = new ArrayList<>();
        dataNames = dn;
    }
    
    public double ComputeTermValue(List<Double> instance){
        double result = 0;
        if(Knot.isEmpty())
            result += Coëff;
        else{
            double prod = 1;
            for(int i = 0; i < Knot.size(); i++){
                if(NegHinge.get(i))
                    prod *= Math.max(0,Knot.get(i) - instance.get(VarRow.get(i)));
                else
                    prod *= Math.max(0,instance.get(VarRow.get(i)) - Knot.get(i));
            }
            result += Coëff*prod;
        }
        return result;
    }
    
    @Override
    public String toString(){
        String s = "";
        s += Double.toString(Coëff);
        for(int i = 0; i < Knot.size(); i++){
            if(NegHinge.get(i)){
                s += "*" + "max(0," + Double.toString(Knot.get(i)) + "-" + dataNames[VarRow.get(i)] + ")";
            }
            else
                s += "*" + "max(0," + dataNames[VarRow.get(i)] + "-" + Double.toString(Knot.get(i)) + ")";
        }
        return s;
    }
}

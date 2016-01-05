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
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Hessel Bongers
 */
public class Model {
    
    private ArrayList<MARSTerm> Formula = new ArrayList<>();
    private double RSS;
    private double GCV;
    private HashMap<String, List<Double>> InstanceValues;
    private int MaxTerms;
    private int MaxTermDepth;
    private String[] dataNames;
    
    private static final int VARIABLE_COUNT = 14;
    private static final int INDEX_RESPONSE = 14;
    
    public Model(){
    }
    
    public ArrayList<MARSTerm> getFormula(){
        return Formula;
    }
    
    public void setMaxTerms(int t){
        MaxTerms = t;
    }
    
    public int getMaxTerms(){
        return MaxTerms;
    }
    
    public void setMaxTermDepth(int td){
        MaxTermDepth = td;
    }
    
    public int getMaxTermDepth(){
        return MaxTermDepth;
    }
    
    public void setFileToReadDataFrom(String text) throws FileNotFoundException, IOException {
        String line;
        BufferedReader br;
        br = new BufferedReader(new FileReader("src/mars/" + text + ".csv"));
        InstanceValues = new HashMap<>();
        dataNames = br.readLine().split(",");
        while((line = br.readLine()) != null)
        {
            String[] perValue = line.split(",");
            String date = perValue[1] + "-" + perValue[5];
            InstanceValues.put(date, new ArrayList<Double>());
            InstanceValues.get(date).add((double)perValue[0].length());
            for(int i = 2; i <= 15; i++)
            {
                try{
                    InstanceValues.get(date).add(Double.parseDouble(perValue[i]));
                } catch (NumberFormatException e) {}
            }
        }    
        //printData();
        //System.out.println(getDataFromDate("2011-04-15-5"));
    }
    
    public List<Double> getDataFromDate(String date)
    {
        return InstanceValues.get(date);
    }
    
    public void printData()
    {
        Iterator<Entry<String, List<Double>>> it = InstanceValues.entrySet().iterator();
        String line;
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            line = (String)pair.getKey();
            for(double d : (List<Double>)pair.getValue())
                line = line + " " + d;
            System.out.println(line);
        }
    }
    
    public ArrayList<MARSTerm> CalculateFormula(){
        ForwardPass();
        BackwardPass();
        return Formula;
    }
    
    private double ComputeRSS(ArrayList<MARSTerm> form){
        Iterator<List<Double>> it = InstanceValues.values().iterator();
        double sum = 0;
        for(List<Double> instance : InstanceValues.values()){
            try{
                double residu = ComputeValue(form, instance) - instance.get(INDEX_RESPONSE);
                sum += residu * residu;
            } catch(IndexOutOfBoundsException e){System.out.println(instance.toString());}
        }
        sum = sum/(double)InstanceValues.size();
        return sum;
    }
    
    private double ComputeGCV(ArrayList<MARSTerm> form){
        double amtOfTerms = form.size() + 2*(form.size()-1)/2;
        double lower = (1 - amtOfTerms/(double)InstanceValues.size());
        lower = (double)InstanceValues.size()*lower*lower;
        return RSS/lower;
    }
    
    public void BackwardPass(){
        GCV = ComputeGCV(Formula);
        RemoveTerms();
    }
    
    private void RemoveTerms(){
        double testGCV = 0;
        MARSTerm removed = new MARSTerm(0, dataNames);
        while(testGCV < GCV){
            removed = RemoveTerm();
            testGCV = ComputeGCV(Formula);
        }
        if(removed.Coëff != 0)
            Formula.add(removed);
    }
    
    private MARSTerm RemoveTerm (){
        ArrayList<MARSTerm> testFormula = (ArrayList<MARSTerm>)Formula.clone();
        MARSTerm removed = new MARSTerm(0, dataNames);
        double RSSdiff = RSS;
        for(int i = 1; i < Formula.size(); i++){
            MARSTerm remTest = testFormula.get(i);
            testFormula.remove(i);
            double testRSS = ComputeRSS(testFormula);
            if(Math.abs(RSS - testRSS) < RSSdiff){
                RSSdiff = Math.abs(RSS - testRSS);
                removed = remTest;
            }
            testFormula.add(i,remTest);
        }
        if(Formula.remove(removed))
            return removed;
        return new MARSTerm(0, dataNames);
    }
    
    public void ForwardPass(){
        GetIntercept();
        RSS = ComputeRSS(Formula);
        while(Formula.size() < MaxTerms){
            FindNextPair();
        }
    }
    
    private void GetIntercept(){
        double ic = 0;
        //Collection<List<Double>> v = InstanceValues.values();
        //System.out.println(InstanceValues.toString());
        
        for(List<Double> instance : InstanceValues.values()){
            try{
                ic += instance.get(INDEX_RESPONSE);
            } catch (IndexOutOfBoundsException e)
                {
                    System.out.println(instance.toString());
                }
        }
        ic = ic/(double)InstanceValues.size();
        Formula.add(new MARSTerm(ic, dataNames));
    }
    
    private void FindNextPair(){
        ArrayList<MARSTerm> best = new ArrayList<>();
        Iterator<List<Double>> it = InstanceValues.values().iterator();
        for(MARSTerm parent : Formula){
            for(int i = 0; i < VARIABLE_COUNT && parent.Knot.size() < MaxTermDepth; i++){
                while(it.hasNext()){
                    List<Double> instance = it.next();
                    double x = TryHingePair(parent, instance, i);
                    if(x < RSS){
                        RSS = x;
                        best = (ArrayList<MARSTerm>)Formula.clone();
                    }
                    System.out.println(parent.toString());
                    Formula.remove(Formula.size()-1);
                    Formula.remove(Formula.size()-1);
                }
            }
        }
        for(MARSTerm mars : best)
        {
            System.out.println(mars.toString());
        }
        Formula = (ArrayList<MARSTerm>)best.clone();
    }
    
    private double TryHingePair(MARSTerm parent, List<Double> instance, int xrow){
        double knot = instance.get(xrow);
        
        MARSTerm new1 = new MARSTerm(ComputeCoëff(Formula.get(0).Coëff, knot, xrow, false), dataNames);
        CopyList(parent.NegHinge, new1.NegHinge);
        new1.NegHinge.add(false);
        CopyList(parent.Knot, new1.Knot);
        new1.Knot.add(knot);
        CopyList(parent.VarRow, new1.VarRow);
        new1.VarRow.add(xrow);
        Formula.add(new1);
        
        MARSTerm new2 = new MARSTerm(ComputeCoëff(Formula.get(0).Coëff, knot, xrow, true), dataNames);
        CopyList(parent.NegHinge, new2.NegHinge);
        new2.NegHinge.add(true);
        CopyList(parent.Knot, new2.Knot);
        new2.Knot.add(knot);
        CopyList(parent.VarRow, new2.VarRow);
        new2.VarRow.add(xrow);
        Formula.add(new2);
        
        return ComputeRSS(Formula);
    }
    
    private double ComputeCoëff(double y, double x, int xrow, boolean neg){
        double lower = 0;
        double upper = 0;
        int instAmt = InstanceValues.size();
        for(List<Double> inst : InstanceValues.values()){
            double a;
            double b;
            if(neg)
                a = x - inst.get(xrow);
            else
                a = inst.get(xrow) - x;
            for(List<Double> inst2 : InstanceValues.values()){
                b = inst2.get(INDEX_RESPONSE) - y;
                upper += a*b;
            }
            lower += a*a;
        }
        upper = upper/(double)(instAmt*instAmt);
        lower = lower/(double)(instAmt*instAmt);
        return upper/lower;
    }
    
    private double ComputeValue(ArrayList<MARSTerm> form, List<Double> instance){
       double result = 0;
       for(MARSTerm cur : form){
           result += cur.ComputeTermValue(instance);
       }
       return result;
    }
    
    public <T>void CopyList(ArrayList<T> a, ArrayList<T> b){
        for(T cur : a){
            b.add(cur);
        }
    }
    
    private double StringtoDouble(String s){
        boolean negative = false;
        int dot = negative ? s.length()-1 : s.length();
        ArrayList<Double> numbers = new ArrayList<>();
        double result = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == '-')
                negative = true;
            else if(s.charAt(i) == '.'){
                if(negative)
                    dot = i - 1;
                else
                    dot = i;
            }
            else
                numbers.add((double)(s.charAt(i) - 48));
        }
        for(int i = 0; i < numbers.size(); i++){
            result += numbers.get(i)*Math.pow(10, dot - i - 1);
        }
        return negative ? -result : result;
    }
}

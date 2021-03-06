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
        String s = br.readLine();
        int index1 = 1 + s.indexOf(',');
        s = s.substring(index1);
        int index2 = 1 + s.indexOf(',');
        s = s.substring(index2);
        dataNames = s.split(",");
        while((line = br.readLine()) != null)
        {
            String[] perValue = line.split(",");
            String date = perValue[1] + "-" + perValue[5];
            InstanceValues.put(date, new ArrayList<Double>());
            for(int i = 2; i <= 16; i++)
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
        boolean keepRunning = true;
        MARSTerm removed = new MARSTerm(0, dataNames);
        while(keepRunning){
            removed = RemoveTerm();
            testGCV = ComputeGCV(Formula);
            if(testGCV < GCV)
                GCV = testGCV;
            else
                keepRunning = false;
        }
        if(removed.Coëff != 0)
            Formula.add(removed);
    }
    
    private MARSTerm RemoveTerm (){
        ArrayList<MARSTerm> testFormula = new ArrayList<>();
        MARSTerm removed = new MARSTerm(0, dataNames);
        double RSSdiff = RSS;
        for(int i = 1; i < Formula.size(); i++){
            copy(Formula, testFormula);
            MARSTerm remTest = testFormula.remove(i);
            double testRSS = ComputeRSS(testFormula);
            if(Math.abs(RSS - testRSS) < RSSdiff && RSSdiff > 0.5){
                RSSdiff = Math.abs(RSS - testRSS);
                removed = remTest;
            }
        }
        if(Formula.remove(removed))
            return removed;
        return new MARSTerm(0, dataNames);
    }
    
    public void ForwardPass(){
        GetIntercept();
        RSS = ComputeRSS(Formula);
        boolean morePairs = true;
        while(Formula.size() < MaxTerms && morePairs){
            morePairs = FindNextPair();
        }
    }
    
    private void GetIntercept(){
        double ic = 0;
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
    
    private boolean FindNextPair(){
        ArrayList<MARSTerm> form = new ArrayList<>();
        copy(Formula, form);
        ArrayList<MARSTerm> best = new ArrayList<>();
        Iterator<List<Double>> it;
        boolean rssChanged = false;
        for(MARSTerm parent : Formula){
            for(int i = 0; i < VARIABLE_COUNT && parent.Knot.size() < MaxTermDepth; i++){
                it = InstanceValues.values().iterator();
                while(it.hasNext()){
                    List<Double> instance = it.next();
                    double x = TryHingePair(parent, instance, i, form);
                    if(x < RSS && Math.abs(x - RSS) > 0.05){
                        RSS = x;
                        rssChanged = true;
                        copy(form, best);
                    }
                    form.remove(form.size()-1);
                    form.remove(form.size()-1);
                }
            }
        }
        for(MARSTerm mars : best)
        {
            System.out.println(mars.toString());
        }
        if(rssChanged){
            copy(best, Formula);
        }
        return rssChanged;
    }
    
    private double TryHingePair(MARSTerm parent, List<Double> instance, int xrow, ArrayList<MARSTerm> form){
        double knot = instance.get(xrow);
        
        MARSTerm new1 = new MARSTerm(ComputeCoëff(Formula.get(0).Coëff, knot, xrow, false, parent), dataNames);
        CopyList(parent.NegHinge, new1.NegHinge);
        new1.NegHinge.add(false);
        CopyList(parent.Knot, new1.Knot);
        new1.Knot.add(knot);
        CopyList(parent.VarRow, new1.VarRow);
        new1.VarRow.add(xrow);
        form.add(new1);
        
        MARSTerm new2 = new MARSTerm(ComputeCoëff(Formula.get(0).Coëff, knot, xrow, true, parent), dataNames);
        CopyList(parent.NegHinge, new2.NegHinge);
        new2.NegHinge.add(true);
        CopyList(parent.Knot, new2.Knot);
        new2.Knot.add(knot);
        CopyList(parent.VarRow, new2.VarRow);
        new2.VarRow.add(xrow);
        form.add(new2);
        
        return ComputeRSS(form);
    }
    
    private double ComputeCoëff(double y, double x, int xrow, boolean neg, MARSTerm parent){
        double lower = 0;
        double upper = 0;
        int instAmt = InstanceValues.size();
        for(List<Double> inst : InstanceValues.values()){
            double a = 1;
            double b;
            if(neg)
                a *= (x - inst.get(xrow));
            else
                a *= (inst.get(xrow) - x);
            for(int i = 0; i < parent.NegHinge.size(); i++){
                if(parent.NegHinge.get(i))
                    a *= (parent.Knot.get(i) - inst.get(parent.VarRow.get(i)));
                else
                    a *= (inst.get(parent.VarRow.get(i)) - parent.Knot.get(i));
            }
            b = inst.get(INDEX_RESPONSE) - y;
            upper += a*b;
            lower += a*a;
        }
        upper = upper/(double)(instAmt);
        lower = lower/(double)(instAmt);
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
    
    private void copy(ArrayList<MARSTerm> one, ArrayList<MARSTerm> two){
        two.clear();
        for(MARSTerm term : one){
            two.add(term);
        }
    }
}

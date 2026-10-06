package com.jryoung.moneygraph.util;
import java.math.*;
import java.text.DecimalFormat;
public final class NumberFormatUtil{
 private NumberFormatUtil(){}
 public static String compact(BigInteger v){if(v==null)return"0";BigDecimal n=new BigDecimal(v);String[] s={"","K","M","B","T","Q"};int i=0;while(n.abs().compareTo(BigDecimal.valueOf(1000))>=0&&i<s.length-1){n=n.divide(BigDecimal.valueOf(1000),3,RoundingMode.HALF_UP);i++;}return i==0?v.toString():new DecimalFormat("0.##").format(n)+s[i];}
 public static String exact(BigInteger v){if(v==null)return"0";String x=v.toString(),o="";for(int i=0;i<x.length();i++){if(i>0&&(x.length()-i)%3==0)o+=',';o+=x.charAt(i);}return o;}
}
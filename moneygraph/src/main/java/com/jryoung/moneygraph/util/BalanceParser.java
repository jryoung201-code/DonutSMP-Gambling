package com.jryoung.moneygraph.util;
import java.math.*;
import java.util.Locale;
import java.util.regex.*;
public final class BalanceParser{
 private BalanceParser(){}
 private static final Pattern LABELED=Pattern.compile("(?i)(?:balance|bal|money|cash|coins?|purse|wallet)[^0-9$]{0,32}\\$?([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbtq])?");
 private static final Pattern CURRENCY=Pattern.compile("(?i)\\$([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbtq])?");
 private static final Pattern SUFFIXED=Pattern.compile("(?i)\\b([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbtq])\\b");
 public static BigInteger parse(String raw){if(raw==null||raw.isBlank())return null;String s=raw.replace('\u00A0',' ').trim();BigInteger x=parse(s,LABELED);if(x!=null)return x;x=parse(s,CURRENCY);return x!=null?x:parse(s,SUFFIXED);}
 private static BigInteger parse(String s,Pattern p){Matcher m=p.matcher(s);if(!m.find())return null;try{BigDecimal v=new BigDecimal(m.group(1).replace(",",""));String suf=m.groupCount()>1?m.group(2):null;if(suf!=null&&!suf.isBlank())v=v.multiply(mult(suf));return v.setScale(0,RoundingMode.HALF_UP).toBigIntegerExact();}catch(Exception e){return null;}}
 private static BigDecimal mult(String s){return switch(s.toLowerCase(Locale.ROOT)){case"k"->new BigDecimal("1000");case"m"->new BigDecimal("1000000");case"b"->new BigDecimal("1000000000");case"t"->new BigDecimal("1000000000000");case"q"->new BigDecimal("1000000000000000");default->BigDecimal.ONE;};}
}
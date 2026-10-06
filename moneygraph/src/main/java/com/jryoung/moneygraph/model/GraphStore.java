package com.jryoung.moneygraph.model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jryoung.moneygraph.MoneyGraphClient;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
public class GraphStore {
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private final Path file=FabricLoader.getInstance().getConfigDir().resolve("moneygraph.json");
 private List<GraphData> graphs=new ArrayList<>();
 private String selectedGraph="Main";
 public GraphStore(){load();if(graphs.isEmpty()){graphs.add(new GraphData("Main"));save();}}
 public synchronized void load(){try{if(!Files.exists(file))return;try(Reader r=Files.newBufferedReader(file,StandardCharsets.UTF_8)){SaveData d=GSON.fromJson(r,SaveData.class);if(d!=null){graphs=d.graphs==null?new ArrayList<>():d.graphs;selectedGraph=d.selectedGraph==null?"Main":d.selectedGraph;}}}catch(Exception e){MoneyGraphClient.LOGGER.error("MoneyGraph load failed",e);graphs=new ArrayList<>();}}
 public synchronized void save(){try{Files.createDirectories(file.getParent());SaveData d=new SaveData();d.graphs=graphs;d.selectedGraph=selectedGraph;Path t=file.resolveSibling(file.getFileName()+".tmp");try(Writer w=Files.newBufferedWriter(t,StandardCharsets.UTF_8)){GSON.toJson(d,w);}Files.move(t,file,StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){MoneyGraphClient.LOGGER.error("MoneyGraph save failed",e);}}
 public synchronized List<GraphData> getGraphs(){return Collections.unmodifiableList(new ArrayList<>(graphs));}
 public synchronized GraphData getSelected(){GraphData g=find(selectedGraph);if(g==null&&!graphs.isEmpty()){g=graphs.get(0);selectedGraph=g.getName();}return g;}
 public synchronized GraphData find(String name){if(name==null)return null;for(GraphData g:graphs)if(g.getName().equalsIgnoreCase(name))return g;return null;}
 public synchronized boolean create(String name){if(name==null||name.isBlank()||find(name)!=null)return false;graphs.add(new GraphData(name.trim()));selectedGraph=name.trim();save();return true;}
 public synchronized boolean delete(String name){GraphData g=find(name);if(g==null||graphs.size()<=1)return false;graphs.remove(g);if(selectedGraph.equalsIgnoreCase(g.getName()))selectedGraph=graphs.get(0).getName();save();return true;}
 public synchronized boolean select(String name){GraphData g=find(name);if(g==null)return false;selectedGraph=g.getName();save();return true;}
 public synchronized boolean clear(String name){GraphData g=find(name);if(g==null)return false;g.getPoints().clear();save();return true;}
 public synchronized boolean rename(String oldName,String newName){GraphData g=find(oldName);if(g==null||newName==null||newName.isBlank()||find(newName)!=null)return false;String old=g.getName();g.setName(newName.trim());if(selectedGraph.equalsIgnoreCase(old))selectedGraph=g.getName();save();return true;}
 public synchronized boolean removePoint(String name,int index){GraphData g=find(name);if(g==null||index<0||index>=g.getPoints().size())return false;g.getPoints().remove(index);save();return true;}
 public synchronized void addSelectedPoint(BigInteger balance,long timestamp){GraphData g=getSelected();if(g!=null){g.getPoints().add(new BalancePoint(balance,timestamp));save();}}
 private static final class SaveData{String selectedGraph;List<GraphData> graphs;}
}
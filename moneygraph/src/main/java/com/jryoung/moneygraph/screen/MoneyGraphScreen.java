package com.jryoung.moneygraph.screen;
import com.jryoung.moneygraph.MoneyGraphClient;
import com.jryoung.moneygraph.model.*;
import com.jryoung.moneygraph.util.NumberFormatUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.math.*;
import java.text.SimpleDateFormat;
import java.util.*;
public class MoneyGraphScreen extends Screen{
 private GraphData graph;private final Screen parent;private double zoom=1;
 public MoneyGraphScreen(Screen p){super(Component.literal("MoneyGraph"));parent=p;}
 @Override protected void init(){graph=MoneyGraphClient.INSTANCE.getStore().getSelected();int y=height-28;addRenderableWidget(Button.builder(Component.literal("Delete"),b->del()).bounds(width-310,y,70,20).build());addRenderableWidget(Button.builder(Component.literal("Clear"),b->clear()).bounds(width-234,y,70,20).build());addRenderableWidget(Button.builder(Component.literal("Reset"),b->{zoom=1;}).bounds(width-158,y,70,20).build());addRenderableWidget(Button.builder(Component.literal("Done"),b->close()).bounds(width-82,y,70,20).build());}
 private void del(){if(graph!=null){MoneyGraphClient.INSTANCE.getStore().delete(graph.getName());graph=MoneyGraphClient.INSTANCE.getStore().getSelected();}}
 private void clear(){if(graph!=null){MoneyGraphClient.INSTANCE.getStore().clear(graph.getName());}}
 private void close(){if(minecraft!=null)minecraft.setScreen(parent);}
 @Override public void onClose(){close();}
 @Override public void render(GuiGraphics g,int mx,int my,float d){renderBackground(g,mx,my,d);if(graph==null){super.render(g,mx,my,d);return;}g.drawString(font,"MoneyGraph",18,18,0xFF55FFFF,true);g.drawString(font,"Graph: "+graph.getName(),18,34,0xFFFFFFFF,false);List<BalancePoint> p=graph.getPoints();if(p.isEmpty()){g.drawCenteredString(font,"Run /bal to add your first point",width/2,height/2,0xFFAAAAAA);super.render(g,mx,my,d);return;}BigInteger cur=p.get(p.size()-1).getBalance();g.drawString(font,"Current: "+NumberFormatUtil.compact(cur),150,34,0xFFFFFFFF,false);BigInteger min=p.get(0).getBalance(),max=min;for(BalancePoint q:p){if(q.getBalance().compareTo(min)<0)min=q.getBalance();if(q.getBalance().compareTo(max)>0)max=q.getBalance();}double lo=new BigDecimal(min).doubleValue(),hi=new BigDecimal(max).doubleValue();if(lo==hi){lo--;hi++;}double pad=(hi-lo)*.08;lo-=pad;hi+=pad;int l=55,t=68,r=width-35,b=height-48;g.fill(l,t,r,b,0xB0101014);for(int i=0;i<=5;i++){double f=i/5d;int y=(int)(b-(b-t)*f);g.fill(l,y,r,y+1,0x33222222);g.drawString(font,NumberFormatUtil.compact(BigDecimal.valueOf(lo+(hi-lo)*f).setScale(0,RoundingMode.HALF_UP).toBigInteger()),8,y-4,0xFF888888,false);}List<P> pts=new ArrayList<>();double span=Math.max(1,p.size()-1)*zoom;for(int i=0;i<p.size();i++){double x=l+5+(i/span)*(r-l-10),v=new BigDecimal(p.get(i).getBalance()).doubleValue(),y=b-5-((v-lo)/(hi-lo))*(b-t-10);pts.add(new P((int)x,(int)y));}for(int i=1;i<pts.size();i++)line(g,pts.get(i-1),pts.get(i));for(int i=0;i<pts.size();i++){P q=pts.get(i);g.fill(q.x-3,q.y-3,q.x+4,q.y+4,0xFFFFFFFF);if(mx>=q.x-5&&mx<=q.x+5&&my>=q.y-5&&my<=q.y+5){BalancePoint bp=p.get(i);String dt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(bp.getTimestamp()));g.renderComponentTooltip(font,List.of(Component.literal("Point #"+i),Component.literal("Balance: "+NumberFormatUtil.exact(bp.getBalance())),Component.literal(dt)),mx,my);}}super.render(g,mx,my,d);}
 private void line(GuiGraphics g,P a,P z){int dx=Math.abs(z.x-a.x),dy=Math.abs(z.y-a.y),sx=a.x<z.x?1:-1,sy=a.y<z.y?1:-1,e=dx-dy,x=a.x,y=a.y;while(true){g.fill(x,y,x+2,y+2,0xFF55FFFF);if(x==z.x&&y==z.y)break;int q=2*e;if(q>-dy){e-=dy;x+=sx;}if(q<dx){e+=dx;y+=sy;}}}
 @Override public boolean mouseScrolled(double x,double y,double h,double v){zoom=Math.max(.25,Math.min(8,zoom*(v>0?1.15:.87)));return true;}
 private record P(int x,int y){}
}
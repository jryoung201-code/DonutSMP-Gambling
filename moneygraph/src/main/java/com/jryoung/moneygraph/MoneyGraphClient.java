package com.jryoung.moneygraph;
import com.jryoung.moneygraph.model.*;
import com.jryoung.moneygraph.screen.MoneyGraphScreen;
import com.jryoung.moneygraph.util.BalanceParser;
import com.mojang.brigadier.arguments.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Locale;
public class MoneyGraphClient implements ClientModInitializer{
 public static final String MOD_ID="moneygraph";
 public static final Logger LOGGER=LoggerFactory.getLogger(MOD_ID);
 public static MoneyGraphClient INSTANCE;
 private GraphStore store; private KeyMapping openKey; private long waitingForBalUntil;
 @Override public void onInitializeClient(){INSTANCE=this;store=new GraphStore();openKey=KeyBindingHelper.registerKeyBinding(new KeyMapping("key.moneygraph.open",InputConstants.Type.KEYSYM,InputConstants.KEY_F7,KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID,"controls"))));registerCommands();ClientSendMessageEvents.COMMAND.register(c->{String n=c.trim().toLowerCase(Locale.ROOT);if(n.equals("bal")||n.startsWith("bal "))waitingForBalUntil=System.currentTimeMillis()+7500;});ClientReceiveMessageEvents.GAME.register((m,o)->tryCapture(m.getString()));ClientReceiveMessageEvents.CHAT.register((m,sender)->tryCapture(m.getString()));ClientTickEvents.END_CLIENT_TICK.register(c->{while(openKey.consumeClick())if(c.screen==null)c.setScreen(new MoneyGraphScreen(null));if(waitingForBalUntil>0&&System.currentTimeMillis()>waitingForBalUntil)waitingForBalUntil=0;});LOGGER.info("MoneyGraph 1.21.11 initialized");}
 private void tryCapture(String message){if(waitingForBalUntil<=0)return;if(System.currentTimeMillis()>waitingForBalUntil){waitingForBalUntil=0;return;}BigInteger b=BalanceParser.parse(message);if(b==null)return;store.addSelectedPoint(b,Instant.now().toEpochMilli());waitingForBalUntil=0;Minecraft c=Minecraft.getInstance();if(c.player!=null){GraphData g=store.getSelected();c.player.displayClientMessage(Component.literal("§aMoneyGraph: added "+b+" to "+g.getName()),false);}}
 private void registerCommands(){ClientCommandRegistrationCallback.EVENT.register((d,r)->d.register(ClientCommandManager.literal("moneygraph").executes(c->openScreen())
 .then(ClientCommandManager.literal("create").then(ClientCommandManager.argument("name",StringArgumentType.greedyString()).executes(c->{String n=StringArgumentType.getString(c,"name").trim();boolean ok=store.create(n);feedback(ok?"Created graph: "+n:"Could not create graph.");return ok?1:0;})))
 .then(ClientCommandManager.literal("delete").then(ClientCommandManager.argument("name",StringArgumentType.greedyString()).executes(c->{String n=StringArgumentType.getString(c,"name").trim();boolean ok=store.delete(n);feedback(ok?"Deleted graph: "+n:"Could not delete graph.");return ok?1:0;})))
 .then(ClientCommandManager.literal("select").then(ClientCommandManager.argument("name",StringArgumentType.greedyString()).executes(c->{String n=StringArgumentType.getString(c,"name").trim();boolean ok=store.select(n);feedback(ok?"Selected graph: "+n:"Graph not found: "+n);return ok?1:0;})))
 .then(ClientCommandManager.literal("clear").then(ClientCommandManager.argument("name",StringArgumentType.greedyString()).executes(c->{String n=StringArgumentType.getString(c,"name").trim();boolean ok=store.clear(n);feedback(ok?"Cleared graph: "+n:"Graph not found: "+n);return ok?1:0;})))
 .then(ClientCommandManager.literal("rename").then(ClientCommandManager.argument("oldName",StringArgumentType.string()).then(ClientCommandManager.argument("newName",StringArgumentType.greedyString()).executes(c->{String a=StringArgumentType.getString(c,"oldName").trim(),b=StringArgumentType.getString(c,"newName").trim();boolean ok=store.rename(a,b);feedback(ok?"Renamed graph to "+b:"Could not rename graph.");return ok?1:0;}))))
 .then(ClientCommandManager.literal("remove-point").then(ClientCommandManager.argument("graph",StringArgumentType.string()).then(ClientCommandManager.argument("index",IntegerArgumentType.integer(0)).executes(c->{String g=StringArgumentType.getString(c,"graph");int i=IntegerArgumentType.getInteger(c,"index");boolean ok=store.removePoint(g,i);feedback(ok?"Removed point #"+i:"Could not remove point.");return ok?1:0;}))))
 .then(ClientCommandManager.literal("list").executes(c->{feedback("Graphs: "+store.getGraphs().stream().map(GraphData::getName).reduce((a,b)->a+", "+b).orElse("none"));return 1;}))
 .then(ClientCommandManager.literal("help").executes(c->{feedback("/moneygraph | create <name> | delete <name> | select <name> | clear <name> | rename <old> <new> | remove-point <graph> <index> | list");return 1;}))));}
 private int openScreen(){Minecraft.getInstance().setScreen(new MoneyGraphScreen(null));return 1;}
 private void feedback(String t){Minecraft c=Minecraft.getInstance();if(c.player!=null)c.player.displayClientMessage(Component.literal("§bMoneyGraph§7: "+t),false);}
 public GraphStore getStore(){return store;}
}
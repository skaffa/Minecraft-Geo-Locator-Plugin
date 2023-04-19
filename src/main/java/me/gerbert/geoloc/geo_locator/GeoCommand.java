package me.gerbert.geoloc.geo_locator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class GeoCommand implements CommandExecutor {
    private final Geo_locator plugin;


    public GeoCommand(Geo_locator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /geo <ip | player>");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player != null) {
            try {
                InetAddress address = player.getAddress().getAddress();
                String ipAddress = address.getHostAddress();
                String username = sender.getName();
                geoInfo(username, ipAddress, player);
            } catch (NullPointerException e) {
                sender.sendMessage("Something went wrong");
                return true;
            }
        } else {
            String[] ipDigits = args[0].split("\\.");
            int ipLength = ipDigits.length;
            if (ipLength == 4) {
                for (String digit : ipDigits) {
                    try {
                        int d = Integer.parseInt(digit);
                        if (!(d >= 0 && d <= 255)) {
                            invalidData(sender);
                            return true;
                        }
                    } catch (NumberFormatException nfe) {
                        invalidData(sender);
                        return true;
                    }
                }
                pingIp(sender, args[0]);
                return true;
            } else {
                invalidData(sender);
                return true;
            }
        }

        return true;
    }

    private void pingIp(CommandSender sender, String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            boolean isReachable = address.isReachable(10000);
            if (isReachable) {
                geoInfo("IP", ip, sender);
            } else {
                sender.sendMessage(ChatColor.RED + "IP is not reachable or timed out");
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Something went wrong, please try again. Or don't");
        }
    }

    private void geoInfo(String target, String ip, CommandSender sender) {
        String apiUrl = "http://ip-api.com/json/{query}?fields=status,message,continent,continentCode,country,countryCode,region,regionName,city,district,zip,lat,lon,timezone,isp,org,as,asname,reverse,mobile,proxy,hosting,query";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl.replace("{query}", ip)))
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response != null) {
            int statusCode = response.statusCode();
            String responseBody = response.body();

            System.out.println("Status code: " + statusCode);
            System.out.println("Response body: " + responseBody);

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);

            String prefix = "§b -";

            StringBuilder message = new StringBuilder();
            message.append("§bGeolocation for §3").append(target).append("\n")
                    .append(prefix).append("Status: §3").append(json.get("status").getAsString()).append("\n")
                    .append(prefix).append("Query: §3").append(json.get("query").getAsString()).append("\n")
                    .append(prefix).append("continent: §3").append(json.get("continent").getAsString()).append("\n")
                    .append(prefix).append("continentCode: §3").append(json.get("continentCode").getAsString()).append("\n")
                    .append(prefix).append("country: §3").append(json.get("country").getAsString()).append("\n")
                    .append(prefix).append("country Code: §3").append(json.get("countryCode").getAsString()).append("\n")
                    .append(prefix).append("region: §3").append(json.get("region").getAsString()).append("\n")
                    .append(prefix).append("regionName: §3").append(json.get("regionName").getAsString()).append("\n")
                    .append(prefix).append("city: §3").append(json.get("city").getAsString()).append("\n")
                    .append(prefix).append("district: §3").append(json.get("district").getAsString()).append("\n")
                    .append(prefix).append("zip: §3").append(json.get("zip").getAsString()).append("\n")
                    .append(prefix).append("lat: §3").append(json.get("lat").getAsDouble()).append("\n")
                    .append(prefix).append("lon: §3").append(json.get("lon").getAsDouble()).append("\n")
                    .append(prefix).append("Timezone: §3").append(json.get("timezone").getAsString()).append("\n")
                    .append(prefix).append("ISP: §3").append(json.get("isp").getAsString()).append("\n")
                    .append(prefix).append("Org: §3").append(json.get("org").getAsString()).append("\n")
                    .append(prefix).append("as: §3").append(json.get("as").getAsString()).append("\n")
                    .append(prefix).append("asname: §3").append(json.get("asname").getAsString()).append("\n")
                    .append(prefix).append("Reverse: §3").append(json.get("reverse").getAsString()).append("\n")
                    .append(prefix).append("Mobile: §3").append(json.get("mobile").getAsBoolean()).append("\n")
                    .append(prefix).append("Proxy: §3").append(json.get("proxy").getAsBoolean()).append("\n")
                    .append(prefix).append("Hosting: §3").append(json.get("hosting").getAsBoolean()).append("\n");
            sender.sendMessage(message.toString());


        } else {
            sender.sendMessage("Something went wrong during the API call.");
        }

    }

    private void invalidData(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid player name or IP address!");
    }
}

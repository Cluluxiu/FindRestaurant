
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindMiddlePointInDrive {
    private static int SPEED_FIRST = 0;
    private static int COST_FIRST = 1;
    private static int DISTANCE_FIRST = 2;
//    private static int COST_FIRST = 3;
//    private static int COST_FIRST = 4;
    private static int MULTI_STRATEGY = 5;

    public static String findMiddlePoint() {
        // todo 地点由调用处传入
        String origin = "116.178778,40.048224"; // 起点坐标
        String destination = "116.178401,39.925789"; // 终点坐标
        String key = "xxxxxxxx"; // 高德地图API密钥

        // 第一步：使用高德地图API的路径规划功能，获取两个地点之间的驾车导航路线
        String urlStr = "https://restapi.amap.com/v3/direction/driving?" +
                "origin=" + URLEncoder.encode(origin) +
                "&destination=" + URLEncoder.encode(destination)  + "&strategy=" + COST_FIRST +
                "&key=" + key;
        String result = sendGetRequest(urlStr);
        System.out.println(result);

        // 第二步：解析路径规划的结果，得到一条包含多个经纬度坐标点的路径
        JSONObject resultJSON = JSON.parseObject(result);
        JSONArray pathJSON = resultJSON.getJSONObject("route").getJSONArray("paths").getJSONObject(0).getJSONArray("steps");
        List<String> pathList = new ArrayList<String>();
        for (int i = 0; i < pathJSON.size(); i++) {
            String polylineJSON = pathJSON.getJSONObject(i).getString("polyline");
            if (polylineJSON == null || polylineJSON.isEmpty()) {
                continue;
            }
            String[] split = polylineJSON.split(";");

            pathList.addAll(Arrays.asList(split));
        }

        // 第三步：计算路径上每一段的长度，以及整条路径的总长度
        double totalDistance = 0.0;
        List<Double> distanceList = new ArrayList<Double>();
        for (int i = 0; i < pathList.size() - 1; i++) {
            String[] p1 = pathList.get(i).split(",");
            String[] p2 = pathList.get(i + 1).split(",");
            double distance = getDistance(Double.parseDouble(p1[1]), Double.parseDouble(p1[0]), Double.parseDouble(p2[1]), Double.parseDouble(p2[0]));
            totalDistance += distance;
            distanceList.add(distance);
        }

        // 第四步：遍历整条路径，累加每一段的长度，直到累加长度达到路径总长度的一半
        double halfDistance = totalDistance / 2.0;
        double currentDistance = 0.0;
        String midpoint = "";
        for (int i = 0; i < distanceList.size(); i++) {
            double distance = distanceList.get(i);
            if (currentDistance + distance >= halfDistance) {
                double ratio = (halfDistance - currentDistance) / distance;
                String[] p1 = pathList.get(i).split(",");
                String[] p2 = pathList.get(i + 1).split(",");
                double lat = Double.parseDouble(p1[1]) + ratio * (Double.parseDouble(p2[1]) - Double.parseDouble(p1[1]));
                double lng = Double.parseDouble(p1[0]) + ratio * (Double.parseDouble(p2[0]) - Double.parseDouble(p1[0]));
                midpoint = lng + "," + lat;
                break;
            } else {
                currentDistance += distance;
            }
        }

        // 第五步：输出驾车导航路线上的中间点
        System.out.println("驾车导航路线上的中间点：" + midpoint);
        return midpoint;
    }

    // 计算两个经纬度坐标点之间的距离（单位：米）
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 地球半径，单位为米
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double radLng1 = Math.toRadians(lng1);
        double radLng2 = Math.toRadians(lng2);
        double a = Math.sin((radLat1 - radLat2) / 2) * Math.sin((radLat1 - radLat2) / 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin((radLng1 - radLng2) / 2) * Math.sin((radLng1 - radLng2) / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        return distance;
    }

    // 发送HTTP GET请求，并返回响应结果
    public static String sendGetRequest(String urlStr) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void main(String[] args) {
        findMiddlePoint();
    }
}

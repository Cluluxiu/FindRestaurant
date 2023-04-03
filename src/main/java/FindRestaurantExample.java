import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据输入的两个坐标地点，查找两个地点中间附近的餐厅
 */
public class FindRestaurantExample {

    private static final String API_KEY = "xxxxxxxx"; //替换成你的API Key
    private static final String BASE_URL = "https://restapi.amap.com/v3/place/around";

    private static boolean isUseDriveMiddle = true;

    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 第一个地点的经纬度
        String location1 = "116.178778,40.048224";
        // 第二个地点的经纬度
        String location2 = "116.178401,39.925789";

        // 计算两个地点之间的距离
        double distance = calculateDistance(location1, location2);

        // todo 中心点选择策略
        // 查找距离两个地点的中心点10公里范围内的餐厅
        String center = isUseDriveMiddle ? FindMiddlePointInDrive.findMiddlePoint() : getCenter(location1, location2);
        // todo 餐厅按评分优先排序
        List<String> restaurants = searchRestaurants(center, 5000);

        System.out.printf("距离两个地点之间的距离为：%.2f千米\n", distance / 1000);
        System.out.printf("在距离两个地点中心点5千米范围内，共找到%d家餐厅：\n", restaurants.size());
        for (String restaurant : restaurants) {
            System.out.println(restaurant);
        }
    }

    /**
     * 计算两个地点之间的距离（单位：米）
     */
    private static double calculateDistance(String location1, String location2) throws IOException {
        String url = "https://restapi.amap.com/v3/distance?key=" + API_KEY + "&origins=" + location1 + "&destination=" + location2;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String json = response.body().string();
            JSONObject jsonObject = JSONObject.parseObject(json);
            JSONArray results = jsonObject.getJSONArray("results");
            JSONObject distanceObj = results.getJSONObject(0);
            return distanceObj.getDoubleValue("distance");
        }
    }

    /**
     * 查找指定位置周围距离一定范围内的餐厅
     */
    private static List<String> searchRestaurants(String location, int radius) throws IOException {
        System.out.println("middle location is " + location);
        List<String> result = new ArrayList<>();
        String url = BASE_URL + "?key=" + API_KEY + "&location=" + location + "&radius=" + radius + "&types=050000&offset=20&page=1&extensions=all";

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String json = response.body().string();
            JSONObject jsonObject = JSONObject.parseObject(json);
            JSONArray pois = jsonObject.getJSONArray("pois");
            for (int i = 0; i < pois.size(); i++) {
                JSONObject poi = pois.getJSONObject(i);
                String name = poi.getString("name");
                String address = poi.getString("address");
                result.add(name + "，地址：" + address);
            }
        }

        return result;
    }

    /**
     * 计算两个经纬度之间的中心点
     */
    private static String getCenter(String location1, String location2) {
        double lat1 = Double.parseDouble(location1.split(",")[1]);
        double lon1 = Double.parseDouble(location1.split(",")[0]);
        double lat2 = Double.parseDouble(location2.split(",")[1]);
        double lon2 = Double.parseDouble(location2.split(",")[0]);

        double lat = (lat1 + lat2) / 2;
        double lon = (lon1 + lon2) / 2;

        return lon + "," + lat;
    }
}

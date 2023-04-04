package com.luchi.middleplace.center;

public class FindMiddlePointInLocation {
    /**
     * 计算两个经纬度之间的中心点
     */
    public static String getCenter(String location1, String location2) {
        double lat1 = Double.parseDouble(location1.split(",")[1]);
        double lon1 = Double.parseDouble(location1.split(",")[0]);
        double lat2 = Double.parseDouble(location2.split(",")[1]);
        double lon2 = Double.parseDouble(location2.split(",")[0]);

        double lat = (lat1 + lat2) / 2;
        double lon = (lon1 + lon2) / 2;

        return lon + "," + lat;
    }
}

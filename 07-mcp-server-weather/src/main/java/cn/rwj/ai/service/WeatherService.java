package cn.rwj.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class WeatherService {

    @Tool(description = "获取指定城市的天气信息")
    public String getCityWeather(String city) {
        // 模拟天气查询逻辑
        return Map.of(
                "city", city,
                "weather", "下雪～～天～～～",
                "temperature", "20-28°C"
        ).toString();
    }

    @Tool(description = "获取指定城市的当前时间")
    public String getCityTime(String city) {
        // 模拟时间查询逻辑
        return city + "的当前时间是：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

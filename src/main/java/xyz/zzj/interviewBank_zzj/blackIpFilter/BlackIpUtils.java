package xyz.zzj.interviewBank_zzj.blackIpFilter;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

/**
 * @BelongsPackage: xyz.zzj.interviewBank_zzj.blackIpFilter
 * @ClassName: BlackIpUtils
 * @Author: zengz
 * @CreateTime: 2025/2/24 23:57
 * @Description: 黑名单过滤器
 * @Version: 1.0
 */
public class BlackIpUtils {

    private static BitMapBloomFilter bloomFilter;

    //判断ip是否在黑名单中
    public static boolean isBlackIp(String ip) {
        return bloomFilter.contains(ip);
    }

    //重建ip黑名单
    public static void rebuildBlackIp(String configInfo) {
        if(StrUtil.isBlank(configInfo)){
            configInfo = "{}";
        }
        //解析yaml文件
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(configInfo, Map.class);
        List<String> blackIpList = (List<String>) map.get("blackIpList");

        //构造布隆过滤器
        //加锁防止并发
        synchronized(BlackIpUtils.class){
            if (CollectionUtil.isNotEmpty(blackIpList)) {
                // 注意构造参数的设置
                BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(958506);
                for (String ip : blackIpList) {
                    bitMapBloomFilter.add(ip);
                }
                bloomFilter = bitMapBloomFilter;
            } else {
                bloomFilter = new BitMapBloomFilter(100);
            }
        }
    }
}

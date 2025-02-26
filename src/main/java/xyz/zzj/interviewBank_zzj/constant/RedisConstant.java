package xyz.zzj.interviewBank_zzj.constant;

/**
 * @BelongsProject: interviewBank_zzj
 * @BelongsPackage: xyz.zzj.interviewBank_zzj.constant
 * @Author: zengz
 * @CreateTime: 2025/2/4 17:18
 * @Description: TODO 描述类的功能
 * @Version: 1.0
 */
public interface RedisConstant {

    //用户签到记录的 Redis key 的前缀
    String USER_SIGN_KEY_PREFIX = "user:signs";

    /**
     * 获取用户签到记录的 Redis key
     * @param year 年份
     * @param userId 用户id
     * @return
     */
    static String getUserSignKey(int year, long userId){
        return String.format("%s:%s:%s", USER_SIGN_KEY_PREFIX, year, userId);
    }

}

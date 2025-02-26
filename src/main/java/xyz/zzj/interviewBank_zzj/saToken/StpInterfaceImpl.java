package xyz.zzj.interviewBank_zzj.saToken;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;
import xyz.zzj.interviewBank_zzj.model.entity.User;

import java.util.Collections;
import java.util.List;

import static xyz.zzj.interviewBank_zzj.constant.UserConstant.USER_LOGIN_STATE;

@Component // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合 (目前没用)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String s) {
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String s) {
        // 从当前登录用户信息中获取角色
        Object userObj = StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        User user = (User) userObj;
        return Collections.singletonList(user.getUserRole());
    }
}
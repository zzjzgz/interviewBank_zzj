package xyz.zzj.interviewBank_zzj.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import xyz.zzj.interviewBank_zzj.common.ErrorCode;
import xyz.zzj.interviewBank_zzj.constant.CommonConstant;
import xyz.zzj.interviewBank_zzj.constant.RedisConstant;
import xyz.zzj.interviewBank_zzj.exception.BusinessException;
import xyz.zzj.interviewBank_zzj.mapper.UserMapper;
import xyz.zzj.interviewBank_zzj.mapper.UserSignMapper;
import xyz.zzj.interviewBank_zzj.model.dto.user.UserQueryRequest;
import xyz.zzj.interviewBank_zzj.model.entity.User;
import xyz.zzj.interviewBank_zzj.model.entity.UserSign;
import xyz.zzj.interviewBank_zzj.model.enums.UserRoleEnum;
import xyz.zzj.interviewBank_zzj.model.vo.LoginUserVO;
import xyz.zzj.interviewBank_zzj.model.vo.UserVO;
import xyz.zzj.interviewBank_zzj.saToken.DeviceUtils;
import xyz.zzj.interviewBank_zzj.service.UserService;
import xyz.zzj.interviewBank_zzj.utils.SqlUtils;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import xyz.zzj.interviewBank_zzj.constant.UserConstant;

import static xyz.zzj.interviewBank_zzj.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
  * @author zzj
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserSignMapper userSignMapper;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "zzjny";



    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态,并指定设备
        StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
        //记录登录态
        StpUtil.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object loginIdD = StpUtil.getLoginIdDefaultNull();
        if (loginIdD == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        User currentUser = this.getById((String)loginIdD);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object loginIdDefaultNull = StpUtil.getLoginIdDefaultNull();
        if (loginIdDefaultNull == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User currentUser = new User();
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = StpUtil.getSession().get(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {

        StpUtil.checkLogin();

        //移除用户当前登录设备的登录态
        Object loginId = StpUtil.getLoginIdDefaultNull();
        String loginDevice = StpUtil.getLoginDevice();
        StpUtil.logout(loginId, loginDevice);

//        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
//        }
//        // 移除登录态
//        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 增加用户签到记录
     * @param userId 用户id
     * @return true 成功 false 失败
     */
    @Override
    public boolean addUserSign(long userId) {
        // 获取当前日期
        LocalDate date = LocalDate.now();
        // 拼接签到记录的key
        String key = RedisConstant.getUserSignKey(date.getYear(), userId);
        // 获取redis中的bitset
        RBitSet bitSet = redissonClient.getBitSet(key);
        //获取当前日期的第几天，从1开始
        int dayOfYear = date.getDayOfYear();
        if(!bitSet.get(dayOfYear)){
            //当天未签到
            bitSet.set(dayOfYear,true);
        }
        // 当天已签到
        return true;
    }

    @Override
    public List<Integer> selectSignRecord(long userId, Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        int currentYear = LocalDate.now().getYear();
        if (year != currentYear) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "年份不是当前年份");
        }

//        //数据库查询签到记录
//        QueryWrapper<UserSign> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("userId", userId);
//        queryWrapper.select("signDay");
//        List<Integer> result = userSignMapper.selectObjs(queryWrapper)
//                .stream()
//                .filter(Objects::nonNull) // 确保过滤掉可能的 null 值
//                .map(item -> Integer.parseInt(item.toString())) // 转换为 Integer
//                .collect(Collectors.toList());


        //使用redis查询签到记录
        // 拼接签到记录的key
        String key = RedisConstant.getUserSignKey(year, userId);
        // 获取redis中的bitset
        RBitSet bitSet = redissonClient.getBitSet(key);
        // 获取bitset中的所有值
        BitSet signInBitSet = bitSet.asBitSet();
        // 构造返回结果，保证返回值有序
        List<Integer> result = new ArrayList<>();

        // 获取当前bitset中已签到的天数
        // 从第一个被设置为1的位置开始遍历，直到没有被设置为1的位置
        int index = signInBitSet.nextSetBit(0);
        while (index >= 0) {
            result.add(index);
            // 获取下一个被设置为1的位置
            index = signInBitSet.nextSetBit(index + 1);
        }
        return result;
    }
}

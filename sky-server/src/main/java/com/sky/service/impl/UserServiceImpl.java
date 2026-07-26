package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final static String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        log.info("开始微信登录，接收到的参数: {}", userLoginDTO);
        log.info("微信登录 code: {}", userLoginDTO.getCode());

        // 检查 code 是否为空
        if(userLoginDTO.getCode() == null || userLoginDTO.getCode().trim().isEmpty()) {
            log.error("微信登录失败：code 参数为空，请检查前端是否正确传递了 code");
            throw new LoginFailedException("登录凭证 code 不能为空，请检查前端代码");
        }

        //调用微信接口服务获取openid
        String openid = getOpenid(userLoginDTO.getCode());
        log.info("从微信接口获取到的 openid: {}", openid);

        //判断用户的openid是否为空，为空就代表用户没有登录，抛出异常
        if(openid == null) {
            log.error("微信登录失败：获取 openid 为空，可能是 code 无效或微信接口返回错误");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断用户是否为新用户
        User user = userMapper.selectByOpenid(openid);
        //如果是新用户，就注册用户，否则就返回用户
        if(user == null) {
            log.info("新用户登录，开始注册用户信息，openid: {}", openid);
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            log.info("新用户注册成功，userId: {}", user.getId());
        } else {
            log.info("老用户登录，userId: {}", user.getId());
        }
        //返回用户
        return user;
    }

    /**
     * 调用微信接口服务，获取微信用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        //调用微信接口服务，获得当前微信用户的openid
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
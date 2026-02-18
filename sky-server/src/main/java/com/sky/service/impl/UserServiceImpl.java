package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class UserServiceImpl implements UserService{

    @Resource
    WeChatProperties weChatProperties;

    @Resource
    UserMapper userMapper;

    @Resource
    JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) throws LoginFailedException{

        String js_code = userLoginDTO.getCode();
        // 向微信端发送请求获取openid
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("appid", weChatProperties.getAppid());
        paraMap.put("secret", weChatProperties.getSecret());
        paraMap.put("js_code", js_code);
        paraMap.put("grant_code", "authorized_code");

        String stringRes = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", paraMap);
        JSONObject jsonObject = JSON.parseObject(stringRes);
        String openid = jsonObject.getString("openid");
        // 是否有openid
        if(openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 从数据中对比是否存在openid，没有就添加用户，有就直接返回
        User user = userMapper.getUserByOpenid(openid);
        if(user == null) {
            user = User.builder()
                            .openid(openid)
                            .createTime(LocalDateTime.now()).build();

            //是否需要补充其它信息？ 后续用户再补充。
            userMapper.insert(user);
        }

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .openid(openid).build();
        return userLoginVO;

    }
}

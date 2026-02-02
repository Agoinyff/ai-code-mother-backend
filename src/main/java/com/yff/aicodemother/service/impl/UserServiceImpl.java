package com.yff.aicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import com.yff.aicodemother.model.entity.User;
import com.yff.aicodemother.mapper.UserMapper;
import com.yff.aicodemother.model.enums.UserRoleEnum;
import com.yff.aicodemother.model.vo.LoginVo;
import com.yff.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户 服务层实现。
 *
 * @author yff
 * @since 2026-02-02 15:36:30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Value("${password.salt}")
    private String SALT;


    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {

        //1.校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        //2.检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        //3。对秘密进行加密
        String encryptPassword = getEncryptedPassword(userPassword);

        //4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();


    }

    @Override
    public LoginVo login(String userAccount, String userPassword) {

        //1.校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        //2.加密
        String encryptPassword = getEncryptedPassword(userPassword);

        //查询用户是否存在

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        //TODO 创建jwt，存redis



        //TODO 数据脱敏返回

        return null;

    }

    public String getEncryptedPassword(String userPassword) {

        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}

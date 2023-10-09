package com.example.service.impl;

import com.example.entity.auth.Account;
import com.example.mapper.UserMapper;
import com.example.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {
    @Resource
    UserMapper mapper;

    @Value("${spring.mail.username}")
    String from;

    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate template;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username == null) {

            throw new UsernameNotFoundException("用户名不存在");
        }
        Account account = mapper.findAccountByUsernameOrEmail(username);

        if (account == null) {

            throw new UsernameNotFoundException("用户不存在 或密码错误");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    @Override
    public String sendValidateEmail(String email, String sessionId,boolean hasAccount) {
        String key = "email:" + sessionId + ":" + email+":"+hasAccount;
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            //获取key的过期时间
            Long expire = Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if (expire > 120) {
                return "try later";
            }
        }
        Account account = mapper.findAccountByUsernameOrEmail(email);
        if(hasAccount && account == null) return "没有此邮件地址的账户";
        if(!hasAccount && account != null) return "此邮箱已被其他用户注册";
        //获取key的过期时间，如果没有则返回0      }
        if (mapper.findAccountByUsernameOrEmail(email) != null) {
            return "该邮箱已注册";
        }

        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setSubject("验证码");
        message.setText("您的验证码为" + code + "，有效期为3分钟，请及时验证");
        message.setTo(email);

        try {
            mailSender.send(message);
            //设置key的值为code，有效期为3分钟
            template.opsForValue().set(key, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        } catch (MailException ee) {
            ee.printStackTrace();
            return "oops,email deliver failed";
        }

    }

    @Override
    public String validateAndRegister(String username, String password, String email, String code, String sessionId) {
        String key = "email:" + sessionId + ":" + email +":false";
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            String s = template.opsForValue().get(key);
            if(s==null) return "Verification code expired";
            if(s.equals(code)){
                template.delete(key);
                password = encoder.encode(password);
                if(mapper.createAccount(username,password,email)>0){
                    return null;
                }else{
                    return "Internal error,please contact with administrate";
                }
            }else{
                return "wrong code,check it and submit then";
            }
        }else {
            return "please request a verification code email first";
        }
    }

    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email:" + sessionId + ":" + email + ":true";
        if(Boolean.TRUE.equals(template.hasKey(key))) {
            String s = template.opsForValue().get(key);
            if(s == null) return "验证码失效，请重新请求";
            if(s.equals(code)) {
                template.delete(key);
                return null;
            } else {
                return "验证码错误，请检查后再提交";
            }
        } else {
            return "请先请求一封验证码邮件";
        }
    }

    @Override
    public boolean resetPassword(String password, String email) {
        password = encoder.encode(password);
        return mapper.resetPasswordByEmail(password, email) > 0;
    }

}



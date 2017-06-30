package com.sandnul.config;

import com.sandnul.security.MyCustomUserService;
import com.sandnul.util.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/28
 * TIME: 11:50
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final static Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    /**
     * 通过 实现UserDetailService 来进行验证
     */
    @Autowired
    private MyCustomUserService myCustomUserService;

    /**
     *
     * @param auth
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{

        //校验用户
        auth.userDetailsService(myCustomUserService)
                //校验密码
                .passwordEncoder(new PasswordEncoder() {

            @Override
            public String encode(CharSequence rawPassword) {
                return Md5Util.MD5(String.valueOf(rawPassword));
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(Md5Util.MD5(String.valueOf(rawPassword)));
            }
        });

    }


    /**
     * 创建自定义的表单
     *
     * 页面、登录请求、跳转页面等
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/","index","/login","/css/**","/js/**")//允许访问
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")//拦截后get请求跳转的页面
                .defaultSuccessUrl("/hello")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }
}

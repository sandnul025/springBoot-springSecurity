package com.sandnul.security;

import com.sandnul.domain.SysRole;
import com.sandnul.domain.SysUser;
import com.sandnul.repository.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/28
 * TIME: 14:18
 */
@Component
public class MyCustomUserService implements UserDetailsService{


    private final static Logger logger = LoggerFactory.getLogger(MyCustomUserService.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 通过验证 将用的所有角色 用户信息中
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.info("根据名称获取用户信息： username is {}",username);

        SysUser user = sysUserMapper.findUserByUsername(username);
        if(user == null)
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));

        //获取所有请求的url
        //List<SysPermission> sysPermissions = sysUserMapper.findPermissionsByUsername(user.getUsername());
        List<SysRole> sysRoles = sysUserMapper.findRolesByUsername(user.getUsername());

        logger.info("用户角色个数为{}",sysRoles.size());
        logger.info("--------------all Roles--------------");
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (SysRole sysRole : sysRoles) {
            //封装用户信息和角色信息 到 SecurityContextHolder全局缓存中
            logger.info("name--->{}",sysRole.getName());
            grantedAuthorities.add(new SimpleGrantedAuthority(sysRole.getName()));
        }
        logger.info("--------------all Roles--------------");
        return new User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}

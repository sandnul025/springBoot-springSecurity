package com.sandnul.security;

import com.sandnul.domain.SysRolePermisson;
import com.sandnul.repository.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/29
 * TIME: 11:17
 */
@Component
public class MyInvocationSecurityMetadataSourceService  implements
        FilterInvocationSecurityMetadataSource {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 每一个资源所需要的角色
     */
    private static HashMap<String, Collection<ConfigAttribute>> map =null;
    public void loadResourceDefine(){

        map = new HashMap<>();

        //权限资源 和 角色对应的表  也就是 角色 权限中间表
        List<SysRolePermisson> rolePermissons = sysUserMapper.findAllRolePermissoin();

        //每个资源 所需要的权限
        for (SysRolePermisson rolePermisson : rolePermissons) {
            String url = rolePermisson.getUrl();
            String roleName = rolePermisson.getRoleName();
            ConfigAttribute role = new SecurityConfig(roleName);
            if(map.containsKey(url)){
                map.get(url).add(role);
            }else{
                map.put(url,new ArrayList<ConfigAttribute>(){{
                    add(role);
                }});
            }
        }
    }

    /**
     * @param object
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if(map ==null)
            loadResourceDefine();
        //object 中包含用户请求的request 信息
        HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
        for(Iterator<String> iter = map.keySet().iterator(); iter.hasNext(); ) {
            String url = iter.next();
            if(new AntPathRequestMatcher(url).matches(request)) {
                return map.get(url);
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
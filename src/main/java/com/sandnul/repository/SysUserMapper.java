package com.sandnul.repository;

import com.sandnul.domain.SysPermission;
import com.sandnul.domain.SysRole;
import com.sandnul.domain.SysRolePermisson;
import com.sandnul.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/28
 * TIME: 14:53
 */
@Mapper
public interface SysUserMapper {

    /**
     * 通过username查找 user
     * username是唯一的前提
     *
     * @param username
     * @return SysUser
     */
    SysUser findUserByUsername(String username);

    /**
     * 通过用户名 查找·
     * @param username
     * @return List<SysRole>
     */
    List<SysRole> findRolesByUsername(String username);

    /**
     * 通过用户名 查找权限
     * @param username
     * @return List<SysPermission>
     */
    List<SysPermission> findPermissionsByUsername(String username);

    List<SysRolePermisson> findAllRolePermissoin();

}

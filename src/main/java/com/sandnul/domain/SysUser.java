package com.sandnul.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/28
 * TIME: 9:30
 */
@Entity
@Table(name = "sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Column(unique = true)
    private String username;

    @NotNull
    private String password;

    @ManyToMany
    @JoinTable(name = "sys_user_role",joinColumns = {@JoinColumn(name = "sys_user_id")},inverseJoinColumns={@JoinColumn(name = "sys_role_id")})
    private List<SysRole> roles;

    public Long getId() {
        return id;
    }

    public void setuserId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(List<SysRole> roles) {
        this.roles = roles;
    }

}

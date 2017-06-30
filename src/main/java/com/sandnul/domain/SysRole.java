package com.sandnul.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/28
 * TIME: 9:25
 */
@Entity
@Table(name = "sys_role")
public class SysRole implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String name;

    @ManyToMany
    @JoinTable(name = "sys_role_permission",joinColumns = {@JoinColumn(name = "sys_role_id")},inverseJoinColumns={@JoinColumn(name = "sys_permission_id")})
    private List<SysPermission> permissions;

    public List<SysPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<SysPermission> permissions) {
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

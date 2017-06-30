在没有用权限框架之前，我们做权限控制的时候一般思路是这样的：
1.登录的时候从数据库中检索user所拥有的privileges存在内存中
2.在用户发出请求的时候，将请求信息与用户所拥有的权限对比。

spring security也有这种实现，然而我们要做的就是：
自定义过滤器，代替原有的FilterSecurityInterceptor过滤器，实现
UserDetailsService （储存用户所有角色）
InvocationSecurityMetadataSourceService（访问资源所需要的角色集合）
AccessDecisionManager（判断用户请求的资源  是否能通过）

---
思路很明了看看实现：
说明：
用了jpa 当时参考了一些资料，走了很多弯路发现没有很大作用，
还是习惯自己写sql，所以这里只是用来创建表格，反正以后扩展crud会用到。
真正做持久化的是mybatis，这两个整合在一起了，只是多创建了一个接口而已。

准备工作
---
```
        <!-- security -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>

		<!-- thymeleaf -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-thymeleaf</artifactId>
		</dependency>

		<!-- mysql -->
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<scope>runtime</scope>
		</dependency>

		<!-- mybatis -->
		<dependency>
			<groupId>org.mybatis.spring.boot</groupId>
			<artifactId>mybatis-spring-boot-starter</artifactId>
			<version>1.3.0</version>
		</dependency>

		<!-- jpa -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
```
```
# server
server:
  port: 8888
spring:
# dataSource
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/nul_blog?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: admin
    password: root
# jpa
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database: mysql
# thymeleaf
  thymeleaf:
    prefix: classpath:/templates/
    cache: false
    suffix: .html
# mybatis
mybatis:
  mapper-locations: mappers/*.xml
```
一.用户 角色 权限 三张表 
很普通的bean，user类并没有继承userdetails
```
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
    //... getter and settter
}
```
```
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
    @JoinTable(name = "sys_role_permission",
joinColumns = {@JoinColumn(name = "sys_role_id")},
inverseJoinColumns={@JoinColumn(name = "sys_permission_id")})
    private List<SysPermission> permissions;
     //... getter and settter
}
```
```
@Entity
@Table(name = "sys_permission")
public class SysPermission implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private Long pid;
    @NotNull
    private String name;
    @NotNull
    private String url;
    @NotNull
    private String description;
    //... getter and settter
}
```
```
public class SysRolePermisson {
    //角色
    private String roleId;
    private String roleName;

    //权限
    private String permissionId;
    private String url;
    //... getter and settter
}
```

![image.png](http://upload-images.jianshu.io/upload_images/6334710-659ef911706420a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
关于数据的设计我是这样想的：

基础用户和高级用户区别是拥有多的权限
 所以多的权限就把他赋给另个角色

所以一个用户最少拥有基础用户的权限
其次才拥有高级用户的权限
```
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
```
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.sandnul.repository.SysUserMapper">
    <select id="findUserByUsername" resultType="com.sandnul.domain.SysUser">
        select id,password,username from SYS_user WHERE username = #{username}
    </select>

    <select id="findPermissionsByUsername" resultType="com.sandnul.domain.SysPermission">
        select sp.*
        from sys_user su
        left join sys_user_role  sur on su.id = sur.sys_user_id
        left join sys_role_permission srp on sur.sys_role_id = srp.sys_role_id
        left join sys_permission sp on srp.sys_permission_id = sp.id
        where su.username = #{username}
    </select>
    
    <select id="findRolesByUsername" resultType="com.sandnul.domain.SysRole">
        select sr.*
        from sys_user su
        left join sys_user_role  sur on su.id = sur.sys_user_id
        left join sys_role sr on sur.sys_role_id = sr.id
        where su.username = #{username}
    </select>
    
    <select id="findAllRolePermissoin" resultType="com.sandnul.domain.SysRolePermisson">
        select sr.id roleId ,sr.name roleName,sp.id permissionId,sp.url
        from sys_role_permission srp
        left join sys_role sr  on sr.id = srp.sys_role_id
        left join sys_permission sp on srp.sys_permission_id = sp.id
    </select>
</mapper>
```
```
public interface SysUserRepository extends JpaRepository<SysUser, Long>{

}
```
---
以下就是思路的实现
---
二、替换原先的拦截器
说实话我不知道这个拦截器和原先的拦截器有什么不同，我去掉后发现权限乱了
，估计还要读一些源码吧。
```
@Component
public class MyFilterSecurityInterceptor extends AbstractSecurityInterceptor implements Filter{

    @Autowired
    private FilterInvocationSecurityMetadataSource securityMetadataSource;

    @Autowired
    public void setMyAccessDecisionManager(MyAccessDecisionManager myAccessDecisionManager) {
        super.setAccessDecisionManager(myAccessDecisionManager);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        FilterInvocation fi = new FilterInvocation(request, response, chain);
        invoke(fi);
    }

    public void invoke(FilterInvocation fi) throws IOException, ServletException {

        InterceptorStatusToken token = super.beforeInvocation(fi);
        try {
            //执行下一个拦截器
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
            super.afterInvocation(token, null);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }
}

```
---
三、重写UserDetailsService,登录认证
认证是由 AuthenticationManager 来管理的，但是真正进行认证的是 AuthenticationManager 中定义的 AuthenticationProvider。Spring Security 默认会使用DaoAuthenticationProvider。DaoAuthenticationProvider 在进行认证的时候需要一个 UserDetailsService 来获取用户的信息 UserDetails。改变认证的方式，就实现 UserDetailsService，返回我们自己userdetails。
```
@Component
public class MyCustomUserService implements UserDetailsService{

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 将用的所有角色存储于用户信息中
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.findUserByUsername(username);
        if(user == null)
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));

        //获取所有请求的url
        //List<SysPermission> sysPermissions = sysUserMapper.findPermissionsByUsername(user.getUsername());
        List<SysRole> sysRoles = sysUserMapper.findRolesByUsername(user.getUsername());

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (SysRole sysRole : sysRoles) {
            //封装用户信息和角色信息 到 SecurityContextHolder全局缓存中
            grantedAuthorities.add(new SimpleGrantedAuthority(sysRole.getName()));
        }
        return new User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}
```
---
四、实现FilterInvocationSecurityMetadataSource
作用为了将所有资源和资源对应需要的角色存在map中
用户请求资源的时候能够返回资源所对应的角色集合给
决策器（MyAccessDecisionManager）
```
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
```
```
@Component
public class MyInvocationSecurityMetadataSourceService  implements
        FilterInvocationSecurityMetadataSource {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 每一个资源所需要的角色 Collection<ConfigAttribute>决策器会用到 不急弄清楚
     */
    private static HashMap<String, Collection<ConfigAttribute>> map =null;
    
    //初始化 所有资源 对应的角色
    public void loadResourceDefine(){
        map = new HashMap<>();
        //权限资源 和 角色对应的表  也就是 角色 权限中间表
        List<SysRolePermisson> rolePermissons = sysUserMapper.findAllRolePermissoin();

        //每个资源 所需要的角色
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
     * 我的理解是 方法会被调用，然后返回资源所需要的角色
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
```
---
四、决策器
主要的方法返回值是void，可以想到既然判断是否有权通过，
那没通过肯定就是抛出异常，让外层捕捉。
```
@Component
public class MyAccessDecisionManager implements AccessDecisionManager {

    private final static Logger logger = LoggerFactory.getLogger(MyAccessDecisionManager.class);

    /**
     * 判定 是否含有权限
     * @param authentication  CustomUserService.loadUserByUsername() 封装的用户信息
     * @param object    request请求信息
     * @param configAttributes InvocationSecurityMetadataSourceService.getAttributes()  中每个资源可访问的角色集合
     * @throws AccessDeniedException
     * @throws InsufficientAuthenticationException
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {

        if(null== configAttributes || configAttributes.size() <=0) {
            return;
        }
        String needRole;
        for(Iterator<ConfigAttribute> iter = configAttributes.iterator(); iter.hasNext(); ) {
            needRole = iter.next().getAttribute();


            for(GrantedAuthority ga : authentication.getAuthorities()) {
                if(needRole.trim().equals(ga.getAuthority().trim())) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("no privilege");
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
```
---
请求和页面
---
```
@Controller
public class TestController {

    @GetMapping(value = {"/","index"})
    public String index(){

        return "index";
    }

    @GetMapping(value = "hello")
    public String hello(){

        return "hello";
    }

    @GetMapping(value = "login")
    public String login(){

        return "login";
    }


    @GetMapping(value = "admin")
    public String admin(Model model){

        model.addAttribute("title","标题");
        model.addAttribute("content","内容");
        model.addAttribute("extraInfo","你是admin");
        return "admin";
    }
}
```
```
admin.html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity4">
<head>
    <meta charset="UTF-8"/>
    <title sec:authentication="name"></title>
    <link rel="stylesheet" th:href="@{css/bootstrap.min.css}"/>
    <style type="text/css">
        body {
            padding-top: 50px;
        }

        .starter-template {
            padding: 40px 15px;
            text-align: center;
        }
    </style>
</head>
<body>
<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">Spring Security演示</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a th:href="@{/}">首页</a></li>
            </ul>
        </div>
    </div>
</nav>
<div class="container">
    <div class="starter-template">
        <h1 th:text="${title}"></h1>
        <p class="bg-primary" th:text="${content}"></p>
        <div sec:authorize="hasRole('ROLE_ADMIN')">
            <p class="bg-info" th:text="${extraInfo}"></p>
        </div>
        <div sec:authorize="hasRole('ROLE_USER')">
            <p class="bg-info">无更多显示信息</p>
        </div>
        <form th:action="@{/logout}" method="post">
            <input type="submit" class="btn btn-primary" value="注销"/>
        </form>
    </div>
</div>
</body>
</html>
```
```
hello.html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity4">
<head>
    <meta charset="UTF-8" />
    <title>Title</title>
</head>
<body>
<h1>hello spring boot with security</h1>
<form th:action="@{/logout}" method="post">
    <input type="submit" class="btn btn-primary" value="注销"/>
</form>
</body>
</html>
```
```
index.html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>Title</title>
</head>
<body>
index
</body>
</html>
```
```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>登录</title>
    <link rel="stylesheet" th:href="@{css/bootstrap.min.css}"/>
    <style type="text/css">
        body {
            padding-top: 50px;
        }

        .starter-template {
            padding: 40px 15px;
            text-align: center;
        }
    </style>
</head>
<body>
<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">Spring Security演示</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a th:href="@{/}">首页</a></li>
            </ul>
        </div>
    </div>
</nav>
<div class="container">
    <div class="starter-template">
        <p th:if="${param.logout}" class="bg-warning">已注销</p>
        <p th:if="${param.error}" class="bg-danger" th:text="${session.SPRING_SECURITY_LAST_EXCEPTION.message}">有错误</p>
        <h2>使用账号密码登录</h2>
        <form name="form" th:action="@{/login}" action="/login" method="post">
            <div class="form-group">
                <label for="username">账号</label>
                <input type="text" class="form-control" name="username" value="" placeholder="账号"/>
            </div>
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" class="form-control" name="password" placeholder="密码"/>
            </div>
            <input type="submit" id="login" value="Login" class="btn btn-primary"/>
        </form>
    </div>
</div>
</body>
</html>
```
ls的账号去登录成功会跳转到hello页面然而，请求admin页面 则会报错no privilege


---
以下是参考的一些文章
http://blog.csdn.net/u012373815/article/details/54633046
http://blog.csdn.net/code__code/article/details/53885510
http://blog.csdn.net/u012367513/article/details/38866465
https://www.w3cschool.cn/springsecurity/uzq31ii7.html

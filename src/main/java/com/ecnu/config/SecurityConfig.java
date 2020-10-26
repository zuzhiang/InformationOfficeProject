package com.ecnu.config;

import com.ecnu.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;

import javax.sql.DataSource;

// 授权和认证的实现类
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserController userDetailService;

    // 自定义的国际化组件放在容器中
    @Bean
    public LocaleResolver localeResolver() {
        return new LocaleResolverConfig();
    }


    // 授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 首页所有人可以访问，功能页只有对应有权限的人可以访问
        // 请求授权的规则
        http.authorizeRequests()
                .antMatchers("/").permitAll() //首页都可以访问
                // 用户管理页面只有角色为admin的用户可以访问，此处不需要加ROLE_前缀
                .antMatchers("/user/**").hasRole("admin")
                .antMatchers("/toAddUserPage/**").hasRole("admin")
                .antMatchers("/toUpdateUserPage/**").hasRole("admin")
                .antMatchers("/addUser/**").hasRole("admin")
                .antMatchers("/updateUser/**").hasRole("admin")
                .antMatchers("/toAddVideoPage/**").hasRole("ordinary")
                .antMatchers("/toUpdateVideoPage/**").hasRole("ordinary")
                .antMatchers("/addVideo/**").hasRole("ordinary")
                .antMatchers("/updateVideo/**").hasRole("ordinary")
                .antMatchers("/deleteVideo/**").hasRole("ordinary")
                .and()
                // 登录页
                .formLogin().loginPage("/login")
                .loginProcessingUrl("/toLogin")
                // 未登录时跳转到首页
                .defaultSuccessUrl("/index")
                // 登录成功后跳转到首页
                .successForwardUrl("/index")
                // 登录失败后跳转到登录页面，并携带错误信息
                .failureUrl("/login?error=true").permitAll()
                .and()
                // 开启注销功能，注销成功后跳到首页
                .logout().logoutSuccessUrl("/index")
                .and()
                // 开启记住我功能，自定义接收前端的参数
                .rememberMe().rememberMeParameter("remember")
                .and()
                // 防止网站攻击：get、post
                .csrf().disable(); //关闭csrf功能
        
    }

    // 认证，2.1.x可以直接使用
    // 密码编码：PassworkEncoder
    // 在spring security5.0+中新增加了很多的加密方法
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        // 与数据库中的用户表做验证
        auth.userDetailsService(userDetailService).passwordEncoder(new BCryptPasswordEncoder());

    }
}

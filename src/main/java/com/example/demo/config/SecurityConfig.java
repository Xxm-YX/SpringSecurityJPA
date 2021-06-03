package com.example.demo.config;

import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略的URL地址，一些静态文件
        web.ignoring().antMatchers(
                "/js/**",
                "/css/**",
                "/images/**"
        );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Bean//不加密
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * 角色继承
     * @return
     */
    @Bean
    RoleHierarchy roleHierarchy(){
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_admin > ROLE_user");
        return hierarchy;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()//基于URL的限制访问开启
                .antMatchers("/admin/**").hasRole("admin")//配置访问路径，那些
                .antMatchers("/user/**").hasRole("user")
                .anyRequest()//映射所有请求
                .authenticated()//验证
                .and()//表示结束当前标签，上下文回到HttpSecurity，开启新一轮的配置。
                .formLogin()
//                .loginPage("/login.html")
                .loginProcessingUrl("/doLogin")
                .permitAll()//表示登录相关的页面/接口不要被拦截。
                .usernameParameter("name")
                .passwordParameter("passwd")
//                .defaultSuccessUrl("/index")  如果访问的是hello，登录成功后，会继续访问hello
//                .successForwardUrl("/index")   如果访问的是hello，登录成功后会，去访问index
//                .failureForwardUrl()   登录失败  发生服务器跳转
//                .failureUrl()          登录失败，发生重定向
                .successHandler((req, resp, authentication) -> { //登录成功的回调函数，
                    //这里传进来的是一个AuthenticationSuccessHandler 对象
                    //AuthenticationSuccessHandler 是个接口
                    Object principal = authentication.getPrincipal();
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(principal));
                    out.flush();
                    out.close();
                })
                .failureHandler((req, resp, e) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(e.getMessage());
                    out.flush();
                    out.close();
                })
                .and()
                .logout()//GET请求
                .logoutUrl("/logout")//修改默认注销URL
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST")) //修改注销URL，还可以修改请求方式
                .logoutSuccessHandler((req, resp, authentication) -> { // 成功后的回调函数，发送注销信息
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    resp.setStatus(200);
                    out.write("注销成功");
                    out.flush();
                    out.close();
                })
//                .logoutSuccessUrl("/index")//登录成功要跳转的页面
                .deleteCookies()//清除cookie
                .clearAuthentication(true)//清除认证信息
                .invalidateHttpSession(true)//使HttpSession失效  默认就会清除
                .permitAll()//绕开过滤器
                .and()
                .csrf().disable()
                .exceptionHandling();
//                .authenticationEntryPoint((req, resp, e) -> {
//                    resp.setContentType("application/json;charset=utf-8");
//                    PrintWriter out = resp.getWriter();
//                    resp.setStatus(403);
//                    out.append("{\"code\":1,\"msg\":\"登录失败,请重新登录!\",\"data\":\"failed\"}");
////                    out.write("尚未登录，请先登录");
//                    out.flush();
//                    out.close();
//                });
    }
}

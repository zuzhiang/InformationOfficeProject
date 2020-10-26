package com.ecnu.controller;

import com.ecnu.mapper.UserMapper;
import com.ecnu.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@Service
public class UserController implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    // 登录验证
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 权限字符转化
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        // 获取后的Roles必须有ROLE_前缀，否则会抛Access is denied无权限异常
        String[] roles = user.getRole().split(",");
        for (String role : roles) {
            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role));
        }
        // 对用户密码做加密
        return new org.springframework.security.core.userdetails.User(user.getUsername(), new BCryptPasswordEncoder().encode(user.getPassword()), simpleGrantedAuthorities);
    }

    // 查询所有用户，并在用户页展示
    @RequestMapping("/user/{state}")
    public String showUser(@PathVariable("state") String state, Model model) {
        Collection<User> users = userMapper.selectAllUser();
        model.addAttribute("users", users);
        if (state.equals("1")){
            model.addAttribute("updateUserState", "更新用户成功！");
        }
        return "user/user";
    }

    // 跳转到添加用户页面
    @RequestMapping("/toAddUserPage")
    public String toAddUserPage() {
        return "user/addUser";
    }

    // 跳转到更新用户页面
    @RequestMapping("/toUpdateUserPage/{username}/{state}")
    public String toUpdateUserPage(@PathVariable("username") String username,
                                   @PathVariable("state") String state,
                                   Model model) {
        // 查出原来的数据
        User user = userMapper.selectUserByUsername(username);
        model.addAttribute("user", user);
        if (state.equals("0")) {
            model.addAttribute("updateUserState", "两次密码不一致！");
        }
        return "user/updateUser";
    }

    // 添加用户
    @RequestMapping("/addUser")
    public String addUser(@RequestParam("username") String username,
                          @RequestParam("password1") String password1,
                          @RequestParam("password2") String password2,
                          @RequestParam("role") String role, Model model) {
        username = username.trim();
        if (username.length() == 0 || username == null || username == "" || username == " ") { // 用户名为空
            model.addAttribute("addUserState", "用户名不能为空！");
        } else if (password1.equals(password2) == false) { // 如果两次密码不一致
            model.addAttribute("addUserState", "两次密码不一致！");
        } else if (userMapper.countUserNum(username) != 0) { // 如果用户已存在
            model.addAttribute("addUserState", "用户名已存在！");
        } else {
            // 创建用户对象并添加
            User user = new User();
            user.setUsername(username);
            user.setPassword(password1);
            // 用户权限，高权限包含低权限
            if (role.equals("ROLE_admin")) {
                role = "ROLE_admin,ROLE_ordinary,ROLE_tourist";
            } else if (role.equals("ROLE_ordinary")) {
                role = "ROLE_ordinary,ROLE_tourist";
            }
            user.setRole(role);
            userMapper.addUser(user);
            model.addAttribute("addUserState", "添加用户成功！");
        }
        return "user/addUser";
    }

    // 更新用户
    @RequestMapping("/updateUser")
    public String updateUser(@RequestParam("username") String username,
                             @RequestParam("password1") String password1,
                             @RequestParam("password2") String password2,
                             @RequestParam("role") String role) {
        if (!password1.equals(password2)) { // 如果两次密码不一致
            return "redirect:/toUpdateUserPage/" + username + "/0";
        } else {
            // 查找用户对象并更新
            User user = userMapper.selectUserByUsername(username);
            user.setPassword(password1);
            // 用户权限，高权限包含低权限
            if (role.equals("ROLE_admin")) {
                role = "ROLE_admin,ROLE_ordinary,ROLE_tourist";
            } else if (role.equals("ROLE_ordinary")) {
                role = "ROLE_ordinary,ROLE_tourist";
            }
            user.setRole(role);
            userMapper.updateUser(user);
            return "redirect:/user/1";
        }
    }

    // 删除用户
    @RequestMapping("/deleteUser/{username}")
    public String deleteUser(@PathVariable("username") String username) {
        userMapper.deleteUser(username);
        return "redirect:/user/0";
    }

}

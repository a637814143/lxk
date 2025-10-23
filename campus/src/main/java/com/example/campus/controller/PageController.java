package com.example.campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "系统管理员控制台");
        model.addAttribute("description", "这里可以管理平台的核心数据和用户。\n如需返回登录，请点击页面底部的链接。");
        return "dashboard-admin";
    }

    @GetMapping("/company/home")
    public String companyHome(Model model) {
        model.addAttribute("title", "企业工作台");
        model.addAttribute("description", "在这里可以发布校招岗位、查看投递情况，并与学生保持沟通。\n如需切换账号，请回到登录页。");
        return "dashboard-company";
    }

    @GetMapping("/student/home")
    public String studentHome(Model model) {
        model.addAttribute("title", "学生成长空间");
        model.addAttribute("description", "欢迎来到校园招聘系统，可以浏览岗位、完善简历并追踪申请进度。\n如需退出，请返回登录页。");
        return "dashboard-student";
    }
}

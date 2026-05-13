package com.ruoyi.wxmini.controller.merchant;

import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.wxmini.domain.MallUser;
import com.ruoyi.wxmini.mapper.MallUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mall/user")
public class MallUserController extends BaseController {

    @Autowired
    private MallUserMapper mallUserMapper;

    @PreAuthorize("@ss.hasPermi('mall:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallUser query) {
        startPage();
        List<MallUser> list = mallUserMapper.selectMallUserList(query);
        return getDataTable(list);
    }
}

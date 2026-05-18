package com.ruoyi.mall.order.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mall/order")
public class MallOrderExtendController extends BaseController {

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/abnormal/list")
    public TableDataInfo abnormalList() {
        startPage();
        MallOrder query = new MallOrder();
        query.setStatus(5);
        List<MallOrder> list = mallOrderMapper.selectMallOrderList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/after-sale/list")
    public TableDataInfo afterSaleList() {
        startPage();
        MallOrder query = new MallOrder();
        query.setStatus(4);
        List<MallOrder> list = mallOrderMapper.selectMallOrderList(query);
        return getDataTable(list);
    }
}

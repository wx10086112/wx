package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.mapper.DistributorMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DistributorServiceImpl implements IDistributorService {

    @Resource
    private DistributorMapper distributorMapper;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Distributor selectDistributorById(Long id) {
        return distributorMapper.selectDistributorById(id);
    }

    @Override
    public List<Distributor> selectDistributorList(Distributor query) {
        return distributorMapper.selectDistributorList(query);
    }

    @Override
    public Distributor selectByUsername(String username) {
        return distributorMapper.selectByUsername(username);
    }

    @Override
    public int insertDistributor(Distributor distributor) {
        String username = normalize(distributor.getUsername());
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("登录账号不能为空");
        }
        distributor.setUsername(username);

        Distributor existing = distributorMapper.selectByUsernameAny(username);
        if (existing != null) {
            if ("0".equals(existing.getDelFlag())) {
                throw new RuntimeException("登录账号已存在");
            }
            throw new RuntimeException("登录账号被历史已删除分销商占用，请先执行分销商账号热修 SQL");
        }

        if (distributor.getPassword() != null && !distributor.getPassword().isEmpty()) {
            distributor.setPassword(encoder.encode(distributor.getPassword()));
        }
        if (distributor.getStatus() == null) {
            distributor.setStatus(1);
        }
        return distributorMapper.insertDistributor(distributor);
    }

    @Override
    public int updateDistributor(Distributor distributor) {
        // 修改时不更新密码字段
        distributor.setPassword(null);
        return distributorMapper.updateDistributor(distributor);
    }

    @Override
    public int deleteDistributorById(Long id) {
        return distributorMapper.deleteDistributorById(id);
    }

    @Override
    public int deleteDistributorByIds(Long[] ids) {
        return distributorMapper.deleteDistributorByIds(ids);
    }

    @Override
    public int resetPassword(Long id, String newPassword) {
        Distributor distributor = new Distributor();
        distributor.setId(id);
        distributor.setPassword(encoder.encode(newPassword));
        return distributorMapper.updateDistributor(distributor);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.Distributor;
import java.util.List;

public interface IDistributorService {

    Distributor selectDistributorById(Long id);

    List<Distributor> selectDistributorList(Distributor query);

    Distributor selectByUsername(String username);

    int insertDistributor(Distributor distributor);

    int updateDistributor(Distributor distributor);

    int deleteDistributorById(Long id);

    int deleteDistributorByIds(Long[] ids);

    int resetPassword(Long id, String newPassword);
}

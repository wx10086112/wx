package com.ruoyi.mall.product.mapper;

import com.ruoyi.mall.product.domain.Distributor;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface DistributorMapper {

    Distributor selectDistributorById(Long id);

    List<Distributor> selectDistributorList(Distributor query);

    Distributor selectByUsername(@Param("username") String username);

    int insertDistributor(Distributor distributor);

    int updateDistributor(Distributor distributor);

    int deleteDistributorById(Long id);

    int deleteDistributorByIds(Long[] ids);
}

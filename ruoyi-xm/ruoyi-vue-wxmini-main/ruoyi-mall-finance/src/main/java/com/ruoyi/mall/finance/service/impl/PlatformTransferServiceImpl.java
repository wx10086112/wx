package com.ruoyi.mall.finance.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.mall.finance.domain.DistributorSettlementRecord;
import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.domain.PlatformTransferRecord;
import com.ruoyi.mall.finance.mapper.PlatformTransferRecordMapper;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IPlatformTransferService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PlatformTransferServiceImpl implements IPlatformTransferService {

    private static final Logger log = LoggerFactory.getLogger(PlatformTransferServiceImpl.class);

    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_TRANSFERRING = "TRANSFERRING";
    private static final String STATUS_ARRIVED = "ARRIVED";
    private static final String STATUS_FAILED = "FAILED";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wx.pay.stub-enabled:false}")
    private boolean stubEnabled;

    @Autowired
    private PlatformTransferRecordMapper transferMapper;
    @Autowired
    private IMerchantSettlementRecordService merchantSettlementService;
    @Autowired
    private IDistributorSettlementRecordService distributorSettlementService;
    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private IDistributorService distributorService;
    @Autowired(required = false)
    private com.github.binarywang.wxpay.service.WxPayService wxPayService;

    @Override
    public PlatformTransferRecord selectById(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public PlatformTransferRecord selectByTransferNo(String transferNo) {
        return transferMapper.selectByTransferNo(transferNo);
    }

    @Override
    public List<PlatformTransferRecord> selectList(PlatformTransferRecord query) {
        return transferMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformTransferRecord createMerchantTransfer(Long settlementId, String operatorId) {
        MerchantSettlementRecord settlement = merchantSettlementService.selectById(settlementId);
        if (settlement == null) {
            throw new RuntimeException("结算记录不存在");
        }
        if (!"WAITING_T1".equals(settlement.getStatus())) {
            throw new RuntimeException("结算记录状态不是 WAITING_T1，当前状态: " + settlement.getStatus());
        }
        if (settlement.getMerchantAmount() == null || settlement.getMerchantAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("结算金额必须大于0");
        }

        // 校验收款账户
        Merchant merchant = merchantService.selectMerchantById(settlement.getMerchantId());
        if (merchant == null || StringUtils.isBlank(merchant.getReceiverOpenid())) {
            throw new RuntimeException("商家未配置收款账户，请先配置 receiver_openid");
        }

        // 幂等校验：检查是否已有活跃转账记录
        PlatformTransferRecord existing = transferMapper.selectActiveBySettlementNo(settlement.getSettlementNo());
        if (existing != null) {
            throw new RuntimeException("该结算单已有活跃的转账记录: " + existing.getTransferNo());
        }

        // 创建转账记录
        PlatformTransferRecord record = new PlatformTransferRecord();
        record.setTransferNo(generateTransferNo());
        record.setSettlementNo(settlement.getSettlementNo());
        record.setTargetType("MERCHANT");
        record.setTargetId(settlement.getMerchantId());
        record.setMerchantId(settlement.getMerchantId());
        record.setOrderNo(settlement.getOrderNo());
        record.setAmount(settlement.getMerchantAmount());
        record.setReceiverOpenid(merchant.getReceiverOpenid());
        record.setReceiverAccountType(merchant.getReceiverType() != null ? merchant.getReceiverType() : "WECHAT_BALANCE");
        record.setStatus(STATUS_WAITING);
        record.setApplyTime(new Date());
        record.setOperatorId(operatorId);
        transferMapper.insert(record);

        // 调用微信转账 (或 stub)
        doWxTransferAndUpdate(record, settlement);

        log.info("商家转账已发起: transferNo={}, settlementNo={}, amount={}",
                record.getTransferNo(), settlement.getSettlementNo(), settlement.getMerchantAmount());
        return transferMapper.selectById(record.getId());
    }

    @Override
    public List<PlatformTransferRecord> batchCreateMerchantTransfer(List<Long> settlementIds, String operatorId) {
        List<PlatformTransferRecord> results = new ArrayList<>();
        for (Long id : settlementIds) {
            try {
                results.add(doSingleMerchantTransfer(id, operatorId));
            } catch (Exception e) {
                log.error("批量转账-商家结算 {} 失败: {}", id, e.getMessage());
            }
        }
        return results;
    }

    /**
     * 单笔商家转账（独立事务，批量场景下不互相影响）
     */
    @Transactional(rollbackFor = Exception.class)
    public PlatformTransferRecord doSingleMerchantTransfer(Long settlementId, String operatorId) {
        return createMerchantTransfer(settlementId, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformTransferRecord createDistributorTransfer(Long settlementId, String operatorId) {
        DistributorSettlementRecord settlement = distributorSettlementService.selectById(settlementId);
        if (settlement == null) {
            throw new RuntimeException("分销商结算记录不存在");
        }
        if (!"WAITING_SETTLEMENT".equals(settlement.getStatus())) {
            throw new RuntimeException("结算记录状态不是 WAITING_SETTLEMENT，当前状态: " + settlement.getStatus());
        }
        if (settlement.getAmount() == null || settlement.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("结算金额必须大于0");
        }

        // 校验收款账户
        Distributor distributor = distributorService.selectDistributorById(settlement.getDistributorId());
        if (distributor == null || StringUtils.isBlank(distributor.getReceiverOpenid())) {
            throw new RuntimeException("分销商未配置收款账户，请先配置 receiver_openid");
        }

        // 幂等校验
        PlatformTransferRecord existing = transferMapper.selectActiveBySettlementNo(settlement.getSettlementNo());
        if (existing != null) {
            throw new RuntimeException("该结算单已有活跃的转账记录: " + existing.getTransferNo());
        }

        // 创建转账记录
        PlatformTransferRecord record = new PlatformTransferRecord();
        record.setTransferNo(generateTransferNo());
        record.setSettlementNo(settlement.getSettlementNo());
        record.setTargetType("DISTRIBUTOR");
        record.setTargetId(settlement.getDistributorId());
        record.setMerchantId(settlement.getMerchantId());
        record.setDistributorId(settlement.getDistributorId());
        record.setOrderNo(settlement.getOrderNo());
        record.setAmount(settlement.getAmount());
        record.setReceiverOpenid(distributor.getReceiverOpenid());
        record.setReceiverAccountType(distributor.getReceiverType() != null ? distributor.getReceiverType() : "WECHAT_BALANCE");
        record.setStatus(STATUS_WAITING);
        record.setApplyTime(new Date());
        record.setOperatorId(operatorId);
        transferMapper.insert(record);

        // 调用微信转账 (或 stub)
        doWxTransferForDistributorAndUpdate(record, settlement);

        log.info("分销商转账已发起: transferNo={}, settlementNo={}, amount={}",
                record.getTransferNo(), settlement.getSettlementNo(), settlement.getAmount());
        return transferMapper.selectById(record.getId());
    }

    @Override
    public List<PlatformTransferRecord> batchCreateDistributorTransfer(List<Long> settlementIds, String operatorId) {
        List<PlatformTransferRecord> results = new ArrayList<>();
        for (Long id : settlementIds) {
            try {
                results.add(doSingleDistributorTransfer(id, operatorId));
            } catch (Exception e) {
                log.error("批量转账-分销商结算 {} 失败: {}", id, e.getMessage());
            }
        }
        return results;
    }

    /**
     * 单笔分销商转账（独立事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public PlatformTransferRecord doSingleDistributorTransfer(Long settlementId, String operatorId) {
        return createDistributorTransfer(settlementId, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTransferNotify(String notifyBody) {
        log.info("处理微信转账回调: {}", notifyBody);

        // 使用 Jackson 解析 JSON
        String detailNo;
        String transferStatus;
        try {
            JsonNode root = objectMapper.readTree(notifyBody);
            detailNo = root.path("out_detail_no").asText(null);
            transferStatus = root.path("transfer_status").asText(null);
        } catch (Exception e) {
            log.error("转账回调 JSON 解析失败: {}", e.getMessage());
            throw new RuntimeException("回调 JSON 解析失败");
        }

        if (StringUtils.isBlank(detailNo)) {
            log.error("回调缺少 out_detail_no");
            throw new RuntimeException("回调缺少 out_detail_no");
        }

        // 按 wechatDetailNo 查找转账记录（需要查全表）
        PlatformTransferRecord query = new PlatformTransferRecord();
        query.setWechatDetailNo(detailNo);
        List<PlatformTransferRecord> records = transferMapper.selectList(query);
        if (records == null || records.isEmpty()) {
            log.error("找不到转账记录: wechatDetailNo={}", detailNo);
            throw new RuntimeException("找不到转账记录");
        }

        PlatformTransferRecord record = records.get(0);

        // 幂等：已到账或已失败的不重复处理
        if (STATUS_ARRIVED.equals(record.getStatus()) || STATUS_FAILED.equals(record.getStatus())) {
            log.info("转账记录 {} 已处于终态 {}，幂等跳过", record.getTransferNo(), record.getStatus());
            return;
        }

        Date now = new Date();
        record.setNotifyTime(now);
        record.setNotifyResult(notifyBody);

        if ("SUCCESS".equals(transferStatus)) {
            record.setStatus(STATUS_ARRIVED);
            record.setArriveTime(now);
            transferMapper.updateById(record);

            // 同步更新结算记录
            updateSettlementToArrived(record);
            log.info("转账回调成功: transferNo={}, 状态→ARRIVED", record.getTransferNo());
        } else {
            record.setStatus(STATUS_FAILED);
            record.setFailReason("微信回调: " + transferStatus);
            transferMapper.updateById(record);
            log.warn("转账回调失败: transferNo={}, 原因={}", record.getTransferNo(), transferStatus);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void queryAndUpdateTransferStatus(String transferNo) {
        PlatformTransferRecord record = transferMapper.selectByTransferNo(transferNo);
        if (record == null || !STATUS_TRANSFERRING.equals(record.getStatus())) {
            return;
        }

        if (stubEnabled) {
            Date now = new Date();
            record.setStatus(STATUS_ARRIVED);
            record.setArriveTime(now);
            record.setNotifyTime(now);
            record.setNotifyResult("stub_timeout_arrived");
            transferMapper.updateById(record);
            updateSettlementToArrived(record);
            log.info("Stub模式: 转账 {} 超时补偿标记为ARRIVED", transferNo);
        } else if (wxPayService != null) {
            // 真实模式：调用微信查询接口
            try {
                com.github.binarywang.wxpay.bean.merchanttransfer.WxBatchesQueryRequest queryReq =
                        new com.github.binarywang.wxpay.bean.merchanttransfer.WxBatchesQueryRequest();
                queryReq.setBatchId(record.getWechatBatchNo() != null ? record.getWechatBatchNo() : transferNo);
                queryReq.setNeedQueryDetail(true);

                com.github.binarywang.wxpay.bean.merchanttransfer.BatchesQueryResult queryResult =
                        wxPayService.getMerchantTransferService().queryWxBatches(queryReq);

                if (queryResult != null && queryResult.getTransferBatch() != null) {
                    String batchStatus = queryResult.getTransferBatch().getBatchStatus();
                    log.info("微信转账状态查询: transferNo={}, batchStatus={}", transferNo, batchStatus);
                    if ("FINISHED".equals(batchStatus)) {
                        Date now = new Date();
                        record.setStatus(STATUS_ARRIVED);
                        record.setArriveTime(now);
                        transferMapper.updateById(record);
                        updateSettlementToArrived(record);
                    } else if ("CLOSED".equals(batchStatus)) {
                        record.setStatus(STATUS_FAILED);
                        record.setFailReason("微信批次已关闭");
                        transferMapper.updateById(record);
                    }
                }
            } catch (Exception e) {
                log.error("查询微信转账状态失败: transferNo={}, error={}", transferNo, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutTransfers(int timeoutMinutes, int batchSize) {
        List<PlatformTransferRecord> timeoutList = transferMapper.selectTimeoutTransfers(timeoutMinutes, batchSize);
        for (PlatformTransferRecord record : timeoutList) {
            try {
                queryAndUpdateTransferStatus(record.getTransferNo());
            } catch (Exception e) {
                log.error("超时补偿处理转账 {} 失败: {}", record.getTransferNo(), e.getMessage(), e);
            }
        }
        if (!timeoutList.isEmpty()) {
            log.info("超时转账补偿处理完成，处理数量: {}", timeoutList.size());
        }
    }

    // ==================== 内部方法 ====================

    private void doWxTransferAndUpdate(PlatformTransferRecord record, MerchantSettlementRecord settlement) {
        Date now = new Date();
        if (stubEnabled) {
            record.setStatus(STATUS_TRANSFERRING);
            record.setTransferTime(now);
            record.setWechatBatchNo("STUB_BATCH_" + record.getTransferNo());
            record.setWechatDetailNo("STUB_DETAIL_" + record.getTransferNo());
            transferMapper.updateById(record);

            settlement.setStatus("TRANSFERRING");
            settlement.setTransferTime(now);
            settlement.setWechatBatchNo(record.getWechatBatchNo());
            settlement.setWechatDetailNo(record.getWechatDetailNo());
            merchantSettlementService.updateById(settlement);
        } else {
            // 真实调用微信商家转账 API
            doRealWxTransfer(record);
            record.setStatus(STATUS_TRANSFERRING);
            record.setTransferTime(now);
            transferMapper.updateById(record);

            settlement.setStatus("TRANSFERRING");
            settlement.setTransferTime(now);
            settlement.setWechatBatchNo(record.getWechatBatchNo());
            settlement.setWechatDetailNo(record.getWechatDetailNo());
            merchantSettlementService.updateById(settlement);
        }
    }

    private void doWxTransferForDistributorAndUpdate(PlatformTransferRecord record, DistributorSettlementRecord settlement) {
        Date now = new Date();
        if (stubEnabled) {
            record.setStatus(STATUS_TRANSFERRING);
            record.setTransferTime(now);
            record.setWechatBatchNo("STUB_BATCH_" + record.getTransferNo());
            record.setWechatDetailNo("STUB_DETAIL_" + record.getTransferNo());
            transferMapper.updateById(record);

            settlement.setStatus("TRANSFERRING");
            settlement.setTransferTime(now);
            distributorSettlementService.updateById(settlement);
        } else {
            doRealWxTransfer(record);
            record.setStatus(STATUS_TRANSFERRING);
            record.setTransferTime(now);
            transferMapper.updateById(record);

            settlement.setStatus("TRANSFERRING");
            settlement.setTransferTime(now);
            distributorSettlementService.updateById(settlement);
        }
    }

    /**
     * 真实调用微信商家转账 API
     */
    private void doRealWxTransfer(PlatformTransferRecord record) {
        if (wxPayService == null) {
            throw new RuntimeException("WxPayService未配置，请检查微信支付配置或启用 stub 模式");
        }
        try {
            com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateRequest request =
                    new com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateRequest();
            request.setAppid(wxPayService.getConfig().getAppId());
            request.setOutBatchNo(record.getTransferNo());
            request.setBatchName("结算转账-" + record.getSettlementNo());
            request.setBatchRemark("商家/分销商结算打款");
            request.setTotalAmount(record.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            request.setTotalNum(1);

            com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateRequest.TransferDetailList detail =
                    new com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateRequest.TransferDetailList();
            detail.setOutDetailNo(record.getTransferNo());
            detail.setTransferAmount(record.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            detail.setOpenid(record.getReceiverOpenid());

            // RSA加密收款人真实姓名
            String receiverName = resolveReceiverName(record);
            if (receiverName != null) {
                detail.setUserName(encryptReceiverName(receiverName));
            } else {
                detail.setUserName(null);
            }

            detail.setTransferRemark("结算打款-" + record.getSettlementNo());
            request.setTransferDetailList(Collections.singletonList(detail));

            com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateResult result =
                    wxPayService.getMerchantTransferService().createTransfer(request);

            record.setWechatBatchNo(result.getBatchId());
            record.setWechatDetailNo(record.getTransferNo());
            log.info("微信商家转账发起成功: transferNo={}, batchId={}", record.getTransferNo(), result.getBatchId());
        } catch (Exception e) {
            log.error("微信商家转账发起失败: transferNo={}, error={}", record.getTransferNo(), e.getMessage(), e);
            record.setStatus(STATUS_FAILED);
            record.setFailReason("微信转账失败: " + e.getMessage());
            transferMapper.updateById(record);
            throw new RuntimeException("微信转账失败: " + e.getMessage(), e);
        }
    }

    private void updateSettlementToArrived(PlatformTransferRecord record) {
        Date now = new Date();
        if ("MERCHANT".equals(record.getTargetType())) {
            MerchantSettlementRecord s = merchantSettlementService.selectBySettlementNo(record.getSettlementNo());
            if (s != null) {
                s.setStatus("ARRIVED");
                s.setArriveTime(now);
                merchantSettlementService.updateById(s);
            }
        } else if ("DISTRIBUTOR".equals(record.getTargetType())) {
            DistributorSettlementRecord s = distributorSettlementService.selectBySettlementNo(record.getSettlementNo());
            if (s != null) {
                s.setStatus("ARRIVED");
                s.setArriveTime(now);
                distributorSettlementService.updateById(s);
            }
        }
    }

    /**
     * 解析收款人真实姓名（商家contact 或 分销商contact）
     */
    private String resolveReceiverName(PlatformTransferRecord record) {
        try {
            if ("MERCHANT".equals(record.getTargetType())) {
                Merchant m = merchantService.selectMerchantById(record.getTargetId());
                if (m != null && StringUtils.isNotBlank(m.getContact())) {
                    return m.getContact();
                }
            } else if ("DISTRIBUTOR".equals(record.getTargetType())) {
                Distributor d = distributorService.selectDistributorById(record.getTargetId());
                if (d != null && StringUtils.isNotBlank(d.getContact())) {
                    return d.getContact();
                }
            }
        } catch (Exception e) {
            log.warn("解析收款人姓名失败: transferNo={}, error={}", record.getTransferNo(), e.getMessage());
        }
        return null;
    }

    /**
     * RSA加密收款人姓名（微信V3要求 OAEP-SHA1-MGF1 填充）
     * 使用微信平台公钥证书加密
     */
    private String encryptReceiverName(String plainName) {
        try {
            java.security.cert.X509Certificate cert = wxPayService.getConfig().getVerifier().getValidCertificate();
            java.security.PublicKey publicKey = cert.getPublicKey();

            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plainName.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("RSA加密收款人姓名失败: error={}", e.getMessage(), e);
            return null;
        }
    }

    private String generateTransferNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "TRF" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

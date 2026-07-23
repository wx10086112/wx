package com.ruoyi.wxmini.service.impl;

import com.ruoyi.common.utils.DesensitizedUtil;
import com.ruoyi.mall.common.bo.WxMiniAuthContext;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import com.ruoyi.mall.common.util.WriteOffCodeGenerator;
import com.ruoyi.mall.finance.domain.TransactionRecord;
import com.ruoyi.mall.finance.domain.WithdrawRecord;
import com.ruoyi.mall.finance.mapper.TransactionRecordMapper;
import com.ruoyi.mall.finance.mapper.WithdrawRecordMapper;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantStore;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.merchant.mapper.MerchantStoreMapper;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import com.ruoyi.mall.order.constant.BookingStatus;
import com.ruoyi.mall.order.domain.BookingRecord;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderStatusHistory;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.BookingRecordMapper;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.OrderItemMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.order.service.IWriteOffService;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.mapper.ProductMapper;
import com.ruoyi.mall.user.domain.MallUser;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.mapper.MallUserMapper;
import com.ruoyi.mall.user.mapper.UserInfoMapper;
import com.ruoyi.wxmini.dto.merchant.*;
import com.ruoyi.wxmini.service.IMerchantMiniService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 商家端小程序 - 真实数据库实现
 * 商家小程序真实数据库服务实现。
 */
@Primary
@Service
public class MerchantMiniServiceImpl implements IMerchantMiniService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String GOODS_STATUS_ON_SHELF = "ON_SHELF";
    private static final String GOODS_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String STATUS_PENDING_VERIFY = "PENDING_VERIFY";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_REFUNDING = "REFUNDING";
    private static final String VERIFY_STATUS_SUCCESS = "SUCCESS";
    private static final String VERIFY_STATUS_FAILED = "FAILED";
    private static final String LEDGER_STATUS_PENDING = "PENDING";
    private static final String LEDGER_STATUS_SETTLED = "SETTLED";
    private static final String SETTLEMENT_STATUS_WAITING_T1 = "WAITING_T1";
    private static final String SETTLEMENT_STATUS_ARRIVED = "ARRIVED";
    private static final String WITHDRAW_STATUS_PROCESSING = "PROCESSING";

    // mall_order.status: 0待支付 1已支付 2已使用 3已完成 4已退款 5已取消
    private static final int ORDER_STATUS_PENDING = 0;
    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_USED = 2;
    private static final int ORDER_STATUS_COMPLETED = 3;
    private static final int ORDER_STATUS_REFUNDED = 4;
    private static final int ORDER_STATUS_CANCELLED = 5;

    // product.status: 0下架 1上架
    private static final int PRODUCT_STATUS_OFF = 0;
    private static final int PRODUCT_STATUS_ON = 1;

    // merchant_user.role: owner管理员 member成员
    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_MEMBER = "member";

    // withdraw_record.status: 0待审核 1审核通过 2已打款 3拒绝
    private static final int WITHDRAW_STATUS_PENDING_AUDIT = 0;
    private static final int WITHDRAW_STATUS_APPROVED = 1;
    private static final int WITHDRAW_STATUS_PAID = 2;
    private static final int WITHDRAW_STATUS_REJECTED = 3;

    private static final int MERCHANT_RATE = 90;
    private static final int PLATFORM_RATE = 10;
    private static final Pattern ORDER_NO_EXTRACT_PATTERN = Pattern.compile("(?:ORD|M)\\d{8,24}");

    @Resource
    private IWxMiniJwtService jwtService;
    @Resource
    private MerchantUserMapper merchantUserMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private MerchantStoreMapper merchantStoreMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private BookingRecordMapper bookingRecordMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private RefundRecordMapper refundRecordMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private TransactionRecordMapper transactionRecordMapper;
    @Resource
    private WithdrawRecordMapper withdrawRecordMapper;
    @Resource
    private MallUserMapper mallUserMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IWriteOffService writeOffService;
    @Resource
    private WriteOffCodeGenerator writeOffCodeGenerator;
    @Resource
    private ApplicationContext applicationContext;

    @org.springframework.beans.factory.annotation.Value("${ruoyi.profile}")
    private String profilePath;

    // ==================== 登录 ====================

    @Override
    public MerchantMiniLoginResponseDto login(String username, String password, Long merchantId, String appid) {
        MerchantUser merchantUser = merchantUserMapper.selectMerchantUserByUsername(username);
        if (merchantUser == null) {
            throw new IllegalArgumentException("商家账号不存在");
        }
        if (!Integer.valueOf(1).equals(merchantUser.getStatus())) {
            throw new IllegalArgumentException("商家账号已被禁用");
        }
        // BCrypt密码校验
        if (!com.ruoyi.common.utils.SecurityUtils.matchesPassword(password, merchantUser.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        Merchant merchant = merchantMapper.selectMerchantById(merchantUser.getMerchantId());
        if (merchant == null) {
            throw new IllegalArgumentException("商家不存在");
        }
        if (merchant.getStatus() == Merchant.STATUS_STOPPED) {
            throw new IllegalArgumentException("商家已停止合作，请联系平台");
        }
        if (merchant.getStatus() != Merchant.STATUS_NORMAL) {
            throw new IllegalArgumentException("商家未审核通过或已被禁用");
        }

        if (merchantId != null && !merchantId.equals(merchant.getId())) {
            throw new IllegalArgumentException("该账号不属于当前扫码商家");
        }

        // 二维码入口是主路径；没有入口ID时，按合并后的C端AppID识别，并兼容旧M端AppID。
        if (merchantId == null && StringUtils.isNotBlank(appid)) {
            Merchant appMerchant = merchantMapper.selectMerchantByCAppId(appid);
            if (appMerchant == null) {
                appMerchant = merchantMapper.selectMerchantByMAppId(appid);
            }
            if (appMerchant == null || !appMerchant.getId().equals(merchant.getId())) {
                throw new IllegalArgumentException("该账号不属于当前小程序所属商家");
            }
        }

        MerchantUser loginUpdate = new MerchantUser();
        loginUpdate.setId(merchantUser.getId());
        loginUpdate.setLastLoginTime(new Date());
        merchantUserMapper.updateMerchantUser(loginUpdate);
        merchantUser.setLastLoginTime(loginUpdate.getLastLoginTime());

        MerchantMiniStaffUserDto staffUser = buildStaffUserFromDb(merchantUser, merchant);

        WxMiniAuthContext authContext = new WxMiniAuthContext();
        authContext.setUserId(String.valueOf(merchantUser.getId()));
        authContext.setUserType(WxMiniAuthContext.USER_TYPE_MERCHANT_STAFF);
        authContext.setStaffId(merchantUser.getId());
        authContext.setMerchantId(merchant.getId());
        authContext.setRoleCodes(Arrays.asList(merchantUser.getRole()));
        authContext.setPermissionCodes(buildPermissions(merchantUser.getRole()));

        MerchantMiniLoginResponseDto responseDto = new MerchantMiniLoginResponseDto();
        responseDto.setToken(jwtService.createToken(authContext));
        responseDto.setStaffUser(staffUser);
        return responseDto;
    }

    // ==================== 工作台 ====================

    @Override
    public MerchantMiniOverviewDto getWorkbenchOverview(String currentUserId) {
        Long merchantId = getMerchantIdFromStaffId(currentUserId);
        MerchantMiniOverviewDto overviewDto = new MerchantMiniOverviewDto();

        // 员工信息
        MerchantUser merchantUser = merchantUserMapper.selectMerchantUserById(Long.valueOf(currentUserId));
        Merchant merchant = merchantMapper.selectMerchantById(merchantId);
        overviewDto.setStaffUser(buildStaffUserFromDb(merchantUser, merchant));

        // 门店信息
        overviewDto.setStoreInfo(buildStoreProfile(merchantId));

        // 统计数据
        overviewDto.setStats(buildWorkbenchStats(merchantId));

        // 待核销订单
        overviewDto.setPendingOrderList(buildPendingOrderList(merchantId));

        return overviewDto;
    }

    // ==================== 订单 ====================

    @Override
    public List<MerchantMiniOrderDto> listOrders(String status) {
        Long merchantId = getMerchantIdFromContext();
        List<MallOrder> orders = mallOrderMapper.selectMallOrderByMerchantId(merchantId);

        List<MerchantMiniOrderDto> result = new ArrayList<>();
        for (MallOrder order : orders) {
            String merchantStatus = mapOrderStatus(order.getStatus());
            if (StringUtils.isNotBlank(status) && !status.equals(merchantStatus)) {
                continue;
            }
            result.add(convertOrderToDto(order));
        }
        // 按创建时间倒序
        result.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
        return result;
    }

    @Override
    public List<MerchantMiniBookingDto> listBookings(String status) {
        Long merchantId = getMerchantIdFromContext();
        bookingRecordMapper.markExpiredPending(new Date());

        BookingRecord query = new BookingRecord();
        query.setMerchantId(merchantId);
        if (StringUtils.isNotBlank(status) && !"ALL".equalsIgnoreCase(status)) {
            query.setStatus(status);
        }
        List<BookingRecord> bookings = bookingRecordMapper.selectBookingRecordList(query);
        List<MerchantMiniBookingDto> result = new ArrayList<>();
        if (bookings != null) {
            for (BookingRecord booking : bookings) {
                result.add(convertBookingToDto(booking));
            }
        }
        result.sort((a, b) -> Long.compare(safeLong(b.getBookingTime()), safeLong(a.getBookingTime())));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniBookingDto confirmBooking(String bookingNo) {
        return updateBookingStatus(bookingNo, BookingStatus.CONFIRMED, BookingStatus.PENDING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniBookingDto completeBooking(String bookingNo) {
        return updateBookingStatus(bookingNo, BookingStatus.COMPLETED, BookingStatus.CONFIRMED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniBookingDto cancelBooking(String bookingNo) {
        return updateBookingStatus(bookingNo, BookingStatus.CANCELLED, BookingStatus.PENDING, BookingStatus.CONFIRMED);
    }

    private MerchantMiniBookingDto updateBookingStatus(String bookingNo, String targetStatus, String... allowedStatuses) {
        if (StringUtils.isBlank(bookingNo)) {
            throw new IllegalArgumentException("预点单号不能为空");
        }
        Long merchantId = getMerchantIdFromContext();
        bookingRecordMapper.markExpiredPending(new Date());
        BookingRecord booking = bookingRecordMapper.selectBookingRecordByBookingNo(bookingNo);
        if (booking == null || !merchantId.equals(booking.getMerchantId())) {
            throw new IllegalArgumentException("预点单记录不存在");
        }
        if (!Arrays.asList(allowedStatuses).contains(booking.getStatus())) {
            throw new IllegalArgumentException("当前预点单状态不可操作");
        }
        bookingRecordMapper.updateBookingStatus(bookingNo, targetStatus, new Date());
        return convertBookingToDto(bookingRecordMapper.selectBookingRecordByBookingNo(bookingNo));
    }

    @Override
    public MerchantMiniOrderDto getOrderDetail(String orderNo) {
        Long merchantId = getMerchantIdFromContext();
        MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return convertOrderToDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniOrderDto writeOff(String code, String currentUserId) {
        Long merchantId = getMerchantIdFromStaffId(currentUserId);
        String normalizedCode = normalizeVerifyInput(code);

        if (StringUtils.isBlank(normalizedCode)) {
            throw new IllegalArgumentException("请输入核销码或订单号");
        }

        MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(normalizedCode);
        if (order == null) {
            order = mallOrderMapper.selectOrderByWriteOffCode(normalizedCode);
        }

        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("未找到对应订单");
        }

        MerchantUser operator = merchantUserMapper.selectMerchantUserById(Long.valueOf(currentUserId));
        String operatorName = operator != null ? operator.getRealName() : "";
        writeOffService.writeOff(order.getWriteOffCode(), merchantId, Long.valueOf(currentUserId), operatorName);

        MallOrder latestOrder = mallOrderMapper.selectMallOrderByOrderNo(order.getOrderNo());
        if (latestOrder == null) {
            throw new IllegalArgumentException("核销成功，但订单读取失败");
        }
        return convertOrderToDto(latestOrder);
    }

    private String normalizeVerifyInput(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        String decoded = decodeRepeatedly(StringUtils.trimToEmpty(code));
        String normalizedWriteOffCode = writeOffCodeGenerator.normalize(decoded);
        if (writeOffCodeGenerator.isValid(normalizedWriteOffCode)) {
            return normalizedWriteOffCode;
        }

        String compact = decoded.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        Matcher orderNoMatcher = ORDER_NO_EXTRACT_PATTERN.matcher(compact);
        if (orderNoMatcher.find()) {
            return orderNoMatcher.group();
        }
        return compact;
    }

    private String decodeRepeatedly(String value) {
        String result = StringUtils.defaultString(value);
        for (int i = 0; i < 2; i++) {
            try {
                String decoded = URLDecoder.decode(result, StandardCharsets.UTF_8.name());
                if (decoded.equals(result)) {
                    break;
                }
                result = decoded;
            } catch (Exception ignored) {
                break;
            }
        }
        return result;
    }

    // ==================== 核销记录 ====================

    @Override
    public List<MerchantMiniVerifyRecordDto> listVerifyRecords(String status) {
        Long merchantId = getMerchantIdFromContext();
        // 查询已使用/已完成的订单作为核销记录
        List<MallOrder> orders = mallOrderMapper.selectMallOrderByMerchantId(merchantId);

        List<MerchantMiniVerifyRecordDto> result = new ArrayList<>();
        long recordId = 1L;
        for (MallOrder order : orders) {
            if (order.getStatus() != ORDER_STATUS_COMPLETED && order.getStatus() != ORDER_STATUS_USED) {
                continue;
            }
            MerchantMiniVerifyRecordDto record = buildVerifyRecordFromOrder(recordId++, order);
            if (StringUtils.isNotBlank(status) && !"ALL".equals(status) && !status.equals(record.getStatus())) {
                continue;
            }
            result.add(record);
        }
        // 按核销时间倒序
        result.sort((a, b) -> Long.compare(b.getVerifyTime(), a.getVerifyTime()));
        return result;
    }

    // ==================== 商品 ====================

    @Override
    public List<MerchantMiniGoodsDto> listGoods(String status) {
        Long merchantId = getMerchantIdFromContext();
        List<Product> products = productMapper.selectProductByMerchantId(merchantId);

        List<MerchantMiniGoodsDto> result = new ArrayList<>();
        for (Product product : products) {
            String goodsStatus = product.getStatus() != null && product.getStatus() == PRODUCT_STATUS_ON
                    ? GOODS_STATUS_ON_SHELF : GOODS_STATUS_OFF_SHELF;
            if (StringUtils.isNotBlank(status) && !"ALL".equals(status) && !status.equals(goodsStatus)) {
                continue;
            }
            MerchantMiniGoodsDto goodsDto = convertProductToDto(product);
            goodsDto.setImageUrl(appendListThumb(goodsDto.getImageUrl()));
            result.add(goodsDto);
        }
        // 按排序倒序
        result.sort((a, b) -> Integer.compare(b.getSort() != null ? b.getSort() : 0, a.getSort() != null ? a.getSort() : 0));
        return result;
    }

    @Override
    public MerchantMiniGoodsDto getGoodsDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        Long merchantId = getMerchantIdFromContext();
        Product product = productMapper.selectProductById(id);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("商品不存在");
        }
        return convertProductToDto(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniGoodsDto saveGoods(MerchantMiniGoodsDto goodsDto) {
        if (goodsDto == null || StringUtils.isBlank(goodsDto.getTitle())) {
            throw new IllegalArgumentException("套餐名称不能为空");
        }
        Long merchantId = getMerchantIdFromContext();

        if (goodsDto.getGoodsId() != null) {
            // 更新
            Product existing = productMapper.selectProductById(goodsDto.getGoodsId());
            if (existing == null || !existing.getMerchantId().equals(merchantId)) {
                throw new IllegalArgumentException("商品不存在");
            }
            Product update = convertDtoToProduct(goodsDto, merchantId);
            update.setId(goodsDto.getGoodsId());
            productMapper.updateProduct(update);
            Product saved = productMapper.selectProductById(goodsDto.getGoodsId());
            return convertProductToDto(saved);
        } else {
            // 新增
            Product product = convertDtoToProduct(goodsDto, merchantId);
            product.setStatus(PRODUCT_STATUS_OFF);
            product.setSales(0);
            productMapper.insertProduct(product);
            return convertProductToDto(product);
        }
    }

    @Override
    public MerchantMiniGoodsDto updateGoodsStatus(Long goodsId, String status) {
        if (goodsId == null) {
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        if (!GOODS_STATUS_ON_SHELF.equals(status) && !GOODS_STATUS_OFF_SHELF.equals(status)) {
            throw new IllegalArgumentException("套餐状态不合法");
        }

        Long merchantId = getMerchantIdFromContext();
        Product product = productMapper.selectProductById(goodsId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("套餐不存在");
        }

        Product update = new Product();
        update.setId(goodsId);
        update.setStatus(GOODS_STATUS_ON_SHELF.equals(status) ? PRODUCT_STATUS_ON : PRODUCT_STATUS_OFF);
        productMapper.updateProduct(update);

        product.setStatus(GOODS_STATUS_ON_SHELF.equals(status) ? PRODUCT_STATUS_ON : PRODUCT_STATUS_OFF);
        return convertProductToDto(product);
    }

    @Override
    public MerchantMiniUploadResultDto uploadGoodsImage(String fileName, Long size) {
        MerchantMiniUploadResultDto resultDto = new MerchantMiniUploadResultDto();
        String safeFileName = StringUtils.isBlank(fileName) ? "goods-image.jpg" : fileName.replace("\\", "_").replace("/", "_");
        resultDto.setFileName(safeFileName);
        resultDto.setSize(size == null ? 0L : size);
        resultDto.setUrl("/profile/merchant-goods/" + System.currentTimeMillis() + "_" + safeFileName);
        return resultDto;
    }

    @Override
    public MerchantMiniUploadResultDto uploadGoodsImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
        // magic byte 校验真实类型
        String ext;
        try {
            ext = detectImageExtension(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("文件读取失败");
        }
        if (ext == null) {
            throw new IllegalArgumentException("仅支持jpg/png/webp格式");
        }
        String contentType = file.getContentType();
        if (contentType != null && !isAllowedImageMimeType(contentType)) {
            throw new IllegalArgumentException("仅支持jpg/png/webp格式");
        }
        try {
            Long merchantId = getMerchantIdFromContext();
            String subDir = "merchant-goods/" + merchantId;
            Path basePath = Paths.get(profilePath).toAbsolutePath().normalize();
            Path dirPath = basePath.resolve(subDir).normalize();
            if (!dirPath.startsWith(basePath)) {
                throw new IllegalArgumentException("非法上传路径");
            }
            Files.createDirectories(dirPath);
            String fileName = subDir + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "." + ext;
            Path destPath = basePath.resolve(fileName).normalize();
            if (!destPath.startsWith(basePath)) {
                throw new IllegalArgumentException("非法上传路径");
            }
            file.transferTo(destPath.toFile());

            MerchantMiniUploadResultDto result = new MerchantMiniUploadResultDto();
            result.setFileName(fileName);
            result.setSize(file.getSize());
            result.setUrl("/profile/" + fileName);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据 magic byte 检测图片真实类型
     */
    private boolean isAllowedImageMimeType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }

    private String detectImageExtension(InputStream is) throws IOException {
        byte[] header = new byte[12];
        int read = is.read(header);
        if (read < 4) return null;
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) return "jpg";
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return "png";
        if (read >= 12 && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) return "webp";
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateGoodsStatus(List<Long> goodsIds, String status) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return 0;
        }
        if (!GOODS_STATUS_ON_SHELF.equals(status) && !GOODS_STATUS_OFF_SHELF.equals(status)) {
            throw new IllegalArgumentException("商品状态不合法");
        }
        Long merchantId = getMerchantIdFromContext();
        int dbStatus = GOODS_STATUS_ON_SHELF.equals(status) ? PRODUCT_STATUS_ON : PRODUCT_STATUS_OFF;
        // 单条SQL批量更新，消除N+1查询（原逻辑：每个ID各执行一次SELECT + UPDATE）
        return productMapper.batchUpdateProductStatus(goodsIds, dbStatus, merchantId);
    }

    // ==================== 门店 ====================

    @Override
    public MerchantMiniStoreDto getStoreProfile() {
        Long merchantId = getMerchantIdFromContext();
        return buildStoreProfile(merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniStoreDto updateStoreProfile(MerchantMiniStoreDto storeDto) {
        if (storeDto == null) {
            throw new IllegalArgumentException("门店信息不能为空");
        }
        Long merchantId = getMerchantIdFromContext();

        // 更新商家基本信息
        Merchant merchantUpdate = new Merchant();
        merchantUpdate.setId(merchantId);
        if (StringUtils.isNotBlank(storeDto.getStoreName())) {
            merchantUpdate.setName(storeDto.getStoreName());
        }
        if (StringUtils.isNotBlank(storeDto.getPhone())) {
            merchantUpdate.setPhone(storeDto.getPhone());
        }
        if (StringUtils.isNotBlank(storeDto.getAddress())) {
            merchantUpdate.setAddress(storeDto.getAddress());
        }
        if (StringUtils.isNotBlank(storeDto.getBusinessHours())) {
            merchantUpdate.setBusinessHours(storeDto.getBusinessHours());
        }
        if (StringUtils.isNotBlank(storeDto.getBrandSlogan())) {
            merchantUpdate.setDescription(storeDto.getBrandSlogan());
        }
        if (storeDto.getSupportRefund() != null) {
            merchantUpdate.setSupportRefund(Boolean.TRUE.equals(storeDto.getSupportRefund()) ? 1 : 0);
        }
        if (storeDto.getSupportBooking() != null) {
            merchantUpdate.setSupportBooking(Boolean.TRUE.equals(storeDto.getSupportBooking()) ? 1 : 0);
        }
        merchantMapper.updateMerchant(merchantUpdate);

        // 更新主门店信息
        Long storeId = storeDto.getStoreId();
        if (storeId == null) {
            List<MerchantStore> stores = merchantStoreMapper.selectMerchantStoreByMerchantId(merchantId);
            if (!stores.isEmpty()) {
                storeId = stores.get(0).getId();
            }
        }
        if (storeId != null) {
            MerchantStore storeUpdate = new MerchantStore();
            storeUpdate.setId(storeId);
            if (StringUtils.isNotBlank(storeDto.getStoreName())) {
                storeUpdate.setName(storeDto.getStoreName());
            }
            if (StringUtils.isNotBlank(storeDto.getPhone())) {
                storeUpdate.setPhone(storeDto.getPhone());
            }
            if (StringUtils.isNotBlank(storeDto.getAddress())) {
                storeUpdate.setAddress(storeDto.getAddress());
            }
            if (StringUtils.isNotBlank(storeDto.getBusinessHours())) {
                storeUpdate.setBusinessHours(storeDto.getBusinessHours());
            }
            if (storeDto.getBusinessStatus() != null) {
                storeUpdate.setStatus(Boolean.TRUE.equals(storeDto.getBusinessStatus()) ? 1 : 0);
            }
            merchantStoreMapper.updateMerchantStore(storeUpdate);
        }

        return buildStoreProfile(merchantId);
    }

    // ==================== 员工 ====================

    @Override
    public List<MerchantMiniStaffUserDto> listStaff() {
        Long merchantId = getMerchantIdFromContext();
        List<MerchantUser> users = merchantUserMapper.selectMerchantUserByMerchantId(merchantId);

        List<MerchantMiniStaffUserDto> result = new ArrayList<>();
        for (MerchantUser user : users) {
            result.add(buildStaffUserFromDb(user, null));
        }
        return result;
    }

    @Override
    public List<MerchantMiniStaffUserDto> updateStaffPermission(MerchantMiniStaffPermissionRequestDto requestDto) {
        if (requestDto == null || requestDto.getStaffId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        Long merchantId = getMerchantIdFromContext();
        MerchantUser target = merchantUserMapper.selectMerchantUserById(requestDto.getStaffId());
        if (target == null || !target.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("员工不存在");
        }

        MerchantUser update = new MerchantUser();
        update.setId(requestDto.getStaffId());
        if (StringUtils.isNotBlank(requestDto.getStatus())) {
            update.setStatus("ACTIVE".equals(requestDto.getStatus()) ? 1 : 0);
        }
        merchantUserMapper.updateMerchantUser(update);

        return listStaff();
    }

    // ==================== 财务 ====================

    @Override
    public MerchantMiniFinanceOverviewDto getFinanceOverview() {
        MerchantMiniSettlementOverviewDto settlementOverview = getSettlementOverview();
        MerchantMiniFinanceOverviewDto overviewDto = new MerchantMiniFinanceOverviewDto();
        overviewDto.setTodayIncomeAmount(settlementOverview.getTodayIncomeAmount());
        overviewDto.setMonthIncomeAmount(settlementOverview.getMonthIncomeAmount());
        overviewDto.setPendingSettleAmount(settlementOverview.getPendingSettleAmount());
        overviewDto.setWithdrawableAmount(settlementOverview.getSettledAmount());
        overviewDto.setPlatformFeeAmount(settlementOverview.getPlatformFeeAmount());
        overviewDto.setCompletedOrderCount(settlementOverview.getCompletedOrderCount());
        overviewDto.setLedgerList(settlementOverview.getLedgerList());
        overviewDto.setWithdrawList(new ArrayList<>());
        return overviewDto;
    }

    @Override
    public MerchantMiniSettlementOverviewDto getSettlementOverview() {
        Long merchantId = getMerchantIdFromContext();

        // SQL聚合查询各项统计（替代全量加载Java循环）
        long completedOrderCount = mallOrderMapper.countCompletedOrdersByMerchantId(merchantId);
        BigDecimal todayIncomeSql = mallOrderMapper.sumTodayIncomeByMerchantId(merchantId);
        BigDecimal monthIncomeSql = mallOrderMapper.sumMonthIncomeByMerchantId(merchantId);
        Long todayIncomeAmount = todayIncomeSql.multiply(BigDecimal.valueOf(100)).longValue();
        Long monthIncomeAmount = monthIncomeSql.multiply(BigDecimal.valueOf(100)).longValue();

        // 查询已完成/已使用的订单总金额，用于计算平台费和分账
        List<Integer> completedStatuses = Arrays.asList(ORDER_STATUS_COMPLETED, ORDER_STATUS_USED);
        BigDecimal totalPayAmount = mallOrderMapper.sumPayAmountByMerchantIdAndStatuses(merchantId, completedStatuses);
        long totalPayAmtCents = totalPayAmount.multiply(BigDecimal.valueOf(100)).longValue();
        BigDecimal totalMerchantBd = BigDecimal.valueOf(totalPayAmtCents)
                .multiply(BigDecimal.valueOf(MERCHANT_RATE))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        Long totalMerchantAmt = totalMerchantBd.longValue();
        Long platformFeeAmount = totalPayAmtCents - totalMerchantAmt;

        // 加载最近50条已完成订单构建分账明细列表
        List<MallOrder> recentOrders = mallOrderMapper.selectRecentCompletedOrdersByMerchantId(merchantId, 50);
        List<MerchantMiniFinanceLedgerDto> ledgerList = new ArrayList<>();
        List<MerchantMiniSettlementRecordDto> settlementRecordList = new ArrayList<>();
        Long pendingSettleAmount = 0L;
        Long settledAmount = 0L;
        long ledgerId = 1L;
        long now = System.currentTimeMillis();

        for (MallOrder order : recentOrders) {
            Long payAmount = order.getPayAmount() != null ? order.getPayAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0L;
            BigDecimal payBd = BigDecimal.valueOf(payAmount);
            BigDecimal merchantBd = payBd.multiply(BigDecimal.valueOf(MERCHANT_RATE))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            Long merchantAmt = merchantBd.longValue();
            Long platformFee = payAmount - merchantAmt;

            Date finishDate = order.getCompleteTime() != null ? order.getCompleteTime() : order.getUseTime();
            Long finishTime = finishDate != null ? finishDate.getTime() : 0L;
            Long settleTime = finishTime + 24 * 60 * 60 * 1000L;
            boolean settled = settleTime <= now;
            if (settled) {
                settledAmount += merchantAmt;
            } else {
                pendingSettleAmount += merchantAmt;
            }

            MerchantMiniFinanceLedgerDto ledger = new MerchantMiniFinanceLedgerDto();
            ledger.setLedgerId(ledgerId++);
            ledger.setOrderNo(order.getOrderNo());
            ledger.setOrderAmount(payAmount);
            ledger.setMerchantAmount(merchantAmt);
            ledger.setPlatformFeeAmount(platformFee);
            ledger.setFinishTime(finishTime);
            ledger.setSettleTime(settleTime);
            ledger.setStatus(settled ? LEDGER_STATUS_SETTLED : LEDGER_STATUS_PENDING);
            ledgerList.add(ledger);
            settlementRecordList.add(buildSettlementRecord(ledger));
        }

        MerchantMiniSettlementOverviewDto overviewDto = new MerchantMiniSettlementOverviewDto();
        overviewDto.setTodayIncomeAmount(todayIncomeAmount);
        overviewDto.setMonthIncomeAmount(monthIncomeAmount);
        overviewDto.setPendingSettleAmount(pendingSettleAmount);
        overviewDto.setSettledAmount(settledAmount);
        overviewDto.setProcessingAmount(0L);
        overviewDto.setPendingAutoTransferAmount(pendingSettleAmount);
        overviewDto.setPlatformFeeAmount(platformFeeAmount);
        overviewDto.setCompletedOrderCount((int) completedOrderCount);
        overviewDto.setAutoTransferMode("微信支付商户结算");
        overviewDto.setNextAutoTransferTime(nextAutoTransferTime());
        overviewDto.setSettlementAccount(buildSettlementAccount(merchantId));
        overviewDto.setSettlementRecordList(settlementRecordList);
        overviewDto.setLedgerList(ledgerList);
        return overviewDto;
    }

    @Override
    public MerchantMiniWithdrawRecordDto applyWithdraw(Long amount) {
        throw new IllegalArgumentException("当前使用微信支付商户号结算，无需平台提现");
    }

    // ==================== 订单操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniOrderDto acceptOrder(String orderNo) {
        Long merchantId = getMerchantIdFromContext();
        MallOrder order = getOrderAndCheckMerchant(orderNo, merchantId);
        if (order.getStatus() != ORDER_STATUS_PENDING && order.getStatus() != ORDER_STATUS_PAID) {
            throw new IllegalArgumentException("当前订单状态不可接单");
        }
        MallOrder update = new MallOrder();
        update.setId(order.getId());
        update.setStatus(ORDER_STATUS_PAID);
        int rows = mallOrderMapper.updateMallOrder(update);
        if (rows > 0 && order.getStatus() != ORDER_STATUS_PAID) {
            recordMerchantOrderHistory(order, order.getStatus(), ORDER_STATUS_PAID,
                    "MERCHANT_ACCEPT", null);
        }
        order.setStatus(ORDER_STATUS_PAID);
        return convertOrderToDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniOrderDto rejectOrder(String orderNo, String reason) {
        Long merchantId = getMerchantIdFromContext();
        MallOrder order = getOrderAndCheckMerchant(orderNo, merchantId);
        if (order.getStatus() != ORDER_STATUS_PENDING && order.getStatus() != ORDER_STATUS_PAID) {
            throw new IllegalArgumentException("当前订单状态不可拒单");
        }
        if (order.getStatus() == ORDER_STATUS_PAID) {
            return requestMerchantFullRefund(order, reason, "MERCHANT_REJECT_REFUND", "商家拒单退款");
        }
        MallOrder update = new MallOrder();
        update.setId(order.getId());
        update.setStatus(ORDER_STATUS_CANCELLED);
        update.setCancelTime(new Date());
        int rows = mallOrderMapper.updateMallOrder(update);
        if (rows > 0) {
            recordMerchantOrderHistory(order, order.getStatus(), ORDER_STATUS_CANCELLED,
                    "MERCHANT_REJECT", reason);
        }
        order.setStatus(ORDER_STATUS_CANCELLED);
        order.setCancelTime(update.getCancelTime());
        return convertOrderToDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniOrderDto cancelOrder(String orderNo, String reason) {
        Long merchantId = getMerchantIdFromContext();
        MallOrder order = getOrderAndCheckMerchant(orderNo, merchantId);
        if (order.getStatus() != ORDER_STATUS_PENDING && order.getStatus() != ORDER_STATUS_PAID) {
            throw new IllegalArgumentException("当前订单状态不可取消");
        }
        if (order.getStatus() == ORDER_STATUS_PAID) {
            return requestMerchantFullRefund(order, reason, "MERCHANT_CANCEL_REFUND", "商家取消订单退款");
        }
        MallOrder update = new MallOrder();
        update.setId(order.getId());
        update.setStatus(ORDER_STATUS_CANCELLED);
        update.setCancelTime(new Date());
        int rows = mallOrderMapper.updateMallOrder(update);
        if (rows > 0) {
            recordMerchantOrderHistory(order, order.getStatus(), ORDER_STATUS_CANCELLED,
                    "MERCHANT_CANCEL", reason);
        }
        order.setStatus(ORDER_STATUS_CANCELLED);
        order.setCancelTime(update.getCancelTime());
        return convertOrderToDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniOrderDto approveRefund(String orderNo) {
        Long merchantId = getMerchantIdFromContext();
        MallOrder order = getOrderAndCheckMerchant(orderNo, merchantId);
        if (order.getStatus() != ORDER_STATUS_PAID) {
            throw new IllegalArgumentException("当前订单状态不可同意退款");
        }
        RefundRecord refundRecord = findPendingRefundRecord(orderNo, merchantId);
        refundRecord.setStatus(RefundRecord.STATUS_APPROVED);
        refundRecord.setOperator(currentOperator());
        refundRecord.setAuditTime(new Date());
        if (refundRecord.getParams() == null) {
            refundRecord.setParams(new HashMap<>());
        }
        refundRecord.getParams().put("oldStatus", RefundRecord.STATUS_PENDING);
        int rows = refundRecordMapper.updateRefundRecord(refundRecord);
        if (rows == 0) {
            throw new IllegalArgumentException("退款记录状态已变更，请刷新后重试");
        }
        recordMerchantOrderHistory(order, order.getStatus(), order.getStatus(),
                "REFUND_APPROVE", null);
        publishRefundApprovedAfterCommit(orderNo, refundRecord.getId(), currentOperator());
        return convertOrderToDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniOrderDto rejectRefund(String orderNo, String reason) {
        Long merchantId = getMerchantIdFromContext();
        MallOrder order = getOrderAndCheckMerchant(orderNo, merchantId);
        if (order.getStatus() != ORDER_STATUS_PAID) {
            throw new IllegalArgumentException("当前订单状态不可拒绝退款");
        }
        RefundRecord refundRecord = findPendingRefundRecord(orderNo, merchantId);
        refundRecord.setStatus(RefundRecord.STATUS_REJECTED);
        refundRecord.setRejectReason(StringUtils.isNotBlank(reason) ? reason : "商家拒绝退款");
        refundRecord.setOperator(currentOperator());
        refundRecord.setAuditTime(new Date());
        if (refundRecord.getParams() == null) {
            refundRecord.setParams(new HashMap<>());
        }
        refundRecord.getParams().put("oldStatus", RefundRecord.STATUS_PENDING);
        int rows = refundRecordMapper.updateRefundRecord(refundRecord);
        if (rows == 0) {
            throw new IllegalArgumentException("退款记录状态已变更，请刷新后重试");
        }
        recordMerchantOrderHistory(order, order.getStatus(), order.getStatus(),
                "REFUND_REJECT", refundRecord.getRejectReason());
        return convertOrderToDto(order);
    }

    private void recordMerchantOrderHistory(MallOrder order, Integer fromStatus, Integer toStatus,
                                            String action, String remark) {
        Long operatorId = com.ruoyi.mall.common.util.WxMiniUserContext.getCurrentStaffId();
        String operatorName = currentOperator();
        mallOrderService.recordOrderStatusHistory(order, fromStatus, toStatus, action,
                "MERCHANT_MINI", operatorId, operatorName, remark);
    }

    private MerchantMiniOrderDto requestMerchantFullRefund(MallOrder order, String reason,
                                                           String action, String defaultReason) {
        String refundReason = normalizeMerchantRefundReason(reason, defaultReason);
        RefundRecord activeRefund = findActiveRefundRecord(order.getOrderNo(), order.getMerchantId());
        if (activeRefund != null) {
            if (activeRefund.getStatus() != null && activeRefund.getStatus() == RefundRecord.STATUS_PENDING) {
                approveExistingRefundRecord(activeRefund, order, action, refundReason);
            }
            return convertOrderToDto(order);
        }

        Date now = new Date();
        MallOrder update = new MallOrder();
        update.setId(order.getId());
        update.setCancelTime(now);
        mallOrderMapper.updateMallOrder(update);
        order.setCancelTime(now);

        String operator = currentOperator();
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setOrderNo(order.getOrderNo());
        refundRecord.setRefundNo(generateRefundNo());
        refundRecord.setMerchantId(order.getMerchantId());
        refundRecord.setUserId(order.getUserId());
        refundRecord.setRefundAmount(order.getPayAmount());
        refundRecord.setRefundReason(refundReason);
        refundRecord.setRefundType(2);
        refundRecord.setStatus(RefundRecord.STATUS_APPROVED);
        refundRecord.setOperator(operator);
        refundRecord.setAuditTime(now);
        refundRecordMapper.insertRefundRecord(refundRecord);

        recordMerchantOrderHistory(order, order.getStatus(), order.getStatus(), action, refundReason);
        publishRefundApprovedAfterCommit(order.getOrderNo(), refundRecord.getId(), operator);
        return convertOrderToDto(order);
    }

    private void approveExistingRefundRecord(RefundRecord refundRecord, MallOrder order,
                                             String action, String refundReason) {
        String operator = currentOperator();
        refundRecord.setStatus(RefundRecord.STATUS_APPROVED);
        refundRecord.setRefundReason(refundReason);
        refundRecord.setOperator(operator);
        refundRecord.setAuditTime(new Date());
        if (refundRecord.getParams() == null) {
            refundRecord.setParams(new HashMap<>());
        }
        refundRecord.getParams().put("oldStatus", RefundRecord.STATUS_PENDING);
        int rows = refundRecordMapper.updateRefundRecord(refundRecord);
        if (rows == 0) {
            throw new IllegalArgumentException("退款记录状态已变更，请刷新后重试");
        }
        recordMerchantOrderHistory(order, order.getStatus(), order.getStatus(), action, refundReason);
        publishRefundApprovedAfterCommit(order.getOrderNo(), refundRecord.getId(), operator);
    }

    private RefundRecord findActiveRefundRecord(String orderNo, Long merchantId) {
        RefundRecord query = new RefundRecord();
        query.setOrderNo(orderNo);
        query.setMerchantId(merchantId);
        List<RefundRecord> records = refundRecordMapper.selectRefundRecordList(query);
        if (records != null) {
            for (RefundRecord record : records) {
                if (orderNo.equals(record.getOrderNo())
                        && merchantId.equals(record.getMerchantId())
                        && record.getStatus() != null
                        && (record.getStatus() == RefundRecord.STATUS_PENDING
                        || record.getStatus() == RefundRecord.STATUS_APPROVED)) {
                    return record;
                }
            }
        }
        return null;
    }

    private String normalizeMerchantRefundReason(String reason, String defaultReason) {
        String normalized = StringUtils.isNotBlank(reason) ? reason.trim() : defaultReason;
        return normalized.length() > 200 ? normalized.substring(0, 200) : normalized;
    }

    private String generateRefundNo() {
        return "RF" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private void publishRefundApprovedAfterCommit(String orderNo, Long refundRecordId, String operator) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    applicationContext.publishEvent(new RefundApprovedEvent(
                            MerchantMiniServiceImpl.this, orderNo, refundRecordId, operator));
                }
            });
            return;
        }
        applicationContext.publishEvent(new RefundApprovedEvent(this, orderNo, refundRecordId, operator));
    }

    private RefundRecord findPendingRefundRecord(String orderNo, Long merchantId) {
        RefundRecord query = new RefundRecord();
        query.setOrderNo(orderNo);
        query.setMerchantId(merchantId);
        List<RefundRecord> records = refundRecordMapper.selectRefundRecordList(query);
        if (records != null) {
            for (RefundRecord record : records) {
                if (orderNo.equals(record.getOrderNo())
                        && merchantId.equals(record.getMerchantId())
                        && record.getStatus() != null
                        && record.getStatus() == RefundRecord.STATUS_PENDING) {
                    return record;
                }
            }
        }
        throw new IllegalArgumentException("未找到待审核退款申请");
    }

    private String currentOperator() {
        Long staffId = com.ruoyi.mall.common.util.WxMiniUserContext.getCurrentStaffId();
        if (staffId != null) {
            return String.valueOf(staffId);
        }
        String userId = com.ruoyi.mall.common.util.WxMiniUserContext.getCurrentUserId();
        return StringUtils.defaultIfBlank(userId, "merchant-mini");
    }

    private MallOrder getOrderAndCheckMerchant(String orderNo, Long merchantId) {
        MallOrder order = mallOrderMapper.selectMallOrderByOrderNoForUpdate(orderNo);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    // ==================== 员工增删改 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniStaffUserDto addStaff(MerchantMiniStaffRequestDto requestDto) {
        if (requestDto == null || StringUtils.isBlank(requestDto.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (StringUtils.isBlank(requestDto.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        Long merchantId = getMerchantIdFromContext();

        // 检查用户名是否已存在
        MerchantUser existing = merchantUserMapper.selectMerchantUserByUsername(requestDto.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        MerchantUser newUser = new MerchantUser();
        newUser.setMerchantId(merchantId);
        newUser.setUsername(requestDto.getUsername());
        newUser.setPassword(com.ruoyi.common.utils.SecurityUtils.encryptPassword(requestDto.getPassword()));
        newUser.setRealName(StringUtils.isNotBlank(requestDto.getRealName()) ? requestDto.getRealName() : requestDto.getUsername());
        newUser.setPhone(requestDto.getPhone());
        newUser.setRole(StringUtils.isNotBlank(requestDto.getRole()) && ROLE_OWNER.equals(requestDto.getRole()) ? ROLE_OWNER : ROLE_MEMBER);
        newUser.setStatus(1);
        merchantUserMapper.insertMerchantUser(newUser);

        return buildStaffUserFromDb(newUser, merchantMapper.selectMerchantById(merchantId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantMiniStaffUserDto updateStaff(MerchantMiniStaffRequestDto requestDto) {
        if (requestDto == null || requestDto.getStaffId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        Long merchantId = getMerchantIdFromContext();
        MerchantUser existing = merchantUserMapper.selectMerchantUserById(requestDto.getStaffId());
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("员工不存在");
        }

        MerchantUser update = new MerchantUser();
        update.setId(requestDto.getStaffId());
        if (StringUtils.isNotBlank(requestDto.getRealName())) {
            update.setRealName(requestDto.getRealName());
        }
        if (StringUtils.isNotBlank(requestDto.getPhone())) {
            update.setPhone(requestDto.getPhone());
        }
        if (StringUtils.isNotBlank(requestDto.getRole())) {
            update.setRole(ROLE_OWNER.equals(requestDto.getRole()) ? ROLE_OWNER : ROLE_MEMBER);
        }
        if (StringUtils.isNotBlank(requestDto.getPassword())) {
            update.setPassword(com.ruoyi.common.utils.SecurityUtils.encryptPassword(requestDto.getPassword()));
        }
        merchantUserMapper.updateMerchantUser(update);

        MerchantUser updated = merchantUserMapper.selectMerchantUserById(requestDto.getStaffId());
        return buildStaffUserFromDb(updated, merchantMapper.selectMerchantById(merchantId));
    }

    // ==================== 辅助方法 ====================

    private Long getMerchantIdFromContext() {
        // 优先从JWT token获取
        Long merchantId = com.ruoyi.mall.common.util.WxMiniUserContext.getCurrentMerchantId();
        if (merchantId != null) {
            return merchantId;
        }
        // 其次取扫码/入口指定的商家
        merchantId = com.ruoyi.mall.common.util.WxMiniUserContext.getMerchantEntryId();
        if (merchantId != null) {
            return merchantId;
        }
        // 回退到AppID方式
        merchantId = com.ruoyi.mall.common.util.WxMiniUserContext.getAppIdMerchantId();
        if (merchantId != null) {
            return merchantId;
        }
        throw new IllegalStateException("无法获取当前商家ID");
    }

    private Long getMerchantIdFromStaffId(String staffIdStr) {
        Long staffId = Long.valueOf(staffIdStr);
        MerchantUser user = merchantUserMapper.selectMerchantUserById(staffId);
        if (user == null) {
            throw new IllegalStateException("找不到商家员工");
        }
        return user.getMerchantId();
    }

    private String mapOrderStatus(Integer dbStatus) {
        if (dbStatus == null) return STATUS_PENDING_VERIFY;
        switch (dbStatus) {
            case ORDER_STATUS_PAID:
            case ORDER_STATUS_USED:
                return STATUS_PENDING_VERIFY;
            case ORDER_STATUS_COMPLETED:
                return STATUS_COMPLETED;
            case ORDER_STATUS_REFUNDED:
                return "REFUNDED";
            case ORDER_STATUS_CANCELLED:
                return "CANCELLED";
            default:
                return "OTHER";
        }
    }

    private Integer mapStatusToDb(String merchantStatus) {
        if (merchantStatus == null) return null;
        switch (merchantStatus) {
            case STATUS_PENDING_VERIFY:
                return ORDER_STATUS_PAID;
            case STATUS_COMPLETED:
                return ORDER_STATUS_COMPLETED;
            case STATUS_REFUNDING:
                return ORDER_STATUS_REFUNDED;
            default:
                return null;
        }
    }

    private MerchantMiniOrderDto convertOrderToDto(MallOrder order) {
        MerchantMiniOrderDto dto = new MerchantMiniOrderDto();
        dto.setOrderId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(mapOrderStatus(order.getStatus()));
        dto.setWriteOffCode(order.getWriteOffCode());
        dto.setPayAmount(order.getPayAmount() != null ? order.getPayAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0L);
        dto.setQuantity(1);
        dto.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().getTime() : 0L);
        dto.setPayTime(order.getPayTime() != null ? order.getPayTime().getTime() : null);
        dto.setVerifyTime(order.getUseTime() != null ? order.getUseTime().getTime() : null);
        dto.setCancelTime(order.getCancelTime() != null ? order.getCancelTime().getTime() : null);
        dto.setRefundTime(order.getRefundTime() != null ? order.getRefundTime().getTime() : null);
        dto.setRemark(order.getRemark());

        // 查订单商品明细取商品名
        List<OrderItem> items = orderItemMapper.selectOrderItemByOrderId(order.getId());
        if (!items.isEmpty()) {
            OrderItem firstItem = items.get(0);
            dto.setGoodsId(firstItem.getProductId());
            dto.setTitle(firstItem.getProductName());
        }

        CustomerSnapshot customer = resolveCustomerSnapshot(order);
        dto.setCustomerName(customer.name);
        dto.setCustomerPhone(maskPhone(customer.phone));

        enrichRefundInfo(dto, order);
        dto.setHistory(buildMerchantOrderHistory(order.getOrderNo()));

        return dto;
    }

    private void enrichRefundInfo(MerchantMiniOrderDto dto, MallOrder order) {
        RefundRecord refundRecord = latestRefundRecord(order.getOrderNo());
        if (refundRecord == null) {
            return;
        }
        dto.setRefundReason(refundRecord.getRefundReason());
        if (refundRecord.getStatus() != null
                && (refundRecord.getStatus() == RefundRecord.STATUS_PENDING
                || refundRecord.getStatus() == RefundRecord.STATUS_APPROVED)) {
            dto.setStatus(STATUS_REFUNDING);
        }
        if (refundRecord.getStatus() != null && refundRecord.getStatus() == RefundRecord.STATUS_REJECTED) {
            dto.setRefundRejectReason(refundRecord.getRejectReason());
            dto.setRefundRejectTime(refundRecord.getAuditTime() != null ? refundRecord.getAuditTime().getTime() : null);
        }
        if (refundRecord.getStatus() != null && refundRecord.getStatus() == RefundRecord.STATUS_REFUNDED) {
            dto.setStatus("REFUNDED");
            dto.setRefundTime(refundRecord.getRefundTime() != null ? refundRecord.getRefundTime().getTime() : dto.getRefundTime());
        }
    }

    private RefundRecord latestRefundRecord(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }
        RefundRecord query = new RefundRecord();
        query.setOrderNo(orderNo);
        List<RefundRecord> records = refundRecordMapper.selectRefundRecordList(query);
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    private List<MerchantMiniOrderDto.HistoryItem> buildMerchantOrderHistory(String orderNo) {
        List<MerchantMiniOrderDto.HistoryItem> result = new ArrayList<>();
        if (StringUtils.isBlank(orderNo)) {
            return result;
        }
        List<MallOrderStatusHistory> histories = mallOrderService.selectOrderStatusHistory(orderNo);
        if (histories == null) {
            return result;
        }
        for (MallOrderStatusHistory history : histories) {
            MerchantMiniOrderDto.HistoryItem item = new MerchantMiniOrderDto.HistoryItem();
            item.setFromStatus(history.getFromStatus());
            item.setToStatus(history.getToStatus());
            item.setStatus(mapOrderStatus(history.getToStatus()));
            item.setAction(history.getAction());
            item.setSource(history.getSource());
            item.setOperatorName(history.getOperatorName());
            item.setRemark(history.getRemark());
            item.setChangeTime(history.getChangeTime() != null ? history.getChangeTime().getTime() : null);
            result.add(item);
        }
        return result;
    }

    private MerchantMiniGoodsDto convertProductToDto(Product product) {
        MerchantMiniGoodsDto dto = new MerchantMiniGoodsDto();
        dto.setGoodsId(product.getId());
        dto.setTitle(product.getName());
        dto.setSubtitle(product.getDescription());
        dto.setImageUrl(product.getCoverImage());
        dto.setPrice(product.getPrice() != null ? product.getPrice().multiply(BigDecimal.valueOf(100)).longValue() : 0L);
        dto.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().multiply(BigDecimal.valueOf(100)).longValue() : 0L);
        dto.setStock(product.getStock());
        dto.setSales(product.getSales());
        dto.setStatus(product.getStatus() != null && product.getStatus() == PRODUCT_STATUS_ON
                ? GOODS_STATUS_ON_SHELF : GOODS_STATUS_OFF_SHELF);
        dto.setSort(product.getSort());
        if (product.getValidDays() != null && product.getValidDays() > 0) {
            dto.setValidPeriod(String.valueOf(product.getValidDays()));
        }
        dto.setVerifyNotice(StringUtils.defaultIfBlank(product.getVerifyNotice(), "到店出示核销码即可使用"));
        return dto;
    }

    private String appendListThumb(String imageUrl) {
        if (StringUtils.isBlank(imageUrl)
                || (!imageUrl.contains("/profile/merchant_images/") && !imageUrl.contains("/profile/merchant-goods/"))
                || imageUrl.contains("?thumb=")
                || imageUrl.contains("&thumb=")) {
            return imageUrl;
        }
        return imageUrl + (imageUrl.contains("?") ? "&" : "?") + "thumb=list";
    }

    private Product convertDtoToProduct(MerchantMiniGoodsDto dto, Long merchantId) {
        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setName(dto.getTitle());
        product.setDescription(dto.getSubtitle());
        product.setCoverImage(dto.getImageUrl());
        product.setPrice(dto.getPrice() != null ? BigDecimal.valueOf(dto.getPrice()).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        product.setOriginalPrice(dto.getOriginalPrice() != null ? BigDecimal.valueOf(dto.getOriginalPrice()).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
        product.setValidDays(parseValidDays(dto.getValidPeriod()));
        product.setVerifyNotice(StringUtils.defaultIfBlank(dto.getVerifyNotice(), "到店出示核销码即可使用"));
        product.setSort(dto.getSort());
        return product;
    }

    private Integer parseValidDays(String validPeriod) {
        if (StringUtils.isBlank(validPeriod)) {
            return 30;
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < validPeriod.length(); i++) {
            char ch = validPeriod.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() == 0) {
            return 30;
        }
        int days = Integer.parseInt(digits.toString());
        return Math.max(1, Math.min(days, 3650));
    }

    private MerchantMiniStoreDto buildStoreProfile(Long merchantId) {
        Merchant merchant = merchantMapper.selectMerchantById(merchantId);
        List<MerchantStore> stores = merchantStoreMapper.selectMerchantStoreByMerchantId(merchantId);
        MerchantStore mainStore = stores.isEmpty() ? null : stores.get(0);

        MerchantMiniStoreDto dto = new MerchantMiniStoreDto();
        dto.setMerchantId(merchantId);
        dto.setMerchantName(merchant != null ? merchant.getName() : "");
        dto.setStoreName(merchant != null ? merchant.getName() : "");
        dto.setBrandSlogan(merchant != null ? merchant.getDescription() : "");
        dto.setBusinessHours(merchant != null ? merchant.getBusinessHours() : "");
        dto.setPhone(merchant != null ? merchant.getPhone() : "");
        dto.setAddress(merchant != null ? merchant.getAddress() : "");
        dto.setBusinessStatus(mainStore == null || mainStore.getStatus() == null || mainStore.getStatus() == 1);
        dto.setSupportRefund(merchant == null || merchant.getSupportRefund() == null || merchant.getSupportRefund() == 1);
        dto.setSupportBooking(merchant == null || merchant.getSupportBooking() == null || merchant.getSupportBooking() == 1);

        List<String> tags = new ArrayList<>();
        tags.add("到店核销");
        tags.add("团购套餐");
        if (merchant != null && merchant.getStatus() != null && merchant.getStatus() == 1) {
            tags.add("营业中");
        }
        dto.setServiceTags(tags);

        List<String> bannerTitles = new ArrayList<>();
        for (MerchantStore store : stores) {
            bannerTitles.add(store.getName());
        }
        dto.setBannerTitles(bannerTitles);

        if (mainStore != null) {
            dto.setStoreId(mainStore.getId());
        }
        return dto;
    }

    private MerchantMiniWorkbenchStatsDto buildWorkbenchStats(Long merchantId) {
        MerchantMiniWorkbenchStatsDto statsDto = new MerchantMiniWorkbenchStatsDto();

        // 使用SQL COUNT替代全量加载（S-2优化）
        Long pendingVerifyCount = mallOrderMapper.countByMerchantIdAndStatusIn(
                merchantId, Arrays.asList(ORDER_STATUS_PAID, ORDER_STATUS_USED));
        Long completedCount = mallOrderMapper.countByMerchantIdAndStatusIn(
                merchantId, Collections.singletonList(ORDER_STATUS_COMPLETED));
        Long refundingCount = refundRecordMapper.countActiveRefundByMerchantId(merchantId);

        // 今日流水按支付成功时间统计，不能按下单或核销时间计算。
        Long todayOrderCount = mallOrderMapper.countTodayPaidOrdersByMerchantId(merchantId);
        BigDecimal todaySalesSql = mallOrderMapper.sumTodaySalesByMerchantId(merchantId);
        long todaySalesAmount = yuanToCent(todaySalesSql);

        int onShelfCount = productMapper.countOnShelfProductByMerchantId(merchantId);

        statsDto.setPendingAcceptCount(0);
        statsDto.setPendingVerifyCount(pendingVerifyCount != null ? pendingVerifyCount.intValue() : 0);
        statsDto.setCompletedCount(completedCount != null ? completedCount.intValue() : 0);
        statsDto.setRefundingCount(refundingCount != null ? refundingCount.intValue() : 0);
        statsDto.setOnShelfCount(onShelfCount);
        statsDto.setPendingBookingCount(countPendingBookings(merchantId));
        statsDto.setTodayOrderCount(todayOrderCount != null ? todayOrderCount.intValue() : 0);
        statsDto.setTodaySalesAmount(todaySalesAmount);
        return statsDto;
    }

    private long yuanToCent(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
    }

    private int countPendingBookings(Long merchantId) {
        bookingRecordMapper.markExpiredPending(new Date());
        BookingRecord query = new BookingRecord();
        query.setMerchantId(merchantId);
        query.setStatus(BookingStatus.PENDING);
        List<BookingRecord> bookings = bookingRecordMapper.selectBookingRecordList(query);
        return bookings == null ? 0 : bookings.size();
    }

    private List<MerchantMiniOrderDto> buildPendingOrderList(Long merchantId) {
        List<MallOrder> orders = mallOrderMapper.selectMallOrderByMerchantId(merchantId);
        List<MerchantMiniOrderDto> result = new ArrayList<>();
        for (MallOrder order : orders) {
            if (order.getStatus() == ORDER_STATUS_PAID || order.getStatus() == ORDER_STATUS_USED) {
                result.add(convertOrderToDto(order));
            }
        }
        // 按创建时间正序（最先创建的排前面）
        result.sort((a, b) -> Long.compare(a.getCreateTime(), b.getCreateTime()));
        return result;
    }

    private MerchantMiniBookingDto convertBookingToDto(BookingRecord booking) {
        MerchantMiniBookingDto dto = new MerchantMiniBookingDto();
        dto.setId(booking.getId());
        dto.setBookingNo(booking.getBookingNo());
        dto.setGoodsId(booking.getProductId());
        dto.setTitle(booking.getProductName());
        dto.setImage(appendListThumb(booking.getProductImage()));
        dto.setPrice(booking.getProductPrice() != null
                ? booking.getProductPrice().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue()
                : 0L);
        dto.setCustomerName(StringUtils.defaultIfBlank(booking.getContactName(), booking.getUserName()));
        dto.setCustomerPhone(booking.getContactPhone());
        dto.setPeopleCount(booking.getPeopleCount());
        dto.setStatus(booking.getStatus());
        dto.setBookingTime(toMillis(booking.getBookingTime()));
        dto.setCreateTime(toMillis(booking.getCreateTime()));
        dto.setConfirmTime(toMillis(booking.getConfirmTime()));
        dto.setCompleteTime(toMillis(booking.getCompleteTime()));
        dto.setCancelTime(toMillis(booking.getCancelTime()));
        dto.setExpireTime(toMillis(booking.getExpireTime()));
        dto.setRemark(booking.getRemark());
        return dto;
    }

    private Long toMillis(Date date) {
        return date == null ? null : date.getTime();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private MerchantMiniVerifyRecordDto buildVerifyRecordFromOrder(long recordId, MallOrder order) {
        MerchantMiniVerifyRecordDto dto = new MerchantMiniVerifyRecordDto();
        dto.setRecordId(recordId);
        dto.setOrderNo(order.getOrderNo());
        dto.setInputCode(order.getWriteOffCode());
        dto.setWriteOffCode(order.getWriteOffCode());
        dto.setPayAmount(order.getPayAmount() != null ? order.getPayAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0L);
        dto.setVerifyTime(order.getUseTime() != null ? order.getUseTime().getTime() : 0L);
        dto.setStatus(VERIFY_STATUS_SUCCESS);
        dto.setFailureReason(null);

        // 查订单商品
        try {
            List<OrderItem> items = orderItemMapper.selectOrderItemByOrderId(order.getId());
            if (!items.isEmpty()) {
                dto.setGoodsId(items.get(0).getProductId());
                dto.setTitle(items.get(0).getProductName());
            }
        } catch (Exception ignored) {
        }

        CustomerSnapshot customer = resolveCustomerSnapshot(order);
        dto.setCustomerName(customer.name);
        dto.setCustomerPhone(maskPhone(customer.phone));
        return dto;
    }

    private CustomerSnapshot resolveCustomerSnapshot(MallOrder order) {
        CustomerSnapshot snapshot = new CustomerSnapshot();
        snapshot.name = normalizeRealUserName(order != null ? order.getUserName() : null);
        snapshot.phone = "";
        if (order != null && order.getUserId() != null) {
            UserInfo userInfo = userInfoMapper.selectUserInfoById(order.getUserId());
            if (userInfo != null) {
                snapshot.name = firstNonBlank(normalizeRealUserName(userInfo.getUserName()), snapshot.name);
                snapshot.phone = StringUtils.defaultIfBlank(userInfo.getPhone(), snapshot.phone);
            }
            if (StringUtils.isBlank(snapshot.name) || StringUtils.isBlank(snapshot.phone)) {
                MallUser mallUser = mallUserMapper.selectMallUserById(order.getUserId());
                if (mallUser != null) {
                    snapshot.name = firstNonBlank(snapshot.name, normalizeRealUserName(mallUser.getNickname()));
                    snapshot.phone = StringUtils.defaultIfBlank(snapshot.phone, mallUser.getPhone());
                }
            }
        }
        snapshot.name = StringUtils.defaultIfBlank(snapshot.name, "未知用户");
        snapshot.phone = StringUtils.defaultIfBlank(snapshot.phone, "");
        return snapshot;
    }

    private String normalizeRealUserName(String name) {
        String normalized = StringUtils.trimToEmpty(name);
        if (StringUtils.isBlank(normalized)
                || "微信用户".equals(normalized)
                || normalized.matches("^用户\\d+$")) {
            return "";
        }
        return normalized;
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : StringUtils.defaultString(second);
    }

    private static class CustomerSnapshot {
        private String name;
        private String phone;
    }

    private MerchantMiniSettlementAccountDto buildSettlementAccount(Long merchantId) {
        Merchant merchant = merchantMapper.selectMerchantById(merchantId);
        MerchantMiniSettlementAccountDto dto = new MerchantMiniSettlementAccountDto();
        dto.setAccountName(merchant != null ? merchant.getName() : "");
        dto.setBankName("微信支付商户平台结算账户");
        dto.setAccountNoTail("");
        dto.setStatus("PENDING");
        return dto;
    }

    private MerchantMiniSettlementRecordDto buildSettlementRecord(MerchantMiniFinanceLedgerDto ledger) {
        MerchantMiniSettlementRecordDto dto = new MerchantMiniSettlementRecordDto();
        dto.setSettlementId("S" + ledger.getOrderNo());
        dto.setOrderNo(ledger.getOrderNo());
        dto.setTitle(ledger.getTitle());
        dto.setAmount(ledger.getMerchantAmount());
        dto.setApplyTime(ledger.getFinishTime());
        dto.setExpectedTransferTime(ledger.getSettleTime());
        if (LEDGER_STATUS_SETTLED.equals(ledger.getStatus())) {
            dto.setStatus(SETTLEMENT_STATUS_ARRIVED);
            dto.setArriveTime(ledger.getSettleTime());
            dto.setRemark("微信支付按商户号结算");
        } else {
            dto.setStatus(SETTLEMENT_STATUS_WAITING_T1);
            dto.setRemark("待微信支付结算周期处理");
        }
        return dto;
    }

    private Long nextAutoTransferTime() {
        long dayMillis = 24L * 60L * 60L * 1000L;
        long now = System.currentTimeMillis();
        long todayStart = now - (now + 8 * 60 * 60 * 1000L) % dayMillis;
        long next = todayStart + dayMillis + 10 * 60 * 60 * 1000L;
        return next <= now ? next + dayMillis : next;
    }

    private MerchantMiniWithdrawRecordDto convertWithdrawToDto(WithdrawRecord record) {
        MerchantMiniWithdrawRecordDto dto = new MerchantMiniWithdrawRecordDto();
        dto.setWithdrawId(record.getId());
        dto.setAmount(record.getAmount() != null ? record.getAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0L);
        dto.setApplyTime(record.getCreateTime() != null ? record.getCreateTime().getTime() : 0L);
        dto.setFinishTime(record.getPayTime() != null ? record.getPayTime().getTime() : null);
        dto.setRemark(record.getRejectReason());

        if (record.getStatus() != null) {
            switch (record.getStatus()) {
                case WITHDRAW_STATUS_PENDING_AUDIT:
                case WITHDRAW_STATUS_APPROVED:
                    dto.setStatus(WITHDRAW_STATUS_PROCESSING);
                    break;
                case WITHDRAW_STATUS_PAID:
                    dto.setStatus("SUCCESS");
                    break;
                case WITHDRAW_STATUS_REJECTED:
                    dto.setStatus("REJECTED");
                    break;
            }
        }
        return dto;
    }

    private MerchantMiniStaffUserDto buildStaffUserFromDb(MerchantUser user, Merchant merchant) {
        MerchantMiniStaffUserDto dto = new MerchantMiniStaffUserDto();
        dto.setUserId(String.valueOf(user.getId()));
        dto.setStaffId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setMerchantId(user.getMerchantId());
        dto.setName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus() != null && user.getStatus() == 1 ? STATUS_ACTIVE : "DISABLED");

        String role = user.getRole();
        dto.setRoleKey(role);
        dto.setRoleName(ROLE_OWNER.equals(role) ? "店长" : "店员");
        dto.setPermissions(new ArrayList<>(buildPermissions(role)));

        if (merchant != null) {
            dto.setMerchantName(merchant.getName());
            List<MerchantStore> stores = merchantStoreMapper.selectMerchantStoreByMerchantId(user.getMerchantId());
            if (!stores.isEmpty()) {
                dto.setStoreId(stores.get(0).getId());
            }
        }
        return dto;
    }

    private List<String> buildPermissions(String role) {
        List<String> permissions = new ArrayList<>();
        permissions.add("stats.view");
        permissions.add("order.manage");
        permissions.add("verify.scan");
        permissions.add("verify.manual");
        permissions.add("verify.record");
        if (ROLE_OWNER.equals(role)) {
            permissions.add("goods.manage");
            permissions.add("store.manage");
            permissions.add("staff.manage");
            permissions.add("finance.manage");
        }
        return permissions;
    }

    private boolean isSameDay(Date date, String todayStr) {
        if (date == null) return false;
        return todayStr.equals(new SimpleDateFormat("yyyyMMdd").format(date));
    }

    private boolean isSameMonth(Date date, String monthStr) {
        if (date == null) return false;
        return monthStr.equals(new SimpleDateFormat("yyyyMM").format(date));
    }

    /**
     * 手机号脱敏：138****1111
     */
    private String maskPhone(String phone) {
        return DesensitizedUtil.phone(phone);
    }

}

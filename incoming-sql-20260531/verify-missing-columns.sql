SELECT COUNT(*) AS expected_missing_columns_present
FROM information_schema.columns
WHERE table_schema='ruoyi-cs' AND (
(table_name='merchant' AND column_name IN ('c_mini_app_id','c_mini_app_secret','m_mini_app_id','m_mini_app_secret','wx_pay_mch_id','wx_pay_api_key','receiver_type','receiver_openid')) OR
(table_name='groupon_activity' AND column_name IN ('source_type')) OR
(table_name='product' AND column_name IN ('main_image','verify_notice')) OR
(table_name='coupon' AND column_name IN ('remain_count','remark')) OR
(table_name='user_address' AND column_name IN ('address')) OR
(table_name='mall_user' AND column_name IN ('union_id','nick_name','avatar_url','last_login_time')) OR
(table_name='distributor' AND column_name IN ('receiver_openid','receiver_type')) OR
(table_name='mall_order' AND column_name IN ('write_off_status','write_off_time','write_off_user_id','valid_days')) OR
(table_name='transaction_record' AND column_name IN ('remark')) OR
(table_name='merchant_bill' AND column_name IN ('bill_period','remark')) OR
(table_name='cart' AND column_name IN ('product_name','product_image','price')) OR
(table_name='banner' AND column_name IN ('merchant_id','image_url')) OR
(table_name='operation_log' AND column_name IN ('merchant_id','operator_id','target','detail','ip_address','create_time')) OR
(table_name='write_off_record' AND column_name IN ('del_flag')) OR
(table_name='payment_record' AND column_name IN ('pay_channel','pay_mch_id','notify_raw')) OR
(table_name='refund_record' AND column_name IN ('apply_time','remark'))
);

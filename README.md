# GoogleBilling_Android
用于谷歌支付的demo

## 开发文档
1.支付接入文档
https://developer.android.google.cn/google/play/billing/getting-ready


## 测试流程
1.1前提条件
- 手机上有谷歌服务
- 手机连接外网
- 手机上登录的google账号在测试白名单中
- 如果测试的app处于test mode状态，需要设备上登录的google账号点击邀请测试连接

1.2 测试步骤
- initsdk->initTASDK->showgoods->pay

Tips:
1.logcat中过滤"roy_billing",查看详细支付结果



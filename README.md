# 回调服务示例

这是一个用于接收第三方通知的回调转发服务，可以用该服务来统一接收与第三方对接的回调。

采用如下配置的方式，需要让第三方发送请求到 `http(s)://回调服务域名或网关域名/callback/{requestCode}` 。
将根据 `requestCode` 获取到目标接口后进行转发。需要注意的是这里采用服务名称进行负载均衡转发。

```yaml
callback:
  request-codes:
    requestCode1: http://targetService/targetPath1
    requestCode2: http://targetService/targetPath2
```


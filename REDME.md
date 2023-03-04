# AccessLimit

## 简介

本项目通过 `Filter` 默认实现了限制某 IP 对接口的访问频率限制。并且使用策略模式让用户可以自定义对于访问频率的处理策略。

## 处理流程

请求将会被 `AccessLimitFilter` 拦截，然后判断该请求是否被限制了访问。若被限制了访问则会调用该访问限制处理器进行处理。

## 使用方法

1. 在 配置文件中 添加 :
   1. `access-limit.enable=true`
   2. `access-limit.redis-base-path=你的项目名称（或者其他的）`
   3. 然后是你的 redis 配置信息，这里略。
2. 在 `Controller` 的类上或方法上添加 `@AccessLimit` 注解，并配置访问频率限制和处理器。当然两者都有默认值，具体
   参考接口注释。
   1. 需要注意的是，在类上标注接口时，该类的所有接口都会被进行相同的访问频率限制。
   2. 方法上的 `@AccessLimit` 注解将会覆盖 类上的 `@AccessLimit` 设置。

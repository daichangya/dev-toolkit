# 包结构说明

## com.daicy.devtools
- core：核心框架包
  - config：配置相关类
  - exception：异常处理类
- plugin：插件管理包
  - api：插件API接口
  - loader：插件加载器
  - manager：插件管理器
  - impl：插件实现类
    - json：JSON相关插件
    - text：文本处理插件
    - url：URL处理插件
  - utils：插件工具类

## 包结构调整计划
1. 将现有的插件实现类按功能分类到不同子包中
2. 将SPI相关类移动到loader包下
3. 创建统一的插件API接口
4. 优化工具类的位置

## 命名规范
- 包名使用小写字母
- 类名使用大驼峰命名法
- 接口名以I开头，使用大驼峰命名法
- 实现类使用大驼峰命名法，并以具体功能命名
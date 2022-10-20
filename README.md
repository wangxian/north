# North

A Simple / Lightweight / Readable & Quick start framework for Java web

## 项目特点

- 简单，够用
- 轻量
- 源码可阅读
- ...

## 快速开始

添加项目依赖

```xml
<!-- pom.xml -->
<dependencies>
  <dependency>
    <groupId>top.xiqiu</groupId>
    <artifactId>north</artifactId>
    <version>1.0.6</version>
  </dependency>
</dependencies>
```

```java
// App.java
package org.example;

import top.xiqiu.north.North;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        North.start(App.class, args);
    }
}

```

```java
// controller/IndexController.java
package org.example.controller;

import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index() {
        return "hello world!";
    }
}
```

运行项目，在浏览器访问 http://0.0.0.0:8080/ 即可。

## 示例&文档

暂无开发文档，请移步 `north-demos` 项目体验 <https://github.com/wangxian/north-demos>

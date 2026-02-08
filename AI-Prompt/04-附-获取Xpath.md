
---

```java
我有一个Xpath，我该如何用Edge开发者工具确定目标页面有没有这个Xpath对应的元素🤔
还有一种`//a[ contains( text(), 'xxx' ) ]`好像是找当前页面有没有包含文字`xxx`的链接🤔
不太好描述我的困惑，反正就是想知道怎么从Edge开发者工具来搜索、确认当前页面的某个元素
```

```java
emm🧐
Console里面输入$x("//a[contains(text(), 'xxx')]")没有搜索到`xxx`元素，但是肉眼可见那个地方确实有这个`xxx`的点击按钮（超链接），必须要复制它的Xpath然后在Ctrl + F才可以搜索到

这是怎么回事，它不在当前页面嘛😶或者是因为它被其他的元素包起来了？？？😵
```

```java
我靠确实😮
`$x("//a[contains(., 'xxx')]")`、`$x("//a[contains(normalize-space(.), 'xxx')]")`确实Console成功了
那么以后推荐用`.`还是`normalize-space(.)`还是哪个来匹配Xpath🤔
```

```java
emm🤔
我的`//a[contains(normalize-space(.), 'xxx')]`
像这种`//a`好像只是针对超链接？？
有没有针对普通文本的、针对通用的等等各种方向的Xpath🧐帮我总结整理、举例子🌰
```

---


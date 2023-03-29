## 简介
Peashooter（豌豆射手）用一句话概括就是 RSS 转换器，使用它可以将 BT 站发布的 RSS 内容转换成 [Sonarr](https://github.com/Sonarr/Sonarr) 能够正确识别的内容，从而完成自动化（目前半自动）追番。

## 使用

### 拉取镜像
`docker pull nekolr/peashooter:latest`

### 启动镜像

```bash
docker run --name peashooter -d  \
    -v /data/peashooter:/data/peashooter \
    -p 8962:8962 \
    -e PEASHOOTER_USERNAME=admin \ 
    -e PEASHOOTER_PASSWORD=admin \
    nekolr/peashooter:latest
```

### 设置
![settings](/resources/settings.png)

需要设置 sonarr、qbittorrent、themoviedb，如果需要，还可以设置代理。

### 数据源
 ![datasource](/resources/datasource.png)
 
这里使用蜜柑的数据，同步间隔决定多长时间刷新该数据源的内容。

### 分组
![group_01](/resources/group_01.png)

打开分组菜单，选择添加分组，然后选择刚才添加的数据源进行查看。

![group_02](/resources/group_02.png)

随便选择一条记录并点击鼠标右键，会弹出**生成正则表达式的选项卡**，点击即可在匹配器中自动填充表达式。

![group_03](/resources/group_03.png)

在匹配器中，选中剧集号，然后使用**替换选择文本为集数**即可将剧集号修改为对应的表达式。

![group_04](/resources/group_04.png)

接下来在选择剧集中搜索对应的番剧，这里其实是搜索 sonarr 中已经添加的番剧，如果搜索不到，请使用右侧的刷新按钮，等待刷新后再次搜索。接着选择数据源，并给分组起一个名字，修改季度信息等。

![group_05](/resources/group_05.png)

接下来即可点击**测试匹配效果**按钮检查一下能否正确匹配到相关的剧集。

![group_06](/resources/group_06.png)

点击**发布所有分组的 RSS 索引**，这会为 sonarr 添加一个 Indexer。

![indexer](/resources/indexer.png)

好了，enjoy it。
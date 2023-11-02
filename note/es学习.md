## Docker安装Elasticsearch8

本人使用的Elasticsearch版本是8.3.2。

Windows基于WSL2的Docker Desktop，版本24.0.6。

### 启用https

`docker-compose.yml`

下面配置未指定es的配置文件，es启动后会默认生成。

```yaml
version: "3"
services:
  es:
    image: elasticsearch:8.3.2
    container_name: es
    networks:
      - elastic
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - ./volumes/data:/usr/share/elasticsearch/data
      - ./volumes/plugins:/usr/share/elasticsearch/plugins
    environment:
      discovery.type: single-node
  kibana:
    image: kibana:8.3.2
    container_name: kibana
    networks:
      - elastic
    ports:
      - "5601:5601"
    depends_on:
      - es
networks:
  elastic:
```

此时如果直接浏览器打开http://localhost:9200，无法看到elasticsearch的状态信息。

使用docker单节点模式安装es8，默认启动安全配置。

- 生成证书和密钥
- TLS加密配置会写入到elasticsearch.yml
- 为elastic用户生成密码
- 为kibana生成注册令牌

要正常访问es需要ca证书以及用户名密码。

讲ca证书从容器中拷贝出来

```shell
docker cp es:/usr/share/elasticsearch/config/certs/http_ca.crt .
```

按照官方的说法，用户密码和注册令牌仅会在es第一次启动时打印在控制台，但是本人发现并没有。

```json
{"@timestamp":"2023-10-18T07:21:27.922Z", "log.level": "INFO", "message":"Auto-configuration will not generate a password for the elastic built-in superuser, as we cannot  determine if there is a terminal attached to the elasticsearch process. You can use the `bin/elasticsearch-reset-password` tool to set the password for the elastic user.", "ecs.version": "1.2.0","service.name":"ES_ECS","event.dataset":"elasticsearch.server","process.thread.name":"main","log.logger":"org.elasticsearch.xpack.security.InitialNodeSecurityAutoConfiguration","elasticsearch.node.name":"e537cedd5778","elasticsearch.cluster.name":"docker-cluster"}
```

密码和令牌可以使用es的脚本生成。

```shell
# -i参数可以指定密码
docker exec -it es /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic
docker exec -it es /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token --scope kibana
```

```shell
curl --cacert http_ca.crt -u elastic:uLBV*RmHZRrt7_*zw7Py https://localhost:9200
{
  "name" : "e537cedd5778",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "VS3WhhdUQtGOnH4mwS8U4w",
  "version" : {
    "number" : "8.3.2",
    "build_type" : "docker",
    "build_hash" : "8b0b1f23fbebecc3c88e4464319dea8989f374fd",
    "build_date" : "2022-07-06T15:15:15.901688194Z",
    "build_snapshot" : false,
    "lucene_version" : "9.2.0",
    "minimum_wire_compatibility_version" : "7.17.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "You Know, for Search"
}
```

查看自动生成的es配置文件

```shell
docker cp es:/usr/share/elasticsearch/config/elasticsearch.yml .
```

```yaml
cluster.name: "docker-cluster"
network.host: 0.0.0.0

#----------------------- BEGIN SECURITY AUTO CONFIGURATION -----------------------
#
# The following settings, TLS certificates, and keys have been automatically      
# generated to configure Elasticsearch security features on 18-10-2023 07:21:12
#
# --------------------------------------------------------------------------------

# Enable security features
xpack.security.enabled: true

xpack.security.enrollment.enabled: true

# Enable encryption for HTTP API client connections, such as Kibana, Logstash, and Agents
xpack.security.http.ssl:
  enabled: true
  keystore.path: certs/http.p12

# Enable encryption and mutual authentication between cluster nodes
xpack.security.transport.ssl:
  enabled: true
  verification_mode: certificate
  keystore.path: certs/transport.p12
  truststore.path: certs/transport.p12
#----------------------- END SECURITY AUTO CONFIGURATION -------------------------
```

### 在Postman中访问https的es

1. postman中配置刚刚从es中复制的ca证书，settings->Certificates，配置域名、端口、ca证书。

   ![image-20231018154550026](./es学习.assets/image-20231018154550026.png)

2. HTTP请求添加Basic Auth，每个请求的Authorization选择Basic Auth，输入用户名密码。

   ![image-20231018154714751](./es学习.assets/image-20231018154714751.png)

### 不使用https

只需要需改配置文件即可，在`docker-compose.yml`挂载配置文件`elasticsearch.yml`

```yaml
#cluster.name: "docker-cluster"
#network.host: 0.0.0.0

cluster.name: "docker-cluster"
network.host: 0.0.0.0

#----------------------- BEGIN SECURITY AUTO CONFIGURATION -----------------------
#
# The following settings, TLS certificates, and keys have been automatically
# generated to configure Elasticsearch security features on 18-10-2023 07:21:12
#
# --------------------------------------------------------------------------------

# Enable security features
xpack.security.enabled: true

xpack.security.enrollment.enabled: true

# 跨域配置
http.cors.enabled: true
http.cors.allow-origin: "*"
http.cors.allow-headers: X-Requested-With,Content-Type,Content-Length,Authorization
```

## 相关概念

- index，索引，文档的集合，相当于关系型数据库的表（Table），包含表结构（mapping）和表配置（setting）两个选项。

- mapping，表结构，每个字段的数据类型相关配置。
- doc，文档，每个文档（Document）相当于关系型数据库中的行（Row），文档的字段（Field）相当于数据库中的列（Column）。
- Inverted index，倒排索引，先对文档进行分词，词条记录对应文档信息，查询时通过词条定位到文档。
- analyzer，分词器，将文本拆分成词条，对于英文，可直接按照空格拆分，默认情况下中文会按每个字拆分，支持中文分词需要安装插件。es中分词器的组合包含三个部分
  - character filters，字符过滤器，在分词之前对文本进行处理，例如删除停用词，替换字符等。
  - tokenizer，将文本切分成词条（term）。
  - tokenizer filters，进一步处理分词结果，例如大小写转换，同义词替换等。

## 映射

- 动态映射，无需指定mapping（建表），直接添加数据，es自动检测数据类型，数据类型推断规则可自定义，一般不推荐使用。
- 显式映射，指定mapping。
- 运行时字段，不改变原索引，在查询时动态计算生成的虚拟字段，支持排序等操作。
- 数据类型，[所有支持的数据类型](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html)
- [元数据字段](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-fields.html)，文档元数据信息，如id，所在索引等。
- 字段参数，创建字段时的各种参数，例如分词器、时间格式，向量距离度量方式。
- 映射限制，限制mapping的字段个数、字段长度、嵌套字段深度等。
- 8版本之后删除了mapping type。

## 特性

- [collapse字段折叠](https://blog.csdn.net/ZYC88888/article/details/83023143)，按照特定的字段分组，每组均返回结果，例如搜索手机，每个品牌都想看看，按品牌字段折叠，返回每个品牌的可排序、过滤的数据。
- [filter过滤](https://juejin.cn/post/7073820135873576997)，与query使用场景不同。
- [highlight高亮]()，对存在检索关键词的结果字段添加特殊标签。
- [async异步搜索](https://blog.csdn.net/UbuntuTouch/article/details/107868114)，检索大量数据，可查看检索的运行状态。
- [near real-time近实时搜索](https://doc.yonyoucloud.com/doc/mastering-elasticsearch/chapter-3/34_README.html)，添加或更新文档不修改旧的索引文件，写新文件到缓存，延迟刷盘。
- [pagination排序]，普通排序，深度分页scroll，search after。
- [inner hits子文档命中](https://www.jianshu.com/p/0d6488a8072b)，对嵌套对象子文档进行搜索时，可以满足查询条件的具体子文档。[]
- selected field返回需要的字段，使用_source和fileds返回需要的文档字段。
- across clusters分布式检索，支持多种检索API的分布式搜索。
- [multiple indices多索引检索]，支持同时从一次从多个索引检索数据。
- [shard routing分片路由]，自适应分片路由以减少搜索响应时间，可自定义检索哪个节点。
- [自定义检索模板search templates]，可复用的检索模板，根据不同变量生成不同query dsl。
- [同义词检索search with synonyms]，定义同义词集、过滤器和分词器，提高检索准确度。
- [排序sort results]，支持多字段，数组字段、嵌套字段排序。
- [最邻近搜索knn search]，检索最邻近的向量，常用于相关性排名、搜索建议、图像视频检索。
- [语义检索semantic search]，按语义和意图检索，而不是词汇检索，基于NLP和向量检索，支持上传模型，在存储和检索时自动编码，支持混合检索。

## Python Client

```python
from pprint import pprint

# 8.3.2
from elasticsearch import Elasticsearch

es_password = 'yq7UbfMeNNGkvFh2gBf_'

client = Elasticsearch(hosts='http://localhost:9200',
                       # ca_certs=os.path.join(os.path.dirname(__file__), 'http_ca.crt'),
                       basic_auth=('elastic', es_password))

pprint(client.info().body)
pprint(client.perform_request('POST', '/_analyze',
                              headers={'Content-Type': "application/vnd.elasticsearch+json;compatible-with=8",
                                       "Accept": "application/vnd.elasticsearch+json;compatible-with=8"},
                              body={
                                  'text': '黑马程序员的Java和Python教的真不错，很好用',
                                  "analyzer": "ik_max_word"
                              }).body)
```

## Java Client

本人在Springboot2.7.16项目中使用访问es，对应Springdata使用的es版本是7.17，使用Rest-High-Level-Client Api，8.x此Client已不再维护，官方推荐[Java Client Api](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.3/indexing.html)，引入和es版本对应的该包。

```xml
<dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>8.3.2</version>
</dependency>
<!-- https://mvnrepository.com/artifact/jakarta.json/jakarta.json-api -->
<dependency>
    <groupId>jakarta.json</groupId>
    <artifactId>jakarta.json-api</artifactId>
    <version>2.0.1</version>
</dependency>
```

Elasticsearch Config配置类

```java
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.AutoCloseableElasticsearchClient;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chunf
 */

@Configuration
public class MyElasticsearchConfiguration extends ElasticsearchConfiguration {
   private final ElasticsearchProperties elasticsearchProperties;

   MyElasticsearchConfiguration(ElasticsearchProperties elasticsearchProperties) {
      this.elasticsearchProperties = elasticsearchProperties;
   }

   @Override
   @NonNull
   @Bean
   public ClientConfiguration clientConfiguration() {
      List<String> uris = elasticsearchProperties.getUris();
      List<String> hostAndPort = new ArrayList<>();
      for (String s : uris) {
         URI uri = URI.create(s);
         hostAndPort.add(uri.getHost() + ":" + uri.getPort());
      }

      return ClientConfiguration
              .builder()
              .connectedTo(hostAndPort.toArray(new String[0]))
              .withBasicAuth(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword())
              .withPathPrefix(elasticsearchProperties.getPathPrefix())
              .withConnectTimeout(elasticsearchProperties.getConnectionTimeout())
              .withSocketTimeout(elasticsearchProperties.getSocketTimeout())
              .build();
   }

   @Override
   @Bean
   @NonNull
   public ElasticsearchClient elasticsearchClient(@NonNull RestClient restClient) {
      ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
      return new AutoCloseableElasticsearchClient(transport);
   }

}
```


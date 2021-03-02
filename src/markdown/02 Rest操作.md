# 说明

​	es主要是以Rest风格进行增删查改的，以下是基本格式

| method | 资源                                    | 描述               |
| ------ | --------------------------------------- | ------------------ |
| PUT    | ${url}/索引名称/类型名称/文档id         | 指定文档id创建文档 |
| POST   | ${url}/索引名称/类型名称                | 随机文档id创建文档 |
| POST   | ${url}/索引名称/文档名称/文档id/_update | 修改文档           |
| DELETE | ${url}/索引名称/类型名称/文档id         | 指定文档id删除文档 |
| GET    | ${url}/索引名称/类型名称/文档id         | 通过id查询         |
| POST   | ${url}/索引名称/类型名称/_search        | 查询所有数据       |



# 过程

​	在kibana操作内可以忽略${url}

## 索引操作

​	直接创建索引test02，同时定义规则，name为文本类型，age为整型，creationTime为date类型。**TODO 这个规则究竟用来干什么？是限定该索引下的文档吗？**

```http
PUT /test02
{
  "mappings": {
    "properties": {
      "name":{
        "type": "text"
      },
      "age":{
        "type": "integer"
      },
      "creationTime":{
        "type": "date"
      }
    }
  }
}
```

## 文档操作

​	创建一个test01的索引，type01的类型，名为user01的文档

	PUT /test01/type01/user01 
	{
	  "id"      :"01",
	  "username":"第一号用户"
	}
​	**注意！！！这种写法是弃用type！！！**

	PUT /test01/_doc/user01 
	{
	  "id"      :"01",
	  "username":"第一号用户"
	}
​	也可以通过这种方式强行修改已创建文档

	PUT /test01/type01/user01 
	{
	  "id"      :"01",
	  "username":"第一号用户改"
	}
​	常规更新已创建文档user01，只修改username为"第一号用户常规改"

	POST /test01/type01/user01/_update
	{
	  "doc":{
	  	"username":"第一个用户常规改"
	  }
	}
​	查看文档user01的元数据

```
GET /test01/type01/user01
```

​	此时他的响应是

```json
{
  "_index" : "test01",	//索引名
  "_type" : "type01",	//type名
  "_id" : "user01",	// 文档id
  "_version" : 2,	// 版本号，即修改次数
  "_seq_no" : 1,
  "_primary_term" : 1,
  "result" : "noop", //注意！！！这个key是新增/修改文档后才会返回，代表结果，有created、updated、noop（无变化）三个值
  "found" : true,
  "_source" : {
    "id" : "01",
    "username" : "第一个用户常规改"
  }
}
```



## 全局操作

​	查看索引test02的元数据

```
GET /test02
```

​	查看集群的健康状况

```
GET _cat/health
```

​	查看所有索引的状态

```
GET _cat/indices?v
```

​	还有其他...待补充
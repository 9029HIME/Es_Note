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

### 新增与修改

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

### 普通查询

​	关键字查询文档信息，如在test01索引，查询username为"第一个用户常规改"的document，这里直接弃用type的概念。

```
GET /test01/_search?q=username:第一个用户常规改
```

​	这是关键字查询的响应

```
{
  "took" : 0,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    },
    "max_score" : 2.3014567,
    "hits" : [
      {
        "_index" : "test01",
        "_type" : "type01",
        "_id" : "user01",
        "_score" : 2.3014567,
        "_source" : {
          "id" : "01",
          "username" : "第一个用户常规改"
        }
      }
    ]
  }
}
```

### 复杂查询

#### 分词匹配与精准匹配

​	新建一个如下结构的索引test04，同时插入一条数据

```json
"mappings" : {
  "properties" : {
    "keyName" : {
      "type" : "keyword"
    },
    "textName" : {
      "type" : "text"
    }
  }
}

PUT /test04/_doc/member01
{
  "keyName":"台湾不可分割",
  "textName":"这是第一个会员"
}
```
​	来看一下最基本的复杂查询，查询test04下textName为"这是第一个会员"的文档

```
GET /test04/_search
{
  "query":{
    "match":{
      "textName": "这是第一个会员"
    }
  }
}
```

​	首先就以这句来展开说，大致分为三部分，query、match、username，其中username为属性名。先说match，他代表分词查询，与之同级的是**term**，代表精确查询。match的查询会将条件和文档进行分词处理，再将分词拿比对。而term会将整个条件拿去与文档比对。比如用match查textName为"会员你好"的结果，因为"会员你好"会被分成"会员"和"你好"，而"这是第一个会员"的其中一个分词是"会员"。因此能查询到member01的文档信息。

```
GET /test04/_search
{
  "query":{
    "match":{
      "textName": "会员你好"
    }
  }
}
```

​	keyword代表字段值不可被分割，**term会通过倒排索引进行查询**，此时以下查询是能查到member01，但只要value与实际文档的值有一点区别，就不会比对成功。

```
GET /test04/_search
{
  "query":{
    "term":{
      "keyName": {
        "value": "台湾不可分割"
      }
    }
  }
}
```

#### 排序

​	和query同级，**属性必须是可排序的类型**

	GET /test04/_search
	{
	  "query":{
	    "match":{
	      "username": "会员你好"
	    }
	  },
	  "sort":[
		{
		"待排序属性名":{
			"order":"asc或者desc"
		}
		}  
	  ]
	}
#### 分页

​	ES的分页有点像MySQL，from代表起始值，size代表偏移量。

```
GET /test02/_search
{
  "query":{
    "match":{
      "username": "会员你好"
    }
  },
  "sort":[
	{
	"待排序属性名":{
		"order":"asc或者desc"
	}
	}  
  ],
  "from":0,
  "size":2
}
```

#### 多条件混合查询

​	与match同级的还有bool，注意！！这个bool并不代表是否有值，而是根据其子key的逻辑来对多个条件进行查询，比如bool + must，代表查询**同时符合多个条件的文档**

```
GET /test04/_search
{
  "query":{
    "bool":{
    	//即必须满足must数组内所有条件
      must:[
      	{
      		"match":{
				"属性1":值1
			}
      	},
      	{
      		"term":{
      			"属性2":值2
      		}
      	}
      ]
    }
  }
}
```

​	bool + should，代表查询**符合其中一个或多个条件的文档**

​	bool + must_not，代表查询**同时不符合多个条件的文档**

#### 条件范围查询

​	与排序同理，需要范围查询的属性必须可排序，ES中有以下范围符

​	gt：大于

​	gte：大于等于

​	lt：小于

​	lte：小于等于

	GET /test02/_search
	{
	  "query":{
	    "match":{
	      "username": "会员你好"
	    }
	  },
	  "sort":[
		{
		"待排序属性名":{
			"order":"asc或者desc"
		}
		}  
	  ],
	  "filter":{
	  	"range":{
	  		"待范围的属性名":{
				"范围符1":范围值
				"范围符2":范围值
			}
	  	}
	  }
	  "from":0,
	  "size":2
	}
#### 高亮

​	结果会返回符合查询条件的高亮部分

```
GET /test04/_search
{
  "query":{
    "match":{
      "username": "会员你好"
    }
  },
  "highlight":{
  	"fields":{
  		"需要高亮的属性名":[]
  	}
  }
}
```

#### term和keyword

​	term和keyword并没有直接的联系，他们俩是不同维度来维护查询条件的完整性

​	term：使用term查询，则必须会将条件拿去精确查询，即使查询的是text属性

​	keyword：查询keyword属性时会将属性作为一个整体去查，即使查询规则是match

​	但是！！！这term对于中文来说不好使，所以使用term查中文的话要搭配keyword（虽然这时候主要发挥作用的是keyword）

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
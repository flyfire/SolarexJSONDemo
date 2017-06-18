# SolarexJSONDemo
自制JSON解析框架

[ ![Download](https://api.bintray.com/packages/solarexsoft/maven/SolarexJSON/images/download.svg) ](https://bintray.com/solarexsoft/maven/SolarexJSON/_latestVersion)

``compile 'com.solarexsoft.solarexjson:solarexjson:1.0.0'``

Usage:

```
        News news = new News();
        news.setId(1);
        news.setTitle("新年放假通知");
        news.setContent("从今天开始放假啦。");
        news.setAuthor(createAuthor());
        news.setReader(createReaders());
        String json = SolarexJSON.toJSON(news);
        News generated = (News) SolarexJSON.fromJSON(json, News.class);
        Log.d(TAG, "fromJSON: " + generated);
```

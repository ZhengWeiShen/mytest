package com.itzws.jd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Service;

import com.itzws.jd.entity.Product;
import com.itzws.jd.entity.Result;
import com.itzws.jd.service.SearchService;

@Service
public class SearchServiceImpl implements SearchService {

	@Resource
	private HttpSolrServer httpSolrServer;
	
	@Override
	public Result searchProduct(String queryString, String catalog_name, String price, Integer page, String sort) {
		
		//创建查询对象（SolrQuery）
		SolrQuery sq = new SolrQuery();
		
		//设置查询关键词（如果 queryString 为空，搜索全部，否则，搜索指定的关键词）
		if (StringUtils.isNotBlank(queryString)) {
			sq.setQuery(queryString);
		} else {
			sq.setQuery("*:*");
		}

		//设置默认搜索域
		sq.set("df", "product_keywords");
		
		//设置过滤条件
		//商品分类名称
		if(StringUtils.isNotBlank(price)) {
			String[] arr = price.split("-");
			price = "product_price:[" + arr[0] + "TO" + arr[1] + "]";
		}
		
		sq.setFilterQueries(catalog_name,price);
		
		//设置分页
		//默认搜索第一页
		if(page == null) {
			page = 1;
		}
		
		//页面大小
		int pageSize = 10; 
		
		sq.setStart((page - 1) * pageSize);
		sq.setRows(pageSize);
		
		//设置排序（1升序，其他降序）
		if("1".equals(sort)) {
			sq.setSort("product_price",ORDER.asc);
		} else {
			sq.setSort("product_price",ORDER.desc);
		}
		
		//设置高亮显示
		sq.setHighlight(true);
		//添加高亮显示的域
		sq.addHighlightField("product_name");
		//设置 html 高亮显示的开始和结尾
		sq.setHighlightSimplePre("<font color = 'red'>");
		sq.setHighlightSimplePost("</font>");
		
		//执行搜索，返回查询响应结果对象（QueryResponse）
		QueryResponse queryRespons = null;
		try{
			queryRespons = httpSolrServer.query(sq);
		} catch(Exception e) {
			
		}
		
		//从 QueryResponse 中，获取搜索结果数据
		//取出搜索结果集
		SolrDocumentList resultList = queryRespons.getResults();
		
		//取出高亮数据
		Map<String, Map<String,List<String>>> highlighting = queryRespons.getHighlighting();
		
		//处理结果集
		//创建 result 对象
		Result result = new Result();
		
		//设置当前页数
		result.setCurPage(page);
		
		//设置页数（总记录数）
		int totals = (int) resultList.getNumFound();
		
		int pageCount = 0;
		if(totals % pageSize == 0) {
			pageCount = totals / pageSize;
		} else {
			pageCount = (totals / pageSize) + 1;
		}
		
		//设置总记录数
		result.setRecordCount(totals);
		
		//设置商品结果集合 list
		List<Product> productList = new ArrayList<Product>();
		for (SolrDocument doc : resultList) {
			
			//获取商品 id
			String pid = doc.get("id").toString();
			
			//获取商品名称
			String pname = "";
			List<String> list = highlighting.get(pid).get("product_name");
			if(list != null && list.size()>0) {
				pname = list.get(0);
			} else {
				pname = doc.get("product_name").toString();
			}
			
			//获取商品价格
			String pprice = doc.get("product_price").toString();
			
			//获取商品图片
			String ppicture = doc.get("product_picture").toString();
			
			//创建商品对象
			Product product = new Product();
			product.setPid(pid);
			product.setName(pname);
			product.setPrice(pprice);
			product.setPicture(ppicture);
			
			productList.add(product);
			
		}
		
		result.setProductList(productList);
		
		return result;
		
	}
	
}

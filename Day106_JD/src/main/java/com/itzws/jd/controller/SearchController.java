package com.itzws.jd.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itzws.jd.entity.Result;
import com.itzws.jd.service.SearchService;

@Controller
public class SearchController {

	@Resource
	private SearchService searchService;
	
	@RequestMapping("/list.action")
	public String list(Model model,String queryString,String catalog_name,String price,Integer page,String sort) {
		
		//搜索商品
		Result result = searchService.searchProduct(queryString, catalog_name, price, page, sort);
		
		//响应搜索结果
		model.addAttribute("result",result);
		
		model.addAttribute("queryString", queryString);
		model.addAttribute("catalog_name", catalog_name);
		model.addAttribute("price", price);
		model.addAttribute("sort", sort);
		
		return "product_list";
	}
	
}

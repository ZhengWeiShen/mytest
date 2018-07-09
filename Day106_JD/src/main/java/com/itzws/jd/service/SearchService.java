package com.itzws.jd.service;

import com.itzws.jd.entity.Result;

public interface SearchService {

	//搜索商品
	Result searchProduct(String queryString,String catalog_name,String price,Integer page,String sort);
	
}

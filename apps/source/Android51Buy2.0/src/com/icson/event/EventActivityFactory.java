package com.icson.event;

public class EventActivityFactory {

	public static Class<?> getEventActivityClass(int templateId) {
		//默认Event1Activity页面
		Class<?> ret = Event1Activity.class;
		switch (templateId) {
		case 1: //商品列表(顶部不含广告)
			ret = Event1Activity.class;
			break;
		case 2: //分类列表
			ret = Event2Activity.class;
			break;
		case 3: //商品列表(顶部含广告)
			ret = Event3Activity.class;
			break;

		}

		return ret;
	}
}

package com.icson.my;

public class OrderStatus {
	public static final int ORIGINAL   = 0; // 待审核
	public static final int WAIT_PAY   = 2; // 待支付
	public static final int WAIT_AUDIT = 3; // 待审核
	public static final int OUTSTOCK   = 4; // 已出库
	
	public static boolean canCancel(int nStatus) {
		return (ORIGINAL == nStatus || WAIT_PAY == nStatus || WAIT_AUDIT == nStatus);
	}
	
	private OrderStatus() {
		
	}
}

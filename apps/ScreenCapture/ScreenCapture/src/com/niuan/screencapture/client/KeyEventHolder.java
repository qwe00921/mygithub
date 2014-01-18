package com.niuan.screencapture.client;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyEventHolder {

	private Map<Character, Integer> mKeyMap = new HashMap<Character, Integer>();
	
	public static final char KEY_LEFT_SHIFT = '1';
	public static final char KEY_ENTER = '2';
	public KeyEventHolder() {
		mKeyMap.put('a', KeyEvent.VK_A);
		mKeyMap.put('b', KeyEvent.VK_B);
		mKeyMap.put('c', KeyEvent.VK_C);
		mKeyMap.put('d', KeyEvent.VK_D);
		mKeyMap.put('e', KeyEvent.VK_E);
		mKeyMap.put('f', KeyEvent.VK_F);
		mKeyMap.put('g', KeyEvent.VK_G);
		mKeyMap.put('h', KeyEvent.VK_H);
		mKeyMap.put('i', KeyEvent.VK_I);
		mKeyMap.put('j', KeyEvent.VK_J);
		mKeyMap.put('k', KeyEvent.VK_K);
		mKeyMap.put('l', KeyEvent.VK_L);
		mKeyMap.put('m', KeyEvent.VK_M);
		mKeyMap.put('n', KeyEvent.VK_N);
		mKeyMap.put('o', KeyEvent.VK_O);
		mKeyMap.put('p', KeyEvent.VK_P);
		mKeyMap.put('q', KeyEvent.VK_Q);
		mKeyMap.put('r', KeyEvent.VK_R);
		mKeyMap.put('s', KeyEvent.VK_S);
		mKeyMap.put('t', KeyEvent.VK_T);
		mKeyMap.put('u', KeyEvent.VK_U);
		mKeyMap.put('v', KeyEvent.VK_V);
		mKeyMap.put('w', KeyEvent.VK_W);
		mKeyMap.put('x', KeyEvent.VK_X);
		mKeyMap.put('y', KeyEvent.VK_Y);
		mKeyMap.put('z', KeyEvent.VK_Z);
		mKeyMap.put('0', KeyEvent.VK_0);
		mKeyMap.put('1', KeyEvent.VK_1);
		mKeyMap.put('2', KeyEvent.VK_2);
		mKeyMap.put('3', KeyEvent.VK_3);
		mKeyMap.put('4', KeyEvent.VK_4);
		mKeyMap.put('5', KeyEvent.VK_5);
		mKeyMap.put('6', KeyEvent.VK_6);
		mKeyMap.put('7', KeyEvent.VK_7);
		mKeyMap.put('8', KeyEvent.VK_8);
		mKeyMap.put('9', KeyEvent.VK_9);
		mKeyMap.put(' ', KeyEvent.VK_SPACE);
		mKeyMap.put('\n', KeyEvent.VK_ENTER);
//		KeyEvent.
//		mKeyMap.put(KEY_LEFT_SHIFT, KeyEvent.VK_SHIFT);
	}
}

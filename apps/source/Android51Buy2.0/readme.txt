 ui资料： \\tencent.com\tfs\跨部门项目\ECC-上海设计中心\01 日常输出\APP\易迅android\
 
 规范：
 
1. 关于ID:

公共： global_textview_xx

页面 : order_textview_xx


2. 关于activity静态变量

requestCode: FLAG_REQUEST_xx

resultCode : FLAAG_RESULT_xx

activity接收参数： REQUEST_xx

A -> B : B中定义requestCode.

B back A : B中定义resultCode;


3. 关于drawable

a. 图片资源, 以i_开头: 公共：i_global_xx.png, 页面 ：i_order_xx.png, drawable-hdpi、drawable-mdpi、drawable-ldpi三个文件夹只存放图片
b. drawable文件夹中资源: 公共: x_global_xx.xml, 页面：x_order_xx.xml, 此文件夹只存放selector
c. drawable.xml文件中的资源: 公共: d_global_xx, 页面: d_order_xx

4. 关于layout

公共：global_xx.xml(如global_title.xml, global_loading.xml)

页面： xx.xml(如orderlist_activity.xml, orderlist_item.xml)

关于分辨率：

http://www.20ju.com/content/V177605.htm

平台版本	API Level
Android 3.2	     13
Android 3.1      12
Android 3.0	     11
Android 2.3.3	 10
Android 2.3	     9
Android 2.2	     8
Android 2.1	     7
Android 2.0.1	 6
Android 2.0	     5
Android 1.6	     4
Android 1.5	     3
Android 1.1	     2
Android 1.0	     1


CENTER：图片大小为原始大小，如果图片大小大于ImageView控件，则截取图片中间部分，若小于，则直接将图片居中显示。
CENTER_CROP：将图片等比例缩放，让图像的短边与ImageView的边长度相同，即不能留有空白，缩放后截取中间部分进行显示。
CENTER_INSIDE：将图片大小大于ImageView的图片进行等比例缩小，直到整幅图能够居中显示在ImageView中，小于ImageView的图片不变，直接居中显示。
FIT_CENTER：ImageView的默认状态，大图等比例缩小，使整幅图能够居中显示在ImageView中，小图等比例放大，同样要整体居中显示在ImageView中。
FIT_END：缩放方式同FIT_CENTER，只是将图片显示在右方或下方，而不是居中。
FIT_START：缩放方式同FIT_CENTER，只是将图片显示在左方或上方，而不是居中。
FIT_XY：将图片非等比例缩放到大小与ImageView相同。
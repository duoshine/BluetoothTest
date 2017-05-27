使用
=
* 打开应用之后就会自动开始扫描蓝牙设备，如果没有开启蓝牙会提示开启蓝牙，3秒后开始扫描设备，可以通过下拉刷新重新扫描


![](https://github.com/duoshine/BluetoothTest/raw/master/img/1_1.png)  

* 点击连接后，会开始连蓝牙设备，这一过程最多持续5秒，五秒如果没有连接上就属于连接超时，不过我觉得3秒应该都是用户等待的极限了。。。连接成功跳转测试指令界面

![](https://github.com/duoshine/BluetoothTest/raw/master/img/2_2.png) 

* 列出了该设备下所有的包括服务、write、notify的uuid，根据协议里规定的uuid，分别手动设置service,notifi,wirte的uuid

![](https://github.com/duoshine/BluetoothTest/raw/master/img/3_3.png)

* 可以发送指令调试了，默认是打开notifi的，发送成功收到的指令就在上面返回了，看我测试发送的

![](https://github.com/duoshine/BluetoothTest/raw/master/img/4_4.png)

* 已经连接过的蓝牙设备，会保存在bonded中，这样首页设备很多的话就不用去翻页了。。。比如我们公司一堆蓝牙设备。。。。

![](https://github.com/duoshine/BluetoothTest/raw/master/img/5_5.png)







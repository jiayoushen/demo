# demo
本地GIF动画+本地音乐文件+科大讯飞测评SDK

2017.8.23 10
将动画资源和音乐资源放在本地，动画资源制作GIF，mediaplayer播放音乐文件，录音测评的话还是用科大讯飞的SDK
1.音乐start的时候开始动画，音乐complete的时候结束动画，100ms开始录音

2017.8.23 11	对话的音乐文件放在sd卡还是资源文件里面？
	  16	顺序：机器→人→机器→人？人→机器→人→机器？或者其他情况？

2017.8.24  9	MediaPlayer的onCompletion是异步的，怎样实现循环一整段话？（实现）
	  15	顺序：人→机器→人→机器的实现：将第一个人分离出来，之后 机器→人 看作一组对话(原实现：机器→人→机器→人)
2017.8.25 10	对话内容怎么显示？
2017.8.28 	怎么实现总分界面？
2017.8.29	怎么传递数据？语音时长？
2017.8.30	总分界面播放与结束语音状态更新？

2017.8.31	显示隐藏对话框：按钮状态，勾选显示，未勾选隐藏；显示状态下判断当前是机器还是人说话，用mediaplay.isPlaying()判断；
		对话结束之后不能显示状态，加sod判断；显示状态下会出现没有文字的bug，不管对话框显示与否都设置文字。
		录音时间提示？开源框架com.github.lzyzsd.circleprogress.DonutProgress；
		录音时长控制？目前写的测试数据是数组，思考怎样将录音时长加上。(科大讯飞设置speech_timeout？是设置该属性)

2017.9.4	显示全段对话文本：双击弹出popupwindow，popupwindow显示封装在grades里面的数据；添加一组完整的对话，解决由人先读的时
		候会出现录音时间不正确的一个BUG；思考怎样去做开始提示？(寻找游戏开始相关的设计，或参考其他app)
		
2017.9.5	思考怎样去做开始提示？弹出一个透明dialog，给上倒计时，handler+runnable实现倒计时；closeDialog(Dialog mDialog)为静	
		态方法时mDialog不为空，但是mDialog.isShowing()为false，改为普通方法没问题；先录音的话，第一段话无声音的问题，是逻	
		辑出了点问题。

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

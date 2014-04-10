package com.sungy.onegame.mclass;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sungy.onegame.LeftFragment;

public class Global {

	//保存所有从后台获取数据的URL
	
	//游戏
	public static final String GAME_LISTOF = "http://3gonegame.sinaapp.com/action.php?c=Game&a=listof";//测试用
	public static final String GAME_CURRENTDAY = "http://3gonegame.sinaapp.com/action.php?c=Game&a=currentday";//获取当天的游戏推荐
	public static final String GAME_CURRENTDAYLIST = "http://3gonegame.sinaapp.com/action.php?c=Game&a=currentdayList";//获取从当天开始的N条推荐，N与你的请求参数pageSize和pageNo有关
	public static final String GAME_GETONEDAY = "http://3gonegame.sinaapp.com/action.php?c=Game&a=getOneDay";//获取某一天的游戏推荐
	public static final String GAME_GETONEDAYLIST = "http://3gonegame.sinaapp.com/action.php?c=Game&a=getOneDayList";//获取从某一天开始的N条推荐
	public static final String GAME_GETGAMEBYID = "http://3gonegame.sinaapp.com/action.php?c=Game&a=getGameById";//根据游戏id获取游戏推荐
	public static final String GAME_GETGAMEFROMDATE = "http://3gonegame.sinaapp.com/action.php?c=Game&a=getGameFromDate";//获取当天到指定日期的所有游戏推荐
	
	//收藏
	public static final String COLLECT_GETBYUSERID = "http://3gonegame.sinaapp.com/action.php?c=Collect&a=getByUserid";//根据用户id获取用户的N条收藏
	public static final String COLLECT_COLLECT = "http://3gonegame.sinaapp.com/action.php?c=Collect&a=collect";//用户收藏游戏推荐
	public static final String COLLECT_CANCLECOLLECT = "http://3gonegame.sinaapp.com/action.php?c=Collect&a=cancleCollent";//用户取消收藏
	public static final String COLLECT_ISCOLLECT = "http://3gonegame.sinaapp.com/action.php?c=Collect&a=isCollect";//用户是否收藏
	
	//评论
	public static final String COMMENT_GETBYUSERID = "http://3gonegame.sinaapp.com/action.php?c=Comment&a=getByUserid";//根据游戏id获取游戏的评论N条
	public static final String COMMENT_COMMENT = "http://3gonegame.sinaapp.com/action.php?c=Comment&a=comment";//用户评论游戏
	public static final String COMMENT_CANCLECOMMENT = "http://3gonegame.sinaapp.com/action.php?c=Comment&a=cancleComment";//用户删除评论
	
	//反馈
	public static final String FEEDBACK_FEEDBACK = "http://3gonegame.sinaapp.com/action.php?c=Feedback&a=feedback";//用户提交反馈
	
	//点赞
	public static final String PRAISE_PRAISE = "http://3gonegame.sinaapp.com/action.php?c=Praise&a=praise";//用户点赞游戏
	public static final String PRAISE_CANCLEPRAISE = "http://3gonegame.sinaapp.com/action.php?c=Praise&a=canclePraise";//用户取消点赞
	public static final String PRAISE_ISRAISE = "http://3gonegame.sinaapp.com/action.php?c=Praise&a=isPraise";//用户是否点赞
	
	//分享
	public static final String SHARE_SHARE = "http://3gonegame.sinaapp.com/action.php?c=Share&a=share";//用户点赞游戏
	
	//用户
	public static final String USER_REGUSER = "http://3gonegame.sinaapp.com/action.php?c=User&a=regUser";//注册用户
	public static final String USER_LOGIN = "http://3gonegame.sinaapp.com/action.php?c=User&a=login";//用户登录
	public static final String USER_THIRDLOGIN = "http://3gonegame.sinaapp.com/action.php?c=User&a=thirdLogin";//第三方用户登录
	
	
	//共享登录数据
	private static String userId ;			//用户id
	private static String userNmae ;		//用户名
	private static String userImage ;		//用户头像
	private static boolean isLogin = false;			//是否已登录
	private static HashMap<String, Integer> detailList = new HashMap<String, Integer>();
	
	public static HashMap<String, Integer> getDetailList() {
		return detailList;
	}
	
	public static String getUserId() {
		return userId;
	}
	public static void setUserId(String userId) {
		Global.userId = userId;
	}
	public static String getUserNmae() {
		return userNmae;
	}
	public static void setUserNmae(String userNmae) {
		Global.userNmae = userNmae;
	}
	public static String getUserImage() {
		return userImage;
	}
	public static void setUserImage(String userImage) {
		Global.userImage = userImage;
	}
	public static boolean isLogin() {
		return isLogin;
	}
	public static void setLogin(boolean isLogin) {
		Global.isLogin = isLogin;
	}
	
	//检查用户是否登录
	public static boolean checkLogin(Context context){
		if(!Global.isLogin()){
//			ToastUtils.showDefaultToast(context, "请先登录", Toast.LENGTH_SHORT);
			selectLogin(context);
			return false;
		}
		return true;
	}
	
	//弹出登录选择
	private static void selectLogin(final Context context){
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(
				context);
		mBuilder.setTitle("选择登录平台").setItems(LeftFragment.plats,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						switch (which) {
						case 0:
							if(listener != null){
								listener.clickToWeibo(context);
							}
							break;
						case 1:
							if(listener != null){
								listener.clickToQQ(context);
							}
							break;
						}
					}
				});
		mBuilder.show();
	}
	
	//登录listener
	static Global.LoginListener listener = null;
	
	public static void setListener(Global.LoginListener mListener){
		listener = mListener;
	}
	
	//Listener 为其他页面登录所用
	public interface LoginListener{
		public void clickToQQ(Context context);
		public void clickToWeibo(Context context);
	}
	
}



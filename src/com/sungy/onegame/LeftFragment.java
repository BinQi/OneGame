package com.sungy.onegame;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;

import com.sungy.onegame.activity.FavoritesFragment;
import com.sungy.onegame.activity.FeedBackActivity;
import com.sungy.onegame.activity.ResourceFragment;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.Global.LoginListener;
import com.sungy.onegame.mclass.HttpUtils;
import com.sungy.onegame.mclass.ToastUtils;

public class LeftFragment extends Fragment implements Callback {
	private TextView userNameTv, userLoginTv;
	private ImageView userImage;
	private final static int WEIBO_NAME = 0;
	private final static int WEIBO_Image = 1;
	private final static int QQ_NAME = 2;
	private final static int QQ_Image = 3;
	public final static String[] plats = { "新浪微博登录", "QQ登录" };
	private Platform qzone;
	private Platform weibo;
	private Context mContext;
	private boolean isDefaultLogin = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ShareSDK.initSDK(getActivity());
		View view = inflater.inflate(R.layout.left_fragment, null);
		userNameTv = (TextView) view.findViewById(R.id.userName);
		userImage = (ImageView) view.findViewById(R.id.userImage);
		userLoginTv = (TextView) view.findViewById(R.id.userLogin);

		// user login
		LinearLayout oneGameLogin = (LinearLayout) view
				.findViewById(R.id.login);
		oneGameLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userLoginTv.getText().equals("退出登录")) {
					new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("您确定要注销？")
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
										}
									})
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											String[] infos = readLoginInfo();
											clearLoginInfo();
											clearGlobal();
											String plat = infos[3];
											if (plat.equals("qq")) {
												qzone = ShareSDK.getPlatform(
														getActivity(),
														QZone.NAME);
											} else if (plat.equals("weibo")) {
												weibo = ShareSDK.getPlatform(
														getActivity(),
														SinaWeibo.NAME);
											}
											userNameTv.setText("name");
											userLoginTv.setText("登录");
											userImage
													.setImageResource(R.drawable.defaultimage);
											ToastUtils.showDefaultToast(
													getActivity(), "注销成功",
													Toast.LENGTH_SHORT);
											isDefaultLogin = false;
										}
									}).show();
				} else {
					AlertDialog.Builder mBuilder = new AlertDialog.Builder(
							getActivity());
					mBuilder.setTitle("选择登录平台").setItems(plats,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										toWeibo();
										break;
									case 1:
										toQQ();
										break;
									}
								}
							});
					mBuilder.show();
				}
			}
		});

		LinearLayout oneGamePage = (LinearLayout) view
				.findViewById(R.id.one_game);
		oneGamePage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.center_frame, new SampleListFragment());
				ft.commit();
				((MainActivity) getActivity()).showLeft();
			}
		});

		LinearLayout oneResourcePage = (LinearLayout) view
				.findViewById(R.id.one_resource);
		oneResourcePage.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ResourceFragment resource = new ResourceFragment();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.center_frame, resource);
				ft.commit();
				((MainActivity) getActivity()).showLeft();
			}
		});

		LinearLayout oneFavoritesPage = (LinearLayout) view
				.findViewById(R.id.one_favorite);
		oneFavoritesPage.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// 检查是否登录
				if (!Global.checkLogin(getActivity())) {
					return;
				}
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.center_frame, new FavoritesFragment());
				ft.commit();
				((MainActivity) getActivity()).showLeft();
			}
		});

		LinearLayout oneFeedbackPage = (LinearLayout) view
				.findViewById(R.id.one_feedback);
		oneFeedbackPage.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// 检查是否登录
				if (!Global.checkLogin(getActivity())) {
					return;
				}
				startActivity(new Intent(getActivity(), FeedBackActivity.class));
			}
		});

		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		ShareSDK.initSDK(getActivity());

		mContext = getActivity();

		// 初始化登录信息
		initLoginInfo();
	}

	// 初始化登录信息
	private void initLoginInfo() {
		String username = "";
		String userimage = "";
		String userid = "";
		String plat = "";
		String thirdid = "";
		String[] infos = null;
		// 写入操作,并授权
		infos = readLoginInfo();
		if (infos != null) {
			isDefaultLogin = true;

			username = infos[0];
			userimage = infos[1];
			userid = infos[2];
			plat = infos[3];
			thirdid = infos[4];
			afterLogin(username, plat, thirdid, userimage);
			if (plat.equals("qq")) {
				qzone = ShareSDK.getPlatform(getActivity(), QZone.NAME);
				qzone.SSOSetting(true);
				// qzone.authorize();
			} else if (plat.equals("weibo")) {
				weibo = ShareSDK.getPlatform(getActivity(), SinaWeibo.NAME);
				weibo.SSOSetting(true);
				// weibo.authorize();
			}
		}
	}

	private Bitmap downloadIcon(String url) {
		try {
			URL mUrl = new URL(url);
			HttpURLConnection mConnection = (HttpURLConnection) mUrl
					.openConnection();
			InputStream mStream = mConnection.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(mStream);
			return bitmap;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case WEIBO_NAME:
			userNameTv.setText((String) msg.obj);
			userLoginTv.setText("退出登录");
			tipLoginSuccess((String) msg.obj);
			break;
		case WEIBO_Image:
			userImage.setImageBitmap((Bitmap) msg.obj);
			break;
		case QQ_NAME:
			userNameTv.setText((String) msg.obj);
			userLoginTv.setText("退出登录");
			tipLoginSuccess((String) msg.obj);
			break;
		case QQ_Image:
			userImage.setImageBitmap((Bitmap) msg.obj);
			break;
		default:
			break;
		}
		return false;
	}

	// 提示登录成功
	private void tipLoginSuccess(String name) {
		Context context = mContext;
		// 是否是其他页面登录
		if (isFromOther) {
			context = otherContext;
		}
		if (!isDefaultLogin) {
			new AlertDialog.Builder(context)
					.setTitle("OneGame")
					.setMessage("欢迎来到OneGame," + name + "!")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
		setIsFromOther(false);
	}



	// 接入微博
	public void toWeibo() {
		if (!HttpUtils.isNetworkConnected(getActivity())) {
			ToastUtils.showCenterToast(getActivity(), "请检查网络状态", Toast.LENGTH_SHORT);
			return;
		}
		weibo = ShareSDK.getPlatform(getActivity(), SinaWeibo.NAME);
		weibo.SSOSetting(true);
		weibo.setPlatformActionListener(new PlatformActionListener() {

			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {

			}

			@Override
			public void onComplete(Platform arg0, int arg1,
					HashMap<String, Object> arg2) {
				final String name = arg0.getDb().getUserName();
				final String userId = arg0.getDb().getUserId();
				final String iconUrl = arg0.getDb().getUserIcon();
				
				afterLogin(name, "weibo", userId, iconUrl);
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {

			}
		});
		
		weibo.authorize();
	}

	// 接入QQ
	public void toQQ() {
		if (!HttpUtils.isNetworkConnected(getActivity())) {
			ToastUtils.showCenterToast(getActivity(), "请检查网络状态", Toast.LENGTH_SHORT);
			return;
		}
		qzone = ShareSDK.getPlatform(getActivity(), QZone.NAME);
		qzone.SSOSetting(true);
		qzone.setPlatformActionListener(new PlatformActionListener() {

			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {

			}

			@Override
			public void onComplete(Platform arg0, int arg1,
					HashMap<String, Object> arg2) {
				final String name = arg0.getDb().getUserName();
				final String userId = arg0.getDb().getUserId();
				final String iconUrl = arg0.getDb().getUserIcon();

				afterLogin(name, "qq", userId, iconUrl);
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {

			}
		});
		qzone.authorize();
	}

	// 写入全局数据
	public void write2Global(String user_id, String name, String iconUrl,
			boolean isLogin) {
		Global.setUserId(user_id);
		Global.setUserNmae(name);
		Global.setUserImage(iconUrl);
		Global.setLogin(isLogin);
	}

	// 消除全局数据
	public void clearGlobal() {
		Global.setUserId("");
		Global.setUserNmae("");
		Global.setUserImage("");
		Global.setLogin(false);
	}

	// 获取登录信息
	private String[] readLoginInfo() {
		// 获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式
		SharedPreferences sp = getActivity().getApplicationContext()
				.getSharedPreferences("onegameLoginInfo",
						Context.MODE_WORLD_READABLE);
		if (sp.getString("username", "").equals("")) {
			return null;
		} else {
			String[] infos = new String[5];
			infos[0] = sp.getString("username", "");
			infos[1] = sp.getString("userimage", "");
			infos[2] = sp.getString("userid", "");
			infos[3] = sp.getString("plat", "");
			infos[4] = sp.getString("thirdid", "");
			return infos;
		}
	}

	// 写入登录信息
	private void writeLoginInfo(String username, String userimage,
			String userid, String plat, String thirdid) {
		// 获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式
		SharedPreferences sp = getActivity().getApplicationContext()
				.getSharedPreferences("onegameLoginInfo",
						Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor edit = sp.edit();
		edit.putString("username", username);
		edit.putString("userimage", userimage);
		edit.putString("userid", userid);
		edit.putString("plat", plat);
		edit.putString("thirdid", thirdid);
		// 提交
		edit.commit();
	}

	// 消除登录信息
	private void clearLoginInfo() {
		// 获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式
		SharedPreferences sp = getActivity().getApplicationContext()
				.getSharedPreferences("onegameLoginInfo",
						Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor edit = sp.edit();
		edit.putString("username", "");
		edit.putString("userimage", "");
		edit.putString("userid", "");
		edit.putString("plat", "");
		edit.putString("thirdid", "");
		// 提交
		edit.commit();
	}

	// 登录后的操作
	private void afterLogin(final String name, final String plat,
			final String thirdId, final String iconUrl) {
		new Thread() {
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_name", name));
				params.add(new BasicNameValuePair("third_plat", plat));
				params.add(new BasicNameValuePair("third_id", thirdId));
				params.add(new BasicNameValuePair("user_image", iconUrl));
				String str = HttpUtils.doPost(Global.USER_THIRDLOGIN, params);

				// 写入全局数据
				JSONObject json;
				String user_id = "";
				try {
					json = new JSONObject(str);
					user_id = json.getString("user_id");
					write2Global(user_id, name, iconUrl, true);
					// 写入sharepreference
					writeLoginInfo(name, iconUrl, user_id, plat, thirdId);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Bitmap mBitmap = downloadIcon(iconUrl);

				Message msg = new Message();
				msg.what = (plat.equals("qq")) ? QQ_NAME : WEIBO_NAME;
				msg.obj = name;
				UIHandler.sendMessage(msg, LeftFragment.this);

				Message msg2 = new Message();
				msg2.what = (plat.equals("qq")) ? QQ_Image : WEIBO_Image;
				msg2.obj = mBitmap;
				UIHandler.sendMessage(msg2, LeftFragment.this);

			};
		}.start();

	}

	// 为其他页面登录所用
	public boolean isFromOther = false;
	public Context otherContext;
	// 为其他页面登录所用
	public Global.LoginListener listener;

	// 设置listner
	public void setListener() {
		listener = new LoginListener() {
			@Override
			public void clickToQQ(Context context) {
				setOtherContext(context);
				setIsFromOther(true);
				toQQ();
			}

			@Override
			public void clickToWeibo(Context context) {
				setOtherContext(context);
				setIsFromOther(true);
				toWeibo();
			}
		};
	}

	public Global.LoginListener getListner() {
		return listener;
	}

	public void setOtherContext(Context context) {
		otherContext = context;
	}

	public void setIsFromOther(boolean is) {
		isFromOther = is;
	}
}

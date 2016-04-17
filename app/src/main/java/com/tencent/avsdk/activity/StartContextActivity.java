package com.tencent.avsdk.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tencent.av.sdk.AVError;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;
import com.tencent.tls.HostLoginActivity;
import com.tencent.tls.TLSConfiguration;
import com.tencent.tls.TLSService;
import com.tencent.tls.helper.TLSHelper;

import java.util.ArrayList;

import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSUserInfo;

public class StartContextActivity extends ListActivity {
	private static final String TAG = "StartContextActivity";
	private static final int DIALOG_LOGIN = 0;
	private static final int DIALOG_LOGOUT = DIALOG_LOGIN + 1;
	private static final int DIALOG_LOGIN_ERROR = DIALOG_LOGOUT + 1;
	private static final int REQUEST_CODE_CREATE_ACTIVITY = 0;
	private static final int REQUEST_CODE_ADD = REQUEST_CODE_CREATE_ACTIVITY + 1;
	private static final int REQUEST_CODE_ADD_USER = REQUEST_CODE_ADD + 1;
	private int mPosition;
	private int mLoginErrorCode = AVError.AV_OK;
	private ArrayAdapter<String> mAdapter = null;
	private ProgressDialog mDialogLogin = null;
	private ProgressDialog mDialogLogout = null;
	private QavsdkControl mQavsdkControl;
	private ArrayList<String> mArrayList = new ArrayList<String>();
	private Context ctx = null;
	private String loginInfoUrl="http://bbs.qcloud.com/forum.php?mod=viewthread&tid=8287&extra=page%3D1%26filter%3Dsortid%26sortid%3D6%26sortid%3D6";
	private TLSService tlsService;
	
	private boolean testEnvStatus = false;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(TAG, "WL_DEBUG onReceive action = " + action);
			Log.e(TAG, "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " In");				
			if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {			
				mLoginErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);

				if (mLoginErrorCode == AVError.AV_OK) {
					refreshWaitingDialog();
					startActivityForResult(
							new Intent(StartContextActivity.this,
									CreateRoomActivity.class).putExtra(
									Util.EXTRA_IDENTIFIER_LIST_INDEX, mPosition),
							REQUEST_CODE_CREATE_ACTIVITY);
				} else {
					showDialog(DIALOG_LOGIN_ERROR);
				}
			} else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {
				mQavsdkControl.setIsInStopContext(false);
				refreshWaitingDialog();
			}
			Log.e(TAG, "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " Out");					
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
//		LogcatHelper.getInstance(this).start();
		Log.e(TAG, "WL_DEBUG onCreate");
		setTitle(R.string.login);
		mArrayList.clear();
		
		ArrayList<String> identifierList = Util.getIdentifierList(this);
		
		if (null != identifierList) {
			mArrayList.addAll(identifierList);
		}
		mArrayList.add("=添加测试号=");		
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mArrayList);
		setListAdapter(mAdapter);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
		intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);	
		registerReceiver(mBroadcastReceiver, intentFilter);
		mQavsdkControl = ((QavsdkApplication) getApplication())
				.getQavsdkControl();		
		// 设置使用TLS SDK所需的配置信息
		TLSConfiguration.setSdkAppid(1400007711); // 必须项, sdkAppid, 1400000955, 1400007711
													// 1400001285, 101122465
		TLSConfiguration.setAccountType(3890); // 必须项, accountType, 373, 117, 107  3890
		TLSConfiguration.setAppVersion("1.0"); // 可选项, 表示app当前版本, 默认为1.0
		TLSConfiguration.setTimeout(3000); // 可选项, 表示网络操作超时时间, 默认为8s

		// 设置QQ APP_ID和APP_KEY
		TLSConfiguration.setQqAppIdAndAppKey("1104701569", "CXtj4p63eTEB2gSu");

		// 设置微信APP_ID和APP_SECRET
		TLSConfiguration.setWxAppIdAndAppSecret("wxc05322d5f11ea2b0", "3ace67c5982c6ed8daa36f8911f609d7");

		tlsService = TLSService.getInstance();
		tlsService.initTlsSdk(StartContextActivity.this); // 需要使用关于短信或字符串账号密码登录注册服务时调用
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshWaitingDialog();
		Log.e(TAG, "WL_DEBUG onResume");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		Log.e(TAG, "WL_DEBUG onDestroy");
//		LogcatHelper.getInstance(this).stop();
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		if (position == (l.getAdapter().getCount()-1)) {
			Intent intent = new Intent(StartContextActivity.this, HostLoginActivity.class);
			startActivityForResult(intent, REQUEST_CODE_ADD_USER);
		} else {
			if (!mQavsdkControl.hasAVContext()){
//				mQavsdkControl.resetAppid();
//				if (!mQavsdkControl.isDefaultAppid()||!mQavsdkControl.isDefaultUid()){
//					final TextView message = new TextView(this);
//					AlertDialog.Builder builder = new AlertDialog.Builder(this);
//					final SpannableString s = 
//				               new SpannableString(loginInfoUrl);
//					Linkify.addLinks(s, Linkify.WEB_URLS);
//					message.setText(s);
//					message.setMovementMethod(LinkMovementMethod.getInstance());
//					builder.setView(message)
//					       .setTitle(R.string.not_default_appid).setNeutralButton("ok", new OnClickListener(){
//
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method stub
//								login(position);
//							}});				
//					AlertDialog dialog = builder.create();
//					dialog.show();
//				}else{
//					login(position);
//				}	
				if (!mQavsdkControl.hasAVContext()) {
					mLoginErrorCode = mQavsdkControl.startContext(
							Util.getIdentifierList(this).get(position), Util
									.getUserSigList(this).get(position));

					if (mLoginErrorCode != AVError.AV_OK) {
						showDialog(DIALOG_LOGIN_ERROR);
					}

					mPosition = position;
					refreshWaitingDialog();
				}
			}
			
		}
		
		Log.e(TAG, "WL_DEBUG onListItemClick");		
	}
	
	private void login(int position){
		mLoginErrorCode = mQavsdkControl.startContext(
		Util.getIdentifierList(StartContextActivity.this).get(position), Util
				.getUserSigList(StartContextActivity.this).get(position));
		if (mLoginErrorCode != AVError.AV_OK) {
			showDialog(DIALOG_LOGIN_ERROR);
		}
	
		mPosition = position;
		refreshWaitingDialog();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_LOGIN:
			dialog = mDialogLogin = Util.newProgressDialog(this,
					R.string.at_login);
			break;

		case DIALOG_LOGOUT:
			dialog = mDialogLogout = Util.newProgressDialog(this,
					R.string.at_logout);
			break;

		case DIALOG_LOGIN_ERROR:
			dialog = Util.newErrorDialog(this, R.string.login_failed);
			break;

		default:
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_LOGIN_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mLoginErrorCode);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "WL_DEBUG onActivityResult requestCode = " + requestCode);
		Log.e(TAG, "WL_DEBUG onActivityResult resultCode = " + resultCode);
		switch (requestCode) {
		case REQUEST_CODE_CREATE_ACTIVITY:
			mQavsdkControl.stopContext();
			refreshWaitingDialog();
			break;

		case REQUEST_CODE_ADD:
			switch (resultCode) {
			case RESULT_OK:
				mArrayList.clear();
				
				ArrayList<String> identifierList = Util.getIdentifierList(this);
				if (null != identifierList) {
					mArrayList.addAll(identifierList);
				}
				
				mArrayList.add("=添加测试号=");				
				mAdapter.notifyDataSetChanged();
				break;
			}
			break;
		case REQUEST_CODE_ADD_USER:
			tlsService = TLSService.getInstance();
			tlsService.refreshUserSig(new TLSService.RefreshUserSigListener() {

				@Override
				public void OnRefreshUserSigFail(TLSErrInfo arg0) {
					// TODO Auto-generated method stub
					TLSHelper.notOK(StartContextActivity.this, arg0);
				}

				@Override
				public void OnRefreshUserSigSuccess(TLSUserInfo arg0) {
					// TODO Auto-generated method stub
					String sig = tlsService.getUserSig(tlsService.getLastUserIdentifier());
					if (!isUserExist(arg0.identifier)) {
						
						saveUser(arg0.identifier, sig);
						mArrayList.clear();
						mArrayList.addAll(Util.getIdentifierList(StartContextActivity.this));
						mArrayList.add("=添加测试号=");
						mAdapter.notifyDataSetChanged();
					}else{
						updateUserSig(arg0.identifier, sig);
					}

				}

				@Override
				public void OnRefreshUserSigTimeout(TLSErrInfo arg0) {
					// TODO Auto-generated method stub
					TLSHelper.notOK(StartContextActivity.this, arg0);
				}

				@Override
				public void onUserSigNotExist() {
					// TODO Auto-generated method stub
//					TLSHelper.showToast(StartContextActivity.this,
//							StartContextActivity.this.getString(R.string.tls_sig_not_exist));

				}

			});
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;

		switch (item.getItemId()) {
		case R.id.action_add:
			Intent intent = new Intent(StartContextActivity.this, HostLoginActivity.class);
			startActivityForResult(intent, REQUEST_CODE_ADD_USER);
			break;
		case R.id.uid_add:
			startActivityForResult(new Intent(this, ModifyAppidUidActivity.class),
					REQUEST_CODE_ADD);
			break;
			
		case R.id.action_changemode:
			testEnvStatus = !testEnvStatus;
			if (testEnvStatus) {
				item.setTitle(R.string.testenv);
				Toast.makeText(getApplicationContext(), this.getResources().getText(R.string.testenv), Toast.LENGTH_SHORT).show();
				item.setEnabled(false);
			} else{
				item.setTitle(R.string.notestenv);
				Toast.makeText(getApplicationContext(), this.getResources().getText(R.string.notestenv), Toast.LENGTH_SHORT).show();
			}
			
			Log.d("action", "testEnvStatus: " + testEnvStatus);	
			mQavsdkControl.setTestEnvStatus(testEnvStatus);
			
		default:
			break;
		}

		return result;
	}

	private void refreshWaitingDialog() {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Util.switchWaitingDialog(ctx, mDialogLogin, DIALOG_LOGIN,
						mQavsdkControl.getIsInStartContext());
				Util.switchWaitingDialog(ctx, mDialogLogout, DIALOG_LOGOUT,
						mQavsdkControl.getIsInStopContext());		
			}
		});

	}
	
	
	private void saveUser(String id, String sig) {
		ArrayList<String> identifierList = Util.getIdentifierList(this);
		ArrayList<String> usersigList = Util.getUserSigList(this);

		identifierList.add(id);
		usersigList.add(sig);
		Util.setIdentifierList(this, identifierList);
		Util.setUserSigList(this, usersigList);
	}
	
	private void updateUserSig(String id,String sig){
		ArrayList<String> identifierList = Util.getIdentifierList(this);
		ArrayList<String> usersigList = Util.getUserSigList(this);
		if (identifierList==null||usersigList==null||identifierList.size()!=usersigList.size()) return;
		for (int i=0;i<identifierList.size();++i){
			if (identifierList.get(i).equals(id)){
				usersigList.set(i, sig);				
			}
		}
		Util.setIdentifierList(this, identifierList);
		Util.setUserSigList(this, usersigList);
	}
	
	private boolean isUserExist(String id) {

		for (String s : mArrayList) {
			if (s.equals(id)) {
				return true;
			}
		}
		return false;
	}
}

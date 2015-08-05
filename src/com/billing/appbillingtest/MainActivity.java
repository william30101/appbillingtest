package com.billing.appbillingtest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;
import com.billing.appbillingtest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Context context;
	private String tag;

	private IInAppBillingService mService;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};

	// ProductID
	private final String productID = "translate_quota_5000";	// Test Product ID by Google

	// View
	private Button btnTest, btnCheck, btnBuy, btnConsume;
	

	private String TAG = "inAppBilling";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		// Context
				context = getApplicationContext();

				// log tag
				tag = "in_app_billing_ex";

				// Bind Service
				final boolean blnBind = bindService(new Intent(
						"com.android.vending.billing.InAppBillingService.BIND"),
						mServiceConn, Context.BIND_AUTO_CREATE);

				Toast.makeText(context, "bindService - return " + String.valueOf(blnBind), Toast.LENGTH_SHORT).show();
				Log.i(tag, "bindService - return " + String.valueOf(blnBind));

				// Assign View
				btnTest    = (Button) findViewById(R.id.btnTest);
				btnCheck   = (Button) findViewById(R.id.btnCheck);
				btnBuy     = (Button) findViewById(R.id.btnBuy);
				btnConsume = (Button) findViewById(R.id.btnConsume);

				btnTest.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!blnBind) return;
						if (mService == null) return;

						int result;
						try {
							result = mService.isBillingSupported(3, getPackageName(), "inapp");

							Toast.makeText(context, "isBillingSupported() - success : return " + String.valueOf(result), Toast.LENGTH_SHORT).show();
							Log.i(tag, "isBillingSupported() - success : return " + String.valueOf(result));
						} catch (RemoteException e) {
							e.printStackTrace();

							Toast.makeText(context, "isBillingSupported() - fail!", Toast.LENGTH_SHORT).show();
							Log.w(tag, "isBillingSupported() - fail!");
							return;
						}

						// TODO: check result
					}
				});

				btnCheck.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!blnBind) return;
						if (mService == null) return;

						Bundle ownedItems;
						try {
							ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

							Toast.makeText(context, "getPurchases() - success return Bundle", Toast.LENGTH_SHORT).show();
							Log.i(tag, "getPurchases() - success return Bundle");
						} catch (RemoteException e) {
							e.printStackTrace();

							Toast.makeText(context, "getPurchases - fail!", Toast.LENGTH_SHORT).show();
							Log.w(tag, "getPurchases() - fail!");
							return;
						}

						int response = ownedItems.getInt("RESPONSE_CODE");
						Toast.makeText(context, "getPurchases() - \"RESPONSE_CODE\" return " + String.valueOf(response), Toast.LENGTH_SHORT).show();
						Log.i(tag, "getPurchases() - \"RESPONSE_CODE\" return " + String.valueOf(response));

						if (response != 0) return;

						ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
						ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
						ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
						String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

						Log.i(tag, "getPurchases() - \"INAPP_PURCHASE_ITEM_LIST\" return " + ownedSkus.toString());
						Log.i(tag, "getPurchases() - \"INAPP_PURCHASE_DATA_LIST\" return " + purchaseDataList.toString());
						Log.i(tag, "getPurchases() - \"INAPP_DATA_SIGNATURE\" return " + (signatureList != null ? signatureList.toString() : "null"));
						Log.i(tag, "getPurchases() - \"INAPP_CONTINUATION_TOKEN\" return " + (continuationToken != null ? continuationToken : "null"));

						// TODO: management owned purchase
					}
				});

				btnBuy.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!blnBind) return;
						if (mService == null) return;

						ArrayList<String> skuList = new ArrayList<String>();
						skuList.add(productID);
						Bundle querySkus = new Bundle();
						querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

						Bundle skuDetails;
						try {
							skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

							Toast.makeText(context, "getSkuDetails() - success return Bundle", Toast.LENGTH_SHORT).show();
							Log.i(tag, "getSkuDetails() - success return Bundle");
						} catch (RemoteException e) {
							e.printStackTrace();

							Toast.makeText(context, "getSkuDetails() - fail!", Toast.LENGTH_SHORT).show();
							Log.w(tag, "getSkuDetails() - fail!");
							return;
						}

						int response = skuDetails.getInt("RESPONSE_CODE");
						Toast.makeText(context, "getSkuDetails() - \"RESPONSE_CODE\" return " + String.valueOf(response), Toast.LENGTH_SHORT).show();
						Log.i(tag, "getSkuDetails() - \"RESPONSE_CODE\" return " + String.valueOf(response));

						if (response != 0) return;

						ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
						Log.i(tag, "getSkuDetails() - \"DETAILS_LIST\" return " + responseList.toString());

						if (responseList.size() == 0) return;

						for (String thisResponse : responseList) {
							try {
								JSONObject object = new JSONObject(thisResponse);

								String sku   = object.getString("productId");
								String title = object.getString("title");
								String price = object.getString("price");

								Log.i(tag, "getSkuDetails() - \"DETAILS_LIST\":\"productId\" return " + sku);
								Log.i(tag, "getSkuDetails() - \"DETAILS_LIST\":\"title\" return " + title);
								Log.i(tag, "getSkuDetails() - \"DETAILS_LIST\":\"price\" return " + price);

								if (!sku.equals(productID)) continue;

								Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

								Toast.makeText(context, "getBuyIntent() - success return Bundle", Toast.LENGTH_SHORT).show();
								Log.i(tag, "getBuyIntent() - success return Bundle");

								response = buyIntentBundle.getInt("RESPONSE_CODE");
								Toast.makeText(context, "getBuyIntent() - \"RESPONSE_CODE\" return " + String.valueOf(response), Toast.LENGTH_SHORT).show();
								Log.i(tag, "getBuyIntent() - \"RESPONSE_CODE\" return " + String.valueOf(response));

								if (response != 0) continue;

								PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
								startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (RemoteException e) {
								e.printStackTrace();

								Toast.makeText(context, "getSkuDetails() - fail!", Toast.LENGTH_SHORT).show();
								Log.w(tag, "getBuyIntent() - fail!");
							} catch (SendIntentException e) {
								e.printStackTrace();
							}
						}
					}
				});

				btnConsume.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!blnBind) return;
						if (mService == null) return;

						int response;
						try {
							response = mService.consumePurchase(3, getPackageName(), "inapp:com.ethanf.in_app_billing_ex:android.test.purchased");

							Toast.makeText(context, "consumePurchase() - success : return " + String.valueOf(response), Toast.LENGTH_SHORT).show();
							Log.i(tag, "consumePurchase() - success : return " + String.valueOf(response));
						} catch (RemoteException e) {
							e.printStackTrace();

							Toast.makeText(context, "consumePurchase() - fail!", Toast.LENGTH_SHORT).show();
							Log.w(tag, "consumePurchase() - fail!");
							return;
						}

						if (response != 0) return;

						// TODO: management comsume purchase
					}
				});
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1001) {
			if (resultCode != RESULT_OK) return;

			int responseCode = data.getIntExtra("RESPONSE_CODE", 1);
			Toast.makeText(context, "onActivityResult() - \"RESPONSE_CODE\" return " + String.valueOf(responseCode), Toast.LENGTH_SHORT).show();
			Log.i(tag, "onActivityResult() - \"RESPONSE_CODE\" return " + String.valueOf(responseCode));

			if (responseCode != 0) return;

			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
			String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

			Log.i(tag, "onActivityResult() - \"INAPP_PURCHASE_DATA\" return " + purchaseData.toString());
			Log.i(tag, "onActivityResult() - \"INAPP_DATA_SIGNATURE\" return " + dataSignature.toString());

			// TODO: management purchase result
		}
	}

	@Override
	protected void onDestroy() {
		// Unbind Service
		if (mService != null)
			unbindService(mServiceConn);

		super.onDestroy();
	}
    
}

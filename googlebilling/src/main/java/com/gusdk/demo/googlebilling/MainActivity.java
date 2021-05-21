package com.gusdk.demo.googlebilling;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aly.sdk.ALYAnalysis;
import com.aly.zflog.ZFLogReport;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "roy_billing";
        private String skuId = "600269.com.fishing.billionaire.android.coin.499";
//    private String skuId = "600260.com.meow.pop.blast.60coins.199";


    private BillingClient billingClient;
    private TextView tvSkuId;
    private String productId = "600269";

    private SkuDetails skuDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSkuId = findViewById(R.id.tv_skuid);
        tvSkuId.setText(skuId);
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                    ZFLogReport.logReport("user001", purchase.getOriginalJson(), purchase.getSignature());

                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                logAndToast("onPurchasesUpdated USER_CANCELED");
            } else {
                // Handle any other error codes.
                logAndToast("onPurchasesUpdated NOK " + billingResult.getResponseCode() + " msg " + billingResult.getDebugMessage());
            }
        }
    };

    private void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {

                Log.i(TAG, "onConsumeResponse: billingResult " + billingResult.toString() + " token : " + purchaseToken);

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    logAndToast("onConsumeResponse ok");
                } else {
                    logAndToast("onConsumeResponse Nok" + billingResult.getResponseCode() + " msg :" + billingResult.getDebugMessage());
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }


    public void initBilling(View view) {
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();


        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    logAndToast("onBillingSetupFinished " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                logAndToast("onBillingServiceDisconnected ");
            }
        });
    }


    private void logAndToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
                Log.i(TAG, content);
            }
        });
    }

    public void showGoods(View view) {
        List<String> skuList = new ArrayList<>();
        skuList.add(skuId);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        logAndToast("onSkuDetailsResponse  billingResult is " + billingResult.getResponseCode() + " msg " + billingResult.getDebugMessage() + " details " + skuDetailsList.toString());

                        if (skuDetailsList != null && skuDetailsList.size() > 0) {
                            skuDetails = skuDetailsList.get(0);
                        }
                    }
                });
    }

    public void pay(View view) {
// Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();

    }

    public void initTasdk(View view) {
        ALYAnalysis.enalbeDebugMode(true);
        ALYAnalysis.init(this, productId, "3000");
    }
}

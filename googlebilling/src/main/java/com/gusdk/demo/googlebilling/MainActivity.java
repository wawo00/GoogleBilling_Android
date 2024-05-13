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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
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
    private String userId="gamer001";

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
                    logAndToast("onBillingSetupFinished SUCCESS" );
                }else{
                    logAndToast("onBillingSetupFinished FAIL，errocode:"+billingResult.getResponseCode()  );
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
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

    public void pay(View view) {
        List<String> skuList = new ArrayList<>();
        skuList.add(skuId);

        if (isProductDetailsSupported()) {
            ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("up_basic_sub")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build();
            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    new ProductDetailsResponseListener() {
                        public void onProductDetailsResponse(BillingResult billingResult,
                                                             List<ProductDetails> productDetailsList) {
                            // check billingResult
                            // process returned productDetailsList
                            if (productDetailsList != null && productDetailsList.size() > 0) {
                                skuDetails = productDetailsList.get(0);
                                //启动购买
                                BillingFlowParams purchaseParams =
                                        BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .setObfuscatedAccountId(userId)
                                                .build();
                                billingClient.launchBillingFlow(MainActivity.this, purchaseParams);
                            }
                        }
                    }
            );

        }else{
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(params.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                         List<SkuDetails> skuDetailsList) {
                            // Process the result.
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                skuDetails = skuDetailsList.get(0);
                                //启动购买
                                BillingFlowParams purchaseParams =
                                        BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .setObfuscatedAccountId(userId)
                                                .build();
                                billingClient.launchBillingFlow(MainActivity.this, purchaseParams);
                            }
                        }
                    });
        }

    }

    public boolean isProductDetailsSupported(){
       BillingResult billingResult= billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS);
       return billingResult.getResponseCode()!=BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED;
    }

    public void initTasdk(View view) {
        ALYAnalysis.enalbeDebugMode(true);
        ALYAnalysis.init(this, productId, "3000");
    }
}

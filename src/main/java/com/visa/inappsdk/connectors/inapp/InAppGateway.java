package com.visa.inappsdk.connectors.inapp;

import android.os.Bundle;
import android.os.Handler;

import com.visa.inappsdk.common.error.SDKError;
import com.visa.inappsdk.connectors.inapp.envelopes.InAppEncryptEnvelope;
import com.visa.inappsdk.connectors.inapp.receivers.TransactionResultReceiver;
import com.visa.inappsdk.datamodel.SDKGateway;
import com.visa.inappsdk.datamodel.response.SDKGatewayResponse;
import com.visa.inappsdk.datamodel.transaction.SDKTransactionObject;
import com.visa.inappsdk.datamodel.transaction.callbacks.SDKConnectionCallback;

/**
 * Created by fzubair on 10/6/2015.
 */
class InAppGateway extends SDKGateway implements TransactionResultReceiver.Receiver {
    private static InAppGateway gatewayInstance = new InAppGateway();

    /** Flag to indicate that a transaction is in progress. */
    private static boolean transactionInProgress = false;

    protected String messageSignature;
    protected String merchantId;
    private SDKConnectionCallback connectionCallback;
    private TransactionResultReceiver resultReceiver;

    public static InAppGateway getGateway() {
        return gatewayInstance;
    }

    @Override
    public boolean performEncryption(SDKTransactionObject transactionObject, SDKConnectionCallback applicationConnectionCallback) {
        if(transactionInProgress)
            return transactionInProgress;
        if (transactionObject == null)
            return false;

        registerResultReceiver();
        transactionInProgress = true;
        this.connectionCallback = applicationConnectionCallback;
        InAppEncryptEnvelope envelope = new InAppEncryptEnvelope(transactionObject, merchantId,
                messageSignature);
        if (envelope == null)
            return false;
        InAppConnectionService.startActionConnect(InAppSDKApiClient.getContext().get(), envelope, resultReceiver);
        return transactionInProgress;
    }

    private InAppGateway() {
    }

    public void setMessageSignature(String messageSignature) {
        this.messageSignature = messageSignature;
    }

    public void setMerchantID(String merchantID) {
        this.merchantId = merchantID;
    }


    protected static void dispose() {
        gatewayInstance = null;
    }

    private void registerResultReceiver() {
        if(resultReceiver != null)
            return;
        resultReceiver = new TransactionResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        transactionInProgress = false;
        switch (resultCode){
            case InAppConnectionService.SERVICE_RESULT_CODE_SDK_RESPONSE:
                SDKGatewayResponse response = (SDKGatewayResponse) resultData
                        .getParcelable(InAppConnectionService.SERVICE_RESULT_RESPONSE_KEY);
                connectionCallback.onApiConnectionFinished(response);
                break;
            case InAppConnectionService.SERVICE_RESULT_CODE_SDK_ERROR:
                SDKError error = (SDKError) resultData
                        .getSerializable(InAppConnectionService.SERVICE_RESULT_ERROR_KEY);
                connectionCallback.onErrorReceived(error);
                break;
        }

    }
}